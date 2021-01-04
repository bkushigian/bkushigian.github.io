---
layout: post
title:  "Adding Exceptions to Cornelius, Part 1"
date:   2020-09-02 12:00:00 -0800
comments: true
published: false
categories: cornelius
tags: [cornelius, exceptions]
group: cornelius
---

## The Problem
Cornelius needs to handle arbitrary exceptions and try/catch blocks. To do this
in its entirety will be hard, so I've decide to start with something simpler:
null pointer dereferencing.

## Null Pointer Dereferencing
Cornelius handles field writing and reading of objects, for instance:

```java
int foo() {
    int x = a.b;
    return x;
}
```
{% include label.html 
   title="Listing 1"
   content="A Java program that dereferences a class variable
  <code>this.a</code>"
%}

If we knew that `a` was never null then we could output something like:

```scheme
(method-root
  ;; The returned value is the result of two reads.
  ;; 1. `(rd (path (var this) (derefs a)) (heap 0))` reads `this.a` from
  ;;    `(heap 0)`
  ;; 2. The result of the above read is used as the base of the outer `rd` node,
  ;;    which dereferences variable name `b` in `(heap 0)`
  (rd (path 
        (rd (path (var this) (derefs a)) (heap 0))
        (derefs b))
      (heap 0))
  ;; The returned heap
  (heap 0))
```
{% include label.html
   title="Listing 2"
   content="The (incorrect) PEG produced by Cornelius currently...note that this
   doesn't handle <code>null</code> dereferences"
%}

I need to check the dereference of `(this.a).b)` for nullity; I can skip the
`null` check for `this` since [`this` is never
null.](https://docs.oracle.com/javase/specs/jls/se8/html/jls-15.html#jls-15.8.3)

## Why is this tricky?
I'll need to capture control flow somewhere. Consider the following method:

```java
int derefAndAssign(Foo a) {
    int x = 0;
    try {
        int y = a.value;  // Might throw NPE
        x = 1;            // Doesn't execute if NPE is thrown.
    } catch (NullPointerException e) {}
    return x;  // This x shouldn't track assignment x = x + 1 when a is null
}
```
{% include label.html
   title="Listing 3"
   content="This method illustates the intersection of concerns between
   handling possible exceptional status and control flow."
%}

If `a` is `null`, then the print statement and executed. The semantics for the
print statement are implicitly handled because `a.value` is serialized to `(rd
(path (var a) (derefs value)) (heap 0))`, which implicitly tracks the
possibility that `a` might be `null`.

But in the case of `x = 1`, this is a simple assignment to a local variable and
doesn't involve the heap at all. Thus if we just update our context naively, `x`
will always be `1` at there return statement.

## The solution

### The Heap
Heaps now track two things: **heap state** and **exception status**: `(heap
STATE STATUS)`. `STATE` is a chain of `wr` nodes, while `STATUS` is a chain of
conditionals testing for conditions and resulting in `unit` if none of the
conditions are met, or specific exception types if the corresponding condition
is met.

For instance, `(heap unit (phi (isnull? (var a) (exception
java.lang.NullPointerException) unit)))` represents a heap with a single
possible NPE triggered by `(var a)` being `null`.

### Exit Conditions
Cornelius tracks **exit conditions** in its contexts:

```java
    final public ImmutableSet<PegNode> exitConditions;
```
{% include label.html
   title="Listing 4"
   content="PegContext.exitConditions"
%}

During serialization, which is basically just a big AST visit, every time we
encounter code that might throw an exception (for now, just
`NullPointerException`s), we register it in the context:
```java
    @Override
    public ExpressionResult visit(FieldAccessExpr n, PegContext arg) {
        final ExpressionResult scope = n.getScope().accept(this, arg);
        final PegNode path = PegNode.path(scope.peg.id, n.getNameAsString());
        // isnull: the exit condition for this dereference
        final PegNode isnull = PegNode.isnull(scope.peg.id);
        // the null pointer exception that is thrown when the 'scope' of the
        // FieldAccessExpr is null (e.g., in a.x, 'a' is the scope)
        final PegNode npe = PegNode.exception("java.lang.NullPointerException");
        // nullCheck: the resulting context after a null check, with the
        // corresponding exception condition
        final PegContext nullCheck = scope.context.withExceptionCondition(isnull, npe);
        return PegNode.rd(path.id, scope.context.heap.id).exprResult(nullCheck);
    }
```
{% include label.html
   title="Listing 5"
   content="PegExprVisitor.visit(FieldAccessExpr, PegContext)"
%}

An `ExpressionResult` is just a wrapper around a `PegContext` and a `PegNode`,
as well as some convenience methods.

Under the hood, `PegContext.withExceptionCondition(PegNode cond, PegNode
exception)` copies the current `PegContext` with the following modifications
1. `cond` is added to the new `PegContext`'s `exitConditions`
2. the new context's heap now tracks the possibility of this exceptional status.
   This is stored in `context.heap.status`. If there have been no exceptions
   thrown, then `context.heap.status` is `unit` (or something that is, in
   theory, rewritable to `unit`). If it is _not_ `unit`, that means that an
   exception has been thrown already, which in turn means that the current
   exception is not thrown. Thus the new status is:
   ```scheme
    (phi (unit? OLD-STATUS) 
         ;; Old status is unit, so whenever the new exit condition is true,
         ;; the status should be the new exception. Otherwise, it's the old
         ;; exception status
         (phi NEW-EXIT-CONDITION EXCEPTION OLD-STATUS)
         ;; The old statis is NOT unit, which means that an exception was
         ;; thrown that hasn't been caught. The old status should be used.
         OLD-STATUS)
   ```
   
On subsequent assignment operations, instead of directly updating the context we
now predicate that update on exit conditions:

```java
    /**
     * A helper method wrapping {@code setLocalVar} to predicate assignments on appropriate checks against
     * exitConditions.
     * @param key variable name we are assigning to
     * @param val value we are assigning
     * @return context resulting from assignment
     */
    public PegContext performAssignLocalVar(final String key, final PegNode val) {
      if (exitConditions.isEmpty()) {
          return setLocalVar(key, val);
      }
      return setLocalVar(key, PegNode.phi(PegNode.exitConditions(exitConditions).id, getLocalVar(key).id, val.id));
    }
```
{% include label.html
   title="Listing 6"
   content="PegContext.performLocalAssign(String, PegNode): this helper method performs
            a local assignment predicated on any possible exit conditions that occurred
            before this"
%}

This also needs to be tracked by context joins, but this is pretty straight
forward...I just union the `exitConditions` and update heap state and status based on control flow:

```java
    /**
     * Combine two contexts, merging control flow.
     * @param c1 the context resulting from the then branch that executes if {@code guardId} is true
     * @param c2 the context resulting from the else branch that executes if {@code guardId} is false
     * @param guardId the id of the branching condition
     * @return combined contexts
     */
    public static PegContext combine(PegContext c1, PegContext c2, Integer guardId) {
        assert c1.fieldNames == c2.fieldNames;  // TODO: is this true? This should be true
        final ImmutableSet<String> domain = c1.localVariableLookup.keySet().stream().filter(c2.localVariableLookup::containsKey)
                .collect(Collectors.collectingAndThen(Collectors.toSet(), ImmutableSet::copyOf));

        final PegNode.Heap combinedHeap = PegNode.heap(
                PegNode.phi(guardId, c1.heap.state, c2.heap.state).id,
                PegNode.phi(guardId, c1.heap.status, c2.heap.status).id
        );

        final ImmutableSet<PegNode> combinedExitConditions = (new ImmutableSet.Builder<PegNode>())
                .addAll(c1.exitConditions)
                .addAll(c2.exitConditions).build();

        return initMap(
                domain,
                p -> PegNode.phi(guardId, c1.getLocalVar(p).id, c2.getLocalVar(p).id),
                c1.fieldNames,
                combinedHeap,
                combinedExitConditions);
    }
```

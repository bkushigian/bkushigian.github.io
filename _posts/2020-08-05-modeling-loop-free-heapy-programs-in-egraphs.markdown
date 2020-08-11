---
layout: post
title:  "Modeling Loop Free Heapy Programs in EGraphs"
date:   2020-08-05 23:00:50 -0400
comments: true
categories: cornelius research
group: cornelius
---
<style>
td {
    font-size: 90%
}

td code {
    font-size: 100%
}

</style>

## The Heap Model

I will use a storeless heap model, where locations in the abstract heap are
labeled by their _access paths_. An _access path_ is a variable followed by zero
or more field dereferences:

```
ACCESS-PATH ::= VAR(.FIELD)*
```

At some point I'll need to add in array dereferencing as well.

An _abstract heap state_ is defined as follows:
```
ABSTRACT-HEAP-STATE ::= (heap INT)
                     | (wr PATH VALUE ABSTRACT-HEAP-STATE)
```

This is just a linked list with some extra data:
- A `heap` node represents an abstract heap state that we don't know anything
  about. Thus it could represent the heap state `{ x -> Integer(3) }` or `{ x ->
  String("hello world!"), y -> Pair<Integer, Integer>(fst=1, snd=2) }`, etc.
  Since `heap` nodes can represent multiple values, I need to disambiguate them
  with an `int` index.

- A `wr` node represents writing a value to a location in an abstract heap
  state. A `wr` node expects an access path, a value to be stored at the
  location represented by the access path, and an abstract heap state to modify.

In addition to `heap` and `wr` nodes, we need to be able to read from a heap.
There are a few ways to do this and I'm not sure the best way; I'll discuss
these options [below](#reading-from-the-heap).

### Reading From The Heap
To complement the `wr` node I define a `rd` node that can read values from a
heap. The basic idea is that every `rd` node has a `path` argument and a `heap`
argument; `rd` and `wr` nodes are related by the following rewrite rule:

```scheme
(rd ?path (wr ?path ?val ?heap)) => ?val
```

I'm running into a problem though. Consider the two programs, which are equivalent:
<a name="listing-example1-double-deref"></a>
```java
int double_deref_a() {
    return this.x.y;
}

int double_deref_b() {
    Foo x = this.x;
    return x.y;
}
```

These translate into the following pegs:
```scheme
;; peg for the returned value of double_deref_a()
(rd "this.x.y" (heap 0))

;; peg for the returned value of double_deref_b()
(rd "x.y" (heap 0))
```

So from this perspective it makes sense to _decompose access paths_ into
individual field dereferences.

The above approach also has other problems: `"x"` can correspond to different
values in different places, so using this lexically doesn't make sense.

Decomposing will involve creating a list-like structure for paths, where each
node is a field dereference of some value:
```scheme
(deref BASE FIELD)
```

where `BASE` is an actual PEG node, and `FIELD` is the string name of the field
dereference. The path `"this.x"` will become:
```scheme
(deref (var "this") "x")
```
At first I'm tempted to represent `this.x.y` as
```scheme
(deref (deref (var "this") "x") "y") ;; THIS IS WRONG
```
but this makes no sense: `(deref (var "this") "x")` isn't a value, it's a
location. This causes some kludge in our above example:<a name="example-of-deref-value-incompatibility"></a>
```scheme
;; Using naive decomposed access paths

;; peg for the returned value of double_deref_a()
(rd (deref (deref (var "this") "x") "y") (heap 0))

;; peg for the returned value of double_deref_b()
(rd (deref (rd (deref (var "this") "x") (heap 0)) "y") (heap 0))
```

Now this could be handled with some rewrite rules, but I'm not sure how sound
they would be. The 'obvious' way to fix the above is to identify
`(rd (deref (var "this") "x") (heap 0))` with `(deref (var "this") "x")`. But
this means that each place that the pure `(deref (var "this") "x")` occurs will
be identified with this value. In particular, if we also have `(rd (deref (var
"this") "x") (heap 97))`, then the two values in the two different heaps will be
identified.

Instead, I want to make each `deref` node take a base _value_ and a _single
field dereference_. The above becomes

```scheme
;; Using naive decomposed access paths

;; peg for the returned value of double_deref_a()
(rd (deref (rd (deref (var "this") "x") (heap 0)) "y") (heap 0))

;; peg for the returned value of double_deref_b()
(rd (deref (rd (deref (var "this") "x") (heap 0)) "y") (heap 0))
```

Great! They match!

How does this play with `wr` nodes?
Consider the following program
```java
int write_then_read(int a) {
    this.x.y = a;
    return this.x.y;
}
```

Using the decomposed access paths, the write node becomes:
```scheme
(wr (deref (rd (deref (var "this") "x") (heap 0)) "y") (var "a") (heap 0))
```
This represents our new heap, so the subsequent `rd` node, which will represent
the returned value, is
```scheme
(rd
  ;; The path
  (deref (rd (deref (var "this") "x") (heap 0)) "y")
  ;; The heap
  (wr (deref (rd (deref (var "this") "x") (heap 0)) "y") (var "a") (heap 0)))
```
The path to the `rd` and `wr` nodes are identical, so the simple rewrite rule
cancels out, and the method returns `(var "a")`, plus modifies the heap.

### Commuting `wr` nodes

If the most recent write to the heap was to the path from which we are trying to
read, then we can just take the value that was written. This has an obvious
problem. The program:

```java
String updateFoo(Foo foo) {
    foo.string = "hello world";
    foo.integer = 7;
    return foo.string;
}
```
returns the value (represented as a PEG):
```scheme
(rd (deref (var "foo") "string")
    (wr (deref (var "foo") "integer")
        7 
        (wr (deref (var "foo") "string")
            (string "hello world")
            (heap 0))))
```

We _should_ be able to rewrite this to `(string "hello world")` but this
involves commuting of either `wr` nodes with one another, or `rd` nodes
commuting with `wr` nodes. This is difficult because of the aliasing problem: I
can only commute writes if they don't interfere with one another.

There are a few ways that I could partially address this, such as [type-driven
rewrites](#type-driven-rewrites) and [formal access-path based
rewrites](#formal-access-path-based-rewrites) but I'd like to chat some more
with folks who know this domain better than I do.

## Encoding Heapy Operations in PEGs
The next question is: how do I encode heapy Java programs? I'm not using loops
yet, so let's dive in with some different language features

### Field Access
Let's say I have the Java expression `a.b.c.d`. First I'll look up `a` in my
`Context` that is storing local variables: `Context: Map<String, PegNode>`.

- If `a` corresponds to a method parameter, it will just get a `VarPegNode` node back.
- If `a` is `this`, then the context will return a `(var this)` node.
- If `a` is a field (not a local variable) using implicit `this` dereferencing,
  I'll prefix the access path with a `this` node.
- If `var` corresponds to a non-parameter local variable it will be
  mapped to a `PegNode` stored in the context.
- Otherwise, error

Assuming that `a` is a field with an implicit `this` prefix, the expression
`a.b.c.d` (in abstract heap state `(heap 0)`) will be encoded as:

```scheme
(deref
  (rd (deref
        (rd (deref
              (rd (deref (var this) "a") (heap 0))
              "b")
            (heap 0))
        "c")
      (heap 0))
  "d")
```

### I'm Still Unsatisfied
It's kind of unsatisfying to have all of the `heap` nodes and the `rd`s in
there, though...I would like to be able to have a pure access path that is
looked up in a heap.

Aside from me being unsatisfied, there is also room for nonsense `deref` chains,
like
```scheme
(rd (deref (rd (deref (var this) "a") (heap 3)) "b") (heap 19))
```
What does that even _mean_?? It's utter nonsense, and I'd rather my
representation didn't include this.

### Size of Heap Representations
In my [my previous post]({% post_url 2020-07-30-potential-challenges-with-modeling-heaps-in-egraphs %})
I spent some time worrying about unbounded access paths and unbounded `wr`
nodes. The fact is, I don't actually have to worry about this, even with loops.
A loop in a PEG is recorded as a _theta_ node, a bounded representation of _all_
the states that will happen during a loops execution. While a theta node
_represents_ all of these different states, it doesn't actually construct them
explicitly. This means that:
1. _all access paths that will be explicitly constructed by Cornelius are known statically_
2. _all abstract heap states that will be explicitly constructed by Cornelius
   have statically known depths_


## Summary and Future Work
This should give Cornelius basic heapy operations. In particular, writing to and
reading from the heap is now possible, though very limited, and kind of hacky.
There are a few constraints that I'm trying to solve, including:

1. the interplay between pure access paths and values used in dereferencing (see
   [this example of the incompatibility of derefs and
   values](#example-of-deref-value-incompatibility))
2. the fact that an access path should, in its purest form, be independent from
   a heap. (see the nonsense example in the section [I'm Still
   Unsatisfied](#im-still-unsatisfied))

In future posts I'll explore:
1. How to _invoke methods_
2. How to _allocate and construct objects_
3. How to commute _reads_ with _writes_
4. How to commute _writes_ with _writes_

Briefly I'll mention a couple things (so I don't forget)

### Type-Driven Rewrites
This technique uses _type information_ to help handle special cases of the
aliasing problem.

If I have heap
```scheme
(wr ("g1.g2" peg2) bar
    (wr "f1.f2" peg1 foo
        (heap 0)))
```
(write `foo` to `peg1.f1.f2`, then write `bar` to `peg2.g1.g2`), and I know that
something assignable to the type of `f2` can't be assigned to either `g1` or
`g2`, then I know that the first `wr` node couldn't have updated anything in the
access path of `peg2` (that is, neither `g1` or `g2` was updated). Likewise, if
a value assignable to `g2` cannot possible be a value assignable to either `f1`
or `f2`, then I know that the second `wr` node couldn't have updated anything in
the access path of `peg1`. I can now commute the two `wr` nodes.

### Formal Access-Path Based Rewrites
Can I use the actual access paths to allow certain rewrites? If so, is this any
more powerful than the type driven rewrites above?

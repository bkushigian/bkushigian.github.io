---
layout: post
title:  "Testing Cornelius Serialization with Clojure"
date:   2020-11-06 12:00:00 -0800
comments: true
published: false
categories: cornelius
tags: cornelius
group: cornelius
---
<pre>
                   _______               __   __
                  |_     _|.-----.-----.|  |_|__|.-----.-----.
                    |   |  |  -__|__ --||   _|  ||     |  _  |
                    |___|  |_____|_____||____|__||__|__|___  |
                                                       |_____|
               ______                          __ __
              |      |.-----.----.-----.-----.|  |__|.--.--.-----.
              |   ---||  _  |   _|     |  -__||  |  ||  |  |__ --|
              |______||_____|__| |__|__|_____||__|__||_____|_____|

    _______              __         __ __               __   __
   |     __|.-----.----.|__|.---.-.|  |__|.-----.---.-.|  |_|__|.-----.-----.
   |__     ||  -__|   _||  ||  _  ||  |  ||-- __|  _  ||   _|  ||  _  |     |
   |_______||_____|__|  |__||___._||__|__||_____|___._||____|__||_____|__|__|

                  __ __   __      ______ __         __
       .--.--.--.|__|  |_|  |--. |      |  |.-----.|__|.--.--.----.-----.
       |  |  |  ||  |   _|     | |   ---|  ||  _  ||  ||  |  |   _|  -__|
       |________||__|____|__|__| |______|__||_____||  ||_____|__| |_____|
                                                  |___|
</pre>

* TOC
{:toc}

## Testing Peg Serialization
**Question:** how can I effectively test PEG serialization? Ultimately, each
test is a set of input/expected output pairs. The input should be some Java code
to be serialized, and the expected output should be some form that can easily be
compared with the generated PEG. 

I could compare PEGs directly, and this might be the right way to do it in the
long run. However, I wanted something easy to implement and easy to write tests
for.

## Original Testing Infrastructure
I wrote a simple comment parser that can inspect method comments for forms like
```java
/**
 * <expected>
 *   (method-root (+ (var a) (var b)) (heap 0 unit))
 * </expected>
 */
public int add(int a, int b) {
    return a + b;
}
```

The test would read the comment, serialize the method, and compare the parsed
string literals with the dereferenced string of the serialized PEG for string
equality, reporting the index of the first difference if one was found.

This was easy to implement, and for easy test cases was easy to write tests.
However, this setup has several shortcomings:
 
1. **Cannot test intermediate state:** The serializer only returns a
   `(method-root peg heap)` node, and this doesn't include intermediate state.
   This means that I can only test against the expected output.

2. **String comparison isn't great:** I don't have a good way of comparing PEGs
   directly: instead, I've been comparing dereferenced strings
   `peg.toDerefString()`

3. **HUGE test cases:** The expected outputs are BIG: a one line method can
   result in a PEG whose dereferenced string is over 2000 characters long (this
   is, after all, why I deduplicate my PEGs when I serialize). Constructing
   these by hand is tedious and error prone

I started off hacking on a small elisp helper script to build up PEG strings
quickly in Emacs. At the advice of Rene switched over to Clojure so that my
testing code could better interact with the serializer, which is in Java.

## Updated Testing Infrastructure
### Testing intermediate state
I solve shortcoming 1 by instrumenting the serializer to scrape statement-level
comments for expected state, and to capture the actual resulting state in an
`ExpressionResult`, which is just a wrapper around a PEG, a context, and a heap.

A quick reminder, in addition to PEGs resulting from expressions there are two
types of state that I need to track during serialization: context and heap
state. A context, represented by `serializer.peg.PegContext`, maps variable
names to PEGs. Thus, after the serializing the statement `x = 1;`, the context
should map `x` to PEG node `(int-lit 1)`.

Heap stores global state that _isn't_ stored in the context. This includes
global state and exception status, and is updated by field reads, method
invocations, and anything that might trigger an exception.

Statements don't have values, which means I'm not going to be reasoning about
individual PEGs; instead, I'm going to be concerned with contexts and heaps (but
if I ever expand to testing expressions I'll want to have access to PEGs, thus
the `ExpressionResult`).

Rather than reconstructing the entire PEG at each comment, I'm marking each
statement with the state update, and optionally including a `snapshot` node,
which tells the testing infrastructure  to check the expected state versus the
serialized state.
```java
int foo(int a, int b) {
    /**
     * <expected>
     *   [a   (lookup-in-ctx "a")
     *    peg (opnode "+" a (int-lit 1))
     *    ctx (update-key-in-ctx ctx "x" peg)
     *    (snapshot {:ctx ctx :heap heap})]
     * </expected>
     */
    int x = a + 1;
    /**
     * <expected>
     *   [b   (lookup-in-ctx "b")
     *    peg (opnode "+" b (int-lit 1))
     *    ctx (update-key-in-ctx ctx "y" peg)
     *    (snapshot {:ctx ctx})]
     * </expected>
     */
    int y = b + 1;
    return x + y;
}
```


Note that I'm currently not testing the returned value from return statements
explicitly (I made some early design decisions based on only having a single
return statement in a method, and I'm going to update that soon---updating the
testing infra doesn't make sense here, since it will only be around for a short
amount of time). Further, the way returns are handled is simply to record the
resulting PEG (if any) in the returned expression, and copy heap info in a
`(method-root PEG HEAP)` node. This is very simple and doesn't exercise any new
machinery that isn't already exercised elsewhere in the method, so any bugs that
show up should be immediately obvious. Anyway, this will be fixed at some point
soon, but it isn't pressing.

The above `expected` decorations should be transformed into a program that runs tests, something like:

```clojure
(t/testing "foo(int,int)"
   (let [ctx  (new-ctx-from-params "this" "a" "b")
         heap (init-heap)]
         (let [a (lookup-in-ctx "a")
               peg (opnode "+" a (int-lit 1))
               ctx (update-key-in-ctx ctx "x" peg)]
               ;; TEST HEAP
               ;;
               ;; This performs an actual test via `(t/is (= str1 str2))`, and
               ;; prints helpful info on failure
               (ensure-strings-are-same "(heap 0 unit)" "(heap 0 unit)")
               ;; TEST CONTEXT
               ;;
               (ensure-strings-are-same (to-deref-string (lookup-key-in-ctx ctx "this")) "(var \"this\")")
               (ensure-strings-are-same (to-deref-string (lookup-key-in-ctx ctx "a")) "(var \"a\")")
               (ensure-strings-are-same (to-deref-string (lookup-key-in-ctx ctx "b")) "(var \"b\")")
               (ensure-strings-are-same (to-deref-string (lookup-key-in-ctx ctx "x")) "(opnode \"+\" (var \"a\") (int-lit 1))")

               ;; RECURSIVELY VISIT REST OF FUNCTION
               (let [b (lookup-in-ctx "b")
                     peg (opnode "+" b (int-lit 1)
                     ctx (update-key-in-ctx ctx "y" peg))]
                     ;; TEST CONTEXT
                     (ensure-strings-are-same (to-deref-string (lookup-key-in-ctx ctx "this")) "(var \"this\")")
                     (ensure-strings-are-same (to-deref-string (lookup-key-in-ctx ctx "a")) "(var \"a\")")
                     (ensure-strings-are-same (to-deref-string (lookup-key-in-ctx ctx "b")) "(var \"b\")")
                     (ensure-strings-are-same (to-deref-string (lookup-key-in-ctx ctx "x")) "(opnode \"+\" (var \"a\") (int-lit 1))")
                     (ensure-strings-are-same (to-deref-string (lookup-key-in-ctx ctx "y")) "(opnode \"+\" (var \"b\") (int-lit 1))")
                     ;; NO HEAP TEST (wasn't specified in statement's snapshot)

                     ;; ... continue
                     ))))
```

## Problems with new implementation
1. **Inlining `String` literals is space inefficient:**
   I'm inlining the actual `String` literals returned from `to-deref-string` for
   each of the serialized PEGs in the context and heap. These will get really
   big. These are generated from `PegNode`s, and I can't use object references
   inside of an `eval`. One way around this is to us a `bindings` wrapper, and
   auto-gen names for each object reference, which is something I can do in the
   future if it becomes an issue.

2. **Testing via String Comparisons:**
   If two PEGs differ, they will have different dereferenced strings.
   These are hard to read as they can be HUGE. A better way to handle this would
   be to write a PEG comparison method that recursively checks for PEG
   equivalence. This also requires that I solve the binding problem that I
   mention above (I can't reference PEGs directly in an `eval`).

3. **Testing contexts is asymmetric:**
   At test generation I have access to the actual contexts (they've already been
   serialized) but I don't have access to the expected contexts...the testing
   program I'm building is defining the expected contexts in a series of nested
   let bindings, and these haven't been executed yet (they're still being
   constructed). This means that when I check that contexts agree on a set of
   keys, I'm only checking that they agree on the keys of the *serialized*
   context. In particular, if the expected context has a bunch of garbage keys
   that aren't part of the serialized context (or if the serialized context
   doesn't include enough keys), these keys won't be tested.
   
   A fix is to write a function that takes a context and a list of keys and
   asserts that the context has the same set of keys.



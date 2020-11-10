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

## Original Testing Infrastructure
I wrote a simple comment parser that can inspect method comments for forms like
```html
<expected>
(heap-root (+ 1 2) (heap 0 unit))
</expected>
```

The test would read this, then serialize the method, and compare the string
literals  for string equality, reporting the index of the first difference if
one was found.

This setup has several shortcomings:
 
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
amount of time).

The above should be transformed into a program that runs tests, something like:

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

I'd like to fix one thing with this design: notice that I'm using the actual
`String` values returned from `(to-deref-string ...)` for each of the serialized
values. These will get really big, and for debugging I'd like to keep this as
simple as possible. However these are generated from `PegNode`s, and I can't use
object references inside of an `eval`. One way around this is to us a `bindings`
wrapper, and auto-gen names for each object reference, which is something I can
do in the future if it becomes an issue.



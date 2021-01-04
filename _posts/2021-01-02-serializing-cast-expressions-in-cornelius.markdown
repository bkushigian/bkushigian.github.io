---
layout: post
title:  "Serializing Cast Expressions in Cornelius"
date:   2021-01-02 12:00:00 -0800
comments: true
published: true
categories: cornelius
tags: cornelius
group: cornelius
---
<pre>
         _______              __         __ __         __
        |     __|.-----.----.|__|.---.-.|  |__|.-----.|__|.-----.-----.
        |__     ||  -__|   _||  ||  _  ||  |  ||-- __||  ||     |  _  |
        |_______||_____|__|  |__||___._||__|__||_____||__||__|__|___  |
                                                                |_____|
                           ______               __
                          |      |.---.-.-----.|  |_
                          |   ---||  _  |__ --||   _|
                          |______||___._|_____||____|

       _______                                      __
      |    ___|.--.--.-----.----.-----.-----.-----.|__|.-----.-----.-----.
      |    ___||_   _|  _  |   _|  -__|__ --|__ --||  ||  _  |     |__ --|
      |_______||__.__|   __|__| |_____|_____|_____||__||_____|__|__|_____|
                     |__|
         __          ______                          __ __
        |__|.-----. |      |.-----.----.-----.-----.|  |__|.--.--.-----.
        |  ||     | |   ---||  _  |   _|     |  -__||  |  ||  |  |__ --|
        |__||__|__| |______||_____|__| |__|__|_____||__|__||_____|_____|

</pre>


In this article I'm going to reason through transforming cast expressions into
PEGs.

# The Problem
The problem is simple: given a cast expression `(T)o`, I want to create a PEG
that represents the semantics of this expression. I need to track the following:

1. **The change to the type:** this is important for method dispatch, assignment
   validity, etc. For instance,

    ```java
    /**
     * Suppose that T extends S, and that overriddenMethod is defined
     * in S and overridden in T.
     */
    void m(S o) {
        o.overriddenMethod();
        T o2 = (T)o;
        o2.overriddenMethod();
    }
    ```
    should properly capture that both method invocations are not necessarily the same.

2. **Class Cast Exceptions:** The following code should represent the
   possibility that something went wrong: 
   
   ```java
   void m(Object o) {
       T = (T) o;
   } 
   ```

   This will be tracked as an exception status stored in a heap node `(heap
   state status)`.
   
   To do this, I'll need at least two new nodes: 
   - A `(can-cast? OBJ TYPE)` node representing the relation that `OBJ` os if
     type `TYPE`, and

   - A `(cast OBJ TYPE)` node that represents the result of `OBJ` being cast to
     `TYPE`

**Question:** can the `can-cast?` relation be replaced with Java's `instanceof`
keyword? That is, is cast expression `(T)o` successful precisely when `o
instanceof T`?

**Answer:** Not quite...these are almost identical, but `null instanceof T`
always returns `false`, while casts to reference types always succeed (e.g.,
`(Object)null` is successful). That's okay, we can introduce a `can-cast?` node
and then rewrite `(can-cast? o t)` to `(and (not is-null? o) (instance-of? o
t))`.

# PEGifying a cast expression
I _think_ it should be as easy as:

1. Add exit condition `(not (can-cast? o T))` to the Context so that subsequent
   changes to local state are predicated on this condition not being satisfied.
2. Modify the heap by replacing it's `STATUS` field with `(phi (is-unit? STATUS)
   (phi (not (can-cast? o T)) java.lang.ClassCastException unit))`
3. Represent the expression as a `(cast o T)` PEG node.


---
layout: post
title:  "More About Access Paths"
date:   2020-08-06 12:28:50 -0800
published: true
comments: true
categories: cornelius research
tags: [cornelius, access paths, heaps, egraphs ]
group: cornelius
---

## The Problem Thus Far
I want to use _access paths_ in Cornelius to address heap locations. This turns
out to be tricky (as evidenced in my waffling in my [last
post][post:loop-free-heapy-programs]). In this post I want to refine the
problem, and I identify several properties I want my heap addressing scheme to
have.

### Desired Properties of Heap Addressing Schemes

1. Syntax/rewrite rules should be **sound** (obviously)
2. Access path representation should be **sensitive to changes in stack memory**
3. Access path representation should be **independent from heap state**
4. Access path representation should handle **weak path decomposition**

## Access-Path Representations
### **Representation 1:** Strictly Lexical Access Paths
Traditionally, I'd have an _abstract heap state_ `H`, and I'd index into this
with an access path `v.a.b.c`, where `v` is a _variable_ that reaches into the
heap (say a method parameter, a local variable, or the `this` keyword), and each
of `.a`, `.b`, and `.c` are _field dereferences_. I'll call this representation
of access path a **lexical access-path**. This would make sense if I were
tracking data at each program point, but I'm not. Consider the following
program:

<a name="listing-example1"></a>
```java
boolean example1(Foo f, Foo g) {
   int x = f.a;    // POINT 1
   f = g;
   int y = f.a;    // POINT 2
   return x == y;
}
```

Using lexical access-paths makes this method always return `true`. First, notice
that this method doesn't update the heap. Suppose `H` is the initial heap state
at the start of the method. The PEG representation of the value stored to `x` at
**POINT 1,** namely `f.a`, is `(rd "f.a" H)`. Likewise, the PEG representation
of the value stored to `y` at **POINT 2,** namely `f.a`, is `(rd "f.a" H)`. This
is syntactically equivalent to the value stored in `x` at **POINT 1**, and using
this strictly lexical access-paths leads to unsoundness (property 1) because it
doesn't track changes on the stack (property 2).

I'm emphasizing _strictly_ in "strictly lexical access-paths" because the only
part of the access path that gives us problems is the variable: we can use
lexical field dereferences (we might run into aliasing issues, but we can handle
these soundly).

### **Representation 2:** Access Paths with Base Values
In my [previous post][post:loop-free-heapy-programs] I suggested using _base
values_ instead of variables. As I mentioned above, the problem with using
lexical access-paths in the [above example](listing-example1) is that the `"f"`
in access-path `"f.a"` is ambiguous: it is a local variable and is stored on the
stack. When it is updated on the stack, say via an assignment `f = g;`, this is
not tracked in the heap parameter `H`. To fix this, I can replace the formal
name of the variable at the base of an access path with the _value_ that is
stored at that variable.

I immediately run into another choice that I need to make: do I track access
paths as a single entity of the form `(path BASE THE-WHOLE-DEREFERENCING-PATH)`,
e.g.:

```scheme
(path (var f) "a.b.c")
```

Or do I decompose paths into dereferencing chains, e.g.:

```scheme
(deref (deref (deref (var f) "a") "b") "c")
```

If I use the `path` form, I might end up violating property 4 (see the [double
dereferencing example][example:double-deref]) from my previous post.

Conversely, if I use the `deref` form, I run into the problem that I'm using
_paths_ as _values_. The `(var f)` node is clearly a _value_, but `(deref (var
f) "a")` is a _path_, not a _value_; to be a value, I'd need to read that
location in a heap state. This becomes

```scheme
(deref (rd (deref (rd (deref (var f) "a") H) "b") H) "c")
```

But now my representation depends on heap information, and this feels very
fragile.

### **Another Approach:** Using Egg's Analyses
I'm pretty convinced that there isn't a nice clean syntax-only solution to this.
Instead I think I'm going to rely on egg's `Analysis` feature. I already use
some basic `Analysis` to do constant folding and tracking of `var`s in
eclasses.

I need to think more about how I can use `Analyses`, but I think that I'll
modify the `path` solution above. This has the following advantages:
1. It actually encapsulates the `deref` version as a special case where every
   `path` is length 1.
2. It won't have arbitrary length nesting of path nodes since this will all be
   handled implicitly by theta nodes (again, assuming I don't do anything like
   loop unrolling in my rewrite rules)
3. It's probably the easiest to implement
4. While I'd like my representation to handle weak path decomposition, it isn't
   necessary, and might not actually be that useful in a lot of the cases
   Cornelius will be handling.

## Summary
I don't need to make this perfect yet. A good starting point is to use the
`path` form of the access-paths with base values.

<!-- URLS -->
[post:loop-free-heapy-programs]:{% post_url 2020-08-05-modeling-loop-free-heapy-programs-in-egraphs %}
[example:double-deref]:{% post_url 2020-08-05-modeling-loop-free-heapy-programs-in-egraphs %}#listing-example1-double-deref

---
layout: post
title:  "Local Reasoning in EGraphs"
date:   2020-07-27 23:28:50 -0400
comments: true
categories: cornelius research
group: cornelius
---

## The Problem
Consider the java program `max`:
```java
int max(int a, int b) {
    if (a > b) return a;
    return b;
}
```
and its mutant
```java
int max(int a, int b) {
    if (a >= b) return a;
    return b;
}
```

These compile to respective PEGs `(phi (> (var a) (var b)) (var a) (var b))` and
`(phi (>= (var a) (var b)) (var a) (var b))`. I'd like for Cornelius to prove
that these are equivalent programs.

While it looks like it would be trivial to prove equivalence, it's actually kind
of tricky to do so soundly with in an EGraph. The main difficulty is that I want
my EGraph to reason about `(var a)` locally in the `then` branches of the
mutant's `phi` node and discover that `(== (var a) (var
b))` whenever `(&& (>= (var a) (var b)) (! (> (var a) (var b)))`.

I can get the mutant into a more workable form by applying a few rewrite rules:

```scheme
(phi (|| (> (var a) (var b))
         (== (var a) (var b)))
     (var a)
     (var b))
```
and at a high level, all I need to show is that both versions of `max` act the
same when `(== (var a) (var b))`. To do this I want to replace the `(var a)` in
the then branch of the mutant with the `(var b)`. Explicitly, I'd rewrite the
`||` node into something like

```scheme
(phi (> (var a) (var b))
    (var a)                      ;; `||` short circuits
    (phi (== (var a) (var b))    ;; when (! (> (var a) (var b )))
        (var a)                  ;; we can replace (var a) with (var b) here!
        (var b)))
```
If I can rewrite the then branch of
`(phi (== (var a) (var b)) (var a) (var b))` to `(var b)`, then I get the form
`(phi (== (var a) (var b)) (var b) (var b))`. This always evaluates to `(var b)`
so I can replace the entire `phi` node with `(var b)`. Substituting this back in
to the above program, we transform the mutant into `(phi (> (var a) (var b))
(var a) (var b))`, which is our original program.

The question is: how can I soundly rewrite the above then branch? This involves
_local reasoning_: reasoning about `(var a)` differently inside of the then
branch of the `phi` node than anywhere else in the program. This turns out to be
difficult to do in an EGraph because it's easy to accidentally identify things
globally.

## Question
What techniques can I use to employ local reasoning in an EGraph? Solving this
is going to be perhaps _the_ crucial task for Cornelius to work. Many other
things are 'mere' engineering, but this problem seems like it is going to be the
crux of the matter.

Possible solutions include:
1. **[Equality Refinement]({% post_url 2020-07-28-local-reasoning-with-equality-refinement %}):**
   Equality saturation allows us to use conditions such as `(== a b)` in a `phi`
   node's condition to replace instances of `a` with `b` inside the then branch.
2. **Condition Replacement:**
   Similar to equality saturation, a node `(phi c thn els)` replaces each
   instance of `c` with `true` in `thn` and `false` in `els`.
3. **Spawning New EGraphs:**
   If we can spawn a new EGraph for a place here certain identifications are
   made and then extract the resulting identifications, this could be profitable.

---
layout: post
title:  "Local Reasoning in EGraphs with Equality Refinement"
date:   2020-07-28 12:28:50 -0400
comments: true
categories: cornelius
group: cornelius
---
In [my previous post]({% post_url 2020-07-27-local-reasoning-in-egraphs %})
I mentioned that local reasoning in EGraphs is likely
crucial to Cornelius's success, and suggested _equality refinement_ as one
possible approach. Here I'll outline what equality refinement is as well as some
of the problems it has and possible solutions.

## Equality Refinement
In equality refinement I use the fact that `a` and `b` are identical in the then
branch of`(phi (== a b) a b)` to transform the term into `(phi (== a b) b b)`.
To accomplish this I would use rules like:


```scheme
refine: (phi (== ?a ?b) ?t ?e) => (phi (== ?a ?b) (swap ?a ?b ?t) ?e)

;; Swapping
swap-match: (swap ?a ?b ?a)          => ?b
swap-plus:  (swap ?a ?b (+ ?l ?r))   => (+  (swap ?a ?b ?l) (swap ?a ?b ?r))
;; etc

    
```

The `refine` rule rewrites the `phi` node to a new `phi` node by updating then
branch `?t` to a new term `(swap ?a ?b ?t)`. A `swap` node has the following
semantics:

> `(swap ?a ?b ?t)` is the term `?t` with each instance of term `?a` replaced with term `?b`.

This is similar to substitution in the lambda calculus, only there are no free
and bound variables to worry about.

Rules like `swap-plus` handle the recursive transformation on the AST, and
`swap-match` handles the cases where `?a` should be replaced by `?b`.

### Pass-through
I'd also like to handle base cases where no match happened. For instance, if a
`swap` encounters a `var`, an `int`, or a `bool` that doesn't satisfy
`swap-match`, I'd like to pass the term through; `(swap (var a) 7 (var b))`
should simplify to `(var b)`.

The obvious way to implement this is with:

```scheme
swap-var-pass-through:   (swap ?a ?b ?c) => ?c if is_var("?c")
swap-const-pass-through: (swap ?a ?b ?c) => ?c if is_const("?c")
```

### Naive Equality Refinement + Pass-through Fails
The above technique fails: it _over identifies_ terms! Consider the following
case:
```scheme
(phi arbitrary-condition
    (phi (== (var a) (var b))
         (var a)
         (var b))
    (var a))
```

With the above implementation of equality refinement, the above program gets
rewritten to `(var b)`! How?

Well, EGraphs reason globally, and they take the transitive symmetric closure
of rewrites. So if term `a` rewrites to both term `b` and term `c`, then `b` and
`c` will be identified with each other in the EGraph. This might not seem
surprising when you read it, but it can introduce some subtle bugs.

In our example, applying equality refinement yields something like
```scheme
(phi arbitrary-condition
    (phi (== (var a) (var b))
         (swap (var a) (var b) (var a))
         (var b))
    (var a))
```

Either of the following two rules can fire on term `(swap (var a) (var b) (var a))`.
1. `swap-match`: this rewrites the `swap` term to `(var b)`; this is the desired effect
2. `swap-var-pass-through`: this rewrites the `swap` term to `(var a)`.

In a normal rewrite system, this firing would replace the `swap` node. An
EGraph, however, will retain the swap node so that the other rule can be applied
as well. Thus, the swap node will be rewritten to both `(var a)` and `(var b)`,
and all three nodes have been identified as  equivalent _globally_.

This is a common theme in EGraphs: either of the above rules are correct.
Rewrite rules that are sound in a normal rewrite system can be unsound in an
EGraph. This can be highly counter-intuitive and I've spent many long hours
trying to track down subtle bugs that are consequences of this.

### Fixing Equality Refinement + Pass-through?
Suppose I add something like the following to our rules:
```scheme
swap-var-pass-through: (swap ?a ?b ?c) => ?c
    if is_var("?c") && are_not_same_nodes("?a", "?c")
swap-const-pass-through: (swap ?a ?b ?c) => ?c 
    if is_const("?c") && are_not_same_nodes("?a", "?c")
```

Does this save us? I'm not sure. Sticking with just `var`s and `const`s for the
moment, this might work. One thing that I am trying to avoid is ever
(globally) identifying a _ground term_, either a `const` or a `var`, with
another ground term.

I'll mention now, since it's probably not obvious: a `var` is an _unknown term_.
It is passed in by the user, and it can be _anything_. These should probably be
called `param`s, since these are parameterizing our programs.
So, if I ever prove that `(var a)` is equal to `(var b)` globally, I've lost,
because I've proven that our user _always_ passes in the same values for
parameters `a` and `b`; this is clearly nonsense!

Likewise, if I prove that `(var a)` is equal to `7`, I've proven that our user
_always_ passes in the number `7` for parameter `a`. Again, balderdash!

Finally, if I prove that `7` and `8` are equal, things are dire indeed. 

_Ground terms must be separate._

I actually need to be more careful than just not rewriting ground terms: what if
I prove that `(and (var a) (var b))` is equal to `(var a)`? Well then I've
proven that our dear user always passes in parameters that satisfy the condition
`(== (var a) (var b))`.

There are a number of ways I can put my foot in it. But let's begin by keeping
ground terms separate.

I'm going to adopt the following terminology: a _value term_ is a term without
any `swap` nodes, while a _swapping_ term is a term that is not a value term.
This is a little complicated since I am dealing with equivalence classes of
terms, but I'll worry about that later.

The errant global identifications are coming from swapping terms being rewritten
to multiple _distinct_ value terms (here, I use distinct to mean that the
different terms are not already in the same eclass). I don't need to worry about
value terms being rewritten _to_ swapping terms: the only place this happens is
in the `refine` rule, and here the rewrite is rooted at a `phi` node. It's only
the roots of the rewritten nodes (and possibly their parents, via congruence)
that get identified: rewriting `(foo a b)` to `(foo b c)` only identifies two
`foo` nodes, not `a` with `b` or `b` with `c`. But identifying a `phi` node with
another `phi` node with appropriate values swapped causes no problems: this is
precisely what I want.

 TODO: worry about effects of congruence (I don't think this will be a problem
 but it might be, and may be sticky to prove).

So I want to prove that a swapping term w/ a `swap` root only has a single valid
rewrite (I'm not considering rewrites of subterms). Let's restate the rules
rewriting `swap` nodes:
```scheme
swap-match:              (swap ?a ?b ?a)        => ?b
swap-plus:               (swap ?a ?b (+ ?l ?r)) => (+  (swap ?a ?b ?l) (swap ?a ?b ?r))
swap-var-pass-through:   (swap ?a ?b ?c)        => ?c
    if is_var("?c") && are_not_same_nodes("?a", "?c")
swap-const-pass-through: (swap ?a ?b ?c)        => ?c 
    if is_const("?c") && are_not_same_nodes("?a", "?c")
```

The first thing that pops out at me is that `swap-plus` is dangerous: I should
guard it against `?a` being `(+ ?l ?r)`.

```scheme
swap-match:              (swap ?a ?b ?a)        => ?b
swap-plus:               (swap ?a ?b (+ ?l ?r)) => (+  (swap ?a ?b ?l) (swap ?a ?b ?r))
    if are_not_same_nodes("?a", "(+ ?l ?r)")
swap-var-pass-through:   (swap ?a ?b ?c)        => ?c
    if is_var("?c") && are_not_same_nodes("?a", "?c")
swap-const-pass-through: (swap ?a ?b ?c)        => ?c 
    if is_const("?c") && are_not_same_nodes("?a", "?c")
```

This almost looks good, but there is still a problem. Consider the following
term:
```scheme
(swap 0 (var a) (+ 0 0)) 
```
At some point `(+ 0 0)` will be identified as `0`.
Applying the `swap` to `(+ 0 0)` produces `(+ (var a) (var a))`, while applying
it to `0` produces `(var a)`; we've proved that `(== (var a) (+ (var a) (var
a))`, which is bogus.

I'm not sure we can avoid this just with our rewrite rules. We might need to use
some other mechanism, perhaps via an analysis.


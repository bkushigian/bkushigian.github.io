---
layout: post
title:  "Debugging Rewrites: 'attempt to add with overflow'"
date:   2020-08-10 12:28:50 -0800
published: true
comments: true
categories: cornelius research
tags: [cornelius, debugging rewrites]
group: cornelius
---

## Setup
Max just pushed a commit to change the semantics of `EGraph::add_expr(expr)`.
Previously this would look at the last node in `expr` and then recursively added
nodes that were reachable from the last node. Now it just iterates through
linearly and adds every node in `expr`, effectively allowing a `RecExpr` to have
multiple roots.

This fixed an issue I was having with my Cornelius refactor (namely, I had
assumed these new semantics and was getting baffling errors where everything was
broken).

Now that the original errors were fixed I started getting a new error when I ran
on the `triangle.xml` subject; I'd get the following output:

```bash
$ cargo run tests/triangle-debug.xml
    Finished dev [unoptimized + debuginfo] target(s) in 0.04s
     Running `target/debug/cornelius tests/triangle-debug.xml`
Running on subject file tests/triangle.xml
Reading from path tests/triangle.xml
...
thread 'main' panicked at 'attempt to add with overflow', src/peg.rs:300:39
note: run with `RUST_BACKTRACE=1` environment variable to display a backtrace
```


## Background
I saw a similar bug when I was refactoring and reimplemented constant folding.
Rules

```rust
    rw!("commute-mul";   "(* ?a ?b)"         => "(* ?b ?a)"),
    rw!("associate-mul"; "(* ?a (* ?b ?c))"  => "(* (* ?a ?b) ?c)"),
    rw!("mul-bot";       "(* ?a 0)"          => "0"),
```

would overflow with expression `(* 0 2)`. From my report on Slack:

> What is happening is that `(* 0 2)` -> `(* 2 0)` -> `0`. Then by
> deduplication, `(* 2 0)` -> `(* 2 (2 0))` becomes `(* 2 (* 2 (* 2 0)))` etc.
> all of these twos are multiplied together before reaching the zero. I tried to
> reproduce this in `lambda.rs` and no luck, so it’s gotta be something dumb I’m
> doing

Chandra replied that I should implement `modify`, and that this would fix my
problem (it did!)

## Bug Minimization

### Input
The input, `triangle.xml`, has over 100 mutants and thousands of entries in the
`id_table`. My first task was to minimize this. With some manual binary searchy
type things, I minimized the buggy input xml file to the following, stored in
`triangle-debug.xml`, to represent the following two expressions:
* `(+ (phi (== (var a) (var b)) 1 0) 2)` (id=11)
* `(phi true 0 -2147483648)` (id=12)

```html
<subjects>
<subject method="Triangle@classify(int,int,int)" sourcefile="../subjects/simple/triangle/regularized/Triangle.java">
<egg>0</egg>
<mutant id="1">
<egg>1</egg>
</mutant>
</subject>
<id_table>
<dedup_entry id="0" peg="a"/>
<dedup_entry id="1" peg="(var 0)"/>
<dedup_entry id="2" peg="b"/>
<dedup_entry id="3" peg="(var 2)"/>
<dedup_entry id="4" peg="-2147483648"/>
<dedup_entry id="5" peg="0"/>
<dedup_entry id="6" peg="1"/>
<dedup_entry id="7" peg="2"/>
<dedup_entry id="8" peg="true"/>
<dedup_entry id="9" peg="(== 1 3)"/>
<dedup_entry id="10" peg="(phi 9 6 5)"/>
<dedup_entry id="11" peg="(+ 10 7)"/>
<dedup_entry id="12" peg="(phi 8 5 4)"/>
</id_table>
</subjects>
```

### Rules
I minimized the rules to three rules:
```rust
    rw!("commute-add";   "(+ ?a ?b)"               => "(+ ?b ?a)"),
    rw!("plus-over-phi"; "(+ (phi ?c ?t ?e) ?rhs)" => "(phi ?c (+ ?t ?rhs) (+ ?e ?rhs))"),
    rw!("if-true";       "(phi true ?x ?y)"        => "?x"),
```

## Building a Test

I want to ensure that an input doesn't cause a panic. To do this I created the
following helper function:

```rust
#[allow(dead_code)]
fn rewrites_do_not_panic(exprs: &[&str]) -> bool{

    let mut egraph: EGraph<Peg, VarAnalysis> = EGraph::default();
    let rules = crate::rewrites::rw_rules();
    let runner = Runner::default();
    for expr in exprs {
        egraph.add_expr(&expr.parse().unwrap());
    }
    runner.with_egraph(egraph).run(rules.iter());

    true
}
```

I used this to create the following test:

```rust
    #[test]
    fn add_ac_1(){
        assert!(rewrites_do_not_panic(&["(+ (phi (== (var a) (var b)) 1 0) 2)", "(phi true 0 -2147483648)"]));
    }
```

Sure enough this failed with

```bash
~/Projects/cornelius on  master! ⌚ 14:08:24
$ cargo test add_ac
    Finished test [unoptimized + debuginfo] target(s) in 0.05s
     Running target/debug/deps/cornelius-d44dd4de81c01466

running 1 test
test tests::misc::add_ac ... FAILED

failures:

---- tests::misc::add_ac stdout ----
thread 'tests::misc::add_ac' panicked at 'attempt to add with overflow', src/peg.rs:300:39
note: run with `RUST_BACKTRACE=1` environment variable to display a backtrace
```

Great, I have a test that documents this bug. I created a couple others just to
get a feel for the behavior (and simplified the `(== (var a) (var b))` condition
to `C`):

```rust
    #[test]
    fn add_ac_1(){
        assert!(rewrites_do_not_panic(&["(+ (phi C 1 0) 2)", "(phi true 0 -2147483648)"]));
    }

    #[test]
    fn add_ac_2a(){
        // Doesn't fail (nothing is subbed into the plus node)
        assert!(rewrites_do_not_panic(&["(+ (phi C 1 1) 2)", "(phi true 0 -2147483648)"]));
    }

    #[test]
    fn add_ac_2b(){
        // fails
        assert!(rewrites_do_not_panic(&["(+ (phi C 1 1) 0)", "(phi true 0 -2147483648)"]));
    }

    #[test]
    fn add_ac_2c(){
        // This doesn't fail: need the doubly nested phi nodes?
        assert!(rewrites_do_not_panic(&["(+ 1 0)", "(phi true 0 -2147483648)"]));
    }

    #[test]
    fn add_ac_2d(){
        assert!(rewrites_do_not_panic(&["(+ (phi C 0 0) 0)", "(phi true 0 -2147483648)"]));
    }

    #[test]
    fn add_ac_3a(){
        // This is relatively close to the boundary that will fail. This doesn't
        // fail, but add_ac_3b does
        assert!(rewrites_do_not_panic(&["(+ (phi C 1 0) 1)", "(phi true 0 -76650000)"]));

    }

    #[test]
    fn add_ac_3b(){
        // This is relatively close to the boundary that will fail
        assert!(rewrites_do_not_panic(&["(+ (phi C 1 0) 1)", "(phi true 0 -76700000)"]));
    }

```

So **2a** and **2c** don't fail. **2a** tells me that I need to substitute the
second expression into the `+` node in the first expression to get the panic.
**2c** tells me that I need the nested phi node.

The fact that **2d** fails tells me that the overflow is coming from adding a
bunch of negative numbers (everything else is `0` here, so the only thing that
can be added is the negative number).

**3a** and **3b** are interesting: coupled with **2d** it seems like there is
some sort of iterative thing happening where the big negative number is being
added over and over until it overflows. `-76650000` goes into `-2147483648` just
over 28 times. Calculating `(/ -2147483648 28)` gives `-76695844`, which should
be the biggest (smallest) number that I can substitute into `(phi true 0 x)`
without triggering the bug. I add the tests:

```rust
    #[test]
    fn add_ac_3c(){
        // this passes
        assert!(rewrites_do_not_panic(&["(+ (phi C 1 0) 1)", "(phi true 0 -76695844)"]));
    }

    #[test]
    fn add_ac_3d(){
        // this fails
        assert!(rewrites_do_not_panic(&["(+ (phi C 1 0) 1)", "(phi true 0 -76695845)"]));
    }
```

Sure enough, **3c** passes but **3d** fails.

## Finding the Bug's Root Cause
Okay, I have a hypothesis: my rewrites are turning my program into something like:
```scheme
(phi true 0 (+ -2147483648 (+ -2147483648 (+ -2147483648 ...) _) _))
```
and constant folding is triggering somewhere. So I need to take my three rules
and see if I can translate the following expressions (by substituting them in to
each other and applying the above rules) into the above form.

```scheme
(+ (phi C 1 0) 2)         ;; expr 1
(phi true 0 -2147483648)  ;; expr 2
```
For ease of writing I'll use symbol `X` for `-2147483648`.

```scheme
;; START
(+ (phi C 1 0) 2)

;; expr 2 ~> 0, so I can sub into the above then clause
(+ (phi C 1 (phi true 0 X)) 2)

;; Fire plus-over-phi rule
(phi C (+ 1 2) (+ (phi true 0 X) 2))

;; Commutativity
(phi C (+ 1 2) (+ 2 (phi true 0 X)))

;; plus-over-phi
(phi C (+ 1 2) (phi true (+ 2 0) (+ 2 X)))

;; Sub `(phi true (+ 2 0) (+ 2 X))` for 2
(phi C (+ 1 2) (phi true 
                    (+ 2 0)
                    (+ (phi true (+ 2 0) (+ 2 X)) X)))

;; plus-over-phi
(phi C
     (+ 1 2)
     (phi true 
          (+ 2 0)
          (phi true
               (+ (+ 2 0) X)
               (+ (+ 2 X) X))))

;; Sub `(phi true (+ 2 0) (+ 2 X))` for 2
(phi C
     (+ 1 2)
     (phi true 
          (+ 2 0)
          (phi true
               (+ (+ 2 0) X)
               ;;    vvv SUBBED HERE
               (+ (+ (phi true (+ 2 0) (+ 2 X)) X) X))))

;; plus-over-phi
(phi C
     (+ 1 2)
     (phi true 
          (+ 2 0)
          (phi true
               (+ (+ 2 0) X)
               (+ (phi true (+ (+ 2 0) X) (+ (+ 2 X) X)) X))))

;; plus-over-phi
(phi C
     (+ 1 2)
     (phi true 
          (+ 2 0)
          (phi true
               (+ (+ 2 0) X)
               (phi true (+ (+ (+ 2 0) X) X) (+ (+ (+ 2 X) X) X)))))
               ;;                            ^-----------------^
               ;; This expression will grow arbitrarily large, and X is added to itself many times.
```

Found it! Now, how to fix it?

## Possible Fixes

### Disable Checked Overflow
At the end of the day I want to spoof Java's semantics, and Java silently
overflows. I chatted with Max during egg office hours and he mentioned
[`std::intrinsics::wrapping_add`](https://doc.rust-lang.org/std/intrinsics/fn.wrapping_add.html).
This also goes away with the `--release` tag.

### Pruning
Max also mentioned pruning as something I should add in the `modify`
method---I'll mark this as a TODO for now.

## The Final Fix
I changed

```rust
    Peg::Add([a, b]) => Some(Peg::Num(x(a)?.as_int()? + x(b)?.as_int()?)),
    Peg::Sub([a, b]) => Some(Peg::Num(x(a)?.as_int()? - x(b)?.as_int()?)),
    Peg::Mul([a, b]) => Some(Peg::Num(x(a)?.as_int()? * x(b)?.as_int()?)),
```

to

```rust
    Peg::Add([a, b]) => Some(Peg::Num((Wrapping(x(a)?.as_int()?) + Wrapping(x(b)?.as_int()?)).0)),
    Peg::Sub([a, b]) => Some(Peg::Num((Wrapping(x(a)?.as_int()?) - Wrapping(x(b)?.as_int()?)).0)),
    Peg::Mul([a, b]) => Some(Peg::Num((Wrapping(x(a)?.as_int()?) * Wrapping(x(b)?.as_int()?)).0)),
```

and everything works!

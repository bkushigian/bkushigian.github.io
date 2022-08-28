---
layout: post
title:  "(Unofficially) Introducing Cornelius"
date:   2020-09-05 12:00:00 -0800
comments: true
published: false
categories: cornelius
tags: cornelius
group: cornelius
---
<pre>
   ___ _______               ___   ___ __        __         __ __         ___
 ,'  _|   |   |.-----.-----.'  _|.'  _|__|.----.|__|.---.-.|  |  |.--.--.|_  `.
 |  | |   |   ||     |  _  |   _||   _|  ||  __||  ||  _  ||  |  ||  |  |  |  |
 |  |_|_______||__|__|_____|__|  |__| |__||____||__||___._||__|__||___  | _|  |
 `.___|                                                           |_____||___,'
       _______         __                 __              __
      |_     _|.-----.|  |_.----.-----.--|  |.--.--.----.|__|.-----.-----.
       _|   |_ |     ||   _|   _|  _  |  _  ||  |  |  __||  ||     |  _  |
      |_______||__|__||____|__| |_____|_____||_____|____||__||__|__|___  |
                                                                   |_____|
               ______                          __ __
              |      |.-----.----.-----.-----.|  |__|.--.--.-----.
              |   ---||  _  |   _|     |  -__||  |  ||  |  |__ --|
              |______||_____|__| |__|__|_____||__|__||_____|_____|
</pre>

* TOC
{:toc}

## What is Cornelius?
Cornelius is a static analysis tool to detect equivalent and redundant mutants
in Java. At its core, Cornelius uses an Egraph to simultaneously compute all
possible rewrites of all programs simultaneously. Do to _deduplication_ or _node
sharing_, Egraphs are able to quickly compute exponentially many rewrites of a
program in a space-efficient manner. Egraphs also make it trivially easy to
check if two programs became equivalent under these rewrites.

In this post I officially unofficially introduce Cornelius. I'm going to start
this post by briefly describing mutation testing, motivating the equivalent
mutant problem and why we want to solve it. Then I'll introduce Egraphs,
focusing on intuition building.

## Background
### Mutation Testing
Imagine you've just finished writing a test suite for one of your projects. How
good is your test suite? If you're anything like me, then not very good. Still,
it would be nice to quantify just how not-good our test suites are.

What should tests do? Catch bugs, of course! Finding real live bugs in code is
hard, so we can't measure this property directly. However, we can seed a bunch
of syntactic faults in our program, producing new broken programs called
_mutants_. Each of these mutants is a proxy for a real world bug.

After mutating the original program, we try to _kill_ mutants by running the
test suite on each mutant. If a mutant causes one of the tests in the suite to
fail, that mutant is _killed_. We killed it. It ded.

#### Example

Consider the following program which computes the max of two `int`s:

```java
int max(int a, int b){
  if (a > b) return a;
  return b;
}
```

Let's look at four of this program's mutants:

**Mutant 1:**
```java
int max(int a, int b){
  // `>` -> `!=`
  if (a != b) return a;
  return b;
}
```

**Mutant 2:**
```java
int max(int a, int b){
  // `>` -> `>=`
  if (a >= b) return a;
  return b;
}
```

**Mutant 3:**
```java
int max(int a, int b){
  // `>` -> `false`
  if (false) return a;
  return b;
}
```

**Mutant 4:**
```java
int max(int a, int b){
  // `return a;` -> `;`
  if (a > b) ;
  return b;
}
```

Let Clarence be a test suite with only a single test.
```java
public class ClarenceTheTestSuite {
    @Test
    public void theOnlyTestClarenceHas() {
        assertEquals(max(1,2), 2);
    }
}
```

#### POP QUIZ 1
1. Which of the above mutants does Clarence kill?
2. What proportion (written as a decimal) of mutants does Clarence kill? This
   number is called the **mutant kill ratio.**
3. Can _you_ write tests to kill the other mutants? If not, why not? If so, what's
   your favorite color?
4. **(mandatory bonus question)** Can _you_ write tests to differentiate _all_
   mutants? That is, can you write a test suite such that for every pair of
   mutants `m1` and `m2`, there is a test `t` that behaves differently on `m1`
   and `m2`?
   If not, why not? If so, what is your favorite smell?

#### Mutant Kill Ratio
Problem 2 asks you to compute the mutant kill ratio. You should have gotten
0.25: `ClarenceTheTestSuite` kills mutant 1 but not mutants 2-4. Thus it kills
1/4 = 0.25 mutants. In general we want the mutant kill ratio to be close to 1;
let's say that for the rest of the day, any mutation kill ratio of above 0.85 is
"good".

#### Equivalent and Redundant Mutants
Unless you broke reality you should have found it impossible to kill mutant 2.
This is because it is _semantically equivalent_ to the original program, even
though it differs syntactically. Such a mutant is called an **equivalent
mutant**.

Likewise, you should have found it impossible to write a test that behaves
differently on mutants 3 and 4. These two mutants are semantically equivalent to
one another. Such mutants are called **redundant mutants**.

##### Problems with Equivalent And Redundant Mutants
Equivalent and redundant mutants cause two problems in mutation testing.

1. _They skew metrics:_ The best a test suite can possibly do is to kill mutants
   1, 3, and 4. This means that at best, the mutant kill ratio will be 0.75,
   which is not 'good' according to our above definition.
   
   Similarly, a killing a redundant mutant kills all the mutants in its
   redundancy class. This means that as long as a redundancy class is left
   unkilled, it as has an overly negative effect on the mutant kill ratio, and
   once it has been killed it has an overly positive effect on the mutant kill
   ratio.

2. _They waste resources:_ We have to run testing infrastructure on each mutant.
   At scale this is expensive, and especially so with equivalent mutants. This
   is because they can _never_ be detected, so we have to run the entire testing
   infrastructure, wasting CPU cycles. What's worse, a human developer might
   have to come in and waste human cycles trying to kill it by hand.

The equivalent mutant problem is one of the reasons why mutation testing isn't
more widely adopted in practice.

#### How We Handle the Equivalent And Redundant Mutant Problem
The only way to handle equivalent and redundant mutants is to detect them
before running the test suite. This is undecidable in general, but there are a
lot of mutants that are 'obviously' equivalent or redundant.

There have been a number of attempts to detect equivalent and redundant mutants.
These attempts fall into two categories:

1. **Use of constraints:** by modeling the semantics of two programs with
   constraints, we can a query a constraint solver to determine if there are
   inputs that force the programs to behave differently.
   - [Constraint-based automatic test data generation (DeMilli and Offutt, 1991)](https://ieeexplore.ieee.org/document/92910/)
   - [Using Constraints to Detect Equivalent Mutants (Offutt and Pan, 1994)](https://www.semanticscholar.org/paper/Using-Constraints-to-Detect-Equivalent-Mutants-Pan-Mason/329d2f8107679740395bac2cc0525f83adf33a20?p2df)
   - [Automatically Detecting Equivalent Mutants and Infeasible Paths (Offutt and Pan, 1997)](https://onlinelibrary.wiley.com/doi/abs/10.1002/(SICI)1099-1689(199709)7:3%3C165::AID-STVR143%3E3.0.CO;2-U)
   - [Using Constraints for Equivalent Mutant Detection (Nica and Wotawa, 2012)](https://arxiv.org/abs/1207.2234)
   - [Medusa: Mutation Equivalence Detection Using SAT Analysis (Kushigian, Rawat, Just, 2019)](https://ieeexplore.ieee.org/document/8728921/)

2. **Using compiler techniques:** compiler optimizations rewrite programs to
   more efficient forms. These rewrites preserve program semantics. Therefore,
   if two different programs are rewritten to the same optimized version they
   must have begun as equivalent.

   - [Detecting Trivial Mutant Equivalences via Compiler Optimisations (Kintis et al., 2018)](https://ieeexplore.ieee.org/ielx7/32/8338178/07882714.pdf?tp=&arnumber=7882714&isnumber=8338178&ref=aHR0cHM6Ly9pZWVleHBsb3JlLmllZWUub3JnL3N0YW1wL3N0YW1wLmpzcD9hcm51bWJlcj03ODgyNzE0)
   - [Trivial Compiler Equivalence: A Large Scale Empirical Study of a Simple, Fast and Effective Equivalent Mutant Detection Technique (Papadakis et al., 2015)](https://ieeexplore.ieee.org/stamp/stamp.jsp?arnumber=7194639&casa_token=SEBDFdKI7FkAAAAA:sdJSiNcUmwvStDWH1wS2UL1g1TnEtRhZBF6yXXmKOUjJ52iYEys-tl-C-mma9l8S7mzp4_Gjz9s&tag=1)
   - [Using compiler optimization techniques to detect equivalent mutants (Offutt and Jefferson, 1994)](https://onlinelibrary.wiley.com/doi/abs/10.1002/stvr.4370040303)

Cornelius uses a new approach: store all mutants in an Egraph and compute
equality saturation.
### Egraphs
Egraphs are a little tricky to explain, and I'm trying to keep this short. I'm
going to build some intuition, and refer the interested reader to
[the egg paper](https://arxiv.org/abs/2004.03082).

An Egraph is a more complicated version a union-find data structure. Union-find
traditionally tracks an _equivalence relation_ as new equivalences between
elements are discovered. Egraphs solve the problem of doing this when we have
function symbols. Consider the following two function applications:
`(f a b)` and `(f a c)`. Suppose we discover along the way that `a` and `c` are
equivalent. Traditional union-find isn't powerful enough to discover that the
two applications have become equivalent and should be merged into the same
equivalence class. Egraphs do this for us.

Of interest to you, dear reader, are the following properties:
1. **Egraphs never forget:** in a traditional rewrite system, rewriting the
   program `(+ a b)` to `(+ b a)` will destroy the original program. Not so in
   Egraphs! Every program you've ever encountered is remembered

2. **Egraphs are _space efficient_:** "Wow, an Egraph remembers every single
   program it encounters? This clearly must take a lot of storage space..."
   Wrong, my friend! Through the _miracle_ technology of _deduplication_ (or
   node-sharing), Egraphs can represent _exponentially_ many programs in _near
   linear space_!

3. **Egraphs are _space efficient_:** "Exponential, you say? Then _surely_
   runtimes are burdensome!" I'm afraid you missed the mark yet again. Egraphs
   are _fast_. Once again, deduplication saves us! By deduplicating nodes, a
   single rewrite rule can in fact rewrite entire _classes_ of rules all at once!

Cornelius uses [egg](https://github.com/mwillsey/egg), an (no, _the_!!!) Egraph
library written by the indomitable, the indefatigable, the one and only [_Maxwell
Q. Willsey, Esq_](https://mwillsey.com/).

### Peggy

Credit where credit is due! Cornelius is inspired by, and is in largely a
partial re-implementation and adaptation of, [Tate et. al's Peggy
system](http://www.cs.cornell.edu/~ross/publications/eqsat/). Peggy performs
compile time optimizations on Java bytecode programs by placing the programs in
an Egraph and running a bunch of semantic-preserving rewrite rules until no more
rewrite rules can fire. This point, where an Egraph holds enough variants so
that a rewrite system cannot fire, is called _equality saturation_. Once an
After reaching equality saturation, Peggy extracts an optimal version of the
program from the Egraph.

Peggy also performs _translation validation_, which basically stuffs a proposed
optimization and the original program in an Egraph, runs a rewrite system until
equality saturation is reached, and then checks to see if the two programs
became equivalent under the rewrites.

## Cornelius
Cornelius basically just performs translation validation. Given an original
program and a bunch of mutants, Cornelius sticks them all in an Egraph and runs
the rewrite rules until equality saturation is reached. Cornelius then extracts
the equivalence relation discovered by the Egraph between the original program
and the mutants, and reports this. Any mutants that are in the same equivalence
class as the original program are _equivalent mutants_; any mutants that are in
each other's equivalence classes are _redundant mutants_.

### Getting Cornelius
Cornelius can be cloned from the [Github
repo](https://github.com/bkushigian/cornelius). Build and run instructions are
in the project's README. In addition, there is a simple example of running
Cornelius on the Triangle program.

## A Challenge! Discovering New Equivalences
Crafting a rewrite system is hard, and I present to you, dear reader, a
challenge of the utmost import!

### The Max Program
First, let's run Cornelius on the `max(int a, int b)` program.

```
$ ./cornelius.sh serializer/tests/subjects/max/Max.java
Created temp working directory /var/folders/90/zz7ry2qd3m12wlsvjwr37kvh0000gn/T/cornelius-.iEcMYyW5
================================================================================
Running Major to generate mutants for /private/var/folders/90/zz7ry2qd3m12wlsvjwr37kvh0000gn/T/cornelius-.iEcMYyW5/Max.java
Generated 4 mutants (19 ms)
================================================================================
Regularizing subject /private/var/folders/90/zz7ry2qd3m12wlsvjwr37kvh0000gn/T/cornelius-.iEcMYyW5/Max.java to /private/var/folders/90/zz7ry2qd3m12wlsvjwr37kvh0000gn/T/cornelius-.iEcMYyW5/regularized\n
....
================================================================================
Serializing subject /private/var/folders/90/zz7ry2qd3m12wlsvjwr37kvh0000gn/T/cornelius-.iEcMYyW5/regularized/Max.java
================================================================================
[+] Visiting method signature: Max@max(int,int)
[+] Source file: Max.java
[+] Name: max(int,int)
Done creating XML File at: /Users/benku/Projects/cornelius/framework/scripts/subjects.xml
Serialized subjects file: /var/folders/90/zz7ry2qd3m12wlsvjwr37kvh0000gn/T/cornelius-.iEcMYyW5/Max.xml
================================================================================
Running Cornelius! Oh boy!!
    Finished release [optimized] target(s) in 0.05s
Reading from path /var/folders/90/zz7ry2qd3m12wlsvjwr37kvh0000gn/T/cornelius-.iEcMYyW5/Max.xml
rec_expr total_size: 42
egraph total_size: 10461
Writing equivalence classes to /var/folders/90/zz7ry2qd3m12wlsvjwr37kvh0000gn/T/cornelius-.iEcMYyW5/equiv-classes:
    /var/folders/90/zz7ry2qd3m12wlsvjwr37kvh0000gn/T/cornelius-.iEcMYyW5/equiv-classes/Max@max(int,int).equiv-class


Working Directory ......... /var/folders/90/zz7ry2qd3m12wlsvjwr37kvh0000gn/T/cornelius-.iEcMYyW5
Generated mutants ......... /var/folders/90/zz7ry2qd3m12wlsvjwr37kvh0000gn/T/cornelius-.iEcMYyW5/mutants
Serialized ................ /var/folders/90/zz7ry2qd3m12wlsvjwr37kvh0000gn/T/cornelius-.iEcMYyW5/Max.xml
Equivalence classes ....... /Users/benku/Projects/cornelius/Max-equiv-classes
```

I can view the discovered equivalence classes by running the following

```
$ cat Max-equiv-classes/Max@max\(int,int\).equiv-class
0
1
2
3 4
```

Each number corresponds to a mutant id generated by Major; mutant id 0 is
reserved for the original program. Each line corresponds to an equivalence
class. The first three lines mean that mutants 0, 1, and 2 were not discovered
to be equivalent to any other mutants; line 4 means that mutants 3 and 4 are
equivalent to one another.

We can view the generated mutants by looking at the path output after the
`Generated mutants ......... ` entry output above:

```
$ ls /var/folders/90/zz7ry2qd3m12wlsvjwr37kvh0000gn/T/cornelius-.iEcMYyW5/mutants
1 2 3 4
```
Let's take a look at mutants 3 and 4:
```java

/**
 * Mutant 3
 */
public class Max {
  int max(int a, int b){
    if (false) {
      return a;
    }
    return b;
  }
}
```

```java
/**
 * Mutant 4
 */
public class Max {
  int max(int a, int b){
    if (a > b) {
      ;
    }
    return b;
  }
}
```

Both of these are equivalent to the program `return b;`. Cool!

Some of the subjects, including `max()`, have ground truth computed for
equivalences.

```
$ cat serializer/tests/subjects/max/gt
1
3 4
0 2
```

Huh, it looks like we missed an equivalent mutant! Let's take a look at the
original program and the mutant:

```java
/**
 * Original program
 */
public class Max {
  int max(int a, int b){
    if (a > b) {
      return a;
    }
    return b;
  }
}
```

```java
/**
 * Mutant 2
 */
public class Max {
  int max(int a, int b){
    if (a >= b) {
      return a;
    }
    return b;
  }
}
```

Yup, these are indeed equivalent! So why can't Cornelius discover this
equivalence? I've written up some initial thoughts on how to discover this with
a rewrite system
[here]({% link _posts/2020-07-27-local-reasoning-in-egraphs.markdown %})
and
[here]({% link _posts/2020-07-28-local-reasoning-with-equality-refinement.markdown %})
  


### The Triangle Program

```
$ ./cornelius.sh serializer/tests/subjects/triangle/Triangle.java
Created temp working directory /var/folders/90/zz7ry2qd3m12wlsvjwr37kvh0000gn/T/cornelius-.zs7t4BhZ
================================================================================
Generating mutants for /private/var/folders/90/zz7ry2qd3m12wlsvjwr37kvh0000gn/T/cornelius-.zs7t4BhZ/Triangle.java
Generated 116 mutants (142 ms)
================================================================================
Regularizing subject /private/var/folders/90/zz7ry2qd3m12wlsvjwr37kvh0000gn/T/cornelius-.zs7t4BhZ/Triangle.java to /private/var/folders/90/zz7ry2qd3m12wlsvjwr37kvh0000gn/T/cornelius-.zs7t4BhZ/regularized\n
....................................................................................................................
================================================================================
Serializing subject /private/var/folders/90/zz7ry2qd3m12wlsvjwr37kvh0000gn/T/cornelius-.zs7t4BhZ/regularized/Triangle.java
================================================================================
[+] Visiting method signature: Triangle@classify(int,int,int)
[+] Source file: Triangle.java
[+] Name: classify(int,int,int)
Done creating XML File at: /Users/benku/Projects/cornelius/framework/scripts/subjects.xml
Serialized subjects file: /var/folders/90/zz7ry2qd3m12wlsvjwr37kvh0000gn/T/cornelius-.zs7t4BhZ/Triangle.xml
================================================================================
Running Cornelius! Oh boy!!
    Finished release [optimized] target(s) in 0.04s
Reading from path /var/folders/90/zz7ry2qd3m12wlsvjwr37kvh0000gn/T/cornelius-.zs7t4BhZ/Triangle.xml
rec_expr total_size: 2349
egraph total_size: 10271
Writing equivalence classes to /var/folders/90/zz7ry2qd3m12wlsvjwr37kvh0000gn/T/cornelius-.zs7t4BhZ/equiv-classes:
    /var/folders/90/zz7ry2qd3m12wlsvjwr37kvh0000gn/T/cornelius-.zs7t4BhZ/equiv-classes/Triangle@classify(int,int,int).equiv-class

Working Directory ......... /var/folders/90/zz7ry2qd3m12wlsvjwr37kvh0000gn/T/cornelius-.zs7t4BhZ
Generated mutants ......... /var/folders/90/zz7ry2qd3m12wlsvjwr37kvh0000gn/T/cornelius-.zs7t4BhZ/mutants
Serialized ................ /var/folders/90/zz7ry2qd3m12wlsvjwr37kvh0000gn/T/cornelius-.zs7t4BhZ/Triangle.xml
Equivalence classes ....... /Users/benku/Projects/cornelius/Triangle-equiv-classes
```

Cornelius wrote all discovered equivalence classes to file, one for each method,
and created a link to the files:
```
$ ls Triangle-equiv-classes
Triangle@classify(int,int,int).equiv-class

$ cat Triangle-equiv-classes/Triangle@classify\(int,int,int\).equiv-class | grep " "
3 6 10 13 17
77 83 85
48 54 58 64 68
21 22 24 25
35 39
105 111 113 116
28 32
91 97 99
```

Each of these numbers corresponds to a _mutant id_ generated by Major, and
each line corresponds to a set of mutants discovered to be equivalent to one
another. Thus, the first line means that mutants 21, 22, 23, and 24 are all
semantically equivalent to one another. This means that Cornelius has discovered
them to be in the same _redundancy class_.

Piping `cat`'s output through `grep " "` only shows equivalence classes
with more than one mutant. This makes the output easier to read.

Mutant id 0 is reserved for the original program: the set of discovered
equivalent mutants are those mutants on the same line as the original program's
mutant id, `0`. The above run did not discover any equivalent mutants.

Triangle, as well as several of the other programs in
`serializer/tests/subjects/`, has ground truth computed by hand in a `gt` file.
Triangle's ground truth is:

```sh
$ cat serializer/tests/subjects/triangle/gt | grep " "
0 40 69 75 104
103 115
105 107 108 111 113 116
21 22 24 25
23 77 79 80 83 85 88
28 29 32
3 6 10 13 17
35 38 39
42 44 45 48 50 51 54 58 60 61 64 68 70
73 74
76 87
91 93 94 97 99 102
```

The first line lists all the equivalent mutants: mutant 40, 69, 75, and 104.
Cornelius didn't discover any of these!

Can _you_ help cornelius discover these equivalences?

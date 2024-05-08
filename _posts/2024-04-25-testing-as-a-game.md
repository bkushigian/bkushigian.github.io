---
layout: post
title: Software Testing as a Game
date: 2024-04-23 01:00:00 -0400
comments: true
published: false
draft: true
categories: software_testing
tags: research
group: research
custom_css: testing-as-a-game
attacker: "<span class='attacker'>attacker</span>"
defender: "<span class='defender'>defender</span>"
defenders: "<span class='defender'>defenders</span>"
tester: "<span class='defender'>tester</span>"
developer: "<span class='attacker'>developer</span>"
---

Writing good tests is hard. There has tremendous literature written over the
decades trying to tackle problems of writing test suites and evaluating the
strength of existing test suites.

Another way of saying a test suite $$T$$ is 'good enough' is to say '$$T$$ is
_adequate_'. There are a variety of adequacy metrics, including [code
coverage][wiki-code-coverage] and [mutation score][wiki-mutation-testing].
In both cases the adequacy metric sets up a _proxy_ for test adequacy and
determines $$T$$'s performance according to this proxy measure.  Current
adequacy metrics still have a lot of room to grow. I've been thinking about this
problem a lot lately during my research, and I keep wanting to model software
testing as a game. This feels intuitive to me, and I think it probably does to
many others, but I haven't come across any work that does this. So I'm going to
lay out some high level ideas. It won't be rigorous, but it should be in
the right direction, and maybe I'll refine it later.

## Testing is Collaborative

Testing a piece of software should be a collaborative effort: the tester and
the developer, often the same person, both want to make good software. Despite
this collaborative spirit, testing software is fundamentally an _adversarial
process_: as a tester I am trying my damnedest to break the thing that you, the
developer, made. This makes testing a natural candidate to model as a game, and
there are several ways to do this.

In this post I define three classes of testing games. Each class
highlights and emphasizes different parts of testing. For instance,
the [collaborative game](#the-collaborative-game) models the case where
tester and developer work together to build correct code,
the [fault finding game](#the-fault-finding-game) models the utility of testing
to discover existing faults placed by adversaries, and the [regression
game](#the-regression-game) models the utility of testing a correct
implementation to prevent future regressions.

I'll be considering [white-box testing][white-box-testing] for the rest of this
post.

## Modeling Costs

To rigorously define a game we would want to model the cost of writing each test
$$C_{T_W}$$, the cost of running each test $$C_{T_R}$$, and the cost of a fault
going undetected $$C_f$$, and combine these in a way to maximize profit. This is
a difficult calculation, and would make for an unwieldy game that would in my
opinion lose a lot of its explanatory power.
Instead, I'll find simpler cost models such as "the tester is only allowed one
thousand tests for this function".

## The Collaborative Game

This game is not adversarial at all: both the developer and the tester work
together to build correct software.  In the collaborative game, a developer
tries their best to write a correct implementation, and the tester tries their
best to think of anything the developer missed and catch it with a test. Both
developer and tester lose the game when a bug goes to production.
We can view both developer and tester as {{page.defender}}s working together
against their common enemy, _The Inherent Difficulty of Writing Correct
Software_.

While this game is ultimately collaborative, there is an implicit adversarial
component between the tester and the developer: when the developer submits a
piece of code for review, they are essentially saying "this code is correct".
When the tester writes a test, they are essentially saying "I think you might be
wrong". They are having an argument. Each test is the tester saying "Oh yeah?
Well what about _this_", and each time the program passes the test is the
developer saying "I already thought of that, and here is how I handle it".
It is this adversariality between the tester and the developer that I want to
probe further.

## The Fault Finding Game

In the Fault Finding Game, we view the _developer as the {{ page.attacker }}_
and the _tester as the {{ page.defender }}_.
This is not normally how I see testing games described (though [Kukreja et
al.][randomizing-regression-tests] take this approach): usually the tester is
the {{ page.attacker }} and the developer is the {{ page.defender }}.
From a strictly formal standpoint it doesn't matter who attacks and who defends.
But from a narrative standpoint it does.

When I view the developer as the {{ page.defender }} and the tester as the
{{ page.attacker }} my goal is to make the system under test (SUT) robust
against any tests a devious tester could throw at it.
This SUT-centric viewpoint of software testing is common and natural: after all,
a test suite is a means to an end (a reliable and fault-free SUT).

But a test suite is a piece of software too, and since I want to write better
test suites and improve testing adequacy metrics, I'll be adopting a
_tester-as-the-{{ page.defender }}_ model.

In this game the {{ page.tester }} has a specification $$S$$, and the
{{ page.developer }} gives them a faulty implementation $$I$$ of $$S$$.
The {{ page.tester }}'s goal is to find the fault by writing a test case
conforming to $$S$$ that fails when run on $$I$$.

Before exploring this game further I need to do a bit of bookkeeping.
Basically, I say that the {{ page.tester }} is performing
[white-box testing](#the-importance-of-white-box-testing) and that the
{{ page.developer }} needs to write a reasonable looking fault that would pass
[code review](#the-code-review-constraint-constraining-the-attacker).
Feel free to skip these and get
[back to the Fault Finding Game](#back-to-the-fault-finding-game).

### The Importance of White Box Testing

In these games it is crucial that the {{ page.tester }} have access to the
implementation details (i.e., perform white-box testing).
Otherwise, it would be trivial for the {{ page.developer }}  to inject a
hard-to-detect fault.
For instance, if the {{ page.developer }} provides an implementation $$I_{id}$$
for the identity function with specification
$$S_{id} = \forall x\in\text{Long}: id(x) = x$$
they could just special case a single input

```java
long id(long x){
    if (x == 98372900402) {
        return 98372900402 + 1;
    }
    return x;
}
```

The {{ page.tester }} has almost no chance in detecting this without running a prohibitive
number of tests. However, if the {{ page.tester }} has access to the internals of `id` then
they can inspect the implementation, realize that there is special logic when
`x == 18446744073709551616`, and write a test case to ensure that
`id(18446744073709551616)` is correct.

### The Code Review Constraint: Constraining the Attacker

There are obvious constraints on the {{ page.tester }}: they cannot write too
many tests (e.g., exhaustive testing) because of the cost of writing and running
tests.
We need to place similar constraints on the {{ page.developer }} as well.
The size of the faulty implementation the {{ page.developer }} submit cannot be
too large.
Otherwise the {{ page.attacker }} could perform a sort of DOS attack on
{{ page.defender }}, who only has so many tests they can write, where they
write an overly complex or obfuscated faulty implementation of `id`:

```java
long id(long x) {
    switch (x) {
        case -9,223,372,036,854,775,808: return -9,223,372,036,854,775,808;
        case -9,223,372,036,854,775,807: return -9,223,372,036,854,775,807;
        case -9,223,372,036,854,775,806: return -9,223,372,036,854,775,806;
        // ...
        case -36,854,775,806: return -36,854,775,806 + 1; // FAULT
        // ...
        case -1: return -1;
        case 0: return 0;
        case 1: return 1;
        // ...
        case 9,223,372,036,854,775,805: return 9,223,372,036,854,775,805;
        case 9,223,372,036,854,775,806: return 9,223,372,036,854,775,806;
        case 9,223,372,036,854,775,807: return 9,223,372,036,854,775,807;
    }
}
```

Now even though the developer has access to the implementation, the
implementation is so large that they still cannot perform adequate testing
because there are quintillions of branches.

There are a number of ways to address this, and I would like to handle this
better in the future, but for now I am imposing a _Code Review Constraint_ on
the {{ page.developer }}, meaning that any implementation they submit to the
{{ page.tester }} needs to pass a reasonably thorough code review process.

<hr/>

### Back to the Fault Finding Game

So how should the {{ page.tester }} allocate their testing resources? This is
actually a very difficult problem, and its solution depends on a lot of
variables, including testing costs, fault tolerance, the domain of the SUT, and the
type of faults that the {{ page.developer }} would write.
Some faults involve using the right building blocks in the wrong way, say by
swapping a `<` operator with a `>` operator.
Many of these types of faults are relatively easy to detect with something as
simple as code coverage adequacy.
As an example, if the {{ code.developer }} tries to write a faulty version of
the `max(int a, int b)` program as

```java
int max(int a, int b) {
    if (a > b) return b;
    return a;
}
```

then writing a test for each branch where `a > b` and `a <= b` will suffice to
detect the fault. The {{ code.developer }} introduced and branched over the
crucial relation `a > b` for correctly implementing `max`, and this makes it
easy to test if the code is correct. The tester can detect this fault with a
single test, say `assertEqual(2, max(0,2))`.
But the {{ code.developer }} could have been subtler with their fault:

```java
int max(int a, int b) {
    if (a > b + 1) return a;
    return b;
}
```

Now this probably doesn't pass code review, but it's such a simple program no
fault would. But now having perfect coverage does not guarantee that we will
detect the fault. For instance, the {{ page.tester }} could write two tests
`assertEqual(2, max(0,2))` and `assertEqual(2, max(2, 0))`, which exercises both
branches, but fail to detect the fault. In fact, to detect this fault we need to
provide `a` and `b` such that `a == b + 1`: `assertEqual(2, max(2, 1))`.

The {{ page.developer }} might provide a branchless implementation of `max`. An x86

```java

```

This makes it harder for the {{ page.tester }} to determine correctness, since the
importance of the ordering relationship between `a` and `b` is no longer
explicitly in the code.
in a way to obfuscate the
semantics and hide the crucial relation `a > b`.

However, this is no always the case. The developer could give
a 

If the {{ page.developer }} introduced all the right building blocks but uses
them incorrectly then we can often look at those usages

## The Regression Game

<!-- REFERENCES -->

[white-box-testing]: https://en.wikipedia.org/wiki/White-box_testing
[randomizing-regression-tests]: https://projects.iq.harvard.edu/files/teamcore/files/2013_32_teamcore_ase.pdf
[foundationsBook-adequacy-sample]: https://www.cs.purdue.edu/homes/apm/foundationsBook/samples/adequacy-chapter-sample.pdf
[wiki-code-coverage]: https://en.wikipedia.org/wiki/Code_coverage
[wiki-mutation-testing]: https://en.wikipedia.org/wiki/Mutation_testing
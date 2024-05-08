---
layout: post
title: Rethinking Test Utility
date: 2024-05-05 01:00:00 -0400
comments: true
draft: true
published: true
categories: software_testing
tags: research
group: research
---

<style>
.fig {
    border: 2px solid;
    padding: 1em;
    width: 75%;
    margin: 2em;
}
</style>

Most software engineers don't need to justify writing tests for their software:
it's such an obviously good idea that they just do it.  Of course they can
rattle off reasons like "we write tests to find bugs," or "we write tests to
prevent regressions." And for day-to-day use cases this is fine.


<!--
    We can frame the question of test suite adequacy as the question "should I
    write another test?" Thus framing sets up our problem in terms of the cost
    of writing and running a new test versus the marginal utility gained by
    adding a new test to the test suite.
-->

As systems scale, so do test suites, and determining when a test suite is
_adequate_, or 'good enough', becomes infeasible. 
To help determine when a test suite is adequate, developers use adequacy
metrics such as _coverage_ (statement, branch, condition, etc) or _mutation
score_. Different adequacy metrics present different tradeoffs:
coverage-adequate test suites is cheaper and easier to build than
mutation-adequate test suites, but they also offer weaker theoretical
guarantees.

<div markdown="1" class="fig"> ![Coverage versus Mutation](/assets/img/CoverageVsMutation.svg){: width="400" float="left" }
<p><b>(Fig 1)</b> <em>Coverage versus Mutation Adequacy:</em> a visualization of the tradeoffs
between coverage adequacy and mutation adequacy</p>
</div>

So how should we determine adequacy? Should we use an adequacy metric like
coverage or mutation score? If so, which one should we use?

There is no single answer to this question, since each project's requirements
are unique.  However, researchers are tasked with quantifying these tradeoffs,
and to do so we they need to estimate the _utility_ of different test suites.

## Test utility

Informally, the _test utility_ $$u(T)$$ of a test suite $$T$$ is the _benefit a
developer gains by using suite $$T$$_. This will suffice for this post, but I'll
also offer a first attempt at a formal definition (feel free to skip, this is more
for my own reference than anyone else).

I'll say that a _test utility function_ is a real valued function $$u$$ on the
space of test suites:

$$u: \text{Tests} \rightarrow \mathbb{R},$$

that satisfies three axioms:

1. **The empty test suite has no utility**: $$u(\varnothing) = 0$$
2. **Test utility is monotonic increasing**: $$T_1 \subseteq T_2 \implies u(T_1) \leq u(T_2)$$
3. **Diminishing marginal utility**: Formally we say that utility is a
  _submodular function:_ $$u(T_1 \cup T_2) + u(T_1 \cap T_2) \leq u(T_1) +
  u(T_2)$$. This is fairly technical, and this post can be understood without
  understanding the details of submodularity, but if you're interested check
  out [Jan Vondrak's lecture notes][vondrak-submodular-functions] or the
  [Wikipedia page on Submodular Set Functions][wiki-submodular-functions]

Again, this is probably not super important but I think it's good to have some
formalism set up in case I want to roll up my sleeves and calculate later on.

## Fault detection as test utility

The classic utility measure for test suites is fault detection. There are
different ways of measuring fault detection. 
The 'ideal' fault detection measure would be the percentage of faults present in
a code base that are detected by a test suite, but this is infeasible as the
number of faults present in a piece of software is unknown.
Instead, researchers often try to estimate the _probability of detecting a
fault_ or the _expected number of faults detected_.

The benefit of using these fault detection measures is that they are fairly easy
to estimate: run a test suite and determine if any tests failed.
It is also fairly easy to simulate running tests on faulty software under
certain assumptions, and this was done quite a bit throughout the 1980s (e.g.,
[Duran and Ntafos][duran-ntafos]).

### Problems with fault detection test utility

Fault detection utility measures do not fully capture the utility of writing
tests. To illustrate this, suppose we write a test, run it on our program, and
it passes. Keeping the test has a non-zero cost (e.g, each run of the test uses
computing resources, maintaining the test costs developer hours, etc). But since
it did not detect any faults it has zero utility according to our utility
measure.  Therefore, according to our utility measures, _we should delete all
passing tests from our test suite!_

Clearly this is ridiculous: tests offer utility beyond simply being able to
detect a fault in the current instance of software. After all, software is not
static. It is constantly changing and evolving, and in this contexts tests have
much more to offer.

And I believe that these problems with the fault-detection metric have led the
community to counterintuitive results. For instance,
[Duran and Ntafos][duran-ntafos] conclude that random testing is superior to partition
testing because their simulations suggest that the effort from using a partition
testing approach (e.g., path testing) does not significantly increase fault
detection over random testing when the fault rate is low.  But their
interpretation misses much of the long-term utility of testing.


## So why do we test?

So why do we write tests? We write tests for many reasons:

1. _Detect faults in the present version of software:_ we've already discussed this.

2. _Have a developer think about the code:_ simply having a developer consider
   parts of code from the perspective of a test writer is immensely valuable.
   Our thought processes when writing versus testing software are different (at
   least mine are). When I'm writing software, I'm trying to build it. I am engaging
   in a form of _optimistic thinking_. My goal is to produce software, and my
   incentives are to conclude that my software is correct. However, when I'm
   testing the same software I'm trying to poke holes in it: I am engaging in
   _pessimistic thinking_. My goal is figure out why my implementation is wrong.

   By forcing a developer (either a the author of the software or another person
   entirely) to test each part of software, we are forcing this sort of
   adversarial relationship. Having a competent tester try to poke holes in
   software is incredibly valuable in increasing our confidence in the software.
   

3. _Encode the developer's/tester's thoughts as partial specifications:_ Not
   only does a tester think about the code, they encode their thoughts as test
   cases. These test cases act as a partial, executable specification. This is
   immensely valuable because we get to reuse all the work from (2) in all
   future versions of the software.

4. _Prevent introduction of new faults (regressions):_ Using the test cases
   from (3), we can catch future errors as they are being introduced. But not
   only do we prevent faults from being committed to the repository, but we can
   help the developer who wrote the fault to fix any misunderstandings they had
   about the code that led to the fault.

   Thus, the test cases written in (3) help to pass on expert knowledge derived
   in step (2) to future developers.

These points are all well and good, but it is not possible to encode these in a
formal utility measure that is feasible to execute. However, we _can_ conclude
that the utility of a test should grow the more times it is executed. Just
because it doesn't detect a fault _now_ doesn't mean it won't detect a fault
_later_. This in turn implies a utility that is super-linear to fault detection.
If an adequacy criterion produces a test suite with fault detection utility
$$u_d(T)$$, then its _true_ utility should be something that increases over time.

## Future Test Utility

I want to model test utility in a way that considers future test runs.
One possible definition would be

$$u_f(T) = {u_d\left(T\right)}^{\left(1 + r\right)^N}$$


where:
- $$u_d$$ is the fault detection rate of the test suite on the current software
<!--
- $$c_{run}$$ is the cost to run the test suite
- $$c_{write}$$ is the cost to write the test suite
-->
- $$0 \leq r \leq 1$$ is the regression detection factor, and encodes how
  effective the test suite will detecting future regressions in each future
  software version, and
- $$N$$ is the number of future versions of software the suite will be run on.

First, we want to show that this is a utility function according to our
definition. I use the fact (left unproven) that $$u_d$$ is a utility function
according to our definition.

1. **The empty test suite has no utility:** 
   Since $$u_d(\varnothing) = 0$$, we have 
  
   $$u_f(\varnothing) = {u_d(\varnothing)}^{1 + r} = 0^{1 + r} = 0$$

   for all values of $$r$$.

2. **Monotonic increasing:**
   $$u_d$$ is monotonic increasing, $$x^{1 + r}$$ is monotonic increasing in
   $$x$$ for $$1 + r > 0$$, and composition of monotonic increasing functions is
   monotonic increasing, so $${u_d(T)}^{1 + r}$$ is monotonic increasing.

3. **Submodularity:** $$u_d$$ is submodular

   $$u_d(T_1 \cup T_2) + u_d(T_1 \cap T_2) \leq u_d(T_1) + u_d(T_2)$$

   Let $$x = 1 + r \geq 1$$. By [Jan Vondrak's lecture
   notes][vondrak-submodular-functions], we can prove submodularity by showing
   that the marginal utility of adding test $$t$$ to a test suite has
   diminishing marginal utility. Formally, we denote the marginal utility
   adding test $$t$$ to suite $$T$$ as $$u^T(i) = u(T + i) - u(T)$$.
   Thus we want to show that for test suites $$T_1 \subset T_2$$ and test $$t$$
   not in either $$T_1$$ or $$T_2$$,

   $$u_f^{T_1}(t) = u_d^{T_1}(t)^x \geq u_d^{T_2}(t)^x = u_f^{T_2}(t).$$

   But this follows from submodularity of $$u_d$$ and $$x \geq 1$$:
   since $$u_d^{T_1}(t) \geq u_d^{T_2}(t) \geq 0$$, and since $$x \geq 1$$,
   we must have 
   $$u_f^{T_1}(t) = u_d^{T_1}(t)^x \geq u_d^{T_2}(t)^x = u_f^{T_2}(t).$$

Thus, we have proved that $$u_f$$ is a utility function.



This model of utility is of course not perfect:

- $$r$$ will not be constant for all software revisions; some versions of
  software will make very minor changes, while some may include large refactors
- the number of future versions $$N$$ cannot be known
- it loses the simplicity of pure fault detection
- it is non-trivial to estimate the cost of writing and running the test suite,
  and this value may change over time.

But the model also has some desirable features. It captures differences in use
cases that, (e.g., personal projects versus enterprise software).
For instance, if I'm testing a personal project then I probably won't have a
million revisions of my software, so I can choose a relatively small $$N$$. This
will lead to lower future testing utility, and I need fewer tests, and a weaker
adequacy criterion will suffice (maybe coverage, maybe something weaker).

If I have a piece of enterprise software then I will have a large $$N$$, and
I'll want to employ stronger adequacy criteria.



We can also simplify the formula to something like:

$$u_f(T) = {\left(u_d(T) - c_{run}(T)\right)}^2 - c_{write}(T)$$

This at the very least begins to capture the future benefits of test writing,
even if we are not precise about it.


<!-- REFERENCES -->

[vondrak-submodular-functions]: https://theory.stanford.edu/~jvondrak/MATH233B-2017/lec14.pdf
[wiki-submodular-functions]: https://en.wikipedia.org/wiki/Submodular_set_function
[duran-ntafos]: http://ieeexplore.ieee.org/document/5010257/
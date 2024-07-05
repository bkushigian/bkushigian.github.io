---
layout: post
title: rethinking test utility
date: 2024-05-08 11:00:00 -0800
comments: true
draft: true
published: true
categories: software_testing
tags: research
group: research
---

<div class="tldr" markdown="1">

<p markdown="1">
  <b>TLDR</b>:
  <i markdown="1">When we compare test adequacy metrics or test suite
  generation techniques we often measure the utility of test suite $$T$$ with
  fault detection $$u_d(T)$$ to determine which approach is better.
  Fault detection does not account for future runs of the test suite (e.g., to
  prevent regressions), and I believe that this results in a systematic
  under-approximation of test suite utility.
  </i>
</p>

<p>
  <i markdown="1">In this post, I argue for a test utility metric that takes
  future fault/regression detection into account. Further, I argue that
  the utility of future executions of the test suite should be correlated with
  the current fault detection, and thus we should see a super-linear
  relationship between actual test utility and fault detection.
   </i>
</p>

<p>
  <i markdown="1">
  I model future test utility $$u_f(T)$$ of a test suite $$T$$ as a function
  $$u_d(T)^{f(N)}$$ that is super-linear to fault detection $$u_d$$, where $$N$$
  is the number of times a test suite will be run in the future. Finally, I
  propose a 'simple' instantiation of the model: $$u_f(T) = u_d(T)^{2}$$ where
  we take the square of the fault detection.</i>
</p>
</div>



## intro

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

<hr class="figdiv"/>

<p><b>(Fig 1)</b>
  <em>Coverage versus Mutation Adequacy:</em>
  a visualization of the tradeoffs between coverage adequacy and mutation
  adequacy</p>
</div>

So how should we determine adequacy? Should we use an adequacy metric like
coverage or mutation score? If so, which one should we use?

There is no single answer to this question, since each project's requirements
are unique.  However, researchers are tasked with quantifying these tradeoffs,
and to do so we they need to estimate the _utility_ of different test suites.

## test utility

I'll start off with an informal definition of test utility.

<div class="definition" markdown="1">
**Definition:**  Test Utility (informal)

The _test utility_ $$u(T)$$ of a test suite $$T$$ is the amount of benefit a
developer gains by using suite $$T$$.
</div>

This informal definition will suffice for this post, but I'll also offer a first
attempt at a formal definition (feel free to skip, this is more for my own
reference than anyone else).

<div class="definition" markdown="1">
**Definition:** Test Utility (formal)

A _test utility function_ is a real valued function $$u$$ on the space
of test suites:

$$u: \text{Tests} \rightarrow \mathbb{R^{\geq 0}},$$

that satisfies three axioms:

1. **The empty test suite has no utility**: $$u(\varnothing) = 0$$

2. **Test utility is monotonic increasing with respect to the subset relation**:
   $$T_1 \subseteq T_2 \implies u(T_1) \leq u(T_2)$$

3. **Diminishing marginal utility**: Formally we say that utility is a
  _submodular function:_ $$u(T_1 \cup T_2) + u(T_1 \cap T_2) \leq u(T_1) +
  u(T_2)$$. This is fairly technical, and this post can be understood without
  understanding the details of submodularity, but if you're interested check
  out [Jan Vondrak's lecture notes][vondrak-submodular-functions] or the
  [Wikipedia page on Submodular Set Functions][wiki-submodular-functions]
</div>

Again, this is probably not super important but I think it's good to have some
formalism set up in case I want to roll up my sleeves and calculate later on.

## fault detection as test utility

The classic utility measure for test suites is fault detection. I'll denote it
$$u_d(T)$$.
There are different ways of measuring fault detection. The 'ideal' fault
detection measure would be the percentage of faults present in a code base that
are detected by a test suite, but this is infeasible as the number of faults
present in a piece of software is unknown.
Instead, researchers often try to estimate the _probability of detecting a
fault_ or the _expected number of faults detected_.

The benefit of using these fault detection measures is that they are fairly easy
to estimate: run a test suite and determine if any tests failed.
It is also fairly easy to simulate running tests on faulty software under
certain assumptions, and this was done quite a bit throughout the 1980s (e.g.,
[Duran and Ntafos][duran-ntafos]).

### problems with fault detection test utility

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


## so why do we test?

We write tests for many reasons:

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

These points are all well and good, but I don't know how to encode them as a
formal utility function that is easy to execute and easy to reason about. However, we _can_ conclude
that the utility of a test should grow the more times it is executed. Just
because it doesn't detect a fault _now_ doesn't mean it won't detect a fault
_later_. This in turn implies a utility that is super-linear to fault detection.
If an adequacy criterion produces a test suite with fault detection utility
$$u_d(T)$$, then its _true_ utility should be something that increases over time.

## future test utility

I want to model test utility in a way that considers future test runs.
There are many ways to do this, and to help narrow down on a metric I'll make
the following assumption:

<div class="fig" markdown="1">
**Assumption:** _Regression detection is positively correlated with fault detection._

I assume that the future regression detection power of a test generation
technique/process (e.g., mutation testing, code coverage, random testing,
partition testing) is positively correlated with the test generation technique's
fault detection rate.
</div>

I'm phrasing this in terms of a test generation technique's fault detection
power rather than in terms of the test suite because I want a robust test suite
to have positive utility even when all tests pass.

One way to construct a utility function that respects our assumption and takes
future utility into account is to model it by assuming that the utility of
regression detection in future software versions is linearly related to fault
detection with coefficient $$r$$. Then, we get fault detection $$u_d(T)$$ from
running testing our SUT with $$T$$, and then we get utility $$r \cdot u_d(T)$$
each of $$N$$ times we run our suite on future software versions.  This gives us
utility function:

$$\begin{aligned}u_f(T) &= u_d\left(T\right) + N\cdot r\cdot u_d\left(T\right)\\ 
                        &= \left(N r + 1\right) u_d\left(T\right)\end{aligned}$$



where:
- $$u_d$$ is the fault detection rate of the test suite on the current software

- $$r > 0$$ is the regression coefficient, and encodes how effective the test suite
  will detecting future regressions in each future software version, and

- $$N$$ is the number of future versions of software the suite will be run on.

<div class="fig" markdown="1">
<p markdown="1"><b>Proof that $$u_f$$ is a test utility function</b></p>

Again, feel free to skip the formalism.

We want to show that this is a utility function according to our
definition. I use the fact (left unproven) that $$u_d$$ is a utility function
according to our definition.

1. **The empty test suite has no utility:** 
   Since $$u_d(\varnothing) = 0$$, we have 
  
   $$u_f(\varnothing) = {u_d(\varnothing)} + N\cdot r \cdot u_d(\varnothing) = 0 + N\cdot r\cdot 0 = 0$$

   for all values of $$r$$.

2. **Monotonic increasing:**
   $$u_d$$ is monotonic increasing, and $$Nr + 1 \geq 1$$, so $$u_f(T)$$ is
   monotonic increasing

3. **Submodularity:**

   For clarity, write $$\alpha = Nr + 1$$ so that $$u_f = \alpha\cdot u_d$$.
   We know that $$u_d$$ is submodular:

   $$u_d(T_1 \cup T_2) + u_d(T_1 \cap T_2) \leq u_d(T_1) + u_d(T_2)$$

   and since $$\alpha > 0$$, we have

   $$\alpha\left(u_d(T_1 \cup T_2) + u_d(T_1 \cap T_2)\right) \leq \alpha\left(u_d(T_1) + u_d(T_2)\right).$$

   It follows that

   $$\begin{aligned}
     u_f(T_1 \cup T_2) + u_f(T_1 \cap T_2) &= \alpha\cdot u_d(T_1 \cup T_2) + \alpha\cdot u_d(T_1 \cap T_2)\\
                                           &= \alpha\left(u_d(T_1 \cup T_2) + u_d(T_1 \cap T_2)\right) \\
                                           &\leq \alpha\left(u_d(T_1) + u_d(T_2)\right)\\
                                           &= \alpha\cdot u_d(T_1) + \alpha\cdot u_d(T_2)\\
                                           &= u_f(T_1) + u_f(T_2)\\
     
   \end{aligned}
   $$

Thus, we have proved that $$u_f$$ is a utility function, according to our definition above.
</div>



This model of utility is of course not perfect:

- $$r$$ will not be constant for all software revisions; some versions of
  software will make very minor changes, while some may include large refactors;
- in fact, $$r$$ may systematically _decrease_ over time as the existing test
  cases pertain less and less to future software versions
- the number of future versions $$N$$ is not known.

But the model also has some desirable features: 
- $$u_f$$ is also easy to implement, analyze, and interpret; after all, for a
  fixed $$r$$ and fixed $$N$$, it is just a constant multiple of $$u_d$$.

- $$u_f$$ captures differences in use cases. For instance, if I'm working on a
  personal project then I probably won't have too many revisions of my software,
  so I can choose a relatively small $$N$$. This will lead to lower future
  testing utility, and I need fewer tests, and a weaker adequacy criterion will
  suffice (maybe coverage, maybe something weaker).

  If I have a piece of enterprise software then I will have a larger $$N$$, and
  I'll want to employ stronger adequacy criteria.

## how do the utility measures differ?

Scaling $$u_d$$ by a constant factor leaves many properties of the measures
unchanged. The ordering of utilities will be the same, as will the ratios:

$$u_d(T_1) / u_d(T_2) = u_f(T_1) / u_f(T_2)$$

But if we are comparing test utility against the _cost_ of writing and running
new tests, this constant factor can make a big difference.

Comparing test utility with the cost of writing test is not easy, but we can do
something simple like try to translate the costs and benefits into dollar
amounts. For instance, some potential dollar amounts (that I just came up with
off the top of my head, don't read too much into them!) might be:

+ **$10,000:** money saved by detecting a fault or a regression with a test
  suite
+ **$50:** cost of writing a single test
+ **$5:** cost run a test




<!-- REFERENCES -->

[vondrak-submodular-functions]: https://theory.stanford.edu/~jvondrak/MATH233B-2017/lec14.pdf
[wiki-submodular-functions]: https://en.wikipedia.org/wiki/Submodular_set_function
[duran-ntafos]: http://ieeexplore.ieee.org/document/5010257/
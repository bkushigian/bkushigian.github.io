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
In this post I officially introduce Cornelius, a framework to detect equivalent
and redundant mutants using Egraphs, a union-find data structure with some
remarkable properties. This post is geared at people who already know a bit
about the problem I'm trying to solve, and about Egraphs, so I won't be
supplying a ton of background info. However, I'll link to resources for the poor
lost soul who stumbles upon this post.

I'm going to start this post by briefly describing mutation analysis, motivating
the equivalent mutant problem and why we want to solve it, and touching briefly
on some attempts at solving it. Then I'll introduce Egraphs very briefly,
focusing more on building intuition on how they can help us attack the
equivalent mutant problem. This background section can be skimmed or skipped if
you already know the basics of mutation analysis and Egraphs.

## Background
### Mutation Analysis
**Mutation analysis** is a class of software analysis techniques that
'mutates' a program by altering it syntactically. These altered versions of the
program, called **mutants**, are typically used as proxies for real world bugs.

In **mutation testing,** a common form of mutation analysis that evaluates the
effectiveness of a program's test suite, the test suite is run on each mutant.
If a mutant causes at least one test in the suite to fail, we say the test suite
**killed** the mutant. The more mutants killed, the better.

#### Mutant Kill Ratio

To quantify how well a test suite performs we define the **mutant kill ratio**
to be _#(mutants killed)/#(mutants generated)_. The higher the mutant kill
ratio, the better.

#### The Equivalent (and Redundant) Mutant Problem
However, some mutants cannot be killed by _any_ test because they are
semantically equivalent to the original program. These are called **equivalent
mutants**.

Other mutants are distinct from the original program but equivalent to other
mutants. These are called **redundant mutant.** For the rest of the article I'll
consider equivalent mutants to be a special case of redundant mutants, even
though this usage departs from the literature.

Equivalent and redundant mutants cause two problems with mutation analysis.
1. **Skewed metrics:** since an equivalent mutant can't be killed by any test,
   it is counted as an unkilled mutant if left undetected. Similarly, redundant
   mutants make killing some mutants 'worth more' than killing other mutants.
   This is because a redundancy class of mutants is killed all at once. 
2. **Performance issues:** The test suite is run on each mutant, running until
   the mutant is killed. Equivalent and redundant mutants cause unnecessary test
   suite executions, a waste of cpu cycles. To make matters worse, unkilled
   mutants are often inspected by a human developer to write new tests to kill
   the mutant. If the mutant is equivalent (and thus unkillable), this wastes
   human cycles.
   
### Example

```java
public int max(int a, int b) {
    if (a > b) return a;
    return b;
}

/**
 * The first mutant of {@code max}:
 * Mutation: Replace `>` with `<`
 */
public int max1(int a, int b) {
    if (a < b) return a;
    return b;
}

/**
 * The second mutant of {@code max}:
 * Mutation: Delete statement `return a;` (replace with empty statement)
 */
public int max2(int a, int b) {
    if (a > b) ;
    return b;
}

/**
 * The third mutant of {@code max}:
 * Mutation: Replace `>` with `>=`
 */
public int max3(int a, int b) {
    if (a >= b) return a;
    return b;
}

/**
 * The fourth mutant of {@code max}:
 * Mutation: Replace `>` with `<=`
 */
public int max4(int a, int b) {
    if (a <= b) return a;
    return b;
}
```
{% include label.html 
   title="Listing 1"
   content="<code>max(int,int)</code> and four of its mutants"
%}

```java
@Test
public void testMax() {
    assertEquals(max(1,2), 2);
}
```
{% include label.html 
   title="Listing 2"
   content="A test for <code>max</code>. This passes on the original program
   as well as variants <code>max2</code> and <code>max3</code>, but fails on
   mutants <code>max1</code> and <code>max4</code>."
%}

The above test detects two of four mutants. This ratio is called the _mutant
kill ratio_, and is defined as _#(killed mutants)/#(generated mutants)_. The
above test suite (comprising only a single test) has a mutant kill ratio of 0.5.
A higher mutant kill ratio is better.



---
layout: post
title:  "Fun With the Collatz Conjecture"
date:   2018-06-10 15:00:00 -0400
comments: true
categories: collatz-conjecture misc
group: misc
---

![Collatz](https://upload.wikimedia.org/wikipedia/commons/thumb/b/b9/Collatz-stopping-time.svg/570px-Collatz-stopping-time.svg.png)
## Introduction

A friend is taking an intro CS class and reached out with a couple questions on
an assignment---more of an "is there anything glaringly wrong with this code?"
sort of thing than anything else (there wasn't). The assignment had to do with
computing Collatz numbers (also referred to as hailstone numbers) but didn't go
into many details so I thought that I'd write up some problems that go a little
deeper into the issue.

## Background

The basic idea of the [Collatz Conjecture][collatz-wiki] is that we have the
function `C` defined by

{% highlight java%}
    int C(int n){
        if (isEven(n)) {
          return n / 2;
        }
        return 3*n + 1;
    }
{% endhighlight %}

and that if we apply `C` to a number `n > 0` enough times we will eventually
reach 1:

{% highlight java %}
    n = 10;
    n = C(n);   // 5
    n = C(n);   // 16
    n = C(n);   // 8
    n = C(n);   // 4
    n = C(n);   // 2
    n = C(n);   // 1
{% endhighlight %}

We call such a sequence of numbers (i.e., 10, 5, 16, 8, 4, 2, 1, 4, 2, 1, ...) a
*Collatz sequence*. The claim that every Collatz sequence contains 1 is still
unproven. Check out the link above for more info. 

The following are some problems and exercises relating to the Collatz Conjecture
that are aimed at someone learning Java for the first time with an eye towards
mathematics.

Some of this references the assignment that I am riffing on but don't worry too
much about it---it just asks the student to write a Java program that accepts an
input integer and computes the Collatz sequence starting at that number,
printing out some statistics about the sequence (such as max number seen, length
of sequence).

Here is the boiler plate code that I'll be using for the remainder of this post:

{% highlight java %}
import java.util.Scanner;

public class Collatz {

    public static void main(String[] args) {
        System.out.print("Enter a positive hailstone starting value: ");
        int num = (new Scanner(System.in)).nextInt();
        int max = num;
        int count = 1;

        while (true) {
            num = C(n);
            if (abs(num) > max) {
                max = abs(num);
            }
            System.out.println(num);
            count += 1;
        }
    }

    public static int C(int n){
        if (n % 2 == 0) {
            return n/2;
        }
        return 3*n + 1;
    }

    public static int abs(int n){
        return n > 0 ? n : -n;
    }
}
{% endhighlight %}

Two things to notice:
1. **This code never terminates**---we have a `while(true)` with no break statement.
   We will be adding ways to terminate in the following problems. To make this
   terminate as normal, change `while (true)` to `while (n != 1)`.
2. **We use `abs(n)`:** using the absolute value of `max` lets us deal with
   negative numbers. Collatz generalizes nicely to the integers and we can write
   some better problems this way.

In all honesty I would prefer working on this problem in a language with a REPL
such as Python or Clojure. However, the original assignment was in Java so we
will stick to Java.

## Some Problems

### Defensive Programming
The following problems tie in with *defensive programming*, which is the
practice that we should assume that our worst enemy will give inputs to our
program trying to break it, and that we should detect these bad inputs,
sanitizing them when possible and exiting gracefully otherwise.

In the original code (not listed) the while loop had condition `while (n != 1)`.
This assumes that every number *will* eventually yield 1 eventually, but what if
it doesn't? Then our loop would never terminate. This would be bad. There are
two ways in which it wouldn't terminate:

1. We found a cycle of hailstone numbers that doesn't include `1`.  For example,
   starting with `n = -1` yields cycle `-1, -2, -1, -1, ...`. 
2. Our Collatz sequence goes off to infinity.

If either of these things happen then our program will never halt. In the
following problems we explore how we could defend against this.

#### Problem 1: *Cycle Detection*

Java has the `HashSet` data structure that lets you store things and look them
up later. For example, the following code snippet stores all even numbers in the
range `[0, 100)` in HashSet `evens` and then prints some stuff about it.

{% highlight java %}
import java.util.HashSet;       // Make sure to import this!

public class SetStuff {
    int main(String[] args){
        HashSet<Integer> evens = new HashSet<>();
        // Store stuff
        for (int i = 0; i < 100; ++i) {
            if (i % 2 == 0){
                evens.add(i);
            }
        }

        // Look stuff up
        for (int i = 0; i < 100; ++i) {
            if (evens.contains(i)){
                System.out.println("Found " + i);
            }
        }
    }
}
{% endhighlight %}

**Question:** <em> How can we use a `HashSet` to detect a cycle?  Use HashSets
to implement cycle detection. Modify `Collatz`'s main function to:

1. Create a HashSet called `visited` before entering the loop
2. Update `visited` in the loop as appropriate
3. Use `visited` to determine if you are in a loop---either use `break` to exit
   the loop or update the `while` loops condition directly: `while (something
   about visited...)`
</em>

#### Problem 2: *Constant Memory Cycle Detection*

The solution to P1 potentially uses a ton of memory. In this problem we walk you
through a (very bad) algorithm to detect cycles without having to store any new
data.

**Question:** <em> Use `max` and `count` to detect a cycle without storing any
extra data. Note that this algorithm doesn't detect a cycle immediately; it may
be in a cycle for a while before it realizes it.  </em>

(hint: look into the [pigeon hole principle][pigeon-hole-wiki] if you need a
hint)

#### Problem 3: *Compromise---A story of space and time*
Well, we have one algorithm that uses a stupid amount of space but takes minimal
time while another algorithm that uses no extra space but takes a looooong time.
This is typical in computer science---the [tradeoff between space and
time][space-time-tradeoff-wiki].

Here, however, we have a very nice compromise. This is a problem that I might
expect in a job interview. Be warned, this is a tricky one.

**Question:** Suppose, in addition to the variables in our boiler plate code,
you have an extra variable `int x` that you can use to store some extra
information, say another number in the Collatz sequence.

How can you use this to detect a cycle in a Collatz sequence?

#### Problem 4: *Some Proofy Exercises*

**Prove the following:**

1. C(N) preserves signs. That is, prove the following:
    - *if N is positive then C(N) is also positive*.
    - *if N is negative then C(N) is also negative*.

2. The `n`th Collatz sequence either cycles or diverges to plus/minus infinity.

#### Problem 5: *Unbounded Sequence Detection*

If we enter a loop as above then we have (hopefully) found a way to detect it as
per Problems 1 and 2. But what if our sequence goes to infinity? Well, we are
working on computers so some funny stuff happens.

As an experiment, run your code on the input `987654321` and see what happens.
On my system I get the output

    -20
    -10
    -5
    -14
    -7

that loops forever. Interesting, why is that? Well, the answer lies in how
computers represent numbers in binary and I won't bore you with the details.

The basic idea, though, is that each `int` only has so much space to store
numbers, both positive and negative, and once a positive number gets big enough
it becomes negative: this is called an *overflow*.  Dually, when a negative
number gets small enough it becomes positive: this is called an *underflow*.
Look into [two's complement] [twos-complement-cornell] arithmetic for more
information.

**Question:** 
1. How can we use our result we proved in problem 4.1 to detect an overflow or
   underflow in a Collatz sequence?
   
2. Say we have found an overflow in a Collatz sequence. Does this prove that our
   input induces this sequence that goes to infinity? Why or why not?


### Variants on Collatz

The Collatz function `C(n)` can be generalized by parametrizing as follows

{% highlight java %}
    int C(int n, int a, int b) {
        if (n % 2 == 0) {
            return n / 2;
        }
        return a*n + b;
    }
{% endhighlight %}
and our familiar version of Collatz `C` can be written as

{% highlight java %}
    int C(int n){ 
        return C(n, 3, 1);
    }
{% endhighlight %}

This is an example of *method overloading*: we use the same name but different
method signatures to get different behavior.

#### Problem 6: *No More Cycles*

**Question:** In the above code, suppose that `a > 0` and `b = a + 1`. Prove that there are no
cycles in this generalized Collatz sequence.

#### Problem 7: *A Shortcut*

**Question:** Is the following shortcut of our Collatz conjecture valid? Why or why not?

{% highlight java %}
    int shortcut(int n){
        if (n % 2 == 0){
            return n / 2;
        }
        return (n*3 + 1) / 2;
    }
{% endhighlight %}

<!-- links -->
[collatz-wiki]: https://en.wikipedia.org/wiki/Collatz_conjecture
[pigeon-hole-wiki]: https://en.wikipedia.org/wiki/Pigeon_hole_principle
[twos-complement-cornell]:https://www.cs.cornell.edu/%7Etomf/notes/cps104/twoscomp.html
[space-time-tradeoff-wiki]:https://en.wikipedia.org/wiki/Space%E2%80%93time_tradeoff

---
layout: post
title:  "The Kolmogorov Complexity of Truth"
date:   2018-08-06 12:00:00 -0400
comments: true
categories: opinion current-events
group: opinion
---

It's a common thing, really, hearing an argument that is too slick by half. You
know the type, where some smooth-talking proponent of a ludicrous and untenable
position leaves you reeling, convinced that up is down and that true is false.

Recently I've been following with some interest the story of [Cody
Wilson][wiki-cody-wilson], the man behind [Defense Distributed]
[defense-distributed], who has been making headlines by distributing schematics
for 3d-printable firearms. Wilson is just such a smooth-talker, as he
demonstrates in [this CBS
interview](https://www.youtube.com/watch?v=KatYW_gN4j8) where he, as the comment
section gleefully points out, picks apart interviewer Tony Dokoupil.

This is no surprise. Wilson is clearly intelligent and has a strong command of
language, having studied English at UT. He has also been an advocate and lead
spokesman for Defense Distributed since 2012, and this has given him time to
hone his arguments and become acquainted with many of the intricacies and
subtleties of his position (for an example of a somewhat less nuanced
presentation of Wilson's position, check out [Bob Garfield's 2012 interview with
Wilson][garfield-wilson-interview], rebroadcast recently on On The Media).
Dokoupil, on the other hand, is no specialist and would not have had time to
do the requisite research needed to challenge Wilson. I'm also guessing there
was some amount of "DIY guns are so clearly ridiculous, this interview will be a
walk in the park". This is often a fatal mistake.

But is this the entirety of Dokoupil's downfall? Equal parts inadequate
preparation and a scrappy opponent, with just a dash of ego to bring out the
flavor? Or is there something more fundamental at play here? I don't know for
sure, but I do have a theory. I'm sure it's not a new theory. It certainly isn't
*my* theory. But it's what's on my mind right now.

What I took away from the CBS interview is the simplicity of Wilson's arguments.
He appeals to generalities, to abstractions that are hard to argue with. For
instance, when Dokoupil asks "Your goal is to make guns as readily available as
possible, is that not your goal?", Wilson replies "I want to benefit the
American rifleman in his *lega* pursuits. You can make an AR15 in this country.
You can understand how to make an AR15. You can examine the plans of an AR15.
None of these are sanctioned activities. They are all expressly legal and have
always been."

Emergent properties are ignored and topics are instead destructured into
their constituent parts and treated separately.  This simplifies the issue
considerably, and I would argue misses the point of the debate. Yes, each
individual piece of the chain is legal, and admittedly the lawsuit filed against
DEFCAD (cite) was somewhat hacked together. But this is because there are
ramifications of new technology that we, as a society, need a chance to catch
up, first conceptually, and then legislatively.

Another example that is in some ways even cleaner than the gun debate is that of
the integer factorization problem. Common cryptographic systems such as RSA use
the fact that factoring numbers into primes is difficult to enable certain types
of encryption. This is heavily relied upon, and if someone were to release an
efficient algorithm tomorrow to factor an integer into its prime factors it is
very possible that our economy would crash. This would be huge: your credit card
numbers would be exposed, passwords would be open for all to see; basically, the
internet would be broken and unusable until it was fixed. This, in turn, would
have unpredictable consequences. There are moves to lessen the reliance on RSA,
but even so this would be disastrous.

Now if I found such an algorithm, should I be able to share it? I would argue
that I shouldn't be---the *cost* of doing so severely outweighs the benefit of
giving me free speech in this one instance.

Wilson's argument is a special case of a more general phenomena that I most
often see coming from libertarians. Let me pause to say that this post is not an
argument against libertarianism in general; to be honest I feel a certain
libertarian streak myself; the simplicity of many of the central tenets of
libertarianism is appealing, be it the championing of the individual and her
rights or to limitations placed on government's ability to interfere and meddle
with our day to day lives. Reading through The Cato Institute's [key concepts of
libertarianism][cato-key-concepts-of-libertarianism], there is nothing listed
that I am against prima facie. Rather, I'm posting to call attention to a
certain style of argument which I feel is deeply flawed, deeply misleading, and
dangerously effective. <!-- TODO: finish... -->

### An Aside on Kolmogorov Complexity
Before I continue I want to briefly discuss a concept called [Kolmogorov
Complexity][kolmogorov-complexity], something normally of concern only to the
computer scientist or the mathematician. As you probably know, programmers can
write programs to do certain things. For instance, a calculator contains a
program to add numbers as well as other elementary operations. The code for
this, written in C, would look something like this:

```C
int add(int a, int b) {
  return a + b;
}
```

The question that Kolmogorov complexity asks is "what is the minimum length a
program must be to do some task X?" More generally, how compactly can we
represent information? We might think of data compression, such as a zip file.
This is a fascinating topic and I recommend reading more about it if the
interest strikes you, though it is fairly technical. For our purposes, we may
simplify our definition to **the shortest accurate presentation of an idea.**

### Back to our Scheduled Program
The world is a complicated place, and it is no doubt a relief to have a short
and eloquent code to follow when grappling with complex issues. Indeed, being
able to refer to the constitution and its amendments is a simple way that we can
in general guide our decisions: when in doubt, defer. This leads to simple
arguments for complicated problems, something that is not without merit.

But are simple arguments more correct? Or do we instead prefer to argue points
that can be argued simply? Are there simply-stated truths which have no simple
argument, no eloquent and condensed sequence of words by which all good-faith
parties can arrive at the conclusion that yes, in fact, this statement is a
Truth?

In short I am asking the quesion *"What is the Kolmogorov Complexity of Truth?"*

In the realm of mathematics there are certainly such truths: [Fermat's Last
Theorem][fermats-last-theorem] is a prime example. But what about in the real
world? This is trickier, certainly. First of all, what is our definition of
truth? In math and first-order logic we may appeal to [Alfred Tarski's
Definition of Truth][tarski-truth]. I will not try to define truth in our
context---this posting is a sketch of an idea and I would only embarrass myself
if I tried to reach beyond my abilities.

And these foundational documents, being relatively unfettered by technicalities
and edge cases, make for simple, concise, and elegant arguments: arguments of
*free speech* and *the right to bear arms* trump inconveniences such as
harming society, and some will even conclude that executing a constitutional
right is precisely what makes our society healthy.

Often this is sound: freedom of speech is a powerful enough ideal that we allow
it, even when there are small scale detriments to our society; in most cases the
benefit far out stripes the detriment.

But are such ideals universal? Do they cover every case? Well, we certainly have
our fair share of counter examples. Felons cannot own firearms (todo: cite), we
cannot yell "FIRE" in a theatre, nor incite violence directly, and our right to
life, liberty, and the pursuit of happiness may be violated if we are deemed
criminals. It is clear that there exists behaviors that are not protected by our
ideals, and this means there is a boundary separating those things that are
clearly protected and those things that are clearly not protected by, say, free
speech. The issue, then, is determining this boundary.

When we accept as our core tenets some set of truths (such as the list of
concepts provided by Cato above) we have essentially designated a
[heuristic][wiki-heuristic] to make hard problems easier. We have posited a
model to simplify a complicated world. An appeal to this heuristic, say to Free
Speech, is really an appeal to a more complicated line of reasoning, whereby we
as a society have agreed that the benefits of allowing a particular item of
speech outweigh the costs of disallowing it. This is, of course, a highly
subjective analysis, but the analysis none-the-less exists.

For items clearly on one side of this dividing boundary, say my right to write
this blog post, there is not really any counter-argument to be made --- it's
obvious that I should be able to write a blog post. But for more complicated
issues such as the right to share the schematics to 3D-print guns, this is much
closer to this boundary.

My argument is this: a simple argument of "freedom of speech" should no longer
be sufficient. Certainly the behavior under question is a form of speech, and
certainly I have no qualms with the notion of freedom of speech in general. In
fact, for the purposes of this argument, I'm not even arguing against sharing
the schematics.

Instead I'm arguing that as expressions of free speech get closer to this
boundary the simple heuristic needed to argue the 'correctness' of the
expression becomes increasingly inadequate, and that defaulting to 'freedom of
speech' should not be considered correct.

[cato-key-concepts-of-libertarianism]: https://www.cato.org/publications/commentary/key-concepts-libertarianism
[four-color-theorem]: https://en.wikipedia.org/wiki/Four_color_theorem
[wiki-heuristic]:https://en.wikipedia.org/wiki/Heuristic
[kolmogorov-complexity]: https://en.wikipedia.org/wiki/Kolmogorov_complexity
[defense-distributed]:https://defdist.org/
[wiki-cody-wilson]:https://en.wikipedia.org/wiki/Cody_Wilson
[garfield-wilson-interview]:https://www.wnycstudios.org/story/battle-over-3-d-guns/
[fermats-last-theorem]:https://en.wikipedia.org/wiki/Fermat%27s_Last_Theorem
[tarski-truth]:https://plato.stanford.edu/entries/tarski-truth/

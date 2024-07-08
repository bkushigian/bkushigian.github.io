---
layout: post
title: "poker diaries, volume 3: variance"
date: 2024-07-09 0:00:00 -0800
comments: true
draft: false
published: true
categories: poker
tags: poker
group: poker-diaries
---


<hr style="margin:2em;">

I'm wrapping up week three of my little poker experiment and I hit a bit of a
rough patch, so I thought this would be a great time to talk about variance.
I'm going to start off with some some high level stuff about this past week,
then talk about variance for a bit, then get into the nitty gritty details of
some poker spots below.

## recapping the week

I covered my Friday session in [volume 2][volume-2]. I've continued struggling
with sleep, but I should be back on my sleep schedule finally (I hope). Since
I've been low on sleep I've been playing 3 tables instead of 4. This means I can
focus more and won't be overloaded 4 tough decisions all at once.

Another side effect of low sleep is I'm less robust to annoyance, which is one
of my main happiness leaks. Some sessions (foreshadowing alert), you get put in
gross spot after gross spot, nothing is easy, and you just feel like you're
playing awful. And I had just such a session on Saturday.

<div markdown="1" style="margin:1em; margin-top:2em;" >
![Saturday's swingy session](/assets/img/poker_diaries/003_a_swingy_session.png)
<div><font size="-1">
<p><i>
    My graph from Saturday. All the big pots were going the wrong way. I ran big
    hands into straights three times, lost some flips, ran a triple barrel bluff
    into top pair, and overall ran pretty bad. I was down 4 buyins (that is,
    -$400) halfway through the session, but a player who I'd tagged as a button
    clicker (that is, a spewy fish), donated $250 to me over two hands, which
    somewhat saved my session.
</i></p>
</font></div>
</div>

I was 3 tabling $100nl. I started earlier in the day (maybe 3pm or so), and was
just running into it. I sat out after a couple hours down $340, and sat back in
a couple hours later, and after some initial runbad, I broke even for a couple
hours before a player kindly donated $250 to me at the end of the session. I'll
go into specific hands later, but my main takeaway is that I played pretty well
overall. I misplayed a couple spots, and that definitely cost me some money, but
they weren't easy spots (well, one of them was, and I should have saved $30 on a
river...more on that in a bit).

<!-- TODO: finish this section...-->
I got back to the tables Sunday (_todo..._)

## variance (in poker)

I lost $140 over 765 hands. This doesn't really bother me because I have played
enough poker to know it's part of the game, and to see how much edge I have over
the pool. But lots of people would see this session and be freaked out. So I
figured I'd chat a bit about the role that variance plays in poker, and
ultimately in life.

There is randomness in poker, as there is in everything. When there is
randomness we can talk about certain statistical properties such as the _mean_.
Roughly, the mean of some random event is what we expect to happen, on average,
if we were to observe that random event a bajillion times. Yeah yeah, this
definition is a bit circular, elliptical at the very least, but we're not gonna
let that stop us.

Another property in the face of randomness is _variance_.  An intuitive
definition of variance is how much data points differ from the mean. High
variance means that data points often lie a long ways away from the mean, or
expected, outcome. Low variance means that the data points will always be
relatively close to the expected outcome.

For instance, flipping a coin for $1 has much lower variance than flipping a
coin for $1,000,000. Both have a mean of $0 (I win and lose half the time in
both scenarios), but the outcomes of the flip for $1,000,000 are 1,000,000 times
further away from the mean of $0 than the outcomes of the flip for $1.

We measure variance in poker winrates in terms of bb/100. On average, no limit
holdem winrates have about 100bb/100 variance. This means that every hundred
hands we are 70% confident that our results will be within 100bb of our expected
winrate. That's actually pretty crazy: 30% of the time we will be a buyin above
or a buyin below our expected win rate!

We can visualize variance using the [Prime Dope variance calculator][prime-dope].
Prime dope simulates playing a bunch of poker hands, modeled as a distribution
with a mean and some variance.  For starters, let's plug in a modest winrate of
2.5bb/100 with 100bb/100 variance and look at the distributions of outcomes

<div markdown="1" style="margin:1em; margin-top:2em;" >
![Prime Dope's simulation of running 100 hands with a 2.5bb/100 winrate and 100bb/100 variance](/assets/img/poker_diaries/003_primedope_2_5bb_100_hands.png)
</div>

This graph shows 20 different simulations of playing 100 hands with the provided
winrate and variance.  The dark black line is the mean result, or expected
value; by the end of 100 hands, our 2.5bb/100 winrate yields 2.5 big blinds of
expected profit. The light green parabolic-looking curve shows our 70%
confidence interval: we expect 70% of our simulations to fall within these
lines. Similarly, the dark green curve shows our 95% confidence interval.

The graph shows that we lose money approximately half the time and win money
approximately half the time, and there is about a 1 in 20 chance that we lose 4
buyins in 100 hands.

This seems stupid: why would you ever play poker if it's so dominated by luck?
Clearly this is just degenerate gambling?

Well, let's increase the sample size a bit. Here I have a slightly higher win
rate of 5bb/100 over 1,000 hands instead of 100. This is what I conservatively
expect my winrate is against the 100nl pool (or will be with a bit more
practice/study).

<div markdown="1" style="margin:1em; margin-top:2em;" >
![Prime Dope's simulation of running 100k hands with a 5bb/100 winrate and 100bb/100 variance](/assets/img/poker_diaries/003_primedope_5bb_100k_hands.png)
</div>

If you look closely you can see a thin black line at zero, and most of the
simulations are at or above this line. In fact, our 70% confidence interval line
(light green parabolic-looking thing) is now solidly above the zero line,
meaning 70% of the time we play 100k hands we will win _something_.
Still, there are a couple of negative lines that go below zero, and one line
goes 5,000bb below (bold maroon line at the bottom)!  That means there is about
a 1 in 20 chance to lose 50 buyins even though on average you should be winning
50 buyins on average. Of course, there is also a chance you run _above_ EV and
win 100 buyins instead of your expected 50 (bold light blue line at the top).

But all this to say: even after _a hundred thousand hands_ there is a good (1 in
20) chance that you'll be down a lot of money.

There is a flip side to this as well: there are many players who are absolutely
losing in expectation but who hit the high side of variance and think they are
winners. It's very tempting to believe that you are just experiencing negative
variance, and half the time you are. The fact is, our brains are just not very
good at reasoning about this stuff, and this is why it's important to be process
oriented: we need to review hands, study theory and strategy, look at population
tendencies, and most importantly, _be honest with ourselves_. Removing biases
and holding ourselves accountable is crucial for improvement.

We, as human beings, are very strongly results-oriented. We see a good outcome
and think "we played well". We see a bad outcome and think "we played bad". Just
look at sports discourse: people are arguing about variance and have no idea.

Overcoming our human results-oriented bias is not easy. We need to actively
focus on things we can control: making good decisions, both on the table and
off.  At the table we want to stick to our strategy, pay attention to our
opponents actions/timings/showdown, check in with ourselves to ensure we are
emotionally level, etc.  Away from the we want to focus on building and sticking
to a routine, getting enough sleep, eating well, taking time off, spending time
with loved ones, and, of course, studying our asses off and inspecting our game
with a fine-toothed comb.

Acknowledging our leaks is the only way we can get better. This is hard in the
face of variance.  When I win 5 buyins I want to come away and say "I played
awesome and I'm amazing". When I lose 5 buyins I want to say "I ran so bad, just
variance". But in either case there will be lots of hands where I left a bit of
money on the table. Maybe a dollar here, three dollars there. Being able to
review a session, winning or losing, ignore the variance, and focus on the small
edges we missed, is the only way to reach our potential.
Ignore the variance. It goes away with sample size.  _"Yes I ran absolutely
horribly, and my runbad cost me 5 buyins, but the real takeaway is that I made
these three mistakes that cost me a total of half a buyin."_

<hr style="margin:2em;">

Just for shits and giggles, before moving on, let's look at a million hand sample:

<div markdown="1" style="margin:1em; margin-top:2em;" >
![Prime Dope's simulation of playing a million hands with a 5bb/100 winrate and 100bb/100 variance](/assets/img/poker_diaries/003_primedope_5bb_1m_hands.png)
</div>

There is still a lot of variance here, but now every simulation is winning, and
the 95% confidence interval fluctuates between winning at 3bb/100 and 7bb/100.
Even after _a million fucking hands_ we can still experience relatively big
deviations in our observed winrate!


## variance (in life)

Variance dominates our lives, whether we like it or not. We can often view
variance as bad, but I like to think of it as the spice of life. It's what keeps
things interesting. Without it, we'd never have a bad meal, sure, but we'd also
never have an _incredible_ meal.

Variance can be problematic, especially if not approached carefully. See, the
downside of variance can be much costlier than the upside. Going back to our
coin flip game, suppose you and I both actually had a million dollars (exactly
one million). Flipping a coin for a single dollar would have no impact on our
lives, and in fact we could do it thousands of times without statistically
changing our life in the slightest. On average neither of us would win any money
(the man is $0). Flipping for $1,000,000 likewise has a mean of $0, but you
probably feel like it's a much worse idea than flipping for $1. Why? More
variance.

See, the variance is in the amount of money we have, and each additional dollar
we own is worth a little less than the previous dollar. If I am broke and I get
$1,000, I can pay rent, buy groceries, etc. My life went from homeless and
starving to living indoors and being well fed.

But if I have a million dollars and I get $1,000 my life won't change. I might
not even notice!

Think about that: life changing money for the first broke version of me isn't
even noticeable for the second rich version of me. This demonstrates the
_diminishing marginal utility_ of each dollar we own. And understanding this is
crucial when we reason about risk.

Anyway, I don't wanna go too far down that rabbit hole, it's not really the
point. However, I do want to talk about variance and how it affects me as a
poker player and a human being.


[volume-2]: https://bkushigian.github.io/2024/07/06/poker-diaries-volume-2.html
[prime-dope]:https://www.primedope.com/poker-variance-calculator/
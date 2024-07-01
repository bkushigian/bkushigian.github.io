---
layout: post
title: "poker diaries, volume 1: the beginining"
date: 2024-07-01 1:00:00 -0800
comments: true
draft: false
published: true
categories: poker
tags: poker
group: poker
---


![Cumulative Graph](/assets/img/poker_diaries/graph_w2_cumulative.png)

Things fell through this summer. I thought I would be making good money at an
internship, but due to a combination of bad luck and some unforced errors (I've
never been good at dealing with bureaucracies), I found myself with an
unexpected summer off. What would I do with my time off? Well, I decided I
wanted to spend my time doing something I love, and I also wanted to make money
doing it.  And anyone that knows me knows that I really only left one option:
poker.

I've been wanting to take a stab at playing professionally for a couple years,
and this is my opportunity. For the first time in my life poker is my sole
source of income, and I'm cataloging my experiences and struggles.  I'll
explaining what I'm working on, delving into topics ranging from lifestyle to
in-depth strategy to writing software to analyze poker sims.

## my first weeks

Today marks the end of my second week of playing professionally. I'm still
playing smaller stakes to get my game back into shape ($25 and $50 buyins).  I'm
only about 5,700 hands in, not much in online poker terms, but so far things feel
good. I'm thinking well, reasoning through spots, and I'm able to read many of
my opponents hands.

I'm slowly getting my volume up, but it's slow going. My first milestone will be
5k hands per week. The first week I got in 2.2k hands, and this week I got to
3.5k hands. This week I could have gotten more hands but I had sleep trouble
earlier in the week which led to lower volume.

I ran pretty hot in my first week. A lot of the volume was at 25nl (that is, $25
buyins), and I had some hands at 50nl as well.

<div markdown="1" style="margin:1em; margin-top:2em;">
![first week's graph](/assets/img/poker_diaries/graph_w1.png)
<div><font size="-1"><i>
    Graph of my winnings over the first week of playing. The dark green line is
    money won, and the yellow line is all-in adjusted. The x-axis shows hands
    played, and the y-axis shows money won.

    The box in the bottom right has some statistics. It shows that I've won $315
    over 2,240 hands, for a win rate of 41 BB/100. This is much higher than my
    actual win rate, probably 2-4x my win rate at peak hours.
</i></font></div>
</div>

<hr style="margin:2em;">

I ran good and played pretty well, though I made lots of mistakes. But some
nights it feels like people just want to give you their money, and who am I to
say no?

Even though this was a great first week it just demonstrates how hard this is
going to be. I ran great, but at the stakes I'm playing I'll need many more
hours of grinding to make ends meet. One way around this is to move up stakes,
and I'll be doing that soon once I feel better about my game and have studied a
bit more.

My second week was a bit tougher.

<div markdown="1" style="margin:1em; margin-top:2em;">
![second week's graph](/assets/img/poker_diaries/graph_w2.png)
<div><font size="-1"><i>
    Graph of my second week. It was a little rougher and I ran quite a bit worse.
</i></font></div>
</div>

I started out playing a 300 hand session on Monday night, signing on at around
1am.  I was tired and honestly shouldn't have been playing. I ran ice cold, and
played pretty bad too. I ran TT and JJ into AA four times. One hand I flopped a
set on KJT. The turn was a Q, AA shoved, and I snap folded my set of jacks (I
played this hand perfectly). There were a number of other gross spots. One hand
my software malfunctioned and I bet the wrong size, and the other player ended
up runner-runnering a 5 high flush vs my aces. Just a freak hand, nothing to be
done, but it really added insult to injury.

After about an hour I was down $150 and I sat out. It was a tough way to start
the week, and tougher still I was having sleep issues, making me miss most of my
volume on Thursday.
Friday and Saturday I had some more hands and ran quite a bit
better until I cooled off today (Sunday). I played 1.1k hands and it was pretty
break-even until the last little bit when I finally won some hands and ended up
$110.

Overall I feel like I played well this week: I started out down 6 buyins and
came back to end up 6 buyins, winning at just over 12bb/100.

<hr style="margin:2em;">

The best games are late at night, and I've been
experimenting with new sleep schedules so that I can be fresh for them. This
means I'm going to bed between 3am and 5am and sleeping until noon or 1. This
makes certain things difficult, and at some point I'll switch to an earlier
schedule, but for right now I think that this is the best way for me to make
this work.

It's not easy completely inverting your schedule. Case in point, I went to bed
at 4:30am this morning and woke up at 11am. I meant to sleep until 12:30 or so
but I just couldn't, so I ended up being a little slow and dull throughout the day.
I try to compensate with coffee but that leaves me a little agitated and overstimulated.

I think if I were to continue playing full time for an extended period of time I
could get my game to a place where I could make money during earlier hours, say
playing 5pm-10pm, asleep by midnight. The issue is edge versus rake. If I'm
better than the daytime pool, say winning 3bb/100 versus the pool, but I'm
paying 3bb/100 in rake to the house, I'd be breaking even.  At this point, I
will have a big edge over the pool during peak hours when all the drunk people
are on, probably closer to 10-15bb/100 post rake, so I should just play during
those hours.  However, if I improve my game so I have an 8bb/100 edge over the
daytime pool, the 3bb/100 in rake I'm paying still leaves me with a 5bb/100
edge. This might be enough to make it worth me playing during normal hours and
have a normal life.


On top of playing and studying, I'm also writing custom software to help me
analyze situations and study spots more efficiently. These days _poker solvers_
are a crucial part of studying poker. In short, a poker solver computes (or
approximates) a _Nash equilibrium_, which is a strategy that has certain
desirable properties. By studying these strategies we can discover how we can
play spots better, but also notice how opponents are misplaying spots and how we
can exploit this.

A solver can only solve one spot at a time, and running a solver on a spot is
called _running a sim_. For instance, the button opens to $2.50 preflop, the big
blind calls, and they see a flop of Ah8s7s. What is each player's strategy here?
The solver can tell you what the equilibrium strategy is (given a certain set of
actions and responses for each player, which have to be specified by the person
running the solver). However, for Ah8s6s the strategy might look different, and
we would have to run a separate sim.

There are 1755 strategically distinct flops, and many different configurations
of actions that can lead to a flop.  I've been running tens of thousands of sims
(I am quickly eating up around 20TB of SSD to keep it all stored).

Simply having the sims only gets you so much...you also need to be able to
understand them. I've been writing custom software to help me visualize
strategies across different boards to find patterns. It's not feasible to
implement a strategy perfectly, but by abstracting a way small differences in
strategies we can capture much of the value of an equilibrium strategy.

I'll be writing more about this custom software soon.


## poker

Poker is fundamentally a game of decision making in the presence of incomplete
information. My goal is to make the best decisions I can given the information I
have. Sometimes I'll be right, and sometimes I'll be horribly wrong.  But if I'm
good enough, I'll be right just a little more than I'm wrong, and over time I
will win money.

Poker is gambling. Technically. However, for a skilled player it is _positive
edge_ gambling, meaning that a good player should _profit in expectation_. In
gambling parlance, we'd say that a skilled player _has an edge_.

Many (most?) folks do not know this. They often group poker with games like
blackjack, craps, slots, etc, games in which everyone loses in the long run and
where there is no edge to be had (this is not quite true, I think you can gain
an edge in blackjack by counting cards, though to be honest I'm not sure...I've
never been particularly interested in blackjack). But I think it should be
grouped more with stock trading. You _can_ make money at it, and in fact many
pros are making six figures ($200k/year is not unheard of).


## what makes a poker pro?

Pros treat poker like a job. Their lives are a mix of clocking in to the poker
table and studying strategies. Some players put in enormous volume while others
play fewer hands but play with more intensity, eeking more edge out of each
hand.  Some players play a simple strategy that works 'well enough' while others
love to toy with ideas, come up with creative lines, and treat the craft more
like an art.
Regardless, pros spend much of their time simply playing poker, trying to make
good decision after good decision. Online pros will play many tables of poker at
once. They have their strategy refined down to a science, and most spots require
little thought or effort; they are rote.

But pros also spend a lot of time studying, trying to get new insights about the
game to get an edge against their competition. This involves working with
solvers and reasoning about different ways of playing a hand (or a _range_ of
hands). There are countless discord servers devoted to discussing poker
strategy, where people can post their hands to get feedback.  "Was this the
right size? Can I polarize in this spot? Should I split my range?  Against this
player type, should I develop a block bet?" A single hand can generate thousands
of words of discussion and lead to many simulations being run.

The game is so complicated that nobody can play it perfectly. This means that
people are always trying to find ways to play as accurately as possible without
simplifying the game to much.

## variance

Variance will be a big part of this series: in some sense, a professional poker
player is taking on variance in exchange for expected profit. Recreational
players get enjoyment from the thrill of gambling, and pros sell recreational
players this thrill in exchange for an expected profit, or what we like to call
an _edge_.  A pro might lose a lot of money to a recreational player over the
course of any given session, and the chance this can happen is essentially what
the pro is selling.  However, in the long run, the recreational is going to lose
money, and lots of it.

Every decision we make in life is subject to variance. Which school we go to,
where we work, who we date, etc.

Variance is costly. If I don't know the outcome of an event, I can't plan
accordingly. Variance also muddies feedback. Did I lose money this month at the
tables because I was playing bad, or because of variance? Finally, variance
actually costs us real-life utility. To illustrate, suppose you have $100,000 in
your bank account. You have two options: you may keep your $100k and go on with
your life as normal, or you can flip a coin to either double it up or go broke.
Which should you do?

It's pretty intuitive: you should not flip the coin. Why? Both have the same
expected value: on average you'll have $100k either way. The difference is
variance: the upside of winning $100k is not worth the downside of losing $100k.
If you win $100k your life will be a little nicer but largely the same. If you
lose $100k you'll be on the street unable to afford dinner.

Thus we fundamentally do not want to take on variance in many situations (there
are situations where variance is actually good to take on, I'll maybe touch on
those later). So why would you ever take on variance? Well, one reason is that
you have some _edge_. Imagine this: instead of winning $100k you win a billion
dollars when you win the coin flip. Now do you take this bet? For many people,
absolutely! The upside of becoming a billionaire probably outweighs the downside
of going broke.

In poker, the edge that we gain is enough to compensate us for the variance we
take on.  Being able to manage big swings, the result of variance, is
fundamental to poker. The mental game of poker is a big side of it, and maybe
not the strongest aspect of my game. It's something I'll be keeping an eye on.

Anyway, I'll be talking more about variance throughout as it is one of the most
crucial aspects of playing poker.

---
layout: post
title: "poker diaries, volume 1: the beginining"
date: 2024-06-28 11:00:00 -0800
comments: true
draft: true
published: true
categories: poker
tags: poker
group: poker
---


Things fell through this summer. I thought I would be making good money at an
internship, but due to a combination of bad luck and some unforced errors (I've
never been good at dealing with bureaucracies), I found myself with an
unexpected summer off. I was mad at first, maybe for twelve hours or so, but I
quickly came around. I've been wanting to take a stab at being a professional poker player
for a couple years now, and this was my opportunity. This summer, for the first
time in my life, I'll be playing poker full time as more sole source of income.

In this series I'll be recording my struggles and experiences, explaining what
I'm working on, and maybe convincing family and friends that I'm not a
degenerate gambler along the way.

## my first week

I am just wrapping up my first full week of playing professionally I'm playing
smaller stakes to get my game back into shape ($25 and $50 buyins).  I'm only
about 4,500 hands in, not much to be honest, but so far things feel good.  I'm
thinking well, I'm reasoning well, and I'm able to read many of my opponents
hands. Here is my first week's graph (along with some hands from last week).

![first week's graph](/assets/img/poker_diaries/graph_week_1.png)

The best games are late at night, so I've been experimenting with new sleep
schedules so that I can be fresh for them. This means I'm going to bed between
3am and 5am and sleeping until noon or 1. This makes certain things difficult,
and at some point I'll switch to an earlier schedule, but for right now I think
that this is the best way for me to make this work.

I spent some of last week getting back into the groove of things, but was also
studying quite a bit, writing software to help me analyze spots, and "running
sims", a term I'll use a lot. Basically, there are programs that can compute a
balanced poker strategy, called a _Nash equilibrium_. You can compute an
equilibrium for a certain part of the game, and learn a strategy for that spot.
This is called "running a sim" for that spot.

Anyhoo, I've been running tens of thousands of sims and writing code to help me
parse it all. More on that soon.

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
never been particularly interested in blackjack).

There are definitely some degenerate players, or _degens_, that play poker for
the pure gamble.  There are also recreational players, or _recs_, that like the
game but haven't devoted the time to build a solid strategy.  They often play
within their means, and some of them are even very good. I myself, up until last
week, considered myself a serious rec. Other players are highly skilled players
who have practiced their craft for years. These are the pros. And as of last
week, this is how I view myself.

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
_solvers_ and reasoning about different ways of playing a hand (or a _range_ of
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

Variance is costly. If I don't the outcome of an event, I can't plan
accordingly. Variance also muddies feedback. Did I lose money this month at the
tables because I was playing bad, or because of variance?

Being able to manage big swings, the result of variance, is fundamental to
poker. The mental game of poker is a big side of it, and maybe not the strongest
aspect of my game. It's something I'll be keeping an eye on.

Anyway, I'll be talking more about variance throughout as it is one of the most
crucial aspects of playing poker.

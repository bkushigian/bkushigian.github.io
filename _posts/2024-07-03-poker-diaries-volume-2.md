---
layout: post
title: "poker diaries, volume 2: study"
date: 2024-07-01 0:00:00 -0800
comments: true
draft: true
published: true
categories: poker
tags: poker
group: poker-diaries
---

<div markdown="1" style="margin:1em; margin-top:2em;" >
![PioSOLVER's Range Explorer](/assets/img/poker_diaries/002_header_01.png)
</div>

<hr style="margin:2em;">

I'm taking a couple days away from the tables, and I'm using this time to study.
Poker is a huge game and it's hard to develop a plan of attack for learning the
massive amounts of information needed to succeed at the table. High level
strategy discussions are useful but good players often have wildly different
opinions. Conversely, studying low level solver outputs can give very concrete
information, but gives little to no intuition to _why_ the solver does what it
does. What's more, there is a lot of _noise_ in a solver's outputs, with much of
the complexity accounting for a very small amount of the winrate, and it's hard
to come away with a concrete _implementable_ idea.

How should I study the game to efficiently build a strategy that is _simple
enough_ that I can implement it in game?  I have several things working in my
favor.  First, I have a very powerful computer that can quickly run sims.
Second, I am a programmer by trade, and this puts me in a position to write code
to automatically analyze solver outputs, allowing me to quickly find high-level
patterns.

## equilibrium

Poker solvers approximate "good" poker strategies called _Nash equilibria_.
A Nash equilibrium is just a strategy with a very desirable mathematical
guarantee: if I am playing my equilibrium strategy then the opponent cannot do
anything to make more money against me. They may mess up and give me more money
than I expect, but I am guaranteed to win a certain amount, in the long run,
by playing an equilibrium strategy.

Roughly, an equilibrium is a balance of forces. In poker we need to balance many
considerations to have a robust strategy, and when we become unbalanced our
strategy becomes vulnerable to exploits.
For example, if I only bet when I have a very strong hand, then my opponents
will realize that when I bet I am very strong, and they can play perfectly
against me, never bluffing when I bet, and only calling with high equity hands
that can beat my strong hands. Thus, I want to balance my strong hands with some
bluffs.  However, if I bluff too much then they can always bluff catch, and now
I'm losing money by bluffing too much.  Thus, I want to bluff just enough so
that the opponent is _indifferent_ between bluff catching and folding. This
dynamic is captured nicely in the AKQ game which Andrew Brokos' [Play Optimal
Poker][play-optimal-poker] sums up nicely.

## solvers

<div markdown="1" style="margin:1em; margin-top:2em;" >
![PioSOLVER Output](/assets/img/poker_diaries/002_sample_solver_output.png)
</div>
<hr style="margin:2em;">

Equilibria are far too hard to compute by hand, and we appeal to solvers
such as [PioSOLVER][piosolver] to compute them (or, accurately, approximate
them) for us. Unfortunately, the output is complex, and we can't just look at a
sim for a few minutes and understand the it. What's more, even if we understand
a single board relatively well, we still don't know anything about different
boards.

So how should we proceed? There are different philosophies on this. Some folks
delve deep into a sim with the intention of learning all its peculiarities. This
can be fun, and it can certainly give you insights into the underlying
mechanisms that cause the strategy. But it's also easy to get sidetracked by
minutia. Some players get obsessed with frequencies: this board bets 40% while
this one bets 45%. Other players try to compute simple strategies that are easy
to implement. These players are more likely to play massive volume: they may not
have the most complex strategy but it's dead simple to play, and this allows
them to play many tables at once.

I don't think I'm going to ever be a volume player, and I love the creativity of
poker. But rather than trying to grapple with all of its complexity at once I
want to learn the broad strokes first. I want to start with a dead simple
strategy and introduce complexity as needed.

To do this I'm running tens of thousands of sims. I'll begin by identifying
a "spot" I want to understand better, such as
_"Action folds to the button who opens to 2.5bb. The smallblind raises to 11bb,
the bigblind folds, and the button calls."_
At this point, the game would proceed by going to a flop, where the smallblind
would be first to act. First, there are 1755 strategically distinct flops that
can come. Of course some flops are similar to others, but to be totally rigorous
I want to run sims for each flop in a spot.
I also want to construct a _game tree_ which tells the solvers which actions
each player is allowed to perform. Can they each bet/raisex 4 different sizes at
each node? Or maybe a player is only allowed one size? Each choice will tell me
something different.

Typically there is a tradeoff: more options for a player leads to longer solve
times and higher complexity of that player's strategy, but also to more EV
(expected value), since there are more things that player can do. So my main
question when I start running sims is "how much can I strip away from a player's
game tree without costing them EV?"

I'll begin by giving a player many possibilities and running all 1755 sims, and
use this as a baseline strategy to compare EVs against.  Then I will strip away
options for the player to determine how much EV loss this corresponds to.

For instance, I might run a sim where the smallblind can check, cbet 33% of the
pot, or cbet 66% of the pot. However, I don't want to learn this strategy because
it is far too complex. So I will also run five more sets of sims:

1. one where the smallblind can only bet 66% pot with range (no 33% or checking)
2. one where the smallblind can only bet 33% pot with range (no 66% or checking)
3. one where the smallblind can only check with range (no betting at all)

I then run _aggregation reports_ that collect high-level statistics of a set of
solves for every board and output the data to a CSV. The output of an
aggregation report might look like this:

```csv
Flop,Global %,OOP Equity,OOP EV,OOP EQR,IP Equity,IP EV,IP EQR,BET 152 freq,CHECK freq
2h 2d 2c,100,56.11,153.082,118.621,43.89,75.4181,74.7089,81.77,18.23
3c 2d 2c,100,53.74,125.268,101.345,46.26,103.232,97.0273,55.92,44.08
3d 3c 2c,100,54.16,130.457,104.727,45.84,98.0432,92.9922,59.80,40.20
3h 3d 3c,100,56.39,155.822,120.143,43.61,72.678,72.4577,84.49,15.51
3h 3d 2c,100,54.89,138.028,109.324,45.11,90.4718,87.2063,68.16,31.84
3h 2d 2c,100,54.48,132.507,105.756,45.52,95.9924,91.6794,64.97,35.03
```

This is not very readable, and to help me understand it I've been working on
[Pious][https://github.com/bkushigian/pious], the Pio Utility Suite. The project
is still in the brain dump phase, and it would honestly be hard for anyone else
to hop in and use many parts of it right now, but there are a few parts of it
that are easy to use today (assuming you know some Python). One of them is the
aggregation report module, that allows you to view aggregation reports, filter
them down based on certain criteria, plot different variables against one
another, etc. Here is a sample session looking at the aggregation report of SB's
cbet node:

```python
>>> import aggregation.report as ar
>>> r = ar.AggregationReport("/Users/benku/DPS/PiousStrats/reports/SimpleTree/3BP/b66/SBvBTN/Root")
>>> r.describe()
            equity           ev          eqr      bet_152        check
count  1755.000000  1755.000000  1755.000000  1755.000000  1755.000000
mean     54.862695   126.054526    99.659857    51.678353    48.321647
std       3.144132    14.210141     6.962486    22.635363    22.635363
min      47.920000    83.745000    74.755400     0.000000     0.000000
25%      52.255000   116.066000    95.242800    36.040000    32.215000
50%      54.740000   125.599000    99.347900    51.950000    48.050000
75%      56.975000   136.111000   103.980500    67.785000    63.960000
max      64.870000   169.516000   126.283000   100.000000   100.000000
>>> r.plot()
```

The last line I ask to plot the data, which produces the following plot:

<div markdown="1" style="margin:1em; margin-top:2em;" >
![Pious Plotting an Aggregation Report](/assets/img/poker_diaries/002_pious_SBvBTN_3bp_all_boards.png)
</div>

Each color corresponds to a different board texture, and different sizes
correspond to different high cards on the flop. The largest points are A-high boards,
while smaller points correspond to smaller boards. Yellow/Orange boards are
monotone boards, while greener boards are connected boards. Blue boards are
paired and trip boards. The colors aren't perfect just yet...it's hard to find
20 distinct colors that each mean something and 'make sense together', but I'll
iron that out. But it's easy to see patterns right away. For instance, flush
boards have a lower betting frequency, or straight boards (green) tend to have
higher EV (to the right) when they are A-high (larger circles).

Maybe we want to investigate this further. We can _filter_ the current board and
plot the result:

```python
# Next I'll filter to only show straight boards without flushes, and plot
# the result without showing the legend (which takes up a lot of space)
r.filter('straight and not flush')
r.plot(legend=False)
```

<div markdown="1" style="margin:1em; margin-top:2em;" >
![Pious Plotting an Aggregation Report](/assets/img/poker_diaries/002_pious_SBvBTN_3bp_straight_boards.png)
</div>

In this way we can get quick high-level visualizations of spots to better
understand them. I'll talk more about some of Pious's capabilities in the next
post.

[play-optimal-poker]: https://www.thinkingpoker.net/poker-books/
[piosolver]:https://piosolver.com/
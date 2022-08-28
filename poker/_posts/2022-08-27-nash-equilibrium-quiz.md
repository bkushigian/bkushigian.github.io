---
layout: post
title:  "Nash Equilibrium Quiz"
date:   2022-08-27 13:28:50 -0400
comments: true
published: true
categories: poker, theory, quiz
group: poker
---

# Nash Equilibrium Quiz
I'm going to ask you a few questions to test your understanding of a Nash
equilibrium. The answers to the first two questions come from the definition of
Nash Equilibrium; the answer to the third involves some thinking.

+ _Definition of Nash Equilibrium_:

  > A Nash equilibrium (for a 2-player game) between players _X_ and _Y_ is a pair
  > of strategies _S = (Sx, Sy)_ for _X_ and _Y_ respectively such that when both
  > players use these strategies, neither can unilaterally alter their strategy to
  > increase their payout.

I'll start off listing questions and then I'll give the solutions below.

## Questions
### Question 1: Comparing Different Equilibrium Strategies
> Suppose you are playing rake free HUNL (heads up no limit) and you've
> discovered two different equilibrium strategies _S_ and _T_. Is it possible
> for _EV(S) > EV(T)_? If so, provide an example. Otherwise, prove that this is
> impossible.

### Question 2: Unused Lines in a Gametree
> Suppose that we have an equilibrium strategy _S = (Sx, Sy)_ for players _X_
> and _Y_ such that player _X_ never uses a particular action _A_ at a given
> node in the game-tree. Now, suppose we modify the game-tree to remove _X_'s
> option to take action _A_ at that node. Can _Y_ modify their strategy _Sy_ to
> exploit this altered game-tree (that is, can _Y_ increase their payout in any
> way?)

### Question 3: Nash Equilibria as Fixed-points of Maximally Exploitative Strategy Sequences

> Suppose you are playing HUNL rake free. You and your opponent start off with
> strategies S0 and T0. You adjust your strategy to maximally exploit T0,
> resulting in S1. Villain then adjusts to maximally exploit your new strategy,
> resulting in T1. You continue iteratively exploiting each other until you reach
> a fixed point (neither one of you can do any better).
>
> 1. Is this fixed point always an equilibrium pair?
> 2. Do you always reach such a fixed point?

## Solutions
### Question 1
It is impossible that _EV(S) > EV(T)_ for equilibrium strategies _S = (Sh, Sv)_
and _T = (Th, Tv)_; here _Sh_ and _Th_ are Hero's (that is to say, your),
equilibrium strategies, and _Sv_ and _Tv_ are villain's equilibrium strategies.

Let's prove by contradiction. Suppose that hero's payout is higher for _S_ than
it is for _T_; that is, _P(S)> P(T)_. Let's look at what happens when Hero plays
_Sh_ against villain's _Tv_. Since _Sh_ is part of an equilibrium strategy it
must win Hero at least _P(S) > P(T)_. This means that Hero could have used _Sh_
against villains _Tv_ and gotten a payout higher than if they'd used strategy
_Th_. Since _(Th, Tv)_ is an equilibrium pair this is impossible, and we've
derived a contradiction. Since we've derived a contradiction we conclude that it
is impossible for two equilibria to have different expected payouts.

### Question 2
Deleting an action unused by player _X_ from the game-tree does not open a
_X_ up to being exploited. This again follows from the definition of Nash
equilibrium: any strategy that would exploit this lack of a line would be
gaining EV against X's equilibrium strategy.

In more detail, fix X's equilibrium strategy _Sx_ and move it to the new
game-tree with action _A_ removed from the game-tree. If player _Y_ exploits
this strategy in the new game tree then they have increased their payout against
_Sx_. But _Sx_ has not changed at all, and this new exploitative strategy from
_Y_ would also be exploiting _Sx_ in the original game tree, contradicting that
_Sx_ was an equilibrium strategy to begin with.

### Question 3
1. When a fixed point is reached it is always an equilibrium. This follows
   directly from the definition of a Nash equilibrium: a fixed point is simply a
   pair of strategies such that neither player can unilaterally increase their
   payout by altering their strategy.

2. Such a fixed point is not always reached. In particular, finding maximally
   exploitative strategies does not handle mixing. A classic example is the AKQ
   game. If the polar player is under bluffing then the condensed player always
   folds, which makes the polar player always bluff, which makes the condensed
   player always call, which makes the polar player never bluff, which makes the
   condensed player always fold, etc.
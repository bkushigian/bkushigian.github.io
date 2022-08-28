---
layout: post
title:  "Introduction to NLHE Positions"
date:   2020-08-25 12:00:00 -0800
comments: true
published: false
categories: poker
tags: [debugging, miscellaneous, cornelius]
group: poker
---


## What is Position?
Suppose we are playing 6 max NLHE with $1/$2 blinds (this means that we are
playing Texas Hold 'em with unlimited bet sizes at a table with 6 seats, and
that the small and big blinds are $1 and $2 respectively). You look at your hole
cards and you see say Q♥7♥ (Queen of hearts, seven of hearts). Question: what do
you do?

This depends on a bunch of factors:
* Your stack size
* Your opponents' stack size
* Your opponents' play style 
* How have people bet already? Are there limpers? Was there a bet/3-bet?
* Who is yet to act?
* What is your _position_?

Preflop action is suuuper complicated, but there is one scenario that is
relatively (kinda) straight forward to handle with a basic strategy (as with all
things, you'll need to update this specific strategy depending on a whole bunch
of variables, but for now suppose everybody is a balanced player and that have
decent sized stacks.) This scenario is "everybody folds to you and you have the
option of _opening_, or being the first to bet, or _folding_". You also
_technically_ have the option of _limping_ (calling the big blind), but you
shouldn't ever do this (again, not a hard and fast rule, but when in doubt,
don't limp).

### Preflop Position
So the question is, given that I'm playing against a bunch of players with
roughly equal stacks who all have a balanced style of play, what hands should I
open? This depends on your _position_, or where you are in the betting order.
From the left of the dealer (called the Button) the positions are:
1. Small Blind (SB)
2. Big BLind (BB)
3. Low Jack (LJ) ---  this is also sometimes called Early Postion (EP)
4. High Jack (HJ) ---  this is also sometimes called Late Postion (LP)
5. Cut Off (CO)
6. Button (BTN) --- aka the dealer

This enumeration tracks _post-flop position_: this means that after the flop,
the Small blind (SB) will be the first to act, followed by the Big Blind (BB),
etc. However, preflop, action opens on the Low Jack (LJ).

Why does position matter when you're opening? Well, it matters because the later
position you are in, the more information you have about your opponents'
actions. If I'm in the Low Jack (LJ) and I see Q♥7♥, my guess is that this is
probably not a very good hand: there are 5 players who haven't acted yet. If I
were to open this hand I'd be putting money on the fact that this will play
better than the next five hands to open. Conversely, if I'm in the button and
action folds to me, I might decide that this is a perfectly fine hand to open
because I only have to beat two other hands.

What's more


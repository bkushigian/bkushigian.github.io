---
layout: post
title: "poker diaries, volume 2: solvers"
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

I'm taking a couple days away from the tables, and I'm using this time to study
and refine my game. There are many ways to study, including hand reviews,
thinking about high level strategies (e.g., sizings, lines, player pool
tendencies, etc), and studying solver outputs. All of these are important and
allow you to look at the game from a different lense. And while these are all
"different", they are ultimately connected.

This post is about solvers. I want to do two things: (1) describe what solvers
are, and (2) describe how I want to use them (and what some of the current
limitations are). This is a broad topic and I'll have to leave a lot out, but I'll
cover more in the future.

## equilibrium

Roughly, an equilibrium is a balance of forces. In poker we need to balance many
considerations to have a robust strategy, and when we become unbalanced our
strategy becomes vulnerable to exploits.

For example, suppose I only bet when I have a very strong hand (_the nuts_ in
poker parlance). Then my opponents will realize that when I bet I am very
strong, and they can play perfectly against me, never bluffing when I bet, and
only calling with high equity hands that can beat my strong hands. Thus, I want
to balance my strong hands with some bluffs.

But what about my medium strength hands? If I'm betting all of my strong hands
and some of my weak hands, then when I check I have only medium strength hands.
My opponent can now exploit this by playing aggressively against my checks,
bluffing me aggressively. To balance this I want to _slowplay_ some of my
strongest hands so that I have _traps_ in my checking range (in poker parlance,
a range is simply a distribution of hands I can have at a giving point).

So how many bluffs should I have? How much value should I trap? What should I do
when they bet into my checking range? These are all further questions of
balance.  The exact details are complicated, and it's not clear how I should
answer them. It of course depends on my opponent. If they bluff too much I
should trap more. If they never bluff I should play my hands 'naturally' (bet
strong and weak, and check medium).

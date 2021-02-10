---
layout: post
title:  "Donking and CBetting | Lojack versus Big Blind | MMM Rainbow/Paired/No Gap"
date:   2021-02-09 13:28:50 -0400
comments: true
categories: poker, cbetting, donking
group: poker
---

In my [previous post]({% post_url 2021-02-09-lj-cbetting-887r %}) I looked at
the scenario where LJ opens for 2.5bb, BB calls, and flop comes 887 rainbow. I
looked at PokerSnowie output, tried to interpret it, and formed a hypothesis
based on this interpretation:

_**Hypothesis 2.0:** On MMM paired flop (i.e., three medium cards from 6-9 with
a pair) the BB has the nuts advantage and will donk bet on this flop with a small
percentage of nutty made hands like 88, 77, and some weaker made hands, as well
as around 1.5x-2x as many bluffs that (a) have good blocking potential, (b)
benefit from folds, and (c) have a potential to improve._

In this post I intend to test this hypothesis for , and to investigate the followup
questions I asked in my last post.

# Preflop Ranges
Here are the preflop charts from PS (these are approximate, and doesn't
differentiate between some mixed 3betting in the BB):

* **LJ: raise 15% of hands:** 66+, A2s+, K9s+, QTs+, JTs, ATo+, KJo+

  ![LJ Open]( {{ "/poker/resources/img/6max-lj-open.png" }} )

* **BB: call with 18% of hands:** 22-TT, A2s-A9s, K4s, K6s+, Q8s+, J8s+, T8s+,
  97s+, 86s+, 75s+, 64s+, 53s+, ATo-AQo, KJo+, QJo 
  
  ![BB Call]( {{"/poker/resources/img/6max-bb-call-v-lj-open.png" }} )


# MMM / Paired / Rainbow / No Gaps
There are only two flops in this category that we haven't checked yet:
1. 998r
2. 776r

I think that BB will donk slightly more in 776 and slightly less on 998, and
that LJ will cbet slightly more on 998 and slightly less on 776.
## 998r
PokerCruncher gives LJ a 55.76% equity over 44.24. This is almost identical to the 887 flop.

### Prediction for Big Blind Donking Range on 998r

I think that BB will donk with about 5% of hands, including:
- 99 (mixed, maybe about 20%)
- T8s-Q8s (infrequently)
- QJs, QTs, KTs w/ BDFD (trying to get fold equity, possibility to improve,
  block some pocket pairs)
  
### Prediction for Lojack CBetting range on 998r
This flop connects a bit more with the LJ, but not much more. BB has the nuts
advantage, and this flop only has about .25% more equity than the last for LJ.

I predict that LJ will cbet 1/2 pot for around 23% of hands:
+ 88
+ 99 
+ A8s 
+ A9s, K9s
+ JTs
+ QTs+
+ Some Kxs


### PokerSnowie: BB Donking Range on 998r
PS only donks 2.8% of the time, less than 1/2 the time as on 887. The only
explanation I can think of is that this connects better with LJ's range. But it
doesn't, really. There are two more possibilities at trips (K9s + A9s versus
A8s), and there is now four OESDs (JTs).

![BB Donk]( {{ "/poker/resources/img/998r-bb-donk.png" }} )

The QJs and QTs combos are still represented as the bluffs, but Q9 is checked to
keep the value range strong. Instead, JTs is added in as a semibluff (it's an
OESD). 99 and various 8x are amost never bet. 9x is always checked.


### PokerSnowie: LJ CBetting Range on 998r

PS actually prefers betting 1/4 pot with around 26.5% of hands:

![LJ Continue with 1/4 pot]( {{ "/poker/resources/img/998r-lj-cbet-quarter-pot.png" }} )
{% include label.html

   title="Lojack's CBetting range at 1/4 pot on 998r" 

   content="The lojack continues with about 26.5% of hands. Hands like QTs+ have
   the same qualities as on 887r now connect a bit better: since 89TJQ makes a
   straight, these are now gutshots, as well as having blocking potential and
   benefiting from folds."
            
%}

![PokerCruncher Range Breakdown]( {{ "/poker/resources/img/998r-lj-breakdown.png" }} )
{% include label.html

   title="Lojack's hand equity breakdown on the flop" 

   content="The lojack continues with about 26.5% of hands. Hands like QTs+ have
   the same qualities as on 887r now connect a bit better: since 89TJQ makes a
   straight, these are now gutshots, as well as having blocking potential and
   benefiting from folds."
            
%}

The betting range is more polar than linear: there are hands in the 40%-43%
range, and hands in the 53%+ range. This isn't quite polar because there are a
lot of 50% equity hands that aren't "the nuts", but it's not quite linear either
because there are some low-equity hands (granted, these have some good implied
odds and might be able to play for stacks), a hole, and then some
decent-to-premium hands. I think it's best to think of this as a semi-polarized
range...

The checking range is relatively condensed. It does have some high-equity hands
like AA, KK with 80-80% equity, but these aren't really nutted: you don't wanna
play for stacks with these hands.

#### Using 1/2 pot bet
This is awfully close to the previous scenario (887r), and I think that the PS
strategy for a 1/2 pot bet should be similar to what it was in my previous post.

Here are the ranges, both from 887 and from 998:

![PokerSnowie Range LJ Cbet for 998r]( {{"/poker/resources/img/998r-lj-cbet-half-pot.png" }} )
{% include label.html

   title="Lojack's 1/2-pot cbetting range on 998r" 

   content=""
%}
![PokerSnowie Range LJ Cbet for 887r]( {{"/poker/resources/img/887r-lj-cbet.png" }} )
{% include label.html

   title="Lojack's 1/2-pot cbetting range on 887r" 

   content=""
%}

These ranges look very similar, with the following changes:

1. 77 and 88 have become 88 and 99
2. KQs and KQo are now bet at about 100% frequency...these have the possibility
   of runner-runnering to the nut straight, so maybe that explains it?
3. A7s and A8s have become A8s (~ around 30% frequency) and A9s

#### Analysis
Using a 1/2 pot bet sizing, a similar betting strategy is used.

What's more, these ranges are a lot more polarized than the 1/4 pot bet. For the
998r flop, 88 and 99 each have about 100% equity, and A9s/K9s both have 90%+
equity. A8s is bet about 30% of the time and has 73% equity; all other hands
that are bet have between 37% equity and 42% equity.

That leaves about 7 value combos and 44 weak hand combos (< 50%). However, many
of these weaker combos can improve in different ways to various over cards and
all have the potential to make straights...these won't be nutted, but will have
serious equity on later streets if they connect.

## 776r
PokerCruncher gives LJ a 56.64% equity over 43.36 for the BB. This is higher
than for 887 or 998, which surprised me.
### Prediction for Big Blind Donking Range on 776r
I think that BB will donk w/ 1/2 pot for about 6% of hands, including:
+ 77 (mixed, about 20%)
+ 66 (mixed, about 20%)
+ J8s+ (mixed, about 75%) w/ BDFDs
+ 87s (mixed, around 15-20%)
+ A6s (around 10%)
+ 64s+ (around 25%)

I am deriving this by shifting the range from 887 diagonally down one square

### Prediction for Lojack CBetting Range on 776r
I predict that LJ should cbet a polarized range about 1/2 pot with about 23% of
hands, including:
+ 66
+ 77
+ A6s
+ A7s
+ JTs
+ K9s
+ QTs

The problem is, the flop really doesn't connect with LJ's range at all, so
trying to find good bluffs that can improve (like QTs on 887) is hard. For this
reason, it might make more sense to expect to see a higher frequency, lower
size, less polarized betting strategy.

### PokerSnowie: Big Blind Donking Range on 776r
PokerSnowie recommends donking 1/2 pot with the following range, comprising
11.12% of hands:
![BB Donk]( {{ "/poker/resources/img/776r-bb-donk.png" }} )
{% include label.html

   title="BB's flop strategy" 

   content=""
            
%}

![PokerCruncher BB Range Breakdown]( {{ "/poker/resources/img/776r-bb-breakdown.png" }} )
{% include label.html

   title="BB's hand equity breakdown on the 776r flop" 

   content=""
            
%}

This is surprising...LJ's donking range is almost entirely made up of trash,
including Jxs and Qxs.

BB donks with a very polarized range.

### PokerSnowie: Lojack Continuing Range on 776r

PS prefers betting 1/4 pot with linear range with about 31.1% of hands:

![LJ Continue with 1/4 pot]( {{ "/poker/resources/img/776r-lj-cbet-quarter-pot.png" }} )
{% include label.html

   title="Lojack's CBetting range at 1/4 pot on 776r" 

   content=""
            
%}

![PokerCruncher Range Breakdown]( {{ "/poker/resources/img/776r-lj-breakdown.png" }} )
{% include label.html

   title="Lojack's hand equity breakdown on the flop" 

   content=""
            
%}


Using the 1/2 pot sizing PS likes to bet a polarized range about 14% of the time:
![LJ Continue with 1/4 pot]( {{ "/poker/resources/img/776r-lj-cbet-half-pot.png" }} )
{% include label.html

   title="Lojack's CBetting range at 1/2 pot on 776r" 

   content=""
            
%}

These are by and large the hands I was expecting, but a lower frequency. I think
this is well explained by the fact that it's hard to find hands that improve on
later streets. This board, while not static, doesn't let the LJ to improve to
the nuts on later streets.

---
layout: post
title:  "On Confidence Intervals and Probabilities"
date:   2022-02-02 23:28:50 -0400
comments: true
categories: statistics probability "confidence intervals"
group: misc
published: true
---

# The problem

My professor made a statement today which I really hated. We were chatting about
confidence intervals. Suppose we have some normally distributed population 
`p ~ N(10, 1)` with a mean of 10 and a standard deviation of 1. Let `X` be a
size-100 random sample from `p`. Suppose we compute the 95% confidence interval
`I = (u, v)` for the mean of `p`. The question is: what is the probability that
`mean(p)` is inside `I`?

Let's look at an example:

```r
library(BSDA)    # For Z test

set.seed(8)
p <- data.frame(val=rnorm(1000000, 10, 1))
mean(p$val)
sd(p$val)
N <- 100    # Sample size
X <- sample(p$val, N, replace=F)
z.test(X, sigma.x=1, mu=10, conf.level=0.90)    # 90% CI, I1
z.test(X, sigma.x=1, mu=10, conf.level=0.95)    # 95% CI, I2
```

This outputs the following (truncated) text describing two z-Tests, one with a 90% confidence interval and one with a 95% confidence interval.

```text
[1] Population mean: 9.99916568117066
[1] Population std:  9.99916568117066

        One-sample z-Test

data:  X
z = 1.8306, p-value = 0.06716
alternative hypothesis: true mean is not equal to 10
90 percent confidence interval:
 10.01857 10.34754
sample estimates:
mean of x 
 10.18306 


        One-sample z-Test

data:  X
z = 1.8306, p-value = 0.06716
alternative hypothesis: true mean is not equal to 10
95 percent confidence interval:
  9.987062 10.379055
sample estimates:
mean of x 
 10.18306 
```

For simplicity, here is the population mean and the confidence intervals:

```text
[1] Population mean: 9.99916568117066

90 percent confidence interval:
 10.01857 10.34754

95 percent confidence interval:
  9.987062 10.379055
```

So Prof asks "What's the probability that the population mean is in the 90%
confidence interval?" and everyone says "90%". Prof is all "lulz nah it's 0",
and we're like "well yeah, obviously, but you know what we mean.." and that's
when he drops one on us: he says that a 90% confidence interval doesn't have a
90% chance of containing the population mean. It has either a 0% chance or 100%
chance.



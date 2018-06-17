---
layout: post
title: "Fundamental Group"
date:   2018-06-17 13:50:00 -0400
future: false
published: true
categories: topology fun-with-cats
group: writeup
---

## Topology and Category Theory
Category theory has its roots in topology so I thought I'd give a writeup with
some background on algebraic topology.

Topology is concerned with properties of spaces that are preserved under
**continuous functions**.  What is a continuous function, you ask? That's a
complicated question and we would have to build up a bit of machinery. For now
let's use the traditional $$\epsilon\delta$$ definition from calculus (or better
yet, the intuitive definition that *"nearby points get mapped to nearby
points"*).

It turns out that topological spaces and continuous functions are a category,
which we label **Top**. To show this category is well defined we need to verify
that continuous functions compose to form continuous functions, and that the
identify function is continuous which we leave to a topology course. [Munkres'
Topoology][munkres] is an excellent introduction to the subject if you are interested.

## Homotopy
We can think of a continuous function $$f : X \to Y$$ as "drawing a picture of
$$X$$ in $$Y$$". The picture isn't always perfect and is sometimes pretty
distorted, but there is always some information preserved.

For example, if I start off with the circle, which we denote by $$S^1$$, then
any continuous function $$f : S^1 \to \mathbb{R^2}$$ cannot be split up into
pieces (say into two disjoint circles). However we *can* do things like squish
the image down to a point with a constant map $$f_c : x \mapsto c$$ which is
*always* continuous, no matter the domain, codomain, or constant that is being
mapped to.

So we can draw (potentially distorted) pictures of our domain with different
functions $$f, g: X \to Y$$, and now we would like to related them. To do this
we want to "smoothly deform" one image to another. Below we are deforming one
image of the unit interval $$I = [0, 1]$$ into another.

![Homotopy-image courtesy of Scientific American](https://upload.wikimedia.org/wikipedia/commons/4/4a/Homotopy.gif)

Formally this is described by defining a **homotopy** from $$f_0$$ to $$f_1$$ to be
a family of maps $$F(t, x), t \in I$$ such that $$F(0, x) = f_0(x)$$ and $$F(1,
x) = f_1(x)$$, and such that $$F$$ is continuous in both variables. The $$t$$
parameter can be thought of as a *time* parameter for a video. If there exists a
homotopy $$F$$ as above we say that $$f$$ is homotopic to $$g$$ and write $$f
\simeq g$$.

A classic homotopy known to all practitioners of 'pop-mathematics' is the
homotopy between a doughnut and a coffee mug:

![The Policeman Homotopy](https://upload.wikimedia.org/wikipedia/commons/2/26/Mug_and_Torus_morph.gif)

### Weak $$\infty$$-Groupoids
A path from endpoints $$x$$ to $$y$$ may be thought of as a homotopy from
constant functions $$f_x$$ and $$f_y$$. Such a homotopy is sometimes called a
*one-dimensional homotopy*. Then a homotopy of paths may be thought of as a
homotopy of homotopies. These are called *two-dimensional homotopies*. This can
be continued *ad infinitum* and the resulting structure, called a weak
$$\infty$$-groupoid, comes up in *homotopy type theory*.


## Homotopy Classes and the $$\mathbf{Htpy}$$ Category
The "is-homotopic-to" relation $$\simeq$$ on functions $$X \to Y$$ is an
[**equivalence relation**](https://en.wikipedia.org/wiki/Equivalence_relation)
and we can talk about the **homotopy classes** of such functions. A homotopy
class is a cell of the partition induced by the equivalence relation $$\simeq$$.

We define the category $$\mathbf{Htpy}$$ to have topological spaces as objects
and homotopy classes as arrows. There is a functor $$H:\mathbf{Top} \to
\mathbf{Htpy}$$ sending objects to themselves and sending arrows to their
homotopy homotopy class.

### Example: The Punctured Plane
Consider the punctured plane $$\mathbb{R}^\ast$$, where the point $$p$$ has been
removed.

![Punctured plane homotopy](https://upload.wikimedia.org/wikipedia/commons/thumb/8/8f/Winding_Number_Around_Point.svg/380px-Winding_Number_Around_Point.svg.png)

We have a curve $$C$$ that is the image of a function $$f$$ mapping the circle
$$S^1 \to \mathbb{R}^\ast$$. There are plenty of functions that are homotopic to
$$C$$: we can undo the loop on the right, we can bend or straighten a segment,
we can stretch part of the curve off the screen, we can move all points of $$C$$
to half their distance from $$p$$, etc. 

We can do a whole bunch of things as long as we can make a homotopy from $$f$$
to our new function *without passing through* $$p$$.

One thing we can't do is make $$f$$ homotopic to a constant function. This is a
fundamental proof of algebraic topology and involves some machinery but it
should be intuitively clear.

Here's an experiment. Draw a line from $$p$$ to the left. It crossess $$C$$
twice. Each time your line crosses $$C$$ and $$C$$ is moving from the right of
the line to the left of the line, count 1; each time $$C$ crosses in the other
direction, subtract 1. With our current line we count to two.

Now instead of drawing the line to the left, draw it to the right. The first
crossing adds one (right to left). The second crossing subtracts one (left to
right). The third crossing adds one, and the fourth crossing adds one. Summing,
we get two again.

It turns out this is an invariant---no matter which line we draw out from $$p$$
we always will get the same number when counting crossings. This is called the
[winding number](https://en.wikipedia.org/wiki/Winding_number) of $$C$$ and we
will see that this has an algebraic interpretation.

Here's another way of calculating the winding number.

![Winding number](https://upload.wikimedia.org/wikipedia/commons/a/ac/Winding_Number_Animation_Small.gif)

Standing at a point, follow the curve, rotating as you do so. Every time you
rotate counter clockwise, add 1; every time you rotate clockwise, subtract one.
In the above gif the observer witnesses a winding number of 2. If the person is
standing at point $$p$$ as in our first example then this we know that the
curves are homotopic to one another and thus in the same homotopy class.

Here are some curves and their winding numbers. None of these are homotopic to
each other since such a homotopy would require passing through the center
point.

![Curves and their Winding Numbers]( {{ "/assets/image/winding-numbers.png" }} )

## Pointed Sets, $$\mathbf{Top_\ast}$$, and $$\mathbf{Htpy_\ast}$$
We are interested in homotopies that fix a base point. Such a homotopy is a
special case of a [relative homotopy][relative-homotopy]; the first gif shows a
homotopy that is relative to its endpoints---i.e., the endpoints are fixed. When
we introduce the fundamental group we will be working with maps of the circle
$$S^1$$ into various spaces that fix a base point. 

These are often denoted as homotopies between **pointed sets**. A pointed set
$$(X,x)$$ is a set $$X$$ and a point $$x \in X$$. We define the category
$$\mathbf{Set_\ast}$$ to be the category with pointed sets as objects and
functions $$f: (X,x) \to (Y,y)$$ such that $$f(x) = y$$ as arrows. By adding our
topological structure (i.e., a notion of continuity) onto these sets we get the
**pointed topology** category $$\mathbf{Top_\ast}$$ with objects the pointed
sets and arrows the point-preserving continuous functions.


[relative-homotopy]:https://en.wikipedia.org/wiki/Homotopy#Relative_homotopy
[munkres]:https://www.amazon.com/Topology-James-R-Munkres/dp/9332549532/ref=sr_1_1?ie=UTF8&qid=1529266872&sr=8-1&keywords=munkres+topology

---
layout: post
title:  "Potential Challenges With Modeling Heaps in EGraphs"
date:   2020-07-30 13:28:50 -0400
comments: true
categories: cornelius research
group: cornelius
---
<style>
td {
    font-size: 90%
}

td code {
    font-size: 100%
}

</style>

## Handling Heaps in EGraphs
Cornelius is trying to handle arbitrary Java code, and this means dealing with
the heap. Due to the nature of the heap (unbounded, dynamically allocated, etc),
Cornelius won't be able to reason about it fully (...Cornelius works
statically). So the question is, how much _can_ we reason about?

To begin with I'm not considering loops. This makes the problem a lot easier.
I'm also not considering method calls yet, but I have some ideas for how to
handle those in a very rough way.

While my first stab at handling heapy code will elide loops and method calls, I
still need to think about the challenges these language features introduce. That
is the point of this posting.

## Peggy
Peggy uses _sigma nodes_ as a heap summary, which aren't really explained very
well. Something about lists of reads and writes, where reads commute? I think
this seems like the right direction.

## Cornelius and Heap Models
There are two primary approaches to modeling the heap: _store-based_ and
_storeless_. Store-based heap models use abstract symbolic addresses to
represent concrete addresses. Storeless heap models use access paths. An access
path is a variable followed by zero or more field dereferences, such as
`list.size` or `x.y.z`, or `foo`. The first item in the access path is a
variable that points to the heap (a reference) while each other item is a field
name, also pointing to the heap.

Cornelius is a rewrite-based approach and reasons about the syntax of a program.
Therefore I think it's logical to use a storeless heap model. Cornelius will be
receiving access paths explicitly in PEG inputs, so I think I should use this
form directly.

Here is my first stab at defining a heap:

> A heap is a list of _writes_.

A _write_ takes three arguments: an _access path_, a _value_, and a _heap_. Thus
if I have a heap which I know nothing about, represented as `nil`, and I want to
process field assignment `x.y = 3;`, I will represent the heap after the field
assignment as:
```scheme
(wr "x.y" 3 nil)
```
The field access path is just a string right now, but later I will need to
update it to be a list.

An important point: in this formulation, I'm not tracking allocations explicitly
on the heap.  Consider the following (not-actually-legal-)Java program:

{% highlight java %}
int example() {
    Foo f = this.foo;
    f.x = 3;
    new Y;        // Create a new Y without a constructor call
    f.y = new Y;
}
{% endhighlight %}

The following table lists the state after each line is executed
<table style="width: 100%">
  <thead>
    <tr>
      <th style="width: 25%">Line</th>
      <th style="width: 30%">Heap After Executing</th>
      <th style="width: 44%">Comments</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><b>ENTRY</b></td>
      <td><code class="highlighter-rouge">nil</code></td>
      <td>Start off with unknown heap <code class="highlighter-rouge">nil</code></td>
    </tr>
    <tr>
      <td><code class="highlighter-rouge">Foo f = this.foo</code></td>
      <td><code class="highlighter-rouge">nil</code></td>
      <td>This line didn't update the heap---we don't know anything new about
      it's structure. You <i>could</i> argue that we now know that there is a
      something called <code>this.foo</code>, but that doesn't actually matter
      to our analysis.</td>
    </tr>
    <tr>
      <td><code class="highlighter-rouge">f.x = 3</code></td>
      <td><code class="highlighter-rouge">(wr "f.x" 3 nil)</code></td>
      <td>write value <code class="highlighter-rouge">3</code> to access path <code class="highlighter-rouge">"f.x"</code></td>
    </tr>
    <tr>
      <td><code class="highlighter-rouge">new Y</code></td>
      <td><code class="highlighter-rouge">(wr "f.x" 3 nil)</code></td>
      <td>This doesn’t change our heap since we can’t actually access this value through an access path</td>
    </tr>
    <tr>
      <td><code class="highlighter-rouge">f.y = new Y</code></td>
      <td>
      <pre>
(wr "f.y" (new Y) 
   (wr "f.x" 3 nil))</pre></td>
      <td>This time we’ve created a new <code class="highlighter-rouge">Y</code> and wr it to access path <code class="highlighter-rouge">"f.y"</code></td>
    </tr>
  </tbody>
</table>

Creating a new object without invoking a constructor isn't actually legal in
Java (but it _is_ legal in JVM bytecode). In practice, invoking a constructor
might actually update the heap in some way, just as any other method invocation
might update the heap in some way. I'll reason about this later, but the easiest
(and least precise) thing to do is to replace the heap with `nil` every time we
invoke a method.

### Over-identifications
There is a problem with the above. `nil` can mean _different things_ in
_different places_. Consider the following example:
{% highlight java %}
bool nilExample() {
    int x1 = this.x;
    methodThatMessesWithHeap();    // this might change `this.x`
    int x2 = this.x;
    return x1 == x2;
}
{% endhighlight %}

| Line                         | Heap After Executing | Local Vars                                          |
| ----                         | -------------------- | --------                                            |
| **ENTRY**                    | `nil`                | NA                                                  |
| `x1 = this.x`                | `nil`                | `x1 = (rd this.x nil)`                              |
| `methodThatMessesWithHeap()` | `nil`                | `x1 = (rd this.x nil)`                              |
| `x3 = this.x`                | `nil`                | `x1 = (rd this.x nil)`,<br/> `x2 = (rd this.x nil)` |

In practice we don't know if this returns `true` or `false`, but an EGraph will
think that this program alwasy returns `true`!

Really `nil` just means "I don't know anything about this heap". However, since
EGraphs will identify two different `nil`s through deduplication, for now I'll
assume that each `nil` is parameterized (i.e., `(nil 1)`, `(nil 2)`, etc). Once
we introduce loops and method calls this will become a problem due to an
arbitrary number of unknown heaps (i.e., every time a method is invoked). This
will be a theme in the interplay between EGraphs and heaps: _abstract states
lead to unsound identifications_. This is the fact that I'll need to overcome.

## Cornelius and Heap Summaries
I won't need to summarize heaps until I include loops or recursion. This is
because all access paths will be known statically. However at some point I'll
need to handle access paths of arbitrary length. For instance, consider the
following Java code summing the elements of a linked list.

{% highlight java %}
int sum(LLNode<Integer> n) {
  int total = 0;
  while (n != null) {
    total += n.value;
    n = n.next;
  }
  return total;
}
{% endhighlight %}

The following table gives the access path of `n` for each iteration of the loop
condition:

| Iteration | Access Path                             | _k_-limiting, _k_ = 2       |
| --------- | -----------                             |                             |
| 0         | `(var n)`                               | `(var n)`                   |
| 1         | `(var n).next`                          | `(var n).next`              |
| 2         | `(var n).next.next`                     | `(var n).next.next`         |
| 3         | `(var n).next.next.next`                | `(var n).next.next(.next)*` |
| 4         | `(var n).next.next.next.next`           | `(var n).next.next(.next)*` |
| ...       | ...                                     | ...                         |

It's plain that once we introduce loops we can get unbounded access paths. To
address this we should use some sort of _heap summary_. A heap summary gives a
bounded representation of a potentially unbounded heap model. There are a number
of ways to do this, but they all involve the same technique: represent multiple
_abstract heap locations_ from the model by a single _summary node._ 

### _k_-limiting
One such summarization technique is called _k_-limiting. In _k_-limiting a
positive integer _k_ is chosen. All access paths with at most _k_ field
dereferences are treated normally. However, if more than _k_ dereferences take
place, these are summarized with a Kleene closure `*`.


### Summaries and EGraphs
There is a pretty severe problem with summaries and EGraphs, and it's a problem
we already encountered with `nil`. Each summary node by definition represents
more than a single abstract heap state but has a single syntactic
representation. As a result, congruence can misfire! Taking the above table,
suppose that we wanted to read the access paths:

| Iteration | Reading Access Path                | Reading w/ _k_-limiting, _k_ = 2 |
| --------- | -----------                        | ---------------------------      |
| 0         | `(rd (var n))`                     | `(rd (var n))`                   |
| 1         | `(rd (var n).next)`                | `(rd (var n).next)`              |
| 2         | `(rd (var n).next.next)`           | `(rd (var n).next.next)`         |
| 3         | `(rd (var n).next.next.next)`      | `(rd (var n).next.next(.next)*)` |
| 4         | `(rd (var n).next.next.next.next)` | `(rd (var n).next.next(.next)*)` |
| ...       | ...                                | ...                              |

The _main_ problem with heap summaries as they relate to EGraphs is that they
take an infinite set and turn it into a finite set (that's the point, after
all). This means that if I'm not careful I will lose soundness.

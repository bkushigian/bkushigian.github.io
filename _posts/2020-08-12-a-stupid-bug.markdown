---
layout: post
title:  "A Stupid Bug"
date:   2020-08-15 12:28:50 -0800
comments: true
categories: random
tags: [debugging, miscellaneous, cornelius]
group: cornelius
---

## Don't Read This
This post is of no interest to anybody. If you know Java, you'll already know
this behavior and won't gain anything from reading this. If you are learning
Java, there are surely better resources than this one.

I'm basically just writing this so that I remember not to be this particular
strain of stupid in the future. I don't mind being stupid, just so long as I
always remember to be stupid in new ways.

## The Bug
### The Behavior
I was getting garbage out when I ran Cornelius on deserialized inputs. All sorts
of random mutants that shouldn't possibly be rewritten to each other were being
identified. I did the obvious thing and manually minimized rewrite rules, trying
to identify the buggy rules. I'd remove a bunch of rules, run equality
saturation, and then look up the indices of each mutant to determine which
equivalence class it was in. This would cause a panic: `index out of bounds: the
len is 6699 but the index is 6761`.

### The Cause
Some quick back story: I was getting exponential blowup during serialization in
Cornelius (the serializer transforms Java ASTs to PEGs in an XML file). The
reason was that there was a bunch of expression duplication. To fix this, I
needed to deduplicate these via indirection. So, instead of outputting the XML
node

```html
<pegs>
<peg>
(+ (+ a b) (+ a b))
</peg>
</pegs>
```

I'd output the XML node
```html
<pegs>
<peg>
3
</peg>
...
</pegs>
<id_table>
<entry id="0" peg="a">
<entry id="1" peg="b">
<entry id="2" peg="(+ 0 1)">
<entry id="3" peg="(+ 2 2)">
</id_table>
```

Each _child_ of a PEG is now a pointer into the `id_table`. This lets me reuse
nodes, preventing exponential blowup. From the Java side, I'd track nodes in an
array:

```java
public class PegNode {
    final String op;
    final Integer[] children;
    PegNode(String op, Integer...children) {
        this.op = op;
        this.children = children;
    }
}
```

I track nodes in a lookup table:
```java
private static Map<String, Map<Integer[], PegNode>> symbolLookup = new HashMap<>();
```

This maps `String -> (Integer[] -> PegNode)`. I give an op and get back a second
map. Then I give this second map an array of `Integer`s, and get back the
`PegNode` that has that op and list of `Integer`s as children, if it exists.

You're a smart person. Right now you're probably thinking "Ben, this is so very
clearly broken. I see the problem _immediately_ and it's very, very bad. You
should be ashamed of yourself."

Yeah, I know, I know, and what's even worse is that I noticed the problem when I
wrote the code; I remember thinking "I don't recall if hashing arrays works how
I want them to, and I should definitely come back to this...this will just be
placeholder code." Then, of course, I went on hacking and totally forgot about it.

The issue, of course, is that I'm hashing an array. This means that the
following program throws an exception.

```java
import java.util.HashSet;

public class ArrayHashing {
    public static void main(String[] args) {
        Integer[] arr1 = {1,2,3};
        Integer[] arr2 = {1,2,3};

        HashSet<Integer[]> set = new HashSet<>();
        set.add(arr1);
        set.add(arr2);

        if (set.size() != 1) {
            throw new BensAnIdiotException("array.hashCode is inherited from Object");
        }
    }

    public static class BensAnIdiotException extends RuntimeException {
        public BensAnIdiotException(String msg) {
            super("Ben's an idiot: " + msg);
        }
    }
}
```

Compiling and running gives the following

```
$ javac ArrayHashing.java
$ java ArrayHashing      
Exception in thread "main" ArrayHashing$BensAnIdiotException: Ben's an idiot: array.hashCode is inherited from Object
        at ArrayHashing.main(ArrayHashing.java:13)

```

Why is this happening? Because calling `array.hashCode()` invokes
`Object.hashCode()` which doesn't take the arrays elements into account. Thus,
two different arrays with the same elements are hashed differently.

## The Solution
Use a `List<Integer>`.

## Epilogue
So my solution isn't a _great_ solution, because I'm hashing a mutable thing. In
my case I'm never mutating the `List`s of node children, but this could cause
another baffling bug down the line...I'm sure I'll be writing a new post about
this some day soon :)

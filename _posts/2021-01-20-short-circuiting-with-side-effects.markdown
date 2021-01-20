---
layout: post
title:  "Short Circuiting with Side Effects"
date:   2021-01-20 12:00:00 -0800
comments: true
published: false
categories: cornelius
tags: [cornelius, rewrites]
group: cornelius
---

In this post I look at how basic rewrite rules fail when dealing with side
effects, as well as exploring possible solutions.

## Short-Circuiting Operators

Consider the following method `shortCircuit()`.

```java
boolean shortCircuit(boolean cond) {
    return cond || getBoolWithSideEffects();
}
```
{% include label.html 
   title="Listing 1"
   content="A short circuiting operator that may or may not trigger side effects"
%}

Currently Cornelius serializes this as if the right hand side is always
executed. Instead I need to handle the short circuiting.

For instance, this would serialize to something like

```scheme
(return-node 
  (|| (var cond) (invoke->peg (invoke getBoolWithSideEffects (var this) actuals heap)))
  (invoke->heap 
      (invoke getBoolWithSideEffects (var this) actuals heap)))
```

{% include label.html 
   title="Listing 2"
   content="Buggy serialized output"
%}

Instead this should serialize to something like this:

```scheme
(return-node 
  (|| (var cond) (invoke->peg (invoke getBoolWithSideEffects (var this) actuals heap)))
  (phi (var cond)
    (heap 0 unit)
    (invoke->heap 
      (invoke getBoolWithSideEffects (var this) actuals (heap 0 unit)))))
```

## Commutativity

How does commutativity work here? It should actually just play out the same as
in [my previous post]({% 2021-01-14-commutativity-with-side-effects %}).


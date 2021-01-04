---
layout: post
title:  "Adding Method Invocations to Cornelius"
date:   2021-01-01 12:00:00 -0800
comments: true
published: false
categories: random
tags: [debugging, miscellaneous, cornelius]
group: cornelius
---

## Background
Tate et. al's [PEGGY paper][tate-et-al] uses the following image to describe
method invocation:

![Run Configuration](/assets/img/tate-fig-method-invocation.png)

There is an `invoke` node that takes four arguments:
1. The heap summary node
2. The receiver object
3. The method identifier
4. The list of actual parameters

They also define two operators: *rho\_sigma* and *rho\_v*, that respectively
project the heap and return value from the invocation.

I'm adding this basic infrastructure to Cornelius:

```rust
define_language! {
  pub enum Peg {

    // ...
    
    /***                       Method Stuff                      ***/

    // (invoke heap receiver method actuals)
    // A normal (i.e., non-static), method invocation
    "invoke" = Invoke([Id; 4]),
    // A normal (i.e., non-static), method invocation
    "invoke-static" = InvokeStatic([Id; 4]),
    // Project the heap from a method invocation
    "proj-heap" = ProjHeap(Id),
    // Project the return value from a method invocation
    "proj-val" = ProjVal(Id),
    // Method name that is being invoked
    "method" = Method(egg::Symbol),
    
  }
}
```

Currently there are no rewrite rules associated with methods, but there is
definitely some low-hanging fruit to be snagged. For instance, if I have access
to the invoked method during serialization I can make certain notes about it,
such as purity, or even inline it directly. This will perhaps aid with
discovering equivalences.

## Serialization

[tate-et-al]: https://homes.cs.washington.edu/~ztatlock/pubs/eqsat-tate-lmcs11.pdf

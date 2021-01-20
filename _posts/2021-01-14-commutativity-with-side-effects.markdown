---
layout: post
title:  "Commutativity with Side Effects"
date:   2021-01-14 12:00:00 -0800
comments: true
published: false
categories: cornelius
tags: [cornelius, rewrites]
group: cornelius
---

This post is just to clarify why I don't have to worry about side effects and
commutative rewrites.

## Commutativity
Addition is commutative, obviously, so when I started working Cornelius a rule
like

```rust
    rw!("commute-add";   "(+ ?a ?b)"         => "(+ ?b ?a)"),
```

was obvious and natural to write. But consider the following program:

```java
class Counter {
    int count = 0;
    int next() {
        return ++count;
    }
    
    int get() {
        return count;
    }
    
    int add() {
        Counter c = new Counter();
        return c.get() + c.next();
    }
}
```

The `+` operator cannot commute here. If `c.count` is `0`, `c.get() + c.next()`
evaluates to `1`, while `c.next() + c.get()` evaluates to `2`.

My first question is

> Does the commutative rewrite potentially change the semantics of a program?

### No
This turns out _not_ to be a problem. Why is that? To see this, let's serialize
the above `add` method. Don't look too closely at the output (it's
huge!)...instead, look at the comments. Look at how big the invocation for the
RHS is versus the LHS:

```scheme

(return-node
  (+
    ;; START LHS
    (invoke->peg 
      (invoke
        (heap 
          (invoke->heap-state (new "Counter" actuals (heap 0 unit)))
          (invoke->exception-status (new "Counter" actuals (heap 0 unit))))
        (invoke->peg (new "Counter" actuals (heap 0 unit))) get actuals))
    ;; END LHS
    ;; RHS
    (invoke->peg 
      (invoke 
        ;; HEAP WE ARE INVOKING IN
        (heap 
          (invoke->heap-state 
            (invoke
              (heap 
                (invoke->heap-state (new "Counter" actuals (heap 0 unit)))
                (invoke->exception-status (new "Counter" actuals (heap 0 unit))))
              (invoke->peg (new "Counter" actuals (heap 0 unit)))
              get
              actuals))
          (invoke->exception-status
            (invoke
              (heap 
                (invoke->heap-state (new "Counter" actuals (heap 0 unit)))
                (invoke->exception-status (new "Counter" actuals (heap 0 unit))))
              (invoke->peg (new "Counter" actuals (heap 0 unit)))
              get
              actuals)))
        ;; RECEIVER OBJECT FOR INVOCATION
        (invoke->peg (new "Counter" actuals (heap 0 unit)))
        ;; METHOD NAME
        next
        ;; ACTUAL PARAMS (empty)
        actuals))
    ;; END RHS
  )    ;;; CLOSE (+ ... ...) NODE
  ;; RETURNED HEAP
  (heap ... ...) 
```

If the LHS and RHS were commuted we actually wouldn't be commuting the order in
which things happen.


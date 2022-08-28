---
layout: post
title:  "Grokking Cyclic Data Structures in Rust"
date:   2020-08-20 12:28:50 -0800
comments: true
categories: learning-rust
tags: [rust, learning rust]
published: false
group: cornelius
---

## The idea
I'm trying to learn Rust, so I'm gonna start taking case studies in Rust and
disecting them. I'll be posting summaries here so that I can remember what I did
later.

The case-study-du-jour is to figure out _cyclic data structures_. These are
structures like:

```rust
struct Node<T> {
    prev: Box<Option<Node<T>>>,
    data: T,
    next: Box<Option<Node<T>>>,
}
```

This represents a node in a tree, and by traversing the cycle
`node.next.unwrap().prev.unwrap().next.unwrap()...` we traverse a cycle
(assuming that `node` has a `next` field). Rust's _borrow checker_ doesn't like
this.



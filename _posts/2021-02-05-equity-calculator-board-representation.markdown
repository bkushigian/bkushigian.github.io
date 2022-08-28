# Equity Calculator: Efficient Representations of Boards

Representing a subset of the 52 card deck can be done easily enough by
designating 52 bits of a 64-bit int as flags. There are two natural ways to do this:
1. **Group ranks together:** the set `AcAhAsAd` would be represented as
   `0x000000000000000F`. This format is called *_Rank Grouped Format (RGF)_*
2. **Group suits together:** the set `AcAhAsAd` would be represented as
   `0x0001000100010001`. This format is called *_Suit Grouped Format (SGF)_*

Both representations has their advantages and disadvantages. For instance, by
grouping ranks together we can easily detect pairs, trips, and quads, and by
grouping suits together we can easily detect flushes, straights, and straight
flushes.

## Rank Grouped Format (RGF)
### Detecting n of a kind
Since ranks are grouped together we can mask the four bits to get just the
rank's grouping, shift the masked value, and then use a lookup table.
```
pub fn n_of_a_kind(u64 cards) -> hand_type {
    let mut best_hand = 0;
    for i in 0..13 {
        let hand = (cards >> i) & 0xf;
        if hand > best_hand {
            let kickers = 
            best_hand = hand;
        }
    }
}
```


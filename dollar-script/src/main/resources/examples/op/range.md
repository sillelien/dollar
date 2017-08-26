Creates a RANGE between the two values specified using the standard Maven range syntax

In pseudocode:
```
(a..b) = {x | a < x < b}
[a..b] = {x | a <= x <= b}
[a..b) = {x | a <= x < b}
(a..b] = {x | a < x <= b}
(a..) = {x | x > a}
[a..) = {x | x >= a}
(..b) = {x | x < b}
(..b] = {x | x <= b}
(..) = all values
```

Please see https://github.com/google/guava/wiki/RangesExplained for more information on the range format used.

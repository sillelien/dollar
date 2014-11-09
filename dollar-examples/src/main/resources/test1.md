#!/usr/bin/env dollar

Testing the embedding of code in markdown
=========================================

```dollar  

testParams := ($2 + " " + $1)

=> $testParams ("Hello", "World") == "World Hello"


!! $testParams ("Hello", "World")

```

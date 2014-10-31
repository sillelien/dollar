#!/usr/bin/env dollar

Introduction
============

Executable Documentation
------------------------

Everything in this documentation is executed as part of the build process, so all the examples are guaranteed to run with the latest master branch of Dollar. 

Yep Dollar can actually run Markdown files, in fact the source file that this page was built from starts with  ->


```
#!/usr/bin/env dollar
```
So it can be executed directly from a Unix command line.

The source for this page (minus that header) is [here](scripting.md)

Getting Started
---------------

Here is an example of what DollarScript looks like:

```dollar  

testParams := ($2 + " " + $1)

=> $testParams ("Hello", "World") == "World Hello"


!! $testParams ("Hello", "World")

```

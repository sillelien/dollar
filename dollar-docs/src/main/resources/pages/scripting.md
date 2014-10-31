#!/usr/bin/env dollar

Introduction
============

Executable Documentation
------------------------

Everything in this documentation is executed as part of the build process, so all the examples are guaranteed to run with the latest master branch of Dollar. 

Yep Dollar can actually run Markdown files, in fact the source file that this page was built from starts with:


```
#!/usr/bin/env dollar
```
So it can be executed directly from a Unix command line.

The source for this page (minus that header) is [here](scripting.md)

Getting Started
---------------

NOTE: At present only Mac OS X is officially supported, however since Dollar is entirely based in Java it's trivial to port to other systems.

First download the Dollar scripting runtime from [ ![Download](https://api.bintray.com/packages/neilellis/dollar/dollar-runtime-osx/images/download.svg) ](https://bintray.com/neilellis/dollar/dollar-runtime-osx/_latestVersion) 
 
Make sure `dollar/bin` is on your PATH.

Run `dollar <filename>` to execute a Dollar script. 
 
Here is an example of what DollarScript looks like

```dollar  

testParams := ($2 + " " + $1)

=> $testParams ("Hello", "World") == "World Hello"

```

Understanding the Basics
========================

DollarScript has it's own peculiarities, mostly these exists to help with it's major function - data/API centric Internet applications. So it's important to understand the basic concepts before getting started.

Reactive Programming
--------------------

DollarScript expressions are by default *lazy*, this is really important to understand otherwise you may get some surprises. This lazy evaluation makes DollarScript a [reactive programming language](http://en.wikipedia.org/wiki/Reactive_programming) by default.


Let's see some of that behaviour in action:

```dollar

variableA := 1
variableB := $variableA
variableA := 2

=> $variableB == 2 
```

In the above example we are declaring (using the declarative operator `:=`) that variableA is current the value 1, we then declare that variableB is the *same as* variableA. So when we change variableA to 2 we also change variableB to 2.

The assertion operator `=>` will throw an assertion error if the value following is either non boolean or not true.


Assignment
----------

Obviously the declarative/reactive behavior is fantastic for templating and creating lambda style expressions, however a lot of the time we want to simply assign a value.

```dollar

variableA = 1
variableB = $variableA
variableA = 2

=> $variableB == 1 
```

So as you can see when we use the `=` assignment operator we assign the *value* of the right hand side to the variable. Watch what happens when we use expressions.


```dollar

variableA = 1
variableB = $variableA
variableC = ($variableA +1 )
variableD := ($variableA + 1)
variableA = 2

=> $variableB == 1 
=> $variableC == 2 
=> $variableD == 3
 
```

So `:=` allows the default behaviour of Dollar, which is to make everything declarative, and `=` is used to nail down a particular value. Later we'll come across the value anchor operator or diamond `<>` which instructs DollarScript to fix a value at the time of declaration. More on that later.





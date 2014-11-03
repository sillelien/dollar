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


Now let's throw in the -> or causes operator :

```dollar
 
a=1
$a -> { >> $a }
a=2
a=3
a=4
 
```

~~~
2
3
4
~~~

That remarkable piece of code will simply output each change made to the variable a, but wait a minute what about ...

```dollar

b=1
a=1
$a + $b + 1 -> { >> "a=" + $a + ", b=" + $b}
a=2
a=3
a=4
b=2 
```

~~~
a=2, b=1 
a=3, b=1 
a=4, b=1 
a=4, b=2 
~~~

Yep, you can write reactive expressions based on collections or arbitrary expressions !! When any component changes the right hand side is re-evaluated (the actual value that changed is passed in as $1).


###Assignment

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

###Blocks

DollarScript supports several block types, the first is the 'line block' a line block lies between `{` and `}` and is separated by either newlines or `;` characters.

```dollar

block := {
    "Hello "
    "World"
}

=> $block == "World"

block2 := {1;2;}

=> $block2 == 2

```
When a line block is evaluated the result is the value of the last entry. For advanced users note that all lines will be evaluated, the value is just ignored. A line block behaves a lot like a function in an imperative language.
 
Next we have the array block, the array block preserves all the values each part is seperated by either a `,` or a newline but is delimited by `[` and `]`.

```dollar

array := [
    "Hello "
    "World"
]

=> $array == ["Hello ","World"]

array2 := [1,2]

=> $array2 == [1,2]

```

Finally we have the appending block, when an appending (or map) block is evaluated the result is the concatenation (using $plus() in the Dollar API) of the parts from top to bottom. The appending block starts and finishes with the `{` `}` braces, however each part is seperated by a `,` not a `;` or *newline*

```dollar

appending := {
    "Hello ",
    "World"
}

=> $appending == "Hello World"

appending2 := { 1, 2}

=> $appending2 == 3

```

Appending blocks can be combined with the pair `:` operator to create maps/JSON like this:


```dollar

appending := {
    first:"Hello ",
    second:"World"
}

>> $appending

=> $appending.second == "World"

```

The stdout operator `>>` is used to send a value to stdout in it's serialized (JSON) format, so the result of the above would be to output `{"first":"Hello ","second":"World"}` a JSON object created using JSON like syntax. This works because of how appending works with pairs, i.e. they are joined together to form a map.

```dollar
 
pair1 := first : "Hello ";
pair2 := second : "World";
  
=> $pair1 + $pair2 == {"first":"Hello ","second":"World"}
 
```

###Reactive Control Flow

DollarScript as previously mentioned is a reactive programming language, that means that changes to one part of your program can automatically affect another. Consider this a 'push' model instead of the usual 'pull' model.

Let's start with the simplest reactive control flow operator, the '->' or 'causes' operator. 

```dollar
 
$a -> { >> $a } //alternatively for clarity '$a causes {>> $a} '

a=1
a=2
a=3
a=4
a=2
 
```

When the code is executed we'll see the values 1,2,3,4,2 printed out, this is because whenever `$a` changes the block { >> $a } is evaluated, resulting in the variable $a being printed to stdout. Imagine how useful that is for debugging changes to a variable!

Next we have the 'when' operator, there is no shorthand for this operator, to help keep you code readable:


```dollar

a=1
 
when $a == 2 { >> $a } 

a=2
a=3
a=4
a=2
 
```

This time we'll just see the number 2 twice, this is because the `when` operator triggers the evaluation of the supplied block ONLY when the supplied expression (`$a == 2`) becomes true. 


###Reactive Operators
 
####Split, Filter and Aggregate

<!--
split - % <filter expression>

Converts a list into a stream of values matching the filter

```
b := $a % (true)
b := $a % ($1 > 3)
```

filter - ^ <filter expression>

```
b := $a ^ ($1 > 100)
```

b will not generate events if a's value is <= 100, also b will be void if queried during that state.

aggregate - & <emit condition>

Aggregate until emit condition is true and then emit the result

$1 == aggregate
$2 == previous value
$3 == current value
$4 == next value

```
    b :=  $a & ($2 == ';')
```
-->

Imperative Control Flow
-----------------------

Parameters &amp; Functions
----------------------

Resources &amp; URIs
----------------

Iterative Operators
-------------------

Numerical Operators
-------------------

Pipe Operators
--------------

Modules & Module Locators
-------------------------

Remaining Operators
-------------------

Concurrency & Threads
---------------------




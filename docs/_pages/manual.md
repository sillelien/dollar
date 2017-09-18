---
layout: single
title:  "Dollar Scripting Language Manual"
permalink: /manual/
---
{% include toc %}

## Introduction

### Executable Documentation

Everything in this documentation is executed as part of the build process, so all the examples are guaranteed to run with the latest master branch of Dollar.

Yep Dollar can actually run Markdown files, in fact the source file that this page was built from starts with:


```
#!/usr/bin/env dollar
```
So it can be executed directly from a Unix command line.

The source for this page (minus that header) is [here](manual.md)

### Getting Started

NOTE: At present only Mac OS X and 64 Bit Ubuntu Linux is officially supported, however since Dollar is entirely based in Java it's trivial to port to other systems.

First download the Dollar scripting runtime from [distribution](http://dollarscript.s3-website-eu-west-1.amazonaws.com/dist/dollar-{{site.release}}.tgz)

Make sure `dollar/bin` is on your PATH.

Run `dollar <filename>` to execute a Dollar script.

Here is an example of what Dollar looks like

```

def testParams {$2 + " " + $1}

@@ testParams ("Hello", "World") 

.: testParams ("Hello", "World") == "World Hello"

```

## Understanding the Basics


Dollar has it's own peculiarities, mostly these exists to help with it's major target: serverside integration projects. So it's important to understand the basic concepts before getting started.

### Functional Programming and the 'pure' operator

Support for functional programming is included in Dollar, this will be widened as the language is developed. For now it is provided by the `pure` operator. This signals that an expression or declaration is a pure expression or function.

In this example we're declaring reverse to be an expression that reverses two values from a supplied array. Because we declare it as `pure` the expression supplied must also be `pure`. To understand what a pure function is please see http://en.wikipedia.org/wiki/Pure_function. Basically it prohibits the reading of external state or the setting of external state. We next swap `[2,1]` within a newly created pure expression, which is subsequently assigned to a. If reverse had not been declared pure it would not be allowed within the pure expression.

 ```
 pure def reverse [$1[1],$1[0]]

 a= pure {
     reverse([2,1])
 }

 ```

Note some builtin functions are not themselves pure and will trigger parser errors if you attempt to use them in a pure expression. Take DATE() for example which supplies an external state (the computers clock).



### Reactive Programming

Dollar expressions are by default *lazy*, this is really important to understand otherwise you may get some surprises. This lazy evaluation is combined with a simple event system to make Dollar a [reactive programming language](http://en.wikipedia.org/wiki/Reactive_programming) by default. 

The simplest way to understand reactive programming is to imagine you are using a spreadsheet. When you say a cell has the value SUM(A1:A4) that value will *react* to changes in any of the cells from A1 to A4. Dollarscript works the same way by default, however you can also *fix* values when you want to write procedural code. 

Let's see some of that behaviour in action:

```

var variableA = 1
const variableB := variableA
variableA = 2

.: variableB == 2
```

In the above example we are declaring (using the declarative operator `:=`) that variableA is current the value 1, we then declare that variableB is the *same as* variableA. So when we change variableA to 2 we also change variableB to 2.

Before we go any further let's clarify `:=` vs `=`, I have chosen to follow the logic [described here](https://math.stackexchange.com/questions/1838678/confused-about-notation-versus-plain-old) so that the `:=` operator is a definition (and by it's nature reactive) and `=` is an assignment ( not reactive and has a fix depth of 1 - more on that later).

This means that `a := b + 1` translates to **a is defined as b + 1** so a is behaving reactively, changes to b cause a change in the value of a. It also means that `a = b + 1` simply assigns `b + 1` to the variable a, changes to b do not cause changes to a. 

At this point it's time to introduce a what is arguably a cleaner and easier to understand short hand for 'const reactiveVar := {...}` the short hand is 'def reactiveVar {...}' such as:

```
    def myFunction { @@ "Hello World"}
```

The `def` keyword implies `const` and it also does not allow dynamic variable names (more on that later). A rule of thumb is if you'd like to have something act like a function use `def`.


**TL;DR `=` behaves like it's Java equivalent, `:=` doesnt't and use `def` to create functions.**

> The assertion operator `.:` will throw an assertion error if the value following is either non boolean or not true.


Now let's throw in the causes operator :

```

var a=1
a causes { @@ $1 }
a=2
a=3
a=4

```

~~~
2
3
4
~~~

That simple piece of code will simply output each change made to the variable a, but wait a minute what about ...

```

var b=1
var a=1
a + b + 1 causes { @@ "a=" + a + ", b=" + b}
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

Yep, you can write reactive expressions based on collections or arbitrary expressions. When any component changes the right hand side is re-evaluated (the actual value that changed is passed in as $1).

But it's even simpler than that, many of Dollars operators are reactive themselves. That means they understand changes to their values. Take `@@` (or `print`) as an example:

```
var b=1
@@b
b=2
```

Outputs 1 then 2 because @@ heard the change to b and re output the new value. Often this is what you want, however if you don't just add the fix operator `&` before the value. That will stop reactive behaviour.

```
var b=1
@@ &b
b=2
```




### Assignment

Obviously the declarative/reactive behavior is fantastic for templating, eventing, creating lambda style expressions etc. however there are times when we want to simply assign a value and perform a single action on that value.

```

var variableA = 1
var variableB = variableA
variableA = 2

.: variableB == 1
```

So as you can see when we use the `=` assignment operator we assign the *value* of the right hand side to the variable. Watch what happens when we use expressions.


```

var variableA = 1
var variableB = variableA
var variableC = (variableA +1 )
const variableD := (variableA + 1)
variableA = 2

.: variableB == 1
.: variableC == 2
.: variableD == 3

```

The assignment operator `=` has a 'fix' depth of 1. This means that any expression will be evaluated, but no maps or line blocks will be. It is also not reactive. A fix depth of 2 causes all expressions to be evaluated and evaluates one depth of maps or line blocks.

The assert equals operator `<=>` will compare two values and throw an exception if they are ever not the same ` a <=> b` is the same as `.: a == b`**

```

def lamdaVar  {$1 + 10}
lamdaVar(5) <=> 15

```

> It's important to note that all values in Dollar are immutable - that means if you wish to change the value of a variable you *must* __reassign__ a new value to the variable. For example `v++` would return the value of `v+1` it does not increment v. If however you want to assign a constant value, one that is both immutable and cannot be reassigned, just use the `const` modifier at the variable assignment (this does not make sense for declarations, so is only available on assignments).

```
const MEDIUM = 23
// MEDIUM= 4 would now produce an error
```

So `:=` supports the full reactive behaviour of Dollar, i.e. it is a declaration not a value assignment, and `=` is used to nail down a particular value or reduce the reactive behaviour. Later we'll come across the fix operator `&` which instructs Dollar to fix a value completely . More on that later.

### Blocks

#### Line Block
Dollar supports several block types, the first is the 'line block' a line block lies between `{` and `}` and is separated by either newlines or `;` characters.

```

var myBlock = {
    "Hello "
    "World"
}

myBlock <=> "World"

const myBlock2 = {1;2}

myBlock2 <=> 2

```

When a line block is evaluated the result is the value of the last entry. For advanced users note that all lines will be evaluated, the value is just ignored. A line block behaves a lot like a function in an imperative language.

#### List Block

Next we have the list block, the list block preserves all the values each part is seperated by either a `,` or a newline but is delimited by `[` and `]`.

```

var list = [
    "Hello "
    "World"
]

list <=> ["Hello ","World"]

const list2 = [1,2]

list2 <=> [1,2]

```

#### Map Block

Finally we have the map block, when an map block is evaluated the result is the aggregation  of the parts from top to bottom into a map. The map block starts and finishes with the `{` `}` braces, however each part is seperated by a `,` not a `;` or *newline* . The default behaviour of a map block is virtually useless, it takes the string value and makes it the key and keeps the original value as the value to be paired with that key.

```

var mapBlock = {
    "Hello",
    "World"
}

mapBlock <=> {"Hello":"Hello", "World":"World"}

const mapBlock2 = { 1, 2}

mapBlock2 <=> {"1":1,"2":2}

```

Map blocks are combined with the pair `:` operator to become useful and create maps/JSON like this:


```

var mapBlock = {
    "first":"Hello ",
   "second":"World"
}

@@ mapBlock

mapBlock.second <=> "World"

```

A map block with one entry that is not a pair is assumed to be a *Line Block*.

The stdout operator `@@` is used to send a value to stdout in it's serialized (JSON) format, so the result of the above would be to output `{"first":"Hello ","second":"World"}` a JSON object created using JSON like syntax. Maps can also be created by joining pairs.

```

var pair1 = "first" : "Hello ";
var pair2 = "second" : "World";

.: pair1 + pair2 == {"first":"Hello ","second":"World"}

```

Dollar maps are also associative arrays (like JavaScript) allowing you to request members from them using the list subscript syntax

```
{"key1":1,"key2":2} ["key"+1] <=> 1
{"key1":1,"key2":2} [1] <=> {"key2":2}
{"key1":1,"key2":2} [1]["key2"] <=> 2
```

As you can see from the example you can request a key/value pair (or Tuple if you like) by it's position using a numeric subscript. Or you can treat it as an associative array and request an entry by specifying the key name. Any expression can be used as a subscript, numerical values will be used as indexes, otherwise the string value will be used as a key.


### Lists

Dollar's lists are pretty similar to JavaScript arrays. They are defined using the `[1,2,3]` style syntax and accessed using the `x[y]` subscript syntax.

```
.: [1,2,3] + 4 == [1,2,3,4];
.: [1,2,3,4] - 4 == [1,2,3];
.: [] + 1 == [1] ;
.: [1] + [1] == [1,1];
.: [1] + 1 == [1,1];

[1,2,3][1] <=> 2

```

> Note we're using the assert equals or `<=>` operator here, this is a combination of `.:` and `==` that will cause an error if the two values are not equal.

You can count the size of the list using the size operator `#`.

```
#[1,2,3,4] <=> 4
```


### Ranges

Dollar (at present) supports numerical and character ranges using Maven style syntax


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

Please see [the Guava docs](https://github.com/google/guava/wiki/RangesExplained) for more information on the range format used.


```

#("a".."c") <=> 1
#["a".."c"] <=> 3
[1..3][1] <=>2

```


### Scopes & Closure

Dollar makes extensive use of code blocks with scope [closure](https://en.wikipedia.org/wiki/Closure_(computer_programming)). Blocks, lists and maps all have scope closure - I suggest reading [this article by Martin Fowler](https://martinfowler.com/bliki/Lambda.html) if you're unfamiliar with closures. Let's start with a simple example:

```
var outer=10;
def func {
    outer;
}
func() <=> 10;
```

In the above example `func` is a block collection which returns `outer`. It has access to `outer` because at the time of decleration outer is in it's parent's lexical scope.

```

def func {
    var inner=10;
    {$1+inner}
}

func()(10) <=> 20;
```

In the above example we now return an anonymous block collection from func which we then paramterize with the value `10`. When `func` is executed it returns the paramterized block, which we then call with `10` and which adds the value `inner` to the parameter (`$1`) - naturally the result is 20.

So all of that looks fairly familiar if you've ever used JavaScript, but remember all of Dollar's collections have scope closure so the following is valid:

```
var outer=10;

const scopedArray := [$1,outer,{var inner=20;inner}]

scopedArray(5)[0] <=> 5;
scopedArray(5)[1] <=> 10;
scopedArray(5)[2]() <=> 20;

```

In this example the list has lexical scope closure and when we parameterize it using `(5)` we can pass in the positional parameter `($1)` for when it is evaluated.

#### Understanding Scopes A Little Deeper

Each parse time scope boundary (_blocks, lists, maps, constraints, parameters etc._) is marked as such during the initial parse of the script. When executed each of these will create a runtime scope. Each runtime boundary will create a hierachy of scopes with the previous being the parent.


Where an executable element with scope closure (such as _lists, blocks and maps_) is executed  **all** current scopes are saved and attached to that element. So when the element is subsequently executed it retains it's original lexical closure (as described [here](https://en.wikipedia.org/wiki/Closure_(computer_programming)# Implementation_and_theory)).

>Please look at the `SourceNodeOptions` class for the three types of scoped nodes, they are `NO_SCOPE` which has no effect on the current scope, `NEW_SCOPE` which creates a new scope but does not have closure and `SCOPE_WITH_CLOSURE` which creates a new scope with lexical closure.


### Error Handling

Error handling couldn't be simpler. Define an error expression using the error keyword, the expression supplied will be evaluated on an error occurring within any sub scope of the scope in which it is defined. The special variables `msg` and `type` will be assigned values.

```
var errorHappened= false
error { @@ msg; errorHappened= true }
var a= << http://fake.com:99999
.: errorHappened
```

### Logging

Logging is done by the `print`,`debug` and `err` keywords and the `@@`,`!!` and `!?` operators.

| Keyword  | Operator |
| -------- | -------- |
| `print`  | `@@`     |
| `debug`  | `!!`     |
| `err`    | `!?`     |


```
@@ "I'm a stdout message"
!! "I'm a debug message"
!? "I'm an error message"
```

## Type System
### Intro
Although Dollar is typeless at compile time, it does support basic runtime typing. At present this includes: STRING, INTEGER,DECIMAL, LIST, MAP, URI, VOID, RANGE, BOOLEAN. The value for a type can be checked using the `is` operator:

```
.: "Hello World" is String
.: ["Hello World"] is List
```

### DATE

Dollar supports a decimal date system where each day is 1.0. This means it's possible to add and remove days from a date using simple arithmetic.

```
@@ DATE()
@@ DATE() + 1
@@ DATE() - 1

.: DATE() + "1.0" is String
.: DATE() / "1.0" is Decimal
```

Components of the date can be accessed using the subscript operators:

```
@@ DATE().DAY_OF_WEEK

@@ DATE()['DAY_OF_YEAR']=1
```

Valid values are those from `java.time.temporal.ChronoField`

```
NANO_OF_SECOND, NANO_OF_DAY, MICRO_OF_SECOND, MICRO_OF_DAY, MILLI_OF_SECOND, MILLI_OF_DAY, SECOND_OF_MINUTE, SECOND_OF_DAY, MINUTE_OF_HOUR, MINUTE_OF_DAY, HOUR_OF_AMPM, CLOCK_HOUR_OF_AMPM, HOUR_OF_DAY, CLOCK_HOUR_OF_DAY, AMPM_OF_DAY, DAY_OF_WEEK, ALIGNED_DAY_OF_WEEK_IN_MONTH, ALIGNED_DAY_OF_WEEK_IN_YEAR, DAY_OF_MONTH, DAY_OF_YEAR, EPOCH_DAY, ALIGNED_WEEK_OF_MONTH, ALIGNED_WEEK_OF_YEAR, MONTH_OF_YEAR, PROLEPTIC_MONTH, YEAR_OF_ERA, YEAR, ERA, INSTANT_SECONDS, OFFSET_SECONDS
```

As you can see we can do date arithmetic, but thanks to another Dollar feature anything that can be specified as xxx(i) can also be written i xxx (where i is an integer or decimal and xxx is an identifier). So we can add days hours and seconds to the date.

```
@@ DATE() + 1 DAY
@@ DATE() + 1 HOUR
@@ DATE() + 1 SEC
```

Those values are built in, but we can easily define them ourselves.

```
def fortnight ($1 * 14)

@@ DATE() + 1 fortnight
```
### STRING
### INTEGER
### DECIMAL
### LIST
### MAP
### URI
### VOID
### NULL
### RANGE
### BOOLEAN

### Constraints

Although there are no compile type constraints in Dollar a runtime type system can be built using constraints. Constraints are declared at the time of variable assignment or declaration. A constraint once declared on a variable cannot be changed. The constraint is placed before the variable name at the time of declaration in parenthesis.

```
var (it < 100) a = 50
var (previous is Void|| it > previous) b = 5
b=6
b=7
var ( it is String) s="String value"
```

The special variables `it` - the current value and `previous` - the previous value, will be available for the constraint.

To build a simple runtime type system simply declare (using `:=`) your type as a boolean expression.

```

//define a pseudo-type
def colorEnum ( it in ["red","green","blue"] )


//Use it as a constraint
var (colorEnum) myColor= "green"

error { @@ msg }

//This fails
var myColor="apple"

```

Of course since the use of `(it is XXXX)` is very common Dollar provides a specific runtime type constraint that can be added in conjunction with other constraints. Simply prefix the assignment or decleration with `<XXXX>` where XXXX is the runtime type.


```
var <String> (#it > 5) s="String value"
```

It is intended that the predictive type system, will be in time combined with runtime types to help spot bugs at compile time.

### Type Coercion
Dollar also supports type coercion, this is done using the `as` operator followed by the type to coerce to.


```
var <String> s= 1 as String
s <=> "1"
```

A few more examples follow.

```
1 as String <=> "1"
1 as Boolean <=> true
1 as List <=> [1]
1 as Map <=> {"value":1}
1 as Void <=> void
1 as Integer <=> 1

"1" as Integer <=> 1
"http://google.com" as URI
"1" as Void <=> void
"true" as Boolean <=> true
"1" as Boolean <=> false
"1" as List <=> ["1"]
"1" as Map <=> {"value":"1"}
"1" as String <=> "1"

true as String <=> "true"
true as Integer <=> 1
true as List <=> [true]
true as Map <=> {"value":true}
true as Boolean <=> true
true as Void <=> void


[1,2,3] as String <=> "[ 1, 2, 3 ]"
[1,2,3] as List <=> [1,2,3]
[1,2,3] as Boolean <=> true
[1,2,3] as Map <=> {"value":[1,2,3]}

{"a":1,"b":2} as String <=> '{"a":1,"b":2}'
{"a":1,"b":2} as List <=> ["a":1,"b":2]
{"a":1,"b":2} as Boolean <=> true
{"a":1,"b":2} as Void <=> void
```


## Imperative Control Flow

With imperative control flow, the control flow operations are only triggered when the block they are contained within is evaluated. I.e. they behave like control flow in imperative languages. So start with these if you're just learning Dollar.

### If

Dollar supports the usual imperative control flow but, unlike some languages, everything is an operator. This is the general rule of Dollar, everything has a value. Dollar does not have the concept of statements and expressions, just expressions. This means that you can use control flow in an expression.

```

var a=1
var b= if a==1 2 else 3
b <=> 2

```

So let's start with the `if` operator. The `if` operator is separate from the `else` operator, it simply evaluates the condition supplied as the first argument. If that value is boolean and true it evaluates the second argument and returns it's value; otherwise it returns boolean false.

The `else` operator is a binary operator which evaluates the left-hand-side (usually the result of an `if` statement), if that has a value of false then the right-hand-side is evaluated and it's result returned, otherwise it returns the left-hand-side.

The combined effect of these two operators is to provide the usual if/else/else if/ control flow

```

var a=5
//Parenthesis added for clarity, not required.
var b= if (a == 1) "one" else if (a == 2) "two" else "more than two"
.: b == "more than two"

```

### For

```

for i in [1..10] {
    @@ i
}

```

### While

```
var a= 1
while a < 10 {
 a= a+1
}
a <=> 10

```



## Reactive Control Flow

### Causes

Dollar as previously mentioned is a reactive programming language, that means that changes to one part of your program can automatically affect another. Consider this a 'push' model instead of the usual 'pull' model.

Let's start with the simplest reactive control flow operator, the '=>' or 'causes' operator.

```
var a=1; var b=1

a => (b= a)

&a <=> 1 ; &b <=> 1

a=2 ; &a <=> 2 ; &b <=> 2

```

Okay so reactive programming can melt your head a little. So let's go through the example step by step.

Firstly we assign fixed values to `a` and `b`, we then say that when `a` changes the action we should take is to assign it's value to `b`. Okay now we check to see if the current value of `a` is equal to 1, we use the fix operator `&` here to say that we are only interested in the current value. Because `<=>` is a reactive operator if we didn't use the fix operator then `a <=> 1` would mean a is always 1. When we add the fix operator it fixes the value of a to the value at this point in the code.

We then do the same with b to see if it is 1.

Next we assign a new value of 2 to `a`. This will immediately (within the same thread) trigger the reactive `->` operator which is triggered by changes to `a`. The trigger assigns the value of `a` to `b`, so `b` is now the same as `a`. The assertions at the end confirm this.

### When

Next we have the 'when' operator which can be specified as a statement, usually for longer pieces of code. Or as the `?` operator, for concise code.


```

var c=1
var d=1

//When c is greater than 3 assign it's value to d
c > 3 ? (d= c)

&c <=> 1; &d <=> 1
c=2; &c <=> 2; &d <=> 1
c=5 ; &c <=> 5 ; &d <=> 5

```

This is similar to the previous example except that we have to set a value greater than 3 for the action to be taken.

```
//Note alternative syntax is when <condition> <expression>
var c=1
when c > 3 { @@ c}
```

### Collect


The `collect` operator listens for changes in the supplied expression adding all the values to a list until the `until` clause is triggered. It then evaluates the second expression with the values `it` for the current value, `count` for the number of messages **received** since last emission and `collected` for the collected values. The whole operator itself emits `void` unless the collection operation is triggered in which case it emits the collection itself. Values can be skipped with an `unless` clause. Skipped messages increase the count value, so use `#collected` if you want the number of collected values.

```

var e=void

//Length is greater than or equal to 4 unless void
var (#it >= 4 || it is Void) collectedValues=void

//count starts at 0 so this means five to collect (except if it contains the value 10)
collect e until count == 4 unless it == 10{
    print count
    print collected
    collectedValues= collected
}

e=1; e=2; e=3; e=4; e=5; e=6
&collectedValues <=> [1,2,3,4,5]
e=7; e=8; e=9; e=10
&collectedValues <=> [6,7,8,9]
e=11; e=12; e=13; e=14; e=15; e=16
&collectedValues <=> [11,12,13,14,15]

```

## Parameters &amp; Functions

In most programming languages you have the concept of functions and parameters, i.e. you can parametrized blocks of code. In Dollar you can parameterize *anything*. For example let's just take a simple expression that adds two strings together, in reverse order, and pass in two parameters.

```
($2 + " " + $1)("Hello", "World") <=> "World Hello"

```

The naming of positional parameters is the same as in shell scripts.

Now if we take this further we can use the declaration operator `:=` to say that a variable is equal to the expression we wish to parameterise, like so:

```

const testParams := ($2 + " " + $1)
testParams ("Hello", "World") <=> "World Hello"

```

Yep we built a function just by naming an expression. You can name anything and parameterise it - including maps, lists, blocks and plain old expressions.


What about named parameters, that would be nice.

```
const testParams := (last + " " + first)
testParams(first="Hello", last="World") <=> "World Hello"
```

Yep you can use named parameters, then refer to the values by the names passed in.


## Resources &amp; URIs

URIs are first class citizen's in Dollar. They refer to a an arbitrary resource, usually remote, that can be accessed using the specified protocol and location. Static URIs can be referred to directly without quotation marks, dynamic URIs can be built by casting to a uri using the `as` operator.

```
var posts = << https://jsonplaceholder.typicode.com/posts 
var titles = posts each { $1.title }
@@ titles
```

In this example we've requested a single value (using `<<`) from a uri and assigned the value to `posts` then we simply iterate over the results  using `each` and each value (passed in to the scope as `$1`) we extract the `title`. The each operator returns a list of the results and that is what is passed to standard out.


## Using Other Languages

Hopefully you'll find Dollar a useful and productive language, but there will be many times when you just want to quickly nip out to a bit of another language. To do so, just surround the code in backticks and prefix with the languages name. Currently only `java` is supported but more will be added soon.

```

var variableA="Hello World"

var java = java `out=in.get(0);` (variableA)

java <=> "Hello World"

```
### Java

A whole bunch of imports are done for you automatically (see below) but you will have to fully qualify any thirdparty libs.
 
> imports `dollar.lang.*``dollar.internal.runtime.script.api.*` `com.sillelien.dollar.api.*` `java.io.*` `java.math.*` `java.net.*` `java.nio.file.*` `java.util.*` `java.util.concurrent.*` `java.util.function.*` `java.util.prefs.*` `java.util.regex.*` `java.util.stream.*`
 
 > static imports `DollarStatic.*` `dollar.internal.runtime.script.java.JavaScriptingStaticImports.*`

The return type will be of type `var` and is stored in the variable `out`. The Java snippet also has access to the scope (Scope) object on which you can get and set Dollar variables.

Reactive behaviour is supported on the Scope object with the `listen` and `notify` methods on variables. You'll need to then built your reactivity around those variables or on the `out` object directly (that's a pretty advanced topic).


## Operators

### Iterative Operators

### Comparison Operators

### Numerical Operators

Dollar support the basic numerical operators +,-,/,*,%,++,-- as well as #

**Remember ++ and -- do not change a variable's value they are a shorthand for a+1 and a-1 not a=a+1 or a=a-1**

```

 1 + 1 <=> 2
 3 -2 <=> 1
 2 * 2 <=> 4
 5 / 4 <=> 1
 5 % 4 <=> 1
 5.0 /4 <=> 1.25
 # [1,2,3] <=> 3
 # 10 <=> 1
 10++ <=> 11
 10-- <=> 9

```

And similar to Java Dollar coerces types as required:

```
.: (1 - 1.0) is Decimal
.: (1.0 - 1.0) is Decimal
.: (1.0 - 1) is Decimal
.: (1 - 1) is Integer

.: (1 + 1.0) is Decimal
.: (1.0 + 1.0) is Decimal
.: (1.0 + 1) is Decimal
.: (1 + 1) is Integer

.: 1 / 1 is Integer
.: 1 / 1.0 is Decimal
.: 2.0 / 1 is Decimal
.: 2.0 / 1.0 is Decimal

.: 1 * 1 is Integer
.: 1 * 1.0 is Decimal
.: 2.0 * 1 is Decimal
.: 2.0 * 1.0 is Decimal


.: 1 % 1 is Integer
.: 1 % 1.0 is Decimal
.: 2.0 % 1 is Decimal
.: 2.0 % 1.0 is Decimal
.: ABS(1) is Integer
.: ABS(1.0) is Decimal
```

### Logical Operators

Dollar support the basic logical operators &&,||,! as well as the truthy operator `~` and the default operator `|`.

#### Truthy
The truthy operator `~` converts any value to a boolean by applying the rule that: void is false, 0 is false, "" is false, empty list is false, empty map is false - all else is true.

```

.: ~ [1,2,3]
.: ! ~ []
.: ~ "anything"
.: ! ~ ""
.: ~ 1
.: ! ~ 0
.: ! ~ {void}
.:  ~ {"a" : 1}
.: ! ~ void

void :- "Hello" <=> "Hello"
1 :- "Hello" <=> 1

```
#### Boolean Operators

The shortcut operators `||` and `&&` work the same as in Java. As do the comparison operators `>`,`<` etc. They also have keyword alternatives such as `and` or `or`.

| Keyword              | Operator | Java Equivalent           |
| --------             | -------- | ---------                 |
| `and`                | `&&`     | `&&`                      |
| `or`                 | `ǀǀ`     | `ǀǀ`                      |
| `equal`              | `==`     | `.equals()`               |
| `not-equal`          | `!=`     | `! .equals()`             |
| `less-than`          | `<`      | `lhs.compareTo(rhs) < 0`  |
| `greater-than`       | `>`      | `lhs.compareTo(rhs) > 0`  |
| `less-than-equal`    | `<=`     | `lhs.compareTo(rhs) <= 0` |
| `greater-than-equal` | `>=`     | `lhs.compareTo(rhs) >= 0` |


Examples:

```
true && true <=> true
true && false <=> false
false && true <=> false
false && false <=> false


true || true <=> true
true || false <=> true
false || true <=> true
false || false <=> false

.: 1 < 2
.: 3 > 2
.: 1 <= 1
.: 1 <= 2
.: 1 > 0
.: 1 >= 1
.: 2 >= 1
.: 1 == 1
.: "Hello" == "Hello"
.: "abc" < "abd"
```

#### Default Operator

The default operator ':-' (keyword `default`) returns the left hand side if it is not `VOID` otherwise it returns the right hand side.

```
void :- "Hello" <=> "Hello"
1 :- "Hello" <=> 1
void default 2 <=> 2
```


### Pipe Operators
### Remaining Operators
## Imports &amp; Modules
### Import
### Modules

Modules can be imported using the `module` keyword and a string representing in URI format the location of the module. At present the standard format is the Github locator so we're going to look at that first.

```
const chat:= module "github:neilellis:dollar-example-module::chat.ds" (channel="test")
var sub= chat.server()
chat.stop_()
```

Let's start by breaking down the module URI. Okay our first part says it's the GitHub scheme and so the GitHub module locator will be used. Next we say the repository belongs to neilellis and the repository is called dollar-example-module. The next part is the optional branch indicator - here we are using the master, so we just leave that empty. Finally we can optionally supply the name of a single script we want to import. If we don't provide that then the main script from the modules 'module.json' file will be used.

The source for the module can be found here: https://github.com/neilellis/dollar-example-module

You will need to have the `git` command on your path and to have access to the repository using `git clone`.

The GitHub resolver will checkout the specified repository and store it under `~/.dollar/runtime/modules/github/<username>/<repo-name>/<branch>` all further interaction with the module will then be done from the checked out version. If you already have a version checked out a git pull will be done to update the branch.

```
const hello := module "github:neilellis:dollar-example-module:0.1.0:branch.ds"
@@ hello
```

### Module Locators
### Writing Modules

Modules consist of a file called module.json with the name of the main script for the module and an optional array of Maven style java dependencies. And then one or more Dollar files.

//TODO: change module.json to module.ds

```
{
"main":"chat.ds",
"dependencies":["org.twitter4j:twitter4j-core:4.0.2"]
}
```

The Dollar files should use the export modifier on assignments that it wishes to make available to client applications and it can refer to variables that don't exist, in which case values for those variables will need to be passed as parameters to the module declaration in the client application.


```
var redis= ("redis://localhost:6379/" + ${channel :- "test"}) as URI
var www= (("http:get://127.0.0.1:8111/" + ${channel :- "test"}) as URI)

export def server  {
           www subscribe {
            $1.params >> redis
            { body :  all redis }
        }
    };

export def stop_ {STOP(www);STOP(redis); @@ [STATE(www),STATE(redis)]}

export def state_ [STATE(www),STATE(redis)]
```


## Builtin Functions

## Concurrency & Threads

Notes:

All types are immutable, including collections.
You cannot reassign a variable from a different thread, so they are readonly from other threads.


### Parallel &amp; Serial Operators
The parallel operator `|:|` or `parallel` causes the right hand side expression to be evaluated in parallel, it's partner the serial operator `|..|` or `serial` forces serial evaluation even if the current expression is being evaluated in parallel.

```

const testList := [ TIME(), {SLEEP(1 SEC); TIME();}, TIME() ];
var a= |..| testList;
var b= |:| testList;
//Test different execution orders
.: a[2] >= a[1]
.: b[2] < b[1]
```

As you can see the order of evaluation of lists and maps **but not line blocks** is affected by the use of parallel evaluation.

### Fork

The fork operator `-<` or `fork` will cause an expression to be evaluated in the background and any reference to the forked expression will block until a value is ready.

```
const sleepTime := {@@ "Background Sleeping";SLEEP(4 SECS); @@ "Background Finished Sleeping";TIME()}
//Any future reference to c will block until c has completed evaluation
var c= fork sleepTime
SLEEP(1 SEC)
@@ "Main thread sleeping ..."
SLEEP(2 SECS)
@@ "Main thread finished sleeping ..."
var d= TIME()
.: c > d
```

In the example the value of c is greater than d because the value of c is evaluated in the background. Note that as soon as you make use of the value of c you block until the value is ready. This is exactly the same as Java's Futures.




## Advanced Topics

TODO


-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running dollar.internal.runtime.script.ParserMainTest
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.331 sec - in dollar.internal.runtime.script.ParserMainTest

Results :

Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

## Appendix A - Operators
### `all` or `<@` {#op-all}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![impure](https://img.shields.io/badge/function-impure-blue.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`('<@'|'all') <expression>`**{: style="font-size: 60%"}



Returns a non-destructive read of all the values of a collection or URI addressed resource.


```
var posts = <@ https://jsonplaceholder.typicode.com/posts
```

___

### `and` or `&&` {#op-and}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![pure](https://img.shields.io/badge/function-pure-green.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`<expression> ('&&'|'and') <expression>`**{: style="font-size: 60%"}



Returns the logical 'and' of two expressions, e.g. `a && b`. Just like in Java it will shortcut, so that if the left-hand-side is false the right-hand-side is never evaluated.

```
true && true <=> true
true && false <=> false
false && true <=> false
false && false <=> false
```

___

### `assert` or `.:` {#op-assert}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![pure](https://img.shields.io/badge/function-pure-green.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`('.:'|'assert') <expression>`**{: style="font-size: 60%"}



The assertion opeartor is used to assert that an expression holds true. It is a reactive operator such that it is evaluated when the right-hand-side expression changes. so `.: a > 10` is asserting that a is **always** greater than 10. To avoid reactive behaviour use the fix operator such as `.: &a > 10` which means that when this statement is evaluated the value of a is compared with 10 - if __at this point__ it is not greater than 10 then the assertion will fail. 

```
.: 1 < 2
.: 3 > 2
.: 1 <= 1
.: 1 <= 2
```

___

### `<->` (assert-equals) {#op-assert-equals}

![non-reactive](https://img.shields.io/badge/reactivity-fixed-blue.svg?style=flat-square) ![pure](https://img.shields.io/badge/function-pure-green.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`<expression> '<->' <expression>`**{: style="font-size: 60%"}



Asserts that at the point of execution that the left-hand-side is equal to the right-hand-side.

```
 1 + 1 <-> 2
```

___

### `<=>` (assert-equals-reactive) {#op-assert-equals-reactive}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![pure](https://img.shields.io/badge/function-pure-green.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`<expression> '<=>' <expression>`**{: style="font-size: 60%"}



Asserts that the left-hand-side is **always** equal to the right-hand-side.

```
def lamdaVar  {$1 + 10}
lamdaVar(5) <=> 15
```

___

### `=` (assign) {#op-assign}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![pure](https://img.shields.io/badge/function-pure-green.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)



Values can be assigned to a variable using the standard `=` assignment operator. A variable is declared using either 'var', 'const' or 'volatile'. 

`var` is used to mark a simple declaration of a mutable variable. E.g. `var a = 1; a= 2`. 

`const` is used to declare a readonly variable, since all values in Dollar are immutable this makes the variable both readonly and immutable.

`volatile` is used to declare the variable as mutable from multiple threads.

Although there are no compile type constraints in Dollar a runtime type system can be built using constraints. Constraints are declared at the time of variable assignment or declaration. A constraint once declared on a variable cannot be changed. The constraint is placed before the variable name at the time of declaration in parenthesis.
 
 Of course since the use of `(it is XXXX)` is very common, so Dollar provides a specific runtime type constraint that can be added in conjunction with other constraints. Simply prefix the assignment or decleration with `<XXXX>` where XXXX is the runtime type.
 
 Values can be also be marked for `export` as in Javascript.


```
var (it < 100) a = 50
var (previous is Void || it > previous ) b = 5
b=6
b=7
var ( it is String) s1="String value"
var <String> (#it > 5) s2="String value"
const immutableValue= "Hello World"
```

___

### `avg` or `[%]` {#op-avg}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![pure](https://img.shields.io/badge/function-pure-green.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`<expression> ('[%]'|'avg')`**{: style="font-size: 60%"}





```
```

___

### block {#op-block}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![impure](https://img.shields.io/badge/function-impure-blue.svg?style=flat-square) ![New Scope](https://img.shields.io/badge/scope-new%20with%20closure-green.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`'{' ( <expression> ';' ) * [ <expression> ] '}'`**{: style="font-size: 60%"}





```
```

___

### builtin {#op-builtin}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![impure](https://img.shields.io/badge/function-impure-blue.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`<name> (<parameter>)*`**{: style="font-size: 60%"}



Dollar has many built-in functions.

//TODO: document them


```
var now= DATE();
```

___

### cast {#op-cast}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![pure](https://img.shields.io/badge/function-pure-green.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`<expression> 'as' <type>`**{: style="font-size: 60%"}



Dollar  supports type coercion using the `as` operator followed by the type to coerce to.


```
1 as String <=> "1"
1 as Boolean <=> true
1 as List <=> [1]
1 as Map <=> {"value":1}
1 as Void <=> void
1 as Integer <=> 1

"1" as Integer <=> 1
"http://google.com" as URI
"1" as Void <=> void
"true" as Boolean <=> true
"1" as Boolean <=> false
"1" as List <=> ["1"]
"1" as Map <=> {"value":"1"}
"1" as String <=> "1"

true as String <=> "true"
true as Integer <=> 1
true as List <=> [true]
true as Map <=> {"value":true}
true as Boolean <=> true
true as Void <=> void


[1,2,3] as String <=> "[ 1, 2, 3 ]"
[1,2,3] as List <=> [1,2,3]
[1,2,3] as Boolean <=> true
[1,2,3] as Map <=> {"value":[1,2,3]}

{"a":1,"b":2} as String <=> '{"a":1,"b":2}'
{"a":1,"b":2} as List <=> ["a":1,"b":2]
{"a":1,"b":2} as Boolean <=> true
{"a":1,"b":2} as Void <=> void
```

___

### `causes` or `=>` {#op-causes}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![pure](https://img.shields.io/badge/function-pure-green.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`<expression> ('=>'|'causes') <expression>`**{: style="font-size: 60%"}



The causes operator is used to link a reactive expression to an imperative action. The left-hand-side is any expression and the right hand-side is any expression that will be evaluated when the left-hand-side is updated such as `a+b => {@@ a; @@ b}`.

```
var a=1; var b=1

a => (b= a)

&a <=> 1 ; &b <=> 1

a=2 ; &a <=> 2 ; &b <=> 2
```

___

### `choose` or `?*` {#op-choose}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![pure](https://img.shields.io/badge/function-pure-green.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`<expression> ('?*'|'choose') <expression>`**{: style="font-size: 60%"}



The choose operator is a simple reversal of the subscript operator used for code clarity.


```
var value= "red";
value choose {"red": "roses", "green": "tomatoes"} <-> "roses"
```

___

### `class` {#op-class}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![impure](https://img.shields.io/badge/function-impure-blue.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`'class' <identifier> <expression>`**{: style="font-size: 60%"}





```
```

___

### `collect` {#op-collect}

![non-reactive](https://img.shields.io/badge/reactivity-fixed-blue.svg?style=flat-square) ![pure](https://img.shields.io/badge/function-pure-green.svg?style=flat-square) ![New Scope](https://img.shields.io/badge/scope-new-blue.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`collect <expression> [ 'until' <expression> ] [ 'unless' <expression> ] <expression>`**{: style="font-size: 60%"}



The `collect` operator listens for changes in the supplied expression adding all the values to a list until the `until` clause is triggered. It then evaluates the second expression with the values `it` for the current value, `count` for the number of messages **received** since last emission and `collected` for the collected values. The whole operator itself emits `void` unless the collection operation is triggered in which case it emits the collection itself. Values can be skipped with an `unless` clause. Skipped messages increase the count value, so use `#collected` if you want the number of collected values.


```
var e=void

//Length is greater than or equal to 4 unless void
var (#it >= 4 || it is Void) collectedValues=void

//count starts at 0 so this means five to collect (except if it contains the value 10)
collect e until count == 4 unless it == 10{
    print count
    print collected
    collectedValues= collected
}

e=1; e=2; e=3; e=4; e=5; e=6
&collectedValues <=> [1,2,3,4,5]
e=7; e=8; e=9; e=10
&collectedValues <=> [6,7,8,9]
e=11; e=12; e=13; e=14; e=15; e=16
&collectedValues <=> [11,12,13,14,15]
```

___

### `create` or `|||>` {#op-create}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![impure](https://img.shields.io/badge/function-impure-blue.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`('|||>'|'create') <expression>`**{: style="font-size: 60%"}



Creates a service described typically by a URI.

```
```

___

### `debug` or `!!` {#op-debug}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![impure](https://img.shields.io/badge/function-impure-blue.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`('!!'|'debug') <expression>`**{: style="font-size: 60%"}



Sends the result of the right-hand-side to the debug log.

```
!! "I'm a debug message"
```

___

### `:=` (declaration) {#op-declaration}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![pure](https://img.shields.io/badge/function-pure-green.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`( [export] [const] <variable-name> ':=' <expression>) | ( def <variable-name> <expression )`**{: style="font-size: 60%"}



Declares a variable to have a value, this is declarative and reactive such that saying `const a := b + 1` means that `a` always equals `b+1` no matter the value of b. The shorthand `def` is the same as `const <variable-name> :=` so `def a {b+1}` is the same as `const a := b + 1` but is syntactically better when declaring function like variables.

Declarations can also be marked as pure so that they can be used in pure scopes, this is done by prefixing the declaration with `pure`.


```
var variableA = 1
const variableB := variableA
variableA = 2

.: variableB == 2
```

___

### `--` (decrement) {#op-decrement}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![pure](https://img.shields.io/badge/function-pure-green.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`'--' <expression>`**{: style="font-size: 60%"}



Returns the right-hand-side decremented. Note the right-hand-side is not changed so `--a` does not not decrement `a`, it __returns__ `a` **decremented**

```
10++ <=> 11
var unchanged= 1;
unchanged++;
unchanged <-> 1;
```

___

### `default` or `:-` {#op-default}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![pure](https://img.shields.io/badge/function-pure-green.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`<expression> (':-'|'default') <expression>`**{: style="font-size: 60%"}



If the left-hand-side is VOID this returns the right-hand-side, otherwise returns the left-hand-side.

```
void :- "Hello" <=> "Hello"
1 :- "Hello" <=> 1
```

___

### `destroy` or `<|||` {#op-destroy}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![impure](https://img.shields.io/badge/function-impure-blue.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`('<|||'|'destroy') <expression>`**{: style="font-size: 60%"}



destroy

```
```

___

### `/` (divide) {#op-divide}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![pure](https://img.shields.io/badge/function-pure-green.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`<expression> '/' <expression>`**{: style="font-size: 60%"}



Divides one value by another.

```
 .: DATE() / "1.0" is Decimal
 5 / 4 <=> 1
 5.0 /4 <=> 1.25
 .: 1 / 1 is Integer
 .: 1 / 1.0 is Decimal
 .: 2.0 / 1 is Decimal
 .: 2.0 / 1.0 is Decimal
```

___

### `drain` or `<-<` {#op-drain}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![impure](https://img.shields.io/badge/function-impure-blue.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`('<-<'|'drain') <expression>`**{: style="font-size: 60%"}



Drain an expression, using a URI of all it's data. This is a complete destructive read.


```
```

___

### `each` or `=>>` {#op-each}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![pure](https://img.shields.io/badge/function-pure-green.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`<expression> ('=>>'|'each') <expression>`**{: style="font-size: 60%"}



Eache will iterate over a collection and pass each value (passed in as `$1`) to the second argument.


```
var posts = << https://jsonplaceholder.typicode.com/posts
var titles = posts each { $1.title }
@@ titles
```

___

### `else` {#op-else}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![pure](https://img.shields.io/badge/function-pure-green.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`<expression> 'else' <expression>`**{: style="font-size: 60%"}



The `else` operator is a binary operator which evaluates the left-hand-side (usually the result of an `if` statement), if that has a value of false then the right-hand-side is evaluated and it's result returned, otherwise it returns the left-hand-side.


```
var a=5
//Parenthesis added for clarity, not required.
var b= if (a == 1) "one" else if (a == 2) "two" else "more than two"
.: b == "more than two"
```

___

### `==` (equal) {#op-equal}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![pure](https://img.shields.io/badge/function-pure-green.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`<expression> '==' <expression>`**{: style="font-size: 60%"}



Compares two values to see if they are equal. Works with all types and maps to the Java .equals() method.


```
.: 1 == 1
.: "Hello" == "Hello"
```

___

### `err` or `!?` {#op-err}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![impure](https://img.shields.io/badge/function-impure-blue.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`('!?'|'err') <expression>`**{: style="font-size: 60%"}



Sends the result of the right-hand-side to `stderr`.

```
!? "What happened"
```

___

### `error` or `?->` {#op-error}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![pure](https://img.shields.io/badge/function-pure-green.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`('?->'|'error') <expression>`**{: style="font-size: 60%"}



The right-hand-side is executed if an error occurs in the current scope.

```
var errorHappened= false
error { err msg; errorHappened= true }
def redis ("redisx://localhost:999999/test" as URI)
write ("Hello World " + DATE()) to redis
.: errorHappened
```

___

### `fix` or `&` {#op-fix}

![non-reactive](https://img.shields.io/badge/reactivity-fixed-blue.svg?style=flat-square) ![pure](https://img.shields.io/badge/function-pure-green.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`('&'|'fix') <expression>`**{: style="font-size: 60%"}



Converts a reactive expression into a fixed value. It fixes the value at the point the fix operator is executed. No reactive events will be passed from the right-hand-side expression.

```
var reactiveValue= 1
// The following line would fail if we used .: reactiveValue == 1
// when the 'reactiveValue= 2' statement executes
.: &reactiveValue == 1
reactiveValue= 2
```

___

### `for` {#op-for}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![pure](https://img.shields.io/badge/function-pure-green.svg?style=flat-square) ![New Scope](https://img.shields.io/badge/scope-new-blue.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`for <variable-name> <iterable-expression> <expression>`**{: style="font-size: 60%"}



The for operator, this will iterate over a set of values and assign the specified variable to the current value when evaluating the expression.


```
for i in [1..10] {
    @@ i
}
```

___

### `fork` or `-<` {#op-fork}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![impure](https://img.shields.io/badge/function-impure-blue.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`('-<'|'fork') <expression>`**{: style="font-size: 60%"}



Executes the right-hand-side in a seperate thread returning a 'future'. Any attempt to make use of the returned value from this operator will block until that thread finishes.

```
def sleepTime {@@ "Background Sleeping "+TIME();SLEEP(4 S); @@ "Background Finished Sleeping "+TIME();fix (TIME())}
//Any future reference to c will block until c has completed evaluation
var forkId= fork sleepTime
@@ "Main thread sleeping ..."
SLEEP(2 S)
@@ "Main thread finished sleeping ..."
var d= TIME()
var forkResult= $(forkId);
@@ forkResult
@@ d
.: forkResult is Integer
.: forkResult > d
```

___

### `>` (greater-than) {#op-greater-than}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![pure](https://img.shields.io/badge/function-pure-green.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`<expression> '>' <expression>`**{: style="font-size: 60%"}



The standard `>` operator, it uses Comparable#compareTo and will work with any Dollar data type, including strings, ranges, lists etc.

```
.: 3 > 2
```

___

### `>=` (greater-than-equal) {#op-greater-than-equal}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![pure](https://img.shields.io/badge/function-pure-green.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`<expression> '>=' <expression>`**{: style="font-size: 60%"}



The standard `>=` operator, it uses Comparable#compareTo and will work with any Dollar data type, including strings, ranges, lists etc.

```
.: 2 >= 2
.: 2 >= 1
.: ! (2>=3)
```

___

### `if` {#op-if}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![pure](https://img.shields.io/badge/function-pure-green.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`<expression> 'if' <expression>`**{: style="font-size: 60%"}



Dollar does not have the concept of statements and expressions, just expressions. This means that you can use control flow in an expression such as:

```dollar

var a=1
var b= if a==1 2 else 3
b <=> 2

```

The `if` operator is separate from the `else` operator, it simply evaluates the condition supplied as the first argument. If that value is boolean and true it evaluates the second argument and returns it's value; otherwise it returns boolean false.

The `else` operator is a binary operator which evaluates the left-hand-side (usually the result of an `if` statement), if that has a value of false then the right-hand-side is evaluated and it's result returned, otherwise it returns the left-hand-side.

The combined effect of these two operators is to provide the usual if/else/else if/ control flow



```
var a=5
//Parenthesis added for clarity, not required.
var b= if (a == 1) "one" else if (a == 2) "two" else "more than two"
.: b == "more than two"
```

___

### `in` or `€` {#op-in}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![pure](https://img.shields.io/badge/function-pure-green.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`<expression> ('€'|'in') <expression>`**{: style="font-size: 60%"}



Returns true if the left-hand-side expression is contained in the right-hand-side expression.


```
.: "red" in ["red","blue","green"]
.: ! (1 in ["red","blue","green"] )
```

___

### `++` (increment) {#op-increment}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![pure](https://img.shields.io/badge/function-pure-green.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`'++' <expression>`**{: style="font-size: 60%"}



Returns the right-hand-side incremented. Note the right-hand-side is not changed so `--a` does not not decrement `a`, it __returns__ `a` **incremented**

```
10-- <=> 9
var unchanged= 1;
unchanged--;
unchanged <-> 1;
```

___

### `is` {#op-is}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![pure](https://img.shields.io/badge/function-pure-green.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`<expression> 'is' <expression>`**{: style="font-size: 60%"}



A boolean operator that returns true if the left-hand-side variable is one of the types listed on the right-hand-side. Analogous to Java's `instanceof`.


```
.: 1 is Integer,Decimal
.: "Hello" is String
.: 1.0 is Decimal,String
```

___

### `<` (less-than) {#op-less-than}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![pure](https://img.shields.io/badge/function-pure-green.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`<expression> '<' <expression>`**{: style="font-size: 60%"}



The standard `<` operator, it uses Comparable#compareTo and will work with any Dollar data type, including strings, ranges, lists etc.

```
.: 2 < 3
.: "a" < "b"
```

___

### `<=` (less-than-equal) {#op-less-than-equal}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![pure](https://img.shields.io/badge/function-pure-green.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`<expression> '<=' <expression>`**{: style="font-size: 60%"}



The standard `<=` operator, it uses Comparable#compareTo and will work with any Dollar data type, including strings, ranges, lists etc.

```
.: 2 <= 3
.: 2 <= 2
.: "a" < "b"
.: "a" <= "a"
```

___

### list {#op-list}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![impure](https://img.shields.io/badge/function-impure-blue.svg?style=flat-square) ![New Scope](https://img.shields.io/badge/scope-new%20with%20closure-green.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`'[' ( <expression> ',' ) * [ <expression> ] ']'`**{: style="font-size: 60%"}




Dollar's lists are pretty similar to JavaScript arrays. They are defined using the `[1,2,3]` style syntax and accessed using the `x[y]` subscript syntax. You can count the size of the list using the size operator `#`. Dollar maps are also associative arrays (like JavaScript) allowing you to request members from them using the list subscript syntax `x[y]` or the member syntax `.`.


```
[1,2,3][1] <=> 2
#[1,2,3,4] <=> 4

.: [1,2,3] + 4 == [1,2,3,4];
.: [1,2,3,4] - 4 == [1,2,3];
.: [] + 1 == [1] ;
.: [1] + [1] == [1,1];
.: [1] + 1 == [1,1];

[1..3][-1] <=> 3
[1..3]<- <=> [3..1] //reverse
[1..3][/] <=> [1,2,3] //split
[1,2,3][<] <=> 1 //min
[1,2,3][>] <=> 3 //max
[1,2,3][+] <=> 6 //sum
[1,2,3][%] <=> 2 //avg
[1,2,3][*] <=> 6 //product
->[3,1,2] <=> [1,2,3] //sort
->[3,1,2]<- <=> [3,2,1] //sort then reverse

```

___

### map {#op-map}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![impure](https://img.shields.io/badge/function-impure-blue.svg?style=flat-square) ![New Scope](https://img.shields.io/badge/scope-new%20with%20closure-green.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`'{' ( <expression> ',' ) * [ <expression> ] '}'`**{: style="font-size: 60%"}





```
```

___

### `max` or `[>]` {#op-max}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![pure](https://img.shields.io/badge/function-pure-green.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`<expression> ('[>]'|'max')`**{: style="font-size: 60%"}





```
```

___

### `.` (member) {#op-member}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![pure](https://img.shields.io/badge/function-pure-green.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`<expression> '.' <expression>`**{: style="font-size: 60%"}



The membership or `.` operator accesses the member of a map by it's key.

```
{"key1":1,"key2":2} ["key"+1] <=> 1
{"key1":1,"key2":2}.key2 <=> 2
{"key1":1,"key2":2}[1].key2 <=> 2
```

___

### `min` or `[<]` {#op-min}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![pure](https://img.shields.io/badge/function-pure-green.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`<expression> ('[<]'|'min')`**{: style="font-size: 60%"}





```
```

___

### `-` (minus) {#op-minus}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![pure](https://img.shields.io/badge/function-pure-green.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`<expression> '-' <expression>`**{: style="font-size: 60%"}



Deducts a value from another value

```
2 - 1 <=> 1
```

___

### `module` {#op-module}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![impure](https://img.shields.io/badge/function-impure-blue.svg?style=flat-square) ![New Scope](https://img.shields.io/badge/scope-new-blue.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`module <name> (<parameter>)*`**{: style="font-size: 60%"}





```
```

___

### `%` (modulus) {#op-modulus}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![pure](https://img.shields.io/badge/function-pure-green.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`<expression> '%' <expression>`**{: style="font-size: 60%"}



Returns the remainder (modulus) of the division of the left-hand-side by the right-hand-side.

```
5 % 4 <=> 1
.: 1 % 1 is Integer
.: 1 % 1.0 is Decimal
.: 2.0 % 1 is Decimal
.: 2.0 % 1.0 is Decimal
```

___

### `*` (multiply) {#op-multiply}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![pure](https://img.shields.io/badge/function-pure-green.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`<expression> '*' <expression>`**{: style="font-size: 60%"}



Returns the product of two values. If the left-hand-side is scalar (non collection) then a straightforward multiplication will take place. If the left-hand-side is a collection and it is multiplied by `n`, e.g. `{a=a+1} * 3` it will be added (`+`) to itself `n` times i.e. `{a=a+1} + {a=a+1} + {a=a+1}`.

```
2 * 5 <=> 10

.: 1 * 1 is Integer
.: 1 * 1.0 is Decimal
.: 2.0 * 1 is Decimal
.: 2.0 * 1.0 is Decimal
.: DATE() * 10 is Decimal

```

___

### `-` (negate) {#op-negate}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![pure](https://img.shields.io/badge/function-pure-green.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`'-' <expression>`**{: style="font-size: 60%"}



Negates a value.

```
 .: -1 < 0
```

___

### `new` {#op-new}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![impure](https://img.shields.io/badge/function-impure-blue.svg?style=flat-square) ![New Scope](https://img.shields.io/badge/scope-new-blue.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`'new' <identifier> (<parameters>)`**{: style="font-size: 60%"}





```
```

___

### `not` or `!` {#op-not}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![pure](https://img.shields.io/badge/function-pure-green.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`('!'|'not') <expression>`**{: style="font-size: 60%"}



Returns the logical negation of the right-hand-side expression.


```
not( true ) <=> false
!true <=> false
```

___

### `!=` (not-equal) {#op-not-equal}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![pure](https://img.shields.io/badge/function-pure-green.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`<expression> '!=' <expression>`**{: style="font-size: 60%"}



Returns true if the two expression are not equal.


```
.: 1 != 2
```

___

### `or` or `||` {#op-or}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![pure](https://img.shields.io/badge/function-pure-green.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`<expression> ('||'|'or') <expression>`**{: style="font-size: 60%"}



Returns the logical 'or' of two expressions, e.g. `a || b`. Just like in Java it will shortcut, so that if the left-hand-side is true the right-hand-side is never evaluated.

```
true or false <=> true
true || false <=> true
false || false <=> false
```

___

### `:` (pair) {#op-pair}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![pure](https://img.shields.io/badge/function-pure-green.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`<expression> ':' <expression>`**{: style="font-size: 60%"}



Creates a pair (or tuple of 2 values) of values with a key and a value.


```
var pair1 = "first" : "Hello ";
var pair2 = "second" : "World";

.: pair1 + pair2 == {"first":"Hello ","second":"World"}
```

___

### `parallel` or `|:|` {#op-parallel}

![non-reactive](https://img.shields.io/badge/reactivity-fixed-blue.svg?style=flat-square) ![pure](https://img.shields.io/badge/function-pure-green.svg?style=flat-square) ![New Parallel Scope](https://img.shields.io/badge/scope-new%20parallel-red.svg?style=flat-square) ![Parallel Execution](https://img.shields.io/badge/order-parallel-blue.svg?style=flat-square)

**`('|:|'|'parallel') <expression>`**{: style="font-size: 60%"}



Causes the right-hand-side expression to be evaluated in parallel, most useful in conjunction with list blocks.

```
const testListS := fix [ TIME(), {SLEEP(4 S); TIME();},  TIME() ];
const testListP := fix |:|  [ TIME(), {SLEEP(4 S); TIME();},  TIME() ];
var a= testListS("serial: ") ;
var b= testListP("parallel: ") ;

@@"a="+a
@@"b="+b
//Test different execution orders
.: a[0] is Integer
.: a[1] >= a[0]
.: a[2] >= a[1]
.: b[2] <= b[1]
```

___

### parameter {#op-parameter}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![pure](https://img.shields.io/badge/function-pure-green.svg?style=flat-square) ![New Scope](https://img.shields.io/badge/scope-new%20with%20closure-green.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`( <expression> | <builtin-name> | <function-name> ) '(' ( <expression> | <name> '=' <expression> )* ')'`**{: style="font-size: 60%"}




In most programming languages you have the concept of functions and parameters, i.e. you can parametrized blocks of code. In Dollar you can parameterize *anything*. For example let's just take a simple expression that adds two strings together, in reverse order, and pass in two parameters.

```dollar
($2 + " " + $1)("Hello", "World") <=> "World Hello"

```

The naming of positional parameters is the same as in shell scripts.

By combining with the `def` decleration we create functions, such as:

```dollar

def testParams($2 + " " + $1)
testParams ("Hello", "World") <=> "World Hello"

```
Yep we built a function just by naming an expression. You can name anything and parametrise it - including maps, lists, blocks and plain old expressions.


Named parameters are also supported. 




```
($2 + " " + $1)("Hello", "World") <=> "World Hello"

var outer=10;
const scopedArray := [$1,outer,{var inner=20;inner}]

@@ scopedArray(5)[0]
scopedArray(5)[0] <=> 5;
@@ scopedArray(5)[1]
scopedArray(5)[1] <=> 10;
scopedArray(5)[2]() <=> 20;

const testParams := (last + " " + first)
testParams(first="Hello", last="World") <=> "World Hello"

def func($2 + " " + $1)
func ("Hello", "World") <=> "World Hello"
```

___

### `pause` or `||>` {#op-pause}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![impure](https://img.shields.io/badge/function-impure-blue.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`('||>'|'pause') <expression>`**{: style="font-size: 60%"}



Pauses a service described typically by a URI.

```
```

___

### `|` (pipe) {#op-pipe}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![pure](https://img.shields.io/badge/function-pure-green.svg?style=flat-square) ![New Scope](https://img.shields.io/badge/scope-new-blue.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`<expression> '|' <expression>`**{: style="font-size: 60%"}



The Pipe operator exists to improve method chaining and is used in the form `funcA() | funcB` where the first expression is evaluated and then the result is passed to the second function and can be chained such as `funcA() | funcB | funcC`.

```
def funcA {
    $1 + 10
}


def funcB {
    $1 - 10
}

10 | funcA | funcA <=> 30
10 | funcA | funcB <=> 10
```

___

### `+` (plus) {#op-plus}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![pure](https://img.shields.io/badge/function-pure-green.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`<expression> '+' <expression>`**{: style="font-size: 60%"}



Appends or adds two values.

```
var pair1 = "first" : "Hello ";
var pair2 = "second" : "World";
.: pair1 + pair2 == {"first":"Hello ","second":"World"}

.: [1,2,3] + 4 == [1,2,3,4];
.: [1,2,3,4] - 4 == [1,2,3];
.: [] + 1 == [1] ;
.: [1] + [1] == [1,1];
.: [1] + 1 == [1,1];

.: (1 + 1.0) is Decimal
.: (1.0 + 1.0) is Decimal
.: (1.0 + 1) is Decimal
.: (1 + 1) is Integer
```

___

### `print` or `@@` {#op-print}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![impure](https://img.shields.io/badge/function-impure-blue.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`('@@'|'print') <expression>`**{: style="font-size: 60%"}



Sends the right-hand-side expression to stdout.

```
@@ "Hello"
print "World"
```

___

### `product` or `[*]` {#op-product}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![pure](https://img.shields.io/badge/function-pure-green.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`<expression> ('[*]'|'product')`**{: style="font-size: 60%"}





```
```

___

### `publish` or `*>` {#op-publish}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![impure](https://img.shields.io/badge/function-impure-blue.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`<expression> ('*>'|'publish') <expression>`**{: style="font-size: 60%"}



publish

```
```

___

### `pure` {#op-pure}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![pure](https://img.shields.io/badge/function-pure-green.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`'pure' <expression>`**{: style="font-size: 60%"}




Support for some aspects of functional programming are included in Dollar. Primarily the idea of pure expressions using the `pure` operator. This signals that an expression or declaration is a pure expression or declaration.

In the example we're declaring reverse to be an expression that reverses two values from a supplied array. Because we declare it as `pure` the expression supplied must also be `pure`. To understand what a pure function is please see [http://en.wikipedia.org/wiki/Pure_function](http://en.wikipedia.org/wiki/Pure_function). Basically it prohibits the reading of external state or the setting of external state. We next swap `[2,1]` within a newly created pure expression, which is subsequently assigned to a. If reverse had not been declared pure it would not be allowed within the pure expression.


Note some builtin functions are not themselves pure and will trigger parser errors if you attempt to use them in a pure expression. Take DATE() for example which supplies an external state (the computers clock).


```
pure def reverse [$1[1],$1[0]]

var a= pure {
 reverse([2,1])
}
```

___

### `..` (range) {#op-range}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![pure](https://img.shields.io/badge/function-pure-green.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`<expression> '..' <expression>`**{: style="font-size: 60%"}



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


```
//Types
.: (1..5) is Range
.: [1..5) is Range
.: [1..5] is Range
.: (1..5] is Range
.: (..5] is Range //  less than or equal to 5
.: (5..] is Range //  greater than 5
.: (..) is Range //   all numbers



//Bounds
.: ! (1 in (1..5))
.:  (1 in [1..5))
.: 3 in (1..5)
.: !(5 in (1..5))
.: 5 in (1..5]
.: !(0 in (1..5))
.: !(6 in (1..5))

//count
# (1..1] <=> void
# [1..1] <=> 1
# [1..1) <=> void


```

___

### `read` {#op-read}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![impure](https://img.shields.io/badge/function-impure-blue.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`'read' ['block'] ['mutate'] ['from'] <expression>`**{: style="font-size: 60%"}



The read operator is used to read data from an expression typically a URI. It has options to read-and-block (`block`) as well as read-and-destroy (`mutate`). Note that the `from` keyword is just syntax sugar and can be omitted. 

```
```

___

### `<<` (read-simple) {#op-read-simple}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![impure](https://img.shields.io/badge/function-impure-blue.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`'<<' <expression>`**{: style="font-size: 60%"}



Performs a simple read from another data item, typically this is used with a URI.

```
```

___

### `reduce` or `>>=` {#op-reduce}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![pure](https://img.shields.io/badge/function-pure-green.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`<expression> ('>>='|'reduce') <expression>`**{: style="font-size: 60%"}



Performs a reduction on the elements of the left-hand-side expression, using an
     associative accumulation function, and returns the reduced value,
     if any. This is equivalent to Java code of
     
```java
 boolean foundAny = false;
 T result = null;
 for (T element : this stream) {
     if (!foundAny) {
         foundAny = true;
         result = element;
     }
     else
         result = accumulator.apply(result, element);
 }
 return foundAny ? Optional.of(result) : Optional.empty();

```
     
but is not constrained to execute sequentially.

The rhs function/expression must be an associative function.



```
[1,2,3,4,5] reduce ($1 + $2)
```

___

### `reversed` or `<-` {#op-reversed}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![pure](https://img.shields.io/badge/function-pure-green.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`<expression> ('<-'|'reversed')`**{: style="font-size: 60%"}





```
```

___

### script {#op-script}

![non-reactive](https://img.shields.io/badge/reactivity-fixed-blue.svg?style=flat-square) ![impure](https://img.shields.io/badge/function-impure-blue.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**` <language-name> ``<script-code>`` `**{: style="font-size: 60%"}




Hopefully you'll find Dollar a useful and productive language, but there will be many times when you just want to quickly nip out to a bit of another language. To do so, just surround the code in backticks and prefix with the languages name. Currently only `java` is supported but more will be added soon.


```
const variableA="Hello World"

//Now we parametrize the script. The parameters are available in a list of 'var' objects (see the dollar-core docs)

const javaWithParam = java`
out=in.get(0).$multiply(in.get(1)).$multiply(in.get(2));
` (10,20,30)

javaWithParam <=> 10*20*30
```

___

### `serial` or `|..|` {#op-serial}

![non-reactive](https://img.shields.io/badge/reactivity-fixed-blue.svg?style=flat-square) ![pure](https://img.shields.io/badge/function-pure-green.svg?style=flat-square) ![New Serial Scope](https://img.shields.io/badge/scope-new%20serial-yellow.svg?style=flat-square) ![Serial Execution](https://img.shields.io/badge/order-serial-green.svg?style=flat-square)

**`('|..|'|'serial') <expression>`**{: style="font-size: 60%"}



Causes the right-hand-side expression to be evaluated in serial, most useful in conjunction with list blocks.

```
```

___

### `#` (size) {#op-size}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![pure](https://img.shields.io/badge/function-pure-green.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`'#' <expression>`**{: style="font-size: 60%"}



Returns the size of non-scalar types or the length of a string.

```
```

___

### `sorted` or `->` {#op-sorted}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![pure](https://img.shields.io/badge/function-pure-green.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`('->'|'sorted') <expression>`**{: style="font-size: 60%"}





```
```

___

### `split` or `[/]` {#op-split}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![pure](https://img.shields.io/badge/function-pure-green.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`<expression> ('[/]'|'split')`**{: style="font-size: 60%"}





```
```

___

### `start` or `|>` {#op-start}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![pure](https://img.shields.io/badge/function-pure-green.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`('|>'|'start') <expression>`**{: style="font-size: 60%"}



Starts a service described typically by a URI.

```
```

___

### `state` or `<|>` {#op-state}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![impure](https://img.shields.io/badge/function-impure-blue.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`('<|>'|'state') <expression>`**{: style="font-size: 60%"}



Returns the state of a service described typically by a URI.

```
```

___

### `stop` or `<|` {#op-stop}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![impure](https://img.shields.io/badge/function-impure-blue.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`('<|'|'stop') <expression>`**{: style="font-size: 60%"}



Stops a service described typically by a URI.

```
```

___

### `subscribe` or `<*` {#op-subscribe}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![impure](https://img.shields.io/badge/function-impure-blue.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`<expression> ('<*'|'subscribe') <expression>`**{: style="font-size: 60%"}



subscribe

```
```

___

### `*=` (subscribe-assign) {#op-subscribe-assign}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![pure](https://img.shields.io/badge/function-pure-green.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)



subscribe-assign

```
```

___

### subscript {#op-subscript}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![pure](https://img.shields.io/badge/function-pure-green.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`( <expression> '[' <index-expression>|<key-expression> ']' ) | ( <expression> '.' (<index-expression>|<key-expression>) )`**{: style="font-size: 60%"}



subscript operator

```
{"key1":1,"key2":2} ["key"+1] <=> 1
{"key1":1,"key2":2}.key2 <=> 2
{"key1":1,"key2":2}[1].key2 <=> 2
```

___

### `sum` or `[+]` {#op-sum}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![pure](https://img.shields.io/badge/function-pure-green.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`<expression> ('[+]'|'sum')`**{: style="font-size: 60%"}





```
```

___

### `~` (truthy) {#op-truthy}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![pure](https://img.shields.io/badge/function-pure-green.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`'~' <expression>`**{: style="font-size: 60%"}



The truthy operator `~` converts any value to a boolean by applying the rule that: void is false, 0 is false, "" is false, empty list is false, empty map is false - all else is true.

```
```

___

### `unique` or `[!]` {#op-unique}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![pure](https://img.shields.io/badge/function-pure-green.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`<expression> ('[!]'|'unique')`**{: style="font-size: 60%"}





```
```

___

### unit {#op-unit}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![impure](https://img.shields.io/badge/function-impure-blue.svg?style=flat-square) ![New Scope](https://img.shields.io/badge/scope-new-blue.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`<numeric> <unit-name>`**{: style="font-size: 60%"}





```
```

___

### `unpause` or `<||` {#op-unpause}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![impure](https://img.shields.io/badge/function-impure-blue.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`('<||'|'unpause') <expression>`**{: style="font-size: 60%"}



Un-pauses a service described typically by a URI.

```
```

___

### `when` or `?` {#op-when}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![pure](https://img.shields.io/badge/function-pure-green.svg?style=flat-square) ![New Scope](https://img.shields.io/badge/scope-new-blue.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`<expression> ('?'|'when') <expression>`**{: style="font-size: 60%"}





```
var c=1
var d=1

//When c is greater than 3 assign it's value to d
c > 3 ? (d= c)

&c <=> 1; &d <=> 1
c=2; &c <=> 2; &d <=> 1
c=5 ; &c <=> 5 ; &d <=> 5

//Note alternative syntax is when <condition> <expression>
when c > 3 { @@ c}
```

___

### when-assign {#op-when-assign}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![pure](https://img.shields.io/badge/function-pure-green.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`('var'|'volatile') [<type-assertion>] <variable-name> '?' <condition-expression> '='<assignment-expression>`**{: style="font-size: 60%"}



The 'when assign' operator assigns updates a variable to the assignment expression whenever the 'condition expression' changes and is true.


```
var h=1
var i ? (h < 3) = (h + 2)
h=4
i is VOID
h=2
i <=>4
```

___

### `while` {#op-while}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![pure](https://img.shields.io/badge/function-pure-green.svg?style=flat-square) ![New Scope](https://img.shields.io/badge/scope-new-blue.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`while <condition> <expression>`**{: style="font-size: 60%"}



while operator

```
```

___

### `window` {#op-window}

![non-reactive](https://img.shields.io/badge/reactivity-fixed-blue.svg?style=flat-square) ![pure](https://img.shields.io/badge/function-pure-green.svg?style=flat-square) ![New Scope](https://img.shields.io/badge/scope-new-blue.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`window <expression> 'over' <duration-expression> [ 'period' <duration-expression> ] [ 'unless' <expression> ] [ 'until' <expression> ]  <window-expression>`**{: style="font-size: 60%"}



The window operator provides a time window based view of a changing value.

The first part of the window operator is the expression this will be listened to for changes and it's value passed into the windowing process. 

The next part is the `over` clause which determines the duration over which changes are captured. Any change that is older than this duration is discarded.

Following this is the optional `period` clause which determines how often the the window is calculated and the window-expression evaluated. If it is not supplied it defaults to the value in the `over` clause.

Next is the optional `unless` clause which specifies which changes to ignore, if this clause is true then the change will be ignored.

Then the optional `until` clause which specifies a condition for when the windowing should stop completely.

Finally the window-expression is the expression which is evaluated with the following variables available:

* `count` a value that increments on every window-expression evaluation
* `collected` a list of values that have been windowed across the duration of the `over` clause and which have not been excluded by the `unless` clause. 


```
var a= 1;
volatile collectedValues= void;
window (a) over (10 S) period (5 S) unless (a == 5)  until (a == 29) {
        @@collected
        collectedValues= collected;
}

for i in [1..32] {
    SLEEP (1 S)
    a=a+1
}


.: #collectedValues > 0
collectedValues <-> [21,22,23,24,25,26,27,28,29]
@@ collectedValues
```

___

### `write` {#op-write}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![impure](https://img.shields.io/badge/function-impure-blue.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`'write' ['block'] ['mutate'] ['to'] <expression>`**{: style="font-size: 60%"}



The write operator is used to write data to an expression typically a URI. It has options to write-and-block (`block`) as well as write-and-destroy (`mutate`). Note that the `to` keyword is just syntax sugar and can be omitted. 

```
```

___

### `>>` (write-simple) {#op-write-simple}

![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square) ![impure](https://img.shields.io/badge/function-impure-blue.svg?style=flat-square) ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square) ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)

**`<expression> '>>' <expression>`**{: style="font-size: 60%"}



Performs a simple write to another data item, mostly used to write to a URI. 

```
```

___

## Appendix B - Keywords
### as


### block


### const

Mark a variable definition as a constant, i.e. readonly.



### def


### every


### export

Export a variable at the point of definition.



### false

Boolean false.



### for


### from


### infinity


### is


### mutate


### no

Boolean false.



### null

A NULL value of ANY type.



### over


### period


### pure

The start of a pure expression.



### to


### true

Boolean true.



### unless


### until


### var

Marks a variable as variable i.e. not readonly.



### void

A VOID value.



### volatile

Marks a variable as volatile, i.e. it can be accessed by multiple threads.



### with


### yes

Boolean true.



## Appendix C - Reserved Keywords, Operators and Symbols
### Keywords

The following keywords are reserved:

> abstract, await, break, case, catch, closure, continue, dispatch, do, dump, emit, enum, extends, fail, filter, final, finally, float, goto, implements, import, impure, include, instanceof, interface, join, lambda, load, measure, native, package, pluripotent, private, protected, public, readonly, return, save, scope, send, short, static, super, switch, synchronized, this, throw, throws, trace, transient, try, unit, variant, varies, vary, wait

### Operators

The following operator keywords are reserved:

> 

The following operator symbols are reserved:

> `&=, &>, +>, ->, -_-, ..., ::, <$, <&, <+, <++, <-, <=<, <?, >&, >->, ?$?, ?..?, ?:, ?>, @, @>, |* `

### Symbols

The following general symbols are reserved:

> ` `

## Appendix D - Operator Precedence

All operators by precedence, highest precedence ([associativity](https://en.wikipedia.org/wiki/Operator_associativity)) first.

|Name                          |Keyword        |Operator  |Type      |
|-------                       |-------        |-------   |-------   |
|[fix](#op-fix)                |`fix`          | `&`      |prefix    |
|[sorted](#op-sorted)          |`sorted`       | `->`     |prefix    |
|[range](#op-range)            |               | `..`     |binary    |
|[default](#op-default)        |`default`      | `:-`     |binary    |
|[member](#op-member)          |               | `.`      |binary    |
|[parameter](#op-parameter)    |               |          |postfix   |
|[subscript](#op-subscript)    |               |          |postfix   |
|[cast](#op-cast)              |               |          |postfix   |
|[decrement](#op-decrement)    |               | `--`     |prefix    |
|[for](#op-for)                |`for`          |          |control   |
|[in](#op-in)                  |`in`           | `€`      |binary    |
|[increment](#op-increment)    |               | `++`     |prefix    |
|[not](#op-not)                |`not`          | `!`      |prefix    |
|[reversed](#op-reversed)      |`reversed`     | `<-`     |postfix   |
|[size](#op-size)              |               | `#`      |prefix    |
|[truthy](#op-truthy)          |               | `~`      |prefix    |
|[while](#op-while)            |`while`        |          |control   |
|[divide](#op-divide)          |               | `/`      |binary    |
|[each](#op-each)              |`each`         | `=>>`    |binary    |
|[modulus](#op-modulus)        |               | `%`      |binary    |
|[multiply](#op-multiply)      |               | `*`      |binary    |
|[reduce](#op-reduce)          |`reduce`       | `>>=`    |binary    |
|[avg](#op-avg)                |`avg`          | `[%]`    |postfix   |
|[max](#op-max)                |`max`          | `[>]`    |postfix   |
|[min](#op-min)                |`min`          | `[<]`    |postfix   |
|[minus](#op-minus)            |               | `-`      |binary    |
|[negate](#op-negate)          |               | `-`      |prefix    |
|[plus](#op-plus)              |               | `+`      |binary    |
|[product](#op-product)        |`product`      | `[*]`    |postfix   |
|[split](#op-split)            |`split`        | `[/]`    |postfix   |
|[sum](#op-sum)                |`sum`          | `[+]`    |postfix   |
|[unique](#op-unique)          |`unique`       | `[!]`    |postfix   |
|[greater-than](#op-greater-than)|               | `>`      |binary    |
|[less-than](#op-less-than)    |               | `<`      |binary    |
|[pipe](#op-pipe)              |               | `|`      |binary    |
|[equal](#op-equal)            |               | `==`     |binary    |
|[greater-than-equal](#op-greater-than-equal)|               | `>=`     |binary    |
|[less-than-equal](#op-less-than-equal)|               | `<=`     |binary    |
|[not-equal](#op-not-equal)    |               | `!=`     |binary    |
|[and](#op-and)                |`and`          | `&&`     |binary    |
|[or](#op-or)                  |`or`           | `||`     |binary    |
|[all](#op-all)                |`all`          | `<@`     |prefix    |
|[causes](#op-causes)          |`causes`       | `=>`     |binary    |
|[choose](#op-choose)          |`choose`       | `?*`     |binary    |
|[drain](#op-drain)            |`drain`        | `<-<`    |prefix    |
|[publish](#op-publish)        |`publish`      | `*>`     |binary    |
|[read](#op-read)              |`read`         |          |prefix    |
|[read-simple](#op-read-simple)|               | `<<`     |prefix    |
|[subscribe](#op-subscribe)    |`subscribe`    | `<*`     |binary    |
|[when](#op-when)              |`when`         | `?`      |binary    |
|[write](#op-write)            |`write`        |          |control   |
|[write-simple](#op-write-simple)|               | `>>`     |binary    |
|[pair](#op-pair)              |               | `:`      |binary    |
|[create](#op-create)          |`create`       | `|||>`   |prefix    |
|[destroy](#op-destroy)        |`destroy`      | `<|||`   |prefix    |
|[else](#op-else)              |`else`         |          |binary    |
|[fork](#op-fork)              |`fork`         | `-<`     |prefix    |
|[if](#op-if)                  |`if`           |          |binary    |
|[is](#op-is)                  |`is`           |          |binary    |
|[parallel](#op-parallel)      |`parallel`     | `|:|`    |prefix    |
|[pause](#op-pause)            |`pause`        | `||>`    |prefix    |
|[serial](#op-serial)          |`serial`       | `|..|`   |prefix    |
|[start](#op-start)            |`start`        | `|>`     |prefix    |
|[state](#op-state)            |`state`        | `<|>`    |prefix    |
|[stop](#op-stop)              |`stop`         | `<|`     |prefix    |
|[unpause](#op-unpause)        |`unpause`      | `<||`    |prefix    |
|[assign](#op-assign)          |               | `=`      |assignment|
|[declaration](#op-declaration)|               | `:=`     |assignment|
|[subscribe-assign](#op-subscribe-assign)|               | `*=`     |assignment|
|[when-assign](#op-when-assign)|               |          |assignment|
|[assert](#op-assert)          |`assert`       | `.:`     |prefix    |
|[assert-equals](#op-assert-equals)|               | `<->`    |binary    |
|[assert-equals-reactive](#op-assert-equals-reactive)|               | `<=>`    |binary    |
|[debug](#op-debug)            |`debug`        | `!!`     |prefix    |
|[err](#op-err)                |`err`          | `!?`     |prefix    |
|[error](#op-error)            |`error`        | `?->`    |prefix    |
|[print](#op-print)            |`print`        | `@@`     |prefix    |
|[block](#op-block)            |               |          |collection|
|[builtin](#op-builtin)        |               |          |other     |
|[class](#op-class)            |`class`        |          |other     |
|[collect](#op-collect)        |`collect`      |          |control   |
|[list](#op-list)              |               |          |collection|
|[map](#op-map)                |               |          |collection|
|[module](#op-module)          |`module`       |          |other     |
|[new](#op-new)                |`new`          |          |prefix    |
|[pure](#op-pure)              |`pure`         |          |prefix    |
|[script](#op-script)          |               |          |other     |
|[unit](#op-unit)              |               |          |postfix   |
|[window](#op-window)          |`window`       |          |control   |

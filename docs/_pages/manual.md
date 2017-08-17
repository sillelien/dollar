---
layout: single
title: "Dollar Scripting Language Manual"
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

```dollar

def testParams {$2 + " " + $1}

@@ testParams ("Hello", "World") 

.: testParams ("Hello", "World") == "World Hello"

```

## Understanding the Basics


Dollar has it's own peculiarities, mostly these exists to help with it's major target: serverside integration projects. So it's important to understand the basic concepts before getting started.

### Functional Programming and the 'pure' operator

Support for functional programming is included in Dollar, this will be widened as the language is developed. For now it is provided by the `pure` operator. This signals that an expression or declaration is a pure expression or function.

In this example we're declaring reverse to be an expression that reverses two values from a supplied array. Because we declare it as `pure` the expression supplied must also be `pure`. To understand what a pure function is please see http://en.wikipedia.org/wiki/Pure_function. Basically it prohibits the reading of external state or the setting of external state. We next swap `[2,1]` within a newly created pure expression, which is subsequently assigned to a. If reverse had not been declared pure it would not be allowed within the pure expression.

 ```dollar
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

```dollar

var variableA = 1
const variableB := variableA
variableA = 2

.: variableB == 2
```

In the above example we are declaring (using the declarative operator `:=`) that variableA is current the value 1, we then declare that variableB is the *same as* variableA. So when we change variableA to 2 we also change variableB to 2.

Before we go any further let's clarify `:=` vs `=`, I have chosen to follow the logic [described here](https://math.stackexchange.com/questions/1838678/confused-about-notation-versus-plain-old) so that the `:=` operator is a definition (and by it's nature reactive) and `=` is an assignment ( not reactive and has a fix depth of 1 - more on that later).

This means that `a := b + 1` translates to **a is defined as b + 1** so a is behaving reactively, changes to b cause a change in the value of a. It also means that `a = b + 1` simply assigns `b + 1` to the variable a, changes to b do not cause changes to a. 

At this point it's time to introduce a what is arguably a cleaner and easier to understand short hand for 'const reactiveVar := {...}` the short hand is 'def reactiveVar {...}' such as:

```dollar
    def myFunction { @@ "Hello World"}
```

The `def` keyword implies `const` and it also does not allow dynamic variable names (more on that later). A rule of thumb is if you'd like to have something act like a function use `def`.


**TL;DR `=` behaves like it's Java equivalent, `:=` doesnt't and use `def` to create functions.**

> The assertion operator `.:` will throw an assertion error if the value following is either non boolean or not true.


Now let's throw in the causes operator :

```dollar

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

```dollar

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

```dollar
var b=1
@@b
b=2
```

Outputs 1 then 2 because @@ heard the change to b and re output the new value. Often this is what you want, however if you don't just add the fix operator `&` before the value. That will stop reactive behaviour.

```dollar
var b=1
@@ &b
b=2
```




### Assignment

Obviously the declarative/reactive behavior is fantastic for templating, eventing, creating lambda style expressions etc. however there are times when we want to simply assign a value and perform a single action on that value.

```dollar

var variableA = 1
var variableB = variableA
variableA = 2

.: variableB == 1
```

So as you can see when we use the `=` assignment operator we assign the *value* of the right hand side to the variable. Watch what happens when we use expressions.


```dollar

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

```dollar

def lamdaVar  {$1 + 10}
lamdaVar(5) <=> 15

```

> It's important to note that all values in Dollar are immutable - that means if you wish to change the value of a variable you *must* __reassign__ a new value to the variable. For example `v++` would return the value of `v+1` it does not increment v. If however you want to assign a constant value, one that is both immutable and cannot be reassigned, just use the `const` modifier at the variable assignment (this does not make sense for declarations, so is only available on assignments).

```dollar
const MEDIUM = 23
// MEDIUM= 4 would now produce an error
```

So `:=` supports the full reactive behaviour of Dollar, i.e. it is a declaration not a value assignment, and `=` is used to nail down a particular value or reduce the reactive behaviour. Later we'll come across the fix operator `&` which instructs Dollar to fix a value completely . More on that later.

### Blocks

#### Line Block
Dollar supports several block types, the first is the 'line block' a line block lies between `{` and `}` and is separated by either newlines or `;` characters.

```dollar

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

```dollar

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

```dollar

var mapBlock = {
    "Hello",
    "World"
}

mapBlock <=> {"Hello":"Hello", "World":"World"}

const mapBlock2 = { 1, 2}

mapBlock2 <=> {"1":1,"2":2}

```

Map blocks are combined with the pair `:` operator to become useful and create maps/JSON like this:


```dollar

var mapBlock = {
    "first":"Hello ",
   "second":"World"
}

@@ mapBlock

mapBlock.second <=> "World"

```

A map block with one entry that is not a pair is assumed to be a *Line Block*.

The stdout operator `@@` is used to send a value to stdout in it's serialized (JSON) format, so the result of the above would be to output `{"first":"Hello ","second":"World"}` a JSON object created using JSON like syntax. Maps can also be created by joining pairs.

```dollar

var pair1 = "first" : "Hello ";
var pair2 = "second" : "World";

.: pair1 + pair2 == {"first":"Hello ","second":"World"}

```


### Lists

Dollar's lists are pretty similar to JavaScript arrays. They are defined using the `[1,2,3]` style syntax and accessed using the `x[y]` subscript syntax.

```dollar
.: [1,2,3] + 4 == [1,2,3,4];
.: [1,2,3,4] - 4 == [1,2,3];
.: [] + 1 == [1] ;
.: [1] + [1] == [1,1];
.: [1] + 1 == [1,1];

[1,2,3][1] <=> 2

```

> Note we're using the assert equals or `<=>` operator here, this is a combination of `.:` and `==` that will cause an error if the two values are not equal.

You can count the size of the list using the size operator `#`.

```dollar
#[1,2,3,4] <=> 4
```

Dollar maps are also associative arrays (like JavaScript) allowing you to request members from them using the list subscript syntax

```dollar
{"key1":1,"key2":2} ["key"+1] <=> 1
{"key1":1,"key2":2} [1] <=> {"key2":2}
{"key1":1,"key2":2} [1]["key2"] <=> 2
```

As you can see from the example you can request a key/value pair (or Tuple if you like) by it's position using a numeric subscript. Or you can treat it as an associative array and request an entry by specifying the key name. Any expression can be used as a subscript, numerical values will be used as indexes, otherwise the string value will be used as a key.


### Ranges

Dollar (at present) supports numerical and character ranges

```dollar

#("a".."c") <=> 3
(1..3)[1] <=>2

```


### Scopes & Closure

Dollar makes extensive use of code blocks with scope [closure](https://en.wikipedia.org/wiki/Closure_(computer_programming)). Blocks, lists and maps all have scope closure - I suggest reading [this article by Martin Fowler](https://martinfowler.com/bliki/Lambda.html) if you're unfamiliar with closures. Let's start with a simple example:

```dollar
var outer=10;
def func {
    outer;
}
func() <=> 10;
```

In the above example `func` is a block collection which returns `outer`. It has access to `outer` because at the time of decleration outer is in it's parent's lexical scope.

```dollar

def func {
    var inner=10;
    {$1+inner}
}

func()(10) <=> 20;
```

In the above example we now return an anonymous block collection from func which we then paramterize with the value `10`. When `func` is executed it returns the paramterized block, which we then call with `10` and which adds the value `inner` to the parameter (`$1`) - naturally the result is 20.

So all of that looks fairly familiar if you've ever used JavaScript, but remember all of Dollar's collections have scope closure so the following is valid:

```dollar
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

```dollar
var errorHappened= false
error { @@ msg; errorHappened= true }
var a=1/0
.: errorHappened
```

### Logging

Logging is done by the `print`,`debug` and `err` keywords and the `@@`,`!!` and `??` operators.

| Keyword  | Operator |
| -------- | -------- |
| `print`  | `@@`     |
| `debug`  | `!!`     |
| `err`    | `??`     |


```dollar
@@ "I'm a stdout message"
!! "I'm a debug message"
?? "I'm an error message"
```

## Type System
### Intro
Although Dollar is typeless at compile time, it does support basic runtime typing. At present this includes: STRING, INTEGER,DECIMAL, LIST, MAP, URI, VOID, RANGE, BOOLEAN. The value for a type can be checked using the `is` operator:

```dollar
.: "Hello World" is String
.: ["Hello World"] is List
```

### DATE

Dollar supports a decimal date system where each day is 1.0. This means it's possible to add and remove days from a date using simple arithmetic.

```dollar
@@ DATE()
@@ DATE() + 1
@@ DATE() - 1

.: DATE() + "1.0" is String
.: DATE() / "1.0" is Decimal
```

Components of the date can be accessed using the subscript operators:

```dollar
@@ DATE().DAY_OF_WEEK

@@ DATE()['DAY_OF_YEAR']=1
```

Valid values are those from `java.time.temporal.ChronoField`

```
NANO_OF_SECOND, NANO_OF_DAY, MICRO_OF_SECOND, MICRO_OF_DAY, MILLI_OF_SECOND, MILLI_OF_DAY, SECOND_OF_MINUTE, SECOND_OF_DAY, MINUTE_OF_HOUR, MINUTE_OF_DAY, HOUR_OF_AMPM, CLOCK_HOUR_OF_AMPM, HOUR_OF_DAY, CLOCK_HOUR_OF_DAY, AMPM_OF_DAY, DAY_OF_WEEK, ALIGNED_DAY_OF_WEEK_IN_MONTH, ALIGNED_DAY_OF_WEEK_IN_YEAR, DAY_OF_MONTH, DAY_OF_YEAR, EPOCH_DAY, ALIGNED_WEEK_OF_MONTH, ALIGNED_WEEK_OF_YEAR, MONTH_OF_YEAR, PROLEPTIC_MONTH, YEAR_OF_ERA, YEAR, ERA, INSTANT_SECONDS, OFFSET_SECONDS
```

As you can see we can do date arithmetic, but thanks to another Dollar feature anything that can be specified as xxx(i) can also be written i xxx (where i is an integer or decimal and xxx is an identifier). So we can add days hours and seconds to the date.

```dollar
@@ DATE() + 1 Day
@@ DATE() + 1 Hour
@@ DATE() + 1 Sec
```

Those values are built in, but we can easily define them ourselves.

```dollar
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

```dollar
var (it < 100) a = 50
var (it > previous || previous is Void) b = 5
b=6
b=7
var ( it is String) s="String value"
```

The special variables `it` - the current value and `previous` - the previous value, will be available for the constraint.

To build a simple runtime type system simply declare (using `:=`) your type as a boolean expression.

```dollar

//define a pseudo-type
def colorEnum ( it in ["red","green","blue"] )


//Use it as a constraint
var (colorEnum) myColor= "green"

error { @@ msg }

//This fails
var myColor="apple"

```

Of course since the use of `(it is XXXX)` is very common Dollar provides a specific runtime type constraint that can be added in conjunction with other constraints. Simply prefix the assignment or decleration with `<XXXX>` where XXXX is the runtime type.


```dollar
var <string> (#it > 5) s="String value"
```

It is intended that the predictive type system, will be in time combined with runtime types to help spot bugs at compile time.

### Type Coercion
Dollar also supports type coercion, this is done using the `as` operator followed by the type to coerce to.


```dollar
var <string> s= 1 as string
s <=> "1"
```

A few more examples follow.

```dollar
1 as string <=> "1"
1 as boolean <=> true
1 as list <=> [1]
1 as map <=> {"value":1}
1 as VOID <=> void
1 as integer <=> 1

"1" as integer <=> 1
"http://google.com" as uri
"1" as VOID <=> void
"true" as boolean <=> true
"1" as boolean <=> false
"1" as list <=> ["1"]
"1" as map <=> {"value":"1"}
"1" as string <=> "1"

true as string <=> "true"
true as integer <=> 1
true as list <=> [true]
true as map <=> {"value":true}
true as boolean <=> true
true as VOID <=> void


[1,2,3] as string <=> "[ 1, 2, 3 ]"
[1,2,3] as list <=> [1,2,3]
[1,2,3] as boolean <=> true
[1,2,3] as map <=> {"value":[1,2,3]}

{"a":1,"b":2} as string <=> '{"a":1,"b":2}'
{"a":1,"b":2} as list <=> ["a":1,"b":2]
{"a":1,"b":2} as boolean <=> true
{"a":1,"b":2} as VOID <=> void
```


## Imperative Control Flow

With imperative control flow, the control flow operations are only triggered when the block they are contained within is evaluated. I.e. they behave like control flow in imperative languages. So start with these if you're just learning Dollar.

### If

Dollar supports the usual imperative control flow but, unlike some languages, everything is an operator. This is the general rule of Dollar, everything has a value. Dollar does not have the concept of statements and expressions, just expressions. This means that you can use control flow in an expression.

```dollar

var a=1
var b= if a==1 2 else 3
b <=> 2

```

So let's start with the `if` operator. The `if` operator is separate from the `else` operator, it simply evaluates the condition supplied as the first argument. If that value is boolean and true it evaluates the second argument and returns it's value; otherwise it returns boolean false.

The `else` operator is a binary operator which evaluates the left-hand-side (usually the result of an `if` statement), if that has a value of false then the right-hand-side is evaluated and it's result returned, otherwise it returns the left-hand-side.

The combined effect of these two operators is to provide the usual if/else/else if/ control flow

```dollar

var a=5
//Parenthesis added for clarity, not required.
var b= if (a == 1) "one" else if (a == 2) "two" else "more than two"
.: b == "more than two"

```

### For

```dollar

for i in 1..10 {
    @@ i
}

```

### While

```dollar
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

```dollar
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


```dollar

var c=1
var d=1

//When c is greater than 3 assign it's value to d
c > 3 ? (d= c)

&c <=> 1; &d <=> 1
c=2; &c <=> 2; &d <=> 1
c=5 ; &c <=> 5 ; &d <=> 5

```

This is similar to the previous example except that we have to set a value greater than 3 for the action to be taken.

```dollar
//Note alternative syntax is when <condition> <expression>
var c=1
when c > 3 { @@ c}
```

### Collect


The `collect` operator listens for changes in the supplied expression adding all the values to a list until the `until` clause is triggered. It then evaluates the second expression with the values `it` for the current value, `count` for the number of messages **received** since last emission and `collected` for the collected values. The whole operator itself emits `void` unless the collection operation is triggered in which case it emits the collection itself. Values can be skipped with an `unless` clause. Skipped messages increase the count value, so use `#collected` if you want the number of collected values.

```dollar

var e=void

//Length is greater than or equal to 4 unless void
var (#it >= 4 || it is VOID) collectedValues=void

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

```dollar
($2 + " " + $1)("Hello", "World") <=> "World Hello"

```

The naming of positional parameters is the same as in shell scripts.

Now if we take this further we can use the declaration operator `:=` to say that a variable is equal to the expression we wish to parameterise, like so:

```dollar

const testParams := ($2 + " " + $1)
testParams ("Hello", "World") <=> "World Hello"

```

Yep we built a function just by naming an expression. You can name anything and parameterise it - including maps, lists, blocks and plain old expressions.


What about named parameters, that would be nice.

```dollar
const testParams := (last + " " + first)
testParams(first="Hello", last="World") <=> "World Hello"
```

Yep you can use named parameters, then refer to the values by the names passed in.


## Resources &amp; URIs

URIs are first class citizen's in Dollar. They refer to a an arbitrary resource, usually remote, that can be accessed using the specified protocol and location. Static URIs can be referred to directly without quotation marks, dynamic URIs can be built by casting to a uri using the `as` operator.

```dollar
var posts = << https://jsonplaceholder.typicode.com/posts 
var titles = posts each { $1.title }
@@ titles
```

In this example we've requested a single value (using `<<`) from a uri and assigned the value to `posts` then we simply iterate over the results  using `each` and each value (passed in to the scope as `$1`) we extract the `title`. The each operator returns a list of the results and that is what is passed to standard out.


## Using Java

Hopefully you'll find Dollar a useful and productive language, but there will be many times when you just want to quickly nip out to a bit of Java. To do so, just surround the Java in backticks.

```dollar

var variableA="Hello World"

var java = `out=scope.get("variableA");`

java <=> "Hello World"

```

A whole bunch of imports are done for you automatically (see below) but you will have to fully qualify any thirdparty libs.
 
> imports `dollar.lang.*``dollar.internal.runtime.script.api.*` `com.sillelien.dollar.api.*` `java.io.*` `java.math.*` `java.net.*` `java.nio.file.*` `java.util.*` `java.util.concurrent.*` `java.util.function.*` `java.util.prefs.*` `java.util.regex.*` `java.util.stream.*`
 
 > static imports `com.sillelien.dollar.api.DollarStatic.*` `dollar.internal.runtime.script.java.JavaScriptingStaticImports.*`

The return type will be of type `var` and is stored in the variable `out`. The Java snippet also has access to the scope (dollar.internal.runtime.script.api.Scope) object on which you can get and set Dollar variables.

Reactive behaviour is supported on the Scope object with the `listen` and `notify` methods on variables. You'll need to then built your reactivity around those variables or on the `out` object directly (that's a pretty advanced topic).


## Operators

### Iterative Operators

### Comparison Operators

### Numerical Operators

Dollar support the basic numerical operators +,-,/,*,%,++,-- as well as #

**Remember ++ and -- do not change a variable's value they are a shorthand for a+1 and a-1 not a=a+1 or a=a-1**

```dollar

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

```dollar
.: (1 - 1.0) is DECIMAL
.: (1.0 - 1.0) is DECIMAL
.: (1.0 - 1) is DECIMAL
.: (1 - 1) is INTEGER

.: (1 + 1.0) is DECIMAL
.: (1.0 + 1.0) is DECIMAL
.: (1.0 + 1) is DECIMAL
.: (1 + 1) is INTEGER

.: 1 / 1 is INTEGER
.: 1 / 1.0 is DECIMAL
.: 2.0 / 1 is DECIMAL
.: 2.0 / 1.0 is DECIMAL

.: 1 * 1 is INTEGER
.: 1 * 1.0 is DECIMAL
.: 2.0 * 1 is DECIMAL
.: 2.0 * 1.0 is DECIMAL


.: 1 % 1 is INTEGER
.: 1 % 1.0 is DECIMAL
.: 2.0 % 1 is DECIMAL
.: 2.0 % 1.0 is DECIMAL
.: ABS(1) is INTEGER
.: ABS(1.0) is DECIMAL
```

### Logical Operators

Dollar support the basic logical operators &&,||,! as well as the truthy operator `~` and the default operator `|`.

#### Truthy
The truthy operator `~` converts any value to a boolean by applying the rule that: void is false, 0 is false, "" is false, empty list is false, empty map is false - all else is true.

```dollar

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

```dollar
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

```dollar
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

```dollar
const chat:= module "github:neilellis:dollar-example-module::chat.ds" (channel="test")
var sub= chat.server()
chat.stop_()
```

Let's start by breaking down the module URI. Okay our first part says it's the GitHub scheme and so the GitHub module locator will be used. Next we say the repository belongs to neilellis and the repository is called dollar-example-module. The next part is the optional branch indicator - here we are using the master, so we just leave that empty. Finally we can optionally supply the name of a single script we want to import. If we don't provide that then the main script from the modules 'module.json' file will be used.

The source for the module can be found here: https://github.com/neilellis/dollar-example-module

You will need to have the `git` command on your path and to have access to the repository using `git clone`.

The GitHub resolver will checkout the specified repository and store it under `~/.dollar/runtime/modules/github/<username>/<repo-name>/<branch>` all further interaction with the module will then be done from the checked out version. If you already have a version checked out a git pull will be done to update the branch.

```dollar
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


```dollar
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

```dollar

const testList := [ TIME(), {SLEEP(1 Sec); TIME();}, TIME() ];
var a= |..| testList;
var b= |:| testList;
//Test different execution orders
.: a[2] >= a[1]
.: b[2] < b[1]
```

As you can see the order of evaluation of lists and maps **but not line blocks** is affected by the use of parallel evaluation.

### Fork

The fork operator `-<` or `fork` will cause an expression to be evaluated in the background and any reference to the forked expression will block until a value is ready.

```dollar
const sleepTime := {@@ "Background Sleeping";SLEEP(4 Sec); @@ "Background Finished Sleeping";TIME()}
//Any future reference to c will block until c has completed evaluation
var c= fork sleepTime
SLEEP(1 Sec)
@@ "Main thread sleeping ..."
SLEEP(2 Secs)
@@ "Main thread finished sleeping ..."
var d= TIME()
.: c > d
```

In the example the value of c is greater than d because the value of c is evaluated in the background. Note that as soon as you make use of the value of c you block until the value is ready. This is exactly the same as Java's Futures.




## Advanced Topics

TODO

## Appendix A - Operators
### `all` or `<@`      {#op-all}

**`('<@'|'all') <expression>`**{: style="font-size: 60%"}



all

```
```

### `and` or `&&`      {#op-and}

**`<expression> ('&&'|'and') <expression>`**{: style="font-size: 60%"}



Returns the logical 'and' of two expressions, e.g. `a && b`. Just like in Java it will shortcut, so that if the left-hand-side is false the right-hand-side is never evaluated.

```
true && true <=> true
true && false <=> false
false && true <=> false
false && false <=> false
```

### `assert` or `.:`      {#op-assert}

**`('.:'|'assert') <expression>`**{: style="font-size: 60%"}



The assertion opeartor is used to assert that an expression holds true. It is a reactive operator such that it is evaluated when the right-hand-side expression changes. so `.: a > 10` is asserting that a is **always** greater than 10. To avoid reactive behaviour use the fix operator such as `.: &a > 10` which means that when this statement is evaluated the value of a is compared with 10 - if __at this point__ it is not greater than 10 then the assertion will fail. 

```
.: 1 < 2
.: 3 > 2
.: 1 <= 1
.: 1 <= 2
```

### `assert-equals` or `<->`      {#op-assert-equals}

**`<expression> ('<->'|'assert-equals') <expression>`**{: style="font-size: 60%"}



Asserts that at the point of execution that the left-hand-side is equal to the right-hand-side.

```
 1 + 1 <-> 2
```

### `<=>` (assert-equals-reactive)      {#op-assert-equals-reactive}

**`<expression> '<=>' <expression>`**{: style="font-size: 60%"}



Asserts that the left-hand-side is **always** equal to the right-hand-side.

```
def lamdaVar  {$1 + 10}
lamdaVar(5) <=> 15
```

### `=` (assign)      {#op-assign}



assign

```
var a=1
a causes { @@ $1 }
a=2
a=3
a=4
```

### cast      {#op-cast}

**`<expression> 'as' <type>`**{: style="font-size: 60%"}



Dollar  supports type coercion using the `as` operator followed by the type to coerce to.


```
1 as string <=> "1"
1 as boolean <=> true
1 as list <=> [1]
1 as map <=> {"value":1}
1 as VOID <=> void
1 as integer <=> 1

"1" as integer <=> 1
"http://google.com" as uri
"1" as VOID <=> void
"true" as boolean <=> true
"1" as boolean <=> false
"1" as list <=> ["1"]
"1" as map <=> {"value":"1"}
"1" as string <=> "1"

true as string <=> "true"
true as integer <=> 1
true as list <=> [true]
true as map <=> {"value":true}
true as boolean <=> true
true as VOID <=> void


[1,2,3] as string <=> "[ 1, 2, 3 ]"
[1,2,3] as list <=> [1,2,3]
[1,2,3] as boolean <=> true
[1,2,3] as map <=> {"value":[1,2,3]}

{"a":1,"b":2} as string <=> '{"a":1,"b":2}'
{"a":1,"b":2} as list <=> ["a":1,"b":2]
{"a":1,"b":2} as boolean <=> true
{"a":1,"b":2} as VOID <=> void
```

### `causes` or `=>`      {#op-causes}

**`<expression> ('=>'|'causes') <expression>`**{: style="font-size: 60%"}



The causes operator is used to link a reactive expression to an imperative action. The left-hand-side is any expression and the right hand-side is any expression that will be evaluated when the left-hand-side is updated such as `a+b => {@@ a; @@ b}`.

```
var a=1; var b=1

a => (b= a)

&a <=> 1 ; &b <=> 1

a=2 ; &a <=> 2 ; &b <=> 2
```

### `choose` or `?*`      {#op-choose}

**`<expression> ('?*'|'choose') <expression>`**{: style="font-size: 60%"}



choose

```

```

### `create` or `|||>`      {#op-create}

**`('|||>'|'create') <expression>`**{: style="font-size: 60%"}



Creates a service described typically by a URI.

```
```

### `debug` or `!!`      {#op-debug}

**`('!!'|'debug') <expression>`**{: style="font-size: 60%"}



Sends the result of the right-hand-side to the debug log.

```
!! "I'm a debug message"
```

### `:=` (declaration)      {#op-declaration}

**`( [export] [const] <variable-name> ':=' <expression>) | ( def <variable-name> <expression )`**{: style="font-size: 60%"}



Declares a variable to have a value, this is declarative and reactive such that saying `const a := b + 1` means that `a` always equals `b+1` no matter the value of b. The shorthand `def` is the same as `const <variable-name> :=` so `def a {b+1}` is the same as `const a := b + 1` but is syntactically better when declaring function like variables.

```
var variableA = 1
const variableB := variableA
variableA = 2

.: variableB == 2
```

### `dec` or `--`      {#op-decrement}

**`('--'|'dec') <expression>`**{: style="font-size: 60%"}



Returns the right-hand-side decremented. Note the right-hand-side is not changed so `--a` does not not decrement `a`, it __returns__ `a` **decremented**

```
10++ <=> 11
var unchanged= 1;
unchanged++;
unchanged <-> 1;
```

### `default` or `:-`      {#op-default}

**`<expression> (':-'|'default') <expression>`**{: style="font-size: 60%"}



If the left-hand-side is VOID this returns the right-hand-side, otherwise returns the left-hand-side.

```
void :- "Hello" <=> "Hello"
1 :- "Hello" <=> 1
```

### `destroy` or `<|||`      {#op-destroy}

**`('<|||'|'destroy') <expression>`**{: style="font-size: 60%"}



destroy

```
```

### `divide` or `/`      {#op-divide}

**`<expression> ('/'|'divide') <expression>`**{: style="font-size: 60%"}



Divides one value by another.

```
 .: DATE() / "1.0" is Decimal
 5 / 4 <=> 1
 5.0 /4 <=> 1.25
 .: 1 / 1 is INTEGER
 .: 1 / 1.0 is DECIMAL
 .: 2.0 / 1 is DECIMAL
 .: 2.0 / 1.0 is DECIMAL
```

### `drain` or `<-<`      {#op-drain}

**`('<-<'|'drain') <expression>`**{: style="font-size: 60%"}



Drain an expression, using a URI of all it's data. This is a complete destructive read.


```
```

### `each` or `=>>`      {#op-each}

**`<expression> ('=>>'|'each') <expression>`**{: style="font-size: 60%"}



Eache will iterate over a collection and pass each value (passed in as `$1`) to the second argument.


```
var posts = << https://jsonplaceholder.typicode.com/posts
var titles = posts each { $1.title }
@@ titles
```

### `else` or `-:`      {#op-else}

**`<expression> ('-:'|'else') <expression>`**{: style="font-size: 60%"}



The `else` operator is a binary operator which evaluates the left-hand-side (usually the result of an `if` statement), if that has a value of false then the right-hand-side is evaluated and it's result returned, otherwise it returns the left-hand-side.


```
var a=5
//Parenthesis added for clarity, not required.
var b= if (a == 1) "one" else if (a == 2) "two" else "more than two"
.: b == "more than two"
```

### `equal` or `==`      {#op-equal}

**`<expression> ('=='|'equal') <expression>`**{: style="font-size: 60%"}



Compares two values to see if they are equal. Works with all types and maps to the Java .equals() method.


```
.: 1 == 1
.: "Hello" == "Hello"
```

### `err` or `??`      {#op-err}

**`('??'|'err') <expression>`**{: style="font-size: 60%"}



Sends the result of the right-hand-side to `stderr`.

```
?? "What happened"
```

### `error` or `?->`      {#op-error}

**`('?->'|'error') <expression>`**{: style="font-size: 60%"}



The right-hand-side is executed if an error occurs in the current scope.

```
var errorHappened= false
error { @@ msg; errorHappened= true }
var a=1/0
.: errorHappened
```

### `fix` or `&`      {#op-fix}

**`('&'|'fix') <expression>`**{: style="font-size: 60%"}



Converts a reactive expression into a fixed value. It fixes the value at the point the fix operator is executed. No reactive events will be passed from the right-hand-side expression.

```
var reactiveValue= 1
// The following line would fail if we used .: reactiveValue == 1
// when the 'reactiveValue= 2' statement executes
.: &reactiveValue == 1
reactiveValue= 2
```

### `for`      {#op-for}

**`for <variable-name> <iterable-expression> <expression>`**{: style="font-size: 60%"}



The for operator, this will iterate over a set of values and assign the specified variable to the current value when evaluating the expression.


```
for i in 1..10 {
    @@ i
}
```

### `fork` or `-<`      {#op-fork}

**`('-<'|'fork') <expression>`**{: style="font-size: 60%"}



Executes the right-hand-side in a seperate thread returning a 'future'. Any attempt to make use of the returned value from this operator will block until that thread finishes.

```
const sleepTime := {@@ "Background Sleeping";SLEEP(4 Sec); @@ "Background Finished Sleeping";TIME()}
//Any future reference to c will block until c has completed evaluation
var c= fork sleepTime
SLEEP(1 Sec)
@@ "Main thread sleeping ..."
SLEEP(2 Secs)
@@ "Main thread finished sleeping ..."
var d= TIME()
.: c > d
```

### `>` (greater-than)      {#op-greater-than}

**`<expression> '>' <expression>`**{: style="font-size: 60%"}



The standard `>` operator, it uses Comparable#compareTo and will work with any Dollar data type, including strings, ranges, lists etc.

```
.: 3 > 2
```

### `>=` (greater-than-equal)      {#op-greater-than-equal}

**`<expression> '>=' <expression>`**{: style="font-size: 60%"}



The standard `>=` operator, it uses Comparable#compareTo and will work with any Dollar data type, including strings, ranges, lists etc.

```
.: 2 >= 2
.: 2 >= 1
.: ! (2>=3)
```

### `if` or `???`      {#op-if}

**`<expression> ('???'|'if') <expression>`**{: style="font-size: 60%"}



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

### `in` or `€`      {#op-in}

**`<expression> ('€'|'in') <expression>`**{: style="font-size: 60%"}



Returns true if the left-hand-side expression is contained in the right-hand-side expression.


```
.: "red" in ["red","blue","green"]
.: ! (1 in ["red","blue","green"] )
```

### `inc` or `++`      {#op-increment}

**`('++'|'inc') <expression>`**{: style="font-size: 60%"}



Returns the right-hand-side incremented. Note the right-hand-side is not changed so `--a` does not not decrement `a`, it __returns__ `a` **incremented**

```
10-- <=> 9
var unchanged= 1;
unchanged--;
unchanged <-> 1;
```

### `<` (less-than)      {#op-less-than}

**`<expression> '<' <expression>`**{: style="font-size: 60%"}



The standard `<` operator, it uses Comparable#compareTo and will work with any Dollar data type, including strings, ranges, lists etc.

```
.: 2 < 3
.: "a" < "b"
```

### `<=` (less-than-equal)      {#op-less-than-equal}

**`<expression> '<=' <expression>`**{: style="font-size: 60%"}



The standard `<=` operator, it uses Comparable#compareTo and will work with any Dollar data type, including strings, ranges, lists etc.

```
.: 2 <= 3
.: 2 <= 2
.: "a" < "b"
.: "a" <= "a"
```

### `?=` (listen-assign)      {#op-listen-assign}



listen-assign

```
```

### `.` (member)      {#op-member}

**`<expression> '.' <expression>`**{: style="font-size: 60%"}



The membership or `.` operator accesses the member of a map by it's key.

```
{"key1":1,"key2":2} ["key"+1] <=> 1
{"key1":1,"key2":2}.key2 <=> 2
{"key1":1,"key2":2}[1].key2 <=> 2
```

### `minus` or `-`      {#op-minus}

**`<expression> ('-'|'minus') <expression>`**{: style="font-size: 60%"}



Deducts a value from another value

```
2 - 1 <=> 1
```

### `mod` or `%`      {#op-modulus}

**`<expression> ('%'|'mod') <expression>`**{: style="font-size: 60%"}



Returns the remainder (modulus) of the division of the left-hand-side by the right-hand-side.

```
5 % 4 <=> 1
.: 1 % 1 is INTEGER
.: 1 % 1.0 is DECIMAL
.: 2.0 % 1 is DECIMAL
.: 2.0 % 1.0 is DECIMAL
```

### `multiply` or `*`      {#op-multiply}

**`<expression> ('*'|'multiply') <expression>`**{: style="font-size: 60%"}



Returns the product of two values. If the left-hand-side is scalar (non collection) then a straightforward multiplication will take place. If the left-hand-side is a collection and it is multiplied by `n`, e.g. `{a=a+1} * 3` it will be added (`+`) to itself `n` times i.e. `{a=a+1} + {a=a+1} + {a=a+1}`.

```
2 * 5 <=> 10

.: 1 * 1 is INTEGER
.: 1 * 1.0 is DECIMAL
.: 2.0 * 1 is DECIMAL
.: 2.0 * 1.0 is DECIMAL

```

### `negate` or `-`      {#op-negate}

**`('-'|'negate') <expression>`**{: style="font-size: 60%"}



Negates a value.

```
 .: -1 < 0
```

### `not` or `!`      {#op-not}

**`('!'|'not') <expression>`**{: style="font-size: 60%"}



Returns the logical negation of the right-hand-side expression.


```
not( true ) <=> false
!true <=> false
```

### `not-equal` or `!=`      {#op-not-equal}

**`<expression> ('!='|'not-equal') <expression>`**{: style="font-size: 60%"}



Returns true if the two expression are not equal.


```
.: 1 != 2
```

### `or` or `||`      {#op-or}

**`<expression> ('||'|'or') <expression>`**{: style="font-size: 60%"}



Returns the logical 'or' of two expressions, e.g. `a || b`. Just like in Java it will shortcut, so that if the left-hand-side is true the right-hand-side is never evaluated.

```
true or false <=> true
true || false <=> true
false || false <=> false
```

### `pair` or `:`      {#op-pair}

**`<expression> (':'|'pair') <expression>`**{: style="font-size: 60%"}



Creates a pair (or tuple of 2 values) of values with a key and a value.


```
var pair1 = "first" : "Hello ";
var pair2 = "second" : "World";

.: pair1 + pair2 == {"first":"Hello ","second":"World"}
```

### `parallel` or `|:|`      {#op-parallel}

**`('|:|'|'parallel') <expression>`**{: style="font-size: 60%"}



Causes the right-hand-side expression to be evaluated in parallel, most useful in conjunction with list blocks.

```
const testList := [ TIME(), {SLEEP(1 Sec); TIME();}, TIME() ];
var a= |..| testList;
var b= |:| testList;

//Test different execution orders
.: a[2] >= a[1]
.: b[2] < b[1]
```

### parameter      {#op-parameter}

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

scopedArray(5)[0] <=> 5;
scopedArray(5)[1] <=> 10;
scopedArray(5)[2]() <=> 20;

const testParams := (last + " " + first)
testParams(first="Hello", last="World") <=> "World Hello"

def func($2 + " " + $1)
func ("Hello", "World") <=> "World Hello"
```

### `pause` or `||>`      {#op-pause}

**`('||>'|'pause') <expression>`**{: style="font-size: 60%"}



Pauses a service described typically by a URI.

```
```

### `pipe` or `|`      {#op-pipe}

**`<expression> ('|'|'pipe') <expression>`**{: style="font-size: 60%"}



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

### `plus` or `+`      {#op-plus}

**`<expression> ('+'|'plus') <expression>`**{: style="font-size: 60%"}



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

.: (1 + 1.0) is DECIMAL
.: (1.0 + 1.0) is DECIMAL
.: (1.0 + 1) is DECIMAL
.: (1 + 1) is INTEGER
```

### `print` or `@@`      {#op-print}

**`('@@'|'print') <expression>`**{: style="font-size: 60%"}



Sends the right-hand-side expression to stdout.

```
@@ "Hello"
print "World"
```

### `publish` or `*>`      {#op-publish}

**`<expression> ('*>'|'publish') <expression>`**{: style="font-size: 60%"}



publish

```
```

### `pure`      {#op-pure}

**`'pure' <expression>`**{: style="font-size: 60%"}




Support for some aspects of functional programming are included in Dollar. Primarily the idea of pure expressions using the `pure` operator. This signals that an expression or declaration is a pure expression or declaration.

In the example we're declaring reverse to be an expression that reverses two values from a supplied array. Because we declare it as `pure` the expression supplied must also be `pure`. To understand what a pure function is please see [http://en.wikipedia.org/wiki/Pure_function](http://en.wikipedia.org/wiki/Pure_function). Basically it prohibits the reading of external state or the setting of external state. We next swap `[2,1]` within a newly created pure expression, which is subsequently assigned to a. If reverse had not been declared pure it would not be allowed within the pure expression.


Note some builtin functions are not themselves pure and will trigger parser errors if you attempt to use them in a pure expression. Take DATE() for example which supplies an external state (the computers clock).


```
pure def reverse [$1[1],$1[0]]

a= pure {
 reverse([2,1])
}
```

### `range` or `..`      {#op-range}

**`<expression> ('..'|'range') <expression>`**{: style="font-size: 60%"}



Creates a RANGE between the two values specified.

```
#("a".."c") <=> 3
(1..3)[1] <=>2
```

### `read`      {#op-read}

**`'read' ['block'] ['mutate'] ['from'] <expression>`**{: style="font-size: 60%"}



The read operator is used to read data from an expression typically a URI. It has options to read-and-block (`block`) as well as read-and-destroy (`mutate`). Note that the `from` keyword is just syntax sugar and can be omitted. 

```
```

### `<<` (read-simple)      {#op-read-simple}

**`'<<' <expression>`**{: style="font-size: 60%"}



Performs a simple read from another data item, typically this is used with a URI.

```
```

### `reduce` or `>>=`      {#op-reduce}

**`<expression> ('>>='|'reduce') <expression>`**{: style="font-size: 60%"}



reduce

```
```

### `serial` or `|..|`      {#op-serial}

**`('|..|'|'serial') <expression>`**{: style="font-size: 60%"}



Causes the right-hand-side expression to be evaluated in serial, most useful in conjunction with list blocks.

```
```

### `size` or `#`      {#op-size}

**`('#'|'size') <expression>`**{: style="font-size: 60%"}



Returns the size of non-scalar types or the length of a string.

```
```

### `start` or `|>`      {#op-start}

**`('|>'|'start') <expression>`**{: style="font-size: 60%"}



Starts a service described typically by a URI.

```
```

### `state` or `<|>`      {#op-state}

**`('<|>'|'state') <expression>`**{: style="font-size: 60%"}



Returns the state of a service described typically by a URI.

```
```

### `stop` or `<|`      {#op-stop}

**`('<|'|'stop') <expression>`**{: style="font-size: 60%"}



Stops a service described typically by a URI.

```
```

### `subscribe` or `<*`      {#op-subscribe}

**`<expression> ('<*'|'subscribe') <expression>`**{: style="font-size: 60%"}



subscribe

```
```

### `*=` (subscribe-assign)      {#op-subscribe-assign}



subscribe-assign

```
```

### subscript      {#op-subscript}

**`( <expression> '[' <index-expression>|<key-expression> ']' ) | ( <expression> '.' (<index-expression>|<key-expression>) )`**{: style="font-size: 60%"}



subscript operator

```
{"key1":1,"key2":2} ["key"+1] <=> 1
{"key1":1,"key2":2}.key2 <=> 2
{"key1":1,"key2":2}[1].key2 <=> 2
```

### `truthy` or `~`      {#op-truthy}

**`('~'|'truthy') <expression>`**{: style="font-size: 60%"}



The truthy operator `~` converts any value to a boolean by applying the rule that: void is false, 0 is false, "" is false, empty list is false, empty map is false - all else is true.

```
```

### `unpause` or `<||`      {#op-unpause}

**`('<||'|'unpause') <expression>`**{: style="font-size: 60%"}



Un-pauses a service described typically by a URI.

```
```

### `when` or `?`      {#op-when}

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

### `while`      {#op-while}

**`while <condition> <expression>`**{: style="font-size: 60%"}



while operator

```
```

### `write`      {#op-write}

**`'write' ['block'] ['mutate'] ['to'] <expression>`**{: style="font-size: 60%"}



The write operator is used to write data to an expression typically a URI. It has options to write-and-block (`block`) as well as write-and-destroy (`mutate`). Note that the `to` keyword is just syntax sugar and can be omitted. 

```
```

### `>>` (write-simple)      {#op-write-simple}

**`<expression> '>>' <expression>`**{: style="font-size: 60%"}



Performs a simple write to another data item, mostly used to write to a URI. 

```
```

## Appendix B - Keywords
### as


### block


### collect


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


### module


### mutate


### no

Boolean false.



### null

A NULL value of ANY type.



### pure

The start of a pure expression.



### read


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



### when


### while


### with


### write


### yes

Boolean true.



## Appendix C - Reserved Keywords, Operators and Symbols
### Keywords

The following keywords are reserved:

> await, closure, dispatch, dollar, dump, emit, fail, filter, import, impure, include, join, lambda, load, measure, pluripotent, readonly, save, scope, send, switch, trace, unit, variant, varies, vary, wait

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
|[range](#op-range)            |`range`        | `..`     |binary    |
|[default](#op-default)        |`default`      | `:-`     |binary    |
|[member](#op-member)          |               | `.`      |binary    |
|[parameter](#op-parameter)    |               |          |postfix   |
|[subscript](#op-subscript)    |               |          |postfix   |
|[cast](#op-cast)              |               |          |postfix   |
|[decrement](#op-decrement)    |`dec`          | `--`     |prefix    |
|[for](#op-for)                |`for`          |          |control   |
|[in](#op-in)                  |`in`           | `€`      |binary    |
|[increment](#op-increment)    |`inc`          | `++`     |prefix    |
|[not](#op-not)                |`not`          | `!`      |prefix    |
|[size](#op-size)              |`size`         | `#`      |prefix    |
|[truthy](#op-truthy)          |`truthy`       | `~`      |prefix    |
|[while](#op-while)            |`while`        |          |control   |
|[divide](#op-divide)          |`divide`       | `/`      |binary    |
|[each](#op-each)              |`each`         | `=>>`    |binary    |
|[modulus](#op-modulus)        |`mod`          | `%`      |binary    |
|[multiply](#op-multiply)      |`multiply`     | `*`      |binary    |
|[reduce](#op-reduce)          |`reduce`       | `>>=`    |binary    |
|[minus](#op-minus)            |`minus`        | `-`      |binary    |
|[negate](#op-negate)          |`negate`       | `-`      |prefix    |
|[plus](#op-plus)              |`plus`         | `+`      |binary    |
|[greater-than](#op-greater-than)|               | `>`      |binary    |
|[less-than](#op-less-than)    |               | `<`      |binary    |
|[pipe](#op-pipe)              |`pipe`         | `|`      |binary    |
|[equal](#op-equal)            |`equal`        | `==`     |binary    |
|[greater-than-equal](#op-greater-than-equal)|               | `>=`     |binary    |
|[less-than-equal](#op-less-than-equal)|               | `<=`     |binary    |
|[not-equal](#op-not-equal)    |`not-equal`    | `!=`     |binary    |
|[and](#op-and)                |`and`          | `&&`     |binary    |
|[or](#op-or)                  |`or`           | `||`     |binary    |
|[all](#op-all)                |`all`          | `<@`     |prefix    |
|[causes](#op-causes)          |`causes`       | `=>`     |binary    |
|[choose](#op-choose)          |`choose`       | `?*`     |binary    |
|[drain](#op-drain)            |`drain`        | `<-<`    |prefix    |
|[publish](#op-publish)        |`publish`      | `*>`     |binary    |
|[pure](#op-pure)              |`pure`         |          |prefix    |
|[read](#op-read)              |`read`         |          |prefix    |
|[read-simple](#op-read-simple)|               | `<<`     |prefix    |
|[subscribe](#op-subscribe)    |`subscribe`    | `<*`     |binary    |
|[when](#op-when)              |`when`         | `?`      |binary    |
|[write](#op-write)            |`write`        |          |control   |
|[write-simple](#op-write-simple)|               | `>>`     |binary    |
|[pair](#op-pair)              |`pair`         | `:`      |binary    |
|[create](#op-create)          |`create`       | `|||>`   |prefix    |
|[destroy](#op-destroy)        |`destroy`      | `<|||`   |prefix    |
|[else](#op-else)              |`else`         | `-:`     |binary    |
|[fork](#op-fork)              |`fork`         | `-<`     |prefix    |
|[if](#op-if)                  |`if`           | `???`    |binary    |
|[parallel](#op-parallel)      |`parallel`     | `|:|`    |prefix    |
|[pause](#op-pause)            |`pause`        | `||>`    |prefix    |
|[serial](#op-serial)          |`serial`       | `|..|`   |prefix    |
|[start](#op-start)            |`start`        | `|>`     |prefix    |
|[state](#op-state)            |`state`        | `<|>`    |prefix    |
|[stop](#op-stop)              |`stop`         | `<|`     |prefix    |
|[unpause](#op-unpause)        |`unpause`      | `<||`    |prefix    |
|[assign](#op-assign)          |               | `=`      |assignment|
|[declaration](#op-declaration)|               | `:=`     |assignment|
|[listen-assign](#op-listen-assign)|               | `?=`     |assignment|
|[subscribe-assign](#op-subscribe-assign)|               | `*=`     |assignment|
|[assert](#op-assert)          |`assert`       | `.:`     |prefix    |
|[assert-equals](#op-assert-equals)|`assert-equals`| `<->`    |binary    |
|[assert-equals-reactive](#op-assert-equals-reactive)|               | `<=>`    |binary    |
|[debug](#op-debug)            |`debug`        | `!!`     |prefix    |
|[err](#op-err)                |`err`          | `??`     |prefix    |
|[error](#op-error)            |`error`        | `?->`    |prefix    |
|[print](#op-print)            |`print`        | `@@`     |prefix    |

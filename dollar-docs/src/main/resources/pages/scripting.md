#!/usr/bin/env dollar

##Introduction

###Executable Documentation

Everything in this documentation is executed as part of the build process, so all the examples are guaranteed to run with the latest master branch of Dollar.

Yep Dollar can actually run Markdown files, in fact the source file that this page was built from starts with:


```
#!/usr/bin/env dollar
```
So it can be executed directly from a Unix command line.

The source for this page (minus that header) is [here](scripting.md)

###Getting Started

NOTE: At present only Mac OS X and 64 Bit Ubuntu Linux is officially supported, however since Dollar is entirely based in Java it's trivial to port to other systems.

First download the Dollar scripting runtime from [distribution](http://dollarscript.s3-website-eu-west-1.amazonaws.com/dist/dollar-{{site.release}}.tgz)

Make sure `dollar/bin` is on your PATH.

Run `dollar <filename>` to execute a Dollar script.

Here is an example of what Dollar looks like

```dollar

testParams := ($2 + " " + $1)

.: testParams ("Hello", "World") == "World Hello"

```

##Understanding the Basics


Dollar has it's own peculiarities, mostly these exists to help with it's major target: serverside integration projects. So it's important to understand the basic concepts before getting started.

###Functional Programming and the 'pure' operator

Support for functional programming is included in Dollar, this will be widened as the language is developed. For now it is provided by the `pure` operator. This signals that an expression or declaration is a pure expression or function.

In this example we're declaring reverse to be an expression that reverses two values from a supplied array. Because we declare it as `pure` the expression supplied must also be `pure`. To understand what a pure function is please see http://en.wikipedia.org/wiki/Pure_function. Basically it prohibits the reading of external state or the setting of external state. We next swap `[2,1]` within a newly created pure expression, which is subsequently assigned to a. If reverse had not been declared pure it would not be allowed within the pure expression.

 ```dollar
 pure reverse := [$1[1],$1[0]]

 a= pure {
     reverse([2,1])
 }

 ```

Note some builtin functions are not themselves pure and will trigger parser errors if you attempt to use them in a pure expression. Take DATE() for example which supplies an external state (the computers clock).



###Reactive Programming

Dollar expressions are by default *lazy*, this is really important to understand otherwise you may get some surprises. This lazy evaluation is combined with a simple event system to make Dollar a [reactive programming language](http://en.wikipedia.org/wiki/Reactive_programming) by default. 

The simplest way to understand reactive programming is to imagine you are using a spreadsheet. When you say a cell has the value SUM(A1:A4) that value will *react* to changes in any of the cells from A1 to A4. Dollarscript works the same way by default, however you can also *fix* values when you want to write procedural code. 

Let's see some of that behaviour in action:

```dollar

variableA := 1
variableB := variableA
variableA := 2

.: variableB == 2
```

In the above example we are declaring (using the declarative operator `:=`) that variableA is current the value 1, we then declare that variableB is the *same as* variableA. So when we change variableA to 2 we also change variableB to 2.

Before we go any further let's clarify `:=` vs `=`, I have chosen to follow the logic [described here](https://math.stackexchange.com/questions/1838678/confused-about-notation-versus-plain-old) so that the `:=` operator is a definition (and by it's nature reactive) and `=` is an assignment ( not reactive and has a fix depth of 1 - more on that later).

This means that `a := b + 1` translates to **a is defined as b + 1** so a is behaving reactively, changes to b cause a change in the value of a. It also means that `a = b + 1` simply assigns `b + 1` to the variable a, changes to b do not cause changes to a. 

**TL;DR `=` behaves like it's Java equivalent, `:=` doesnt't**

> The assertion operator `.:` will throw an assertion error if the value following is either non boolean or not true.


Now let's throw in the causes operator :

```dollar

a=1
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

b=1
a=1
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
b=1
@@b
b=2
```

Outputs 1 then 2 because @@ heard the change to b and re output the new value. Often this is what you want, however if you don't just add the fix operator `&` before the value. That will stop reactive behaviour.

```dollar
b=1
@@ &b
b=2
```




###Assignment

Obviously the declarative/reactive behavior is fantastic for templating, eventing, creating lambda style expressions etc. however there are times when we want to simply assign a value and perform a single action on that value.

```dollar

variableA = 1
variableB = variableA
variableA = 2

.: variableB == 1
```

So as you can see when we use the `=` assignment operator we assign the *value* of the right hand side to the variable. Watch what happens when we use expressions.


```dollar

variableA = 1
variableB = variableA
variableC = (variableA +1 )
variableD := (variableA + 1)
variableA = 2

.: variableB == 1
.: variableC == 2
.: variableD == 3

```

The assignment operator `=` has a 'fix' depth of 1. This means that any expression will be evaluated, but no maps or line blocks will be. It is also not reactive. A fix depth of 2 causes all expressions to be evaluated and evaluates one depth of maps or line blocks.

The assert equals operator `<=>` will compare two values and throw an exception if they are not the same ` a <=> b` is the same as `.: a == b`**

```dollar

lamdaVar = {$1 + 10}
lamdaVar(5) <=> 15

```

> It's important to note that all values in Dollar are immutable - that means if you wish to change the value of a variable you *must* __reassign__ a new value to the variable. For example `v++` would return the value of `v+1` it does not increment v. If however you want to assign a constant value, one that is both immutable and cannot be reassigned, just use the `const` modifier at the variable assignment (this does not make sense for declarations, so is only available on assignments).

```dollar
const MEDIUM = 23
// MEDIUM= 4 would now produce an error
```

So `:=` supports the full reactive behaviour of Dollar, i.e. it is a declaration not a value assignment, and `=` is used to nail down a particular value or reduce the reactive behaviour. Later we'll come across the fix operator `&` which instructs Dollar to fix a value completely . More on that later.

###Blocks

####Line Block
Dollar supports several block types, the first is the 'line block' a line block lies between `{` and `}` and is separated by either newlines or `;` characters.

```dollar

myBlock := {
    "Hello "
    "World"
}

myBlock <=> "World"

myBlock2 := {1;2}

myBlock2 <=> 2

```

When a line block is evaluated the result is the value of the last entry. For advanced users note that all lines will be evaluated, the value is just ignored. A line block behaves a lot like a function in an imperative language.

####List Block

Next we have the list block, the list block preserves all the values each part is seperated by either a `,` or a newline but is delimited by `[` and `]`.

```dollar

list := [
    "Hello "
    "World"
]

list <=> ["Hello ","World"]

list2 := [1,2]

list2 <=> [1,2]

```

####Map Block

Finally we have the map block, when an map block is evaluated the result is the aggregation  of the parts from top to bottom into a map. The map block starts and finishes with the `{` `}` braces, however each part is seperated by a `,` not a `;` or *newline* . The default behaviour of a map block is virtually useless, it takes the string value and makes it the key and keeps the original value as the value to be paired with that key.

```dollar

mapBlock := {
    "Hello",
    "World"
}

mapBlock <=> {"Hello":"Hello", "World":"World"}

mapBlock2 := { 1, 2}

mapBlock2 <=> {"1":1,"2":2}

```

Map blocks are combined with the pair `:` operator to become useful and create maps/JSON like this:


```dollar

mapBlock := {
    "first":"Hello ",
   "second":"World"
}

@@ mapBlock

mapBlock.second <=> "World"

```

A map block with one entry that is not a pair is assumed to be a *Line Block*.

The stdout operator `@@` is used to send a value to stdout in it's serialized (JSON) format, so the result of the above would be to output `{"first":"Hello ","second":"World"}` a JSON object created using JSON like syntax. Maps can also be created by joining pairs.

```dollar

pair1 := "first" : "Hello ";
pair2 := "second" : "World";

.: pair1 + pair2 == {"first":"Hello ","second":"World"}

```


###Lists

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


###Ranges

Dollar (at present) supports numerical and character ranges

```dollar

#("a".."c") <=> 3
(1..3)[1] <=>2

```

###Error Handling

Error handling couldn't be simpler. Define an error expression using the error keyword, the expression supplied will be evaluated on an error occurring within any sub scope of the scope in which it is defined. The special variables `msg` and `type` will be assigned values.

```dollar
errorHappened= false
error { @@ msg; errorHappened= true }
a=1/0
.: errorHappened
```

###Error Handling

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

##Type System
###Intro
Although Dollar is typeless at compile time, it does support basic runtime typing. At present this includes: STRING, INTEGER,DECIMAL, LIST, MAP, URI, VOID, RANGE, BOOLEAN. The value for a type can be checked using the `is` operator:

```dollar
.: "Hello World" is String
.: ["Hello World"] is List
```

###DATE

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
fortnight := ($1 * 14)

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
(it < 100) a = 50
(it > previous || previous is Void) b = 5
b=6
b=7
( it is String) s="String value"
```

The special variables `it` - the current value and `previous` - the previous value, will be available for the constraint.

To build a simple runtime type system simply declare (using `:=`) your type as a boolean expression.

```dollar

//define a pseudo-type
colorEnum := ( it in ["red","green","blue"] )


//Use it as a constraint
(colorEnum) myColor= "green"

error { @@ msg }

//This fails
myColor="apple"

```

Of course since the use of `(it is XXXX)` is very common Dollar provides a specific runtime type constraint that can be added in conjunction with other constraints. Simply prefix the assignment or decleration with `<XXXX>` where XXXX is the runtime type.


```dollar
<string> (#it > 5) s="String value"
```

It is intended that the predictive type system, will be in time combined with runtime types to help spot bugs at compile time.

### Type Coercion
Dollar also supports type coercion, this is done using the `as` operator followed by the type to coerce to.


```dollar
<string> s= 1 as string
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


##Imperative Control Flow

With imperative control flow, the control flow operations are only triggered when the block they are contained within is evaluated. I.e. they behave like control flow in imperative languages. So start with these if you're just learning Dollar.

###If

Dollar supports the usual imperative control flow but, unlike some languages, everything is an operator. This is the general rule of Dollar, everything has a value. Dollar does not have the concept of statements and expressions, just expressions. This means that you can use control flow in an expression.

```dollar

a=1
b= if a==1 2 else 3
b <=> 2

```

So let's start with the `if` operator. The `if` operator is separate from the `else` operator, it simply evaluates the condition supplied as the first argument. If that value is boolean and true it evaluates the second argument and returns it's value; otherwise it returns boolean false.

The `else` operator is a binary operator which evaluates the left-hand-side (usually the result of an `if` statement), if that has a value of false then the right-hand-side is evaluated and it's result returned, otherwise it returns the left-hand-side.

The combined effect of these two operators is to provide the usual if/else/else if/ control flow

```dollar

a=5
//Parenthesis added for clarity, not required.
b= if (a == 1) "one" else if (a == 2) "two" else "more than two"
.: b == "more than two"

```

###For

```dollar

for i in 1..10 {
    @@ i
}

```

###While

```dollar
a= 1
while a < 10 {
 a= a+1
}
a <=> 10

```



##Reactive Control Flow

###Causes

Dollar as previously mentioned is a reactive programming language, that means that changes to one part of your program can automatically affect another. Consider this a 'push' model instead of the usual 'pull' model.

Let's start with the simplest reactive control flow operator, the '?->' or 'causes' operator.

```dollar
a=1; b=1

a ?-> (b= a)

&a <=> 1 ; &b <=> 1

a=2 ; &a <=> 2 ; &b <=> 2

```

Okay so reactive programming can melt your head a little. So let's go through the example step by step.

Firstly we assign fixed values to `a` and `b`, we then say that when `a` changes the action we should take is to assign it's value to `b`. Okay now we check to see if the current value of `a` is equal to 1, we use the fix operator `&` here to say that we are only interested in the current value. Because `<=>` is a reactive operator if we didn't use the fix operator then `a <=> 1` would mean a is always 1. When we add the fix operator it fixes the value of a to the value at this point in the code.

We then do the same with b to see if it is 1.

Next we assign a new value of 2 to `a`. This will immediately (within the same thread) trigger the reactive `->` operator which is triggered by changes to `a`. The trigger assigns the value of `a` to `b`, so `b` is now the same as `a`. The assertions at the end confirm this.

###When

Next we have the 'when' operator which can be specified as a statement, usually for longer pieces of code. Or as the `?` operator, for concise code.


```dollar

c=1
d=1

//When c is greater than 3 assign it's value to d
c > 3 ? (d= c)

&c <=> 1; &d <=> 1
c=2; &c <=> 2; &d <=> 1
c=5 ; &c <=> 5 ; &d <=> 5

```

This is similar to the previous example except that we have to set a value greater than 3 for the action to be taken.

```dollar
//Note alternative syntax is when <condition> <expression>
c=1
when c > 3 { @@ c}
```

###Collect


The `collect` operator listens for changes in the supplied expression adding all the values to a list until the `until` clause is triggered. It then evaluates the second expression with the values `it` for the current value, `count` for the number of messages **received** since last emission and `collected` for the collected values. The whole operator itself emits `void` unless the collection operation is triggered in which case it emits the collection itself. Values can be skipped with an `unless` clause. Skipped messages increase the count value, so use `#collected` if you want the number of collected values.

```dollar

e=void

//Length is greater than or equal to 4 unless void
(#it >= 4 || it is VOID) collectedValues=void

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

##Parameters &amp; Functions

In most programming languages you have the concept of functions and parameters, i.e. you can parametrized blocks of code. In Dollar you can parameterize *anything*. For example let's just take a simple expression that adds two strings together, in reverse order, and pass in two parameters.

```dollar
($2 + " " + $1)("Hello", "World") <=> "World Hello"

```

The naming of positional parameters is the same as in shell scripts.

Now if we take this further we can use the declaration operator `:=` to say that a variable is equal to the expression we wish to parameterise, like so:

```dollar

testParams := ($2 + " " + $1)
testParams ("Hello", "World") <=> "World Hello"

```

Yep we built a function just by naming an expression. You can name anything and parameterise it - including maps, lists, blocks and plain old expressions.


What about named parameters, that would be nice.

```dollar
testParams := (last + " " + first)
testParams(first="Hello", last="World") <=> "World Hello"
```

Yep you can use named parameters, then refer to the values by the names passed in.


##Resources &amp; URIs

URIs are first class citizen's in Dollar. They refer to a an arbitrary resource, usually remote, that can be accessed using the specified protocol and location. Static URIs can be referred to directly without quotation marks, dynamic URIs can be built by casting to a uri using the `as` operator.

```dollar
posts = << https://jsonplaceholder.typicode.com/posts 
titles = posts each { $1.title }
@@ titles
```

In this example we've requested a single value (using `<<`) from a uri and assigned the value to `posts` then we simply iterate over the results  using `each` and each value (passed in to the scope as `$1`) we extract the `title`. The each operator returns a list of the results and that is what is passed to standard out.


##Using Java

Hopefully you'll find Dollar a useful and productive language, but there will be many times when you just want to quickly nip out to a bit of Java. To do so, just surround the Java in backticks.

```dollar

variableA="Hello World"

java = `out=scope.get("variableA");`

java <=> "Hello World"

```

A whole bunch of imports are done for you automatically (see below) but you will have to fully qualify any thirdparty libs.
 
> imports `dollar.lang.*``com.sillelien.dollar.script.api.*` `com.sillelien.dollar.api.*` `java.io.*` `java.math.*` `java.net.*` `java.nio.file.*` `java.util.*` `java.util.concurrent.*` `java.util.function.*` `java.util.prefs.*` `java.util.regex.*` `java.util.stream.*`
 
 > static imports `com.sillelien.dollar.api.DollarStatic.*` `com.sillelien.dollar.script.java.JavaScriptingStaticImports.*`

The return type will be of type `var` and is stored in the variable `out`. The Java snippet also has access to the scope (com.sillelien.dollar.script.api.Scope) object on which you can get and set Dollar variables.

Reactive behaviour is supported on the Scope object with the `listen` and `notify` methods on variables. You'll need to then built your reactivity around those variables or on the `out` object directly (that's a pretty advanced topic).


##Operators

###Iterative Operators

###Comparison Operators

###Numerical Operators

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

###Logical Operators

Dollar support the basic logical operators &&,||,! as well as the truthy operator `~` and the default operator `|`.

#### Truthy
The truthy operator converts any value to a boolean by applying the rule that: void is false, 0 is false, "" is false, empty list is false, empty map is false - all else is true.

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

void | "Hello" <=> "Hello"
1 | "Hello" <=> 1

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

The default operator '|' (keyword `default`) returns the left hand side if it is not `VOID` otherwise it returns the right hand side. 

```dollar
void | "Hello" <=> "Hello"
1 | "Hello" <=> 1
void default 2 <=> 2
```


###Pipe Operators
###Remaining Operators
##Imports &amp; Modules
###Import
###Modules

Modules can be imported using the `module` keyword and a string representing in URI format the location of the module. At present the standard format is the Github locator so we're going to look at that first.

```dollar
chat:= module "github:neilellis:dollar-example-module::chat.ds" (channel="test")
sub= chat.server()
chat.stop()
```

Let's start by breaking down the module URI. Okay our first part says it's the GitHub scheme and so the GitHub module locator will be used. Next we say the repository belongs to neilellis and the repository is called dollar-example-module. The next part is the optional branch indicator - here we are using the master, so we just leave that empty. Finally we can optionally supply the name of a single script we want to import. If we don't provide that then the main script from the modules 'module.json' file will be used.

The source for the module can be found here: https://github.com/neilellis/dollar-example-module

You will need to have the `git` command on your path and to have access to the repository using `git clone`.

The GitHub resolver will checkout the specified repository and store it under `~/.dollar/runtime/modules/github/<username>/<repo-name>/<branch>` all further interaction with the module will then be done from the checked out version. If you already have a version checked out a git pull will be done to update the branch.

```dollar
hello := module "github:neilellis:dollar-example-module:0.1.0:branch.ds"
@@ hello
```

###Module Locators
###Writing Modules

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
redis= ("redis://localhost:6379/" + ${channel | "test"}) as URI
www= (("http:get://127.0.0.1:8111/" + ${channel | "test"}) as URI)

export server := {
           www subscribe {
            $1.params >> redis
            { body :  all redis }
        }
    };

export stop := {stop(www);stop(redis); @@ [state(www),state(redis)]}

export state:= [state(www),state(redis)]
```


##Builtin Functions

##Concurrency & Threads

Notes:

All types are immutable, including collections.
You cannot reassign a variable from a different thread, so they are readonly from other threads.


###Parallel &amp; Serial Operators
The parallel operator `|:|` or `parallel` causes the right hand side expression to be evaluated in parallel, it's partner the serial operator `|..|` or `serial` forces serial evaluation even if the current expression is being evaluated in parallel.

```dollar

testList := [ TIME(), {SLEEP(1 Sec); TIME();}, TIME() ];
a= |..| testList;
b= |:| testList;
//Test different execution orders
.: a[2] >= a[1]
.: b[2] < b[1]
```

As you can see the order of evaluation of lists and maps **but not line blocks** is affected by the use of parallel evaluation.

###Fork

The fork operator `-<` or `fork` will cause an expression to be evaluated in the background and any reference to the forked expression will block until a value is ready.

```dollar
sleepTime := {@@ "Background Sleeping";SLEEP(4 Sec); @@ "Background Finished Sleeping";TIME()}
//Any future reference to c will block until c has completed evaluation
c= fork sleepTime
SLEEP(1 Sec)
@@ "Main thread sleeping ..."
SLEEP(2 Secs)
@@ "Main thread finished sleeping ..."
d= TIME()
.: c > d
```

In the example the value of c is greater than d because the value of c is evaluated in the background. Note that as soon as you make use of the value of c you block until the value is ready. This is exactly the same as Java's Futures.




##Advanced Topics



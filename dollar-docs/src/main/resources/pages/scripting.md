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

NOTE: At present only Mac OS X is officially supported, however since Dollar is entirely based in Java it's trivial to port to other systems.

First download the Dollar scripting runtime from [ ![Download](https://api.bintray.com/packages/neilellis/dollar/dollar-runtime-osx/images/download.svg) ](https://bintray.com/neilellis/dollar/dollar-runtime-osx/_latestVersion)

Make sure `dollar/bin` is on your PATH.

Run `dollar <filename>` to execute a Dollar script.

Here is an example of what DollarScript looks like

```dollar

testParams := ($2 + " " + $1)

.: testParams ("Hello", "World") == "World Hello"

```

##Understanding the Basics


DollarScript has it's own peculiarities, mostly these exists to help with it's major function - data/API centric Internet applications. So it's important to understand the basic concepts before getting started.

###Reactive Programming

DollarScript expressions are by default *lazy*, this is really important to understand otherwise you may get some surprises. This lazy evaluation makes DollarScript a [reactive programming language](http://en.wikipedia.org/wiki/Reactive_programming) by default.


Let's see some of that behaviour in action:

```dollar

variableA := 1
variableB := variableA
variableA := 2

.: variableB == 2
```

In the above example we are declaring (using the declarative operator `:=`) that variableA is current the value 1, we then declare that variableB is the *same as* variableA. So when we change variableA to 2 we also change variableB to 2.

The assertion operator `.:` will throw an assertion error if the value following is either non boolean or not true.


Now let's throw in the => or causes operator :

```dollar

a=1
a => { @@ $1 }
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
a + b + 1 => { @@ "a=" + a + ", b=" + b}
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

Yep, you can write reactive expressions based on collections or arbitrary expressions @@ When any component changes the right hand side is re-evaluated (the actual value that changed is passed in as $1).


###Assignment

Obviously the declarative/reactive behavior is fantastic for templating and creating lambda style expressions, however a lot of the time we want to simply assign a value.

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

It's important to note that all values in DollarScript are immutable - that means if you wish to change the value of a variable you *must* __reassign__ a new value to the variable. For example `v++` would return the value of `v+1` it does not increment v. If however you want to assign a constant value, one that is both immutable and cannot be reassigned, just use the `const` modifier at the variable assignment (this does not make sense for declarations, so is only available on assignments).

```dollar
const MEDIUM = 23
// MEDIUM= 4 would now produce an error
```

So `:=` supports the reactive behaviour of Dollar, i.e. it is a declaration not a value assignment, and `=` is used to nail down a particular value. Later we'll come across the value anchor operator or diamond `@` which instructs DollarScript to fix a value at the time of declaration. More on that later.

###Blocks
####Line Block
DollarScript supports several block types, the first is the 'line block' a line block lies between `{` and `}` and is separated by either newlines or `;` characters.

```dollar

block := {
    "Hello "
    "World"
}

.: block == "World"

block2 := {1;2;}

.: block2 == 2

```

When a line block is evaluated the result is the value of the last entry. For advanced users note that all lines will be evaluated, the value is just ignored. A line block behaves a lot like a function in an imperative language.

####List Block

Next we have the list block, the list block preserves all the values each part is seperated by either a `,` or a newline but is delimited by `[` and `]`.

```dollar

list := [
    "Hello "
    "World"
]

.: list == ["Hello ","World"]

list2 := [1,2]

.: list2 == [1,2]

```

####Appending / Map Block

Finally we have the appending block, when an appending (or map) block is evaluated the result is the concatenation (using $plus() in the Dollar API) of the parts from top to bottom. The appending block starts and finishes with the `{` `}` braces, however each part is seperated by a `,` not a `;` or *newline*

```dollar

appending := {
    "Hello ",
    "World"
}

.: appending == "Hello World"

appending2 := { 1, 2}

.: appending2 == 3

```

Appending blocks can be combined with the pair `:` operator to create maps/JSON like this:


```dollar

appending := {
    "first":"Hello ",
   "second":"World"
}

@@ appending

.: appending.second == "World"

```

The stdout operator `@@` is used to send a value to stdout in it's serialized (JSON) format, so the result of the above would be to output `{"first":"Hello ","second":"World"}` a JSON object created using JSON like syntax. This works because of how appending works with pairs, i.e. they are joined together to form a map.

```dollar

pair1 := "first" : "Hello ";
pair2 := "second" : "World";

.: pair1 + pair2 == {"first":"Hello ","second":"World"}

```


###Lists

DollarScript's lists are pretty similar to JavaScript arrays. They are defined using the `[1,2,3]` style syntax and accessed using the `x[y]` subscript syntax.

```dollar
.: [1,2,3] + 4 == [1,2,3,4];
.: [1,2,3,4] - 4 == [1,2,3];
.: [] + 1 == [1] ;
.: [1] + [1] == [1,[1]];
.: [1] + 1 == [1,1];

[1,2,3][1] <=> 2

```

*Note we're using the assert equals or `<=>` operator here, this is a combination of `.:` and `==` that will cause an error if the two values are not equal.*

You can count the size of the list using the size operator `#`.

```dollar
#[1,2,3,4] <=> 4
```

DollarScript maps are also associative arrays (like JavaScript) allowing you to request members from them using the list subscript syntax

```dollar
{"key1":1,"key2":2} ["key"+1] <=> 1
{"key1":1,"key2":2} [1] <=> {"key2":2}
{"key1":1,"key2":2} [1]["key2"] <=> 2
```

As you can see from the example you can request a key/value pair (or Tuple if you like) by it's position using a numeric subscript. Or you can treat it as an associative array and request an entry by specifying the key name. Any expression can be used as a subscript, numerical values will be used as indexes, otherwise the string value will be used as a key.


###Ranges

DollarScript (at present) supports numerical and character ranges

```dollar

"a".."c" <=> ["a","b","c"]
1..3 <=> [1,2,3]

```

###Error Handling

Error handling couldn't be simpler. Define an error expression using the error keyword, the expression supplied will be evaluated on an error occurring within any sub scope of the scope in which it is defined. The special variables `msg` and `type` will be assigned values.

```dollar
errorHappened= false
error { @@ msg; errorHappened= true; }
a=1/0
.: errorHappened
```

##Type System
###Types
Although DollarScript is typeless at compile time, it does support basic runtime typing. At present this includes: STRING, NUMBER, LIST, MAP, URI, VOID, RANGE, BOOLEAN. The value for a type can be checked using the `is` operator:

```dollar
.: "Hello World" is STRING
.: ["Hello World"] is LIST
```

###Constraints

Although there are no compile type constraints in DollarScript a runtime type system can be built using constraints. Constraints are declared at the time of variable assignment or declaration. A constraint once declared on a variable cannot be changed. The constraint is placed before the variable name at the time of declaration in parenthesis.

```dollar
(it < 100) a = 50
(it > previous || previous is VOID) b = 5
b=6
b=7
( it is STRING) s="String value"
```

The special variables `it` - the current value and `previous` - the previous value, will be available for the constraint.

To build a simple type system simply declare (using `:=`) your type as a boolean expression.

```dollar

//define a pseudo-type
colorEnum := ( it in ["red","green","blue"] )


//Use it as a constraint
(colorEnum) myColor= "green"

error { @@ msg }

//This fails
myColor="apple"

```

Of course since the use of `(it is XXXX)` is very common DollarScript provides a specific runtime type constraint that can be added in conjunction with other constraints. Simply prefix the assignment or decleration with `<XXXX>` where XXXX is the runtime type.


```dollar
<string> (#it > 5) s="String value"
```

### Type Coercion
DollarScript also supports type coercion, this is done using the `as` operator followed by the type to coerce to.


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
1 as number <=> 1

"1" as number <=> 1
"http://google.com" as uri
"1" as VOID <=> void
"true" as boolean <=> true
"1" as boolean <=> false
"1" as list <=> ["1"]
"1" as map <=> {"value":"1"}
"1" as string <=> "1"

true as string <=> "true"
true as number <=> 1
true as list <=> [true]
true as map <=> {"value":true}
true as boolean <=> true
true as VOID <=> void


[1,2,3] as string <=> "[1,2,3]"
[1,2,3] as list <=> [1,2,3]
[1,2,3] as boolean <=> true
[1,2,3] as map <=> {"value":[1,2,3]}

{"a":1,"b":2} as string <=> '{"a":1,"b":2}'
{"a":1,"b":2} as list <=> ["a":1,"b":2]
{"a":1,"b":2} as boolean <=> true
{"a":1,"b":2} as VOID <=> void
```


##Reactive Control Flow

DollarScript as previously mentioned is a reactive programming language, that means that changes to one part of your program can automatically affect another. Consider this a 'push' model instead of the usual 'pull' model.

Let's start with the simplest reactive control flow operator, the '=>' or 'causes' operator.

```dollar

a=0
a => { @@ a } //alternatively for clarity 'a causes {@@ a} '

a=1
a=2
a=3
a=4
a=2

```

When the code is executed we'll see the values 1,2,3,4,2 printed out, this is because whenever `a` changes the block `{ @@ a }` is evaluated, resulting in the variable `a` being printed to stdout. Imagine how useful that is for debugging changes to a variable!

Next we have the 'when' operator, there is no shorthand for this operator, to help keep you code readable:


```dollar

a=1

when a == 2 { @@ a }

a=2
a=3
a=4
a=2

```

This time we'll just see the number 2 twice, this is because the `when` operator triggers the evaluation of the supplied block ONLY when the supplied expression (`a == 2`) becomes true.



##Imperative Control Flow

###If

DollarScript supports the usual imperative control flow but, unlike some languages, everything is an operator. This is the general rule of DollarScript, everything has a value. DollarScript does not have the concept of statements and expressions, just expressions. This means that you can use control flow in an expression.

```dollar

a=1
b= if a==1 2 else 3
b <=> 2

```

So let's start with the `if` operator. The `if` operator is seperate from the `else` operator, it simply evaluates the condition supplied as the first argument. If that value is boolean and true it evaluates the second argument and returns it's value; otherwise it returns boolean false.

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

##Parameters &amp; Functions

In most programming languages you have the concept of functions and parameters, i.e. you can parametrized blocks of code. In DollarScript you can parameterize *anything*. For example let's just take a simple expression that adds two strings together, in reverse order, and pass in two parameters.

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
testParams(first:"Hello", last:"World") <=> "World Hello"
```

Yep you can use named parameters, then refer to the values by the names passed in.


##Resources &amp; URIs

URIs are first class citizen's in DollarScript. They refer to a an arbitrary resource, usually remote, that can be accessed using the specified protocol and location. Static URIs can be referred to directly without quotation marks, dynamic URIs can be built using the `uri` operator

```dollar


search="Unikitty"

dynamicURI= ("camel:http://google.com?q="+search) as uri

marinaVideos = << camel:https://itunes.apple.com/search?term=Marina+And+The+Diamonds&entity=musicVideo
@@ marinaVideos.results each { $1.trackViewUrl }

```

In this example we've requested a single value (using `<+`) from a uri and assigned the value to `marinaVideos` then we simply iterate over the results  using `each` and each value (passed in to the scope as `$1`) we extract the `trackViewUrl`. The each operator returns a list of the results and that is what is passed to standard out.

``

##Using Java

Hopefully you'll find DollarScript a useful and productive language, but there will be many times when you just want to quickly nip out to a bit of Java. To do so, just surround the Java in backticks.

```dollar

variableA="Hello World"

java = `out=scope.get("variableA");`

java <=> "Hello World"

```

A whole bunch of imports are done for you automatically (java.util.*, java.math.*) but you will have to fully qualify any thirdparty libs. The return type will be of type `var` and is stored in the variable `out`. The Java snippet also has access to the scope (me.neilellis.dollar.script.Scope) object on which you can get and set DollarScript variables.

Reactive behaviour is supported on the Scope object with the listen and notify methods on variables. You'll need to then built your reactivity around those variables or on the `out` object directly (that's a pretty advanced topic).


##Operators

###Iterative Operators

###Comparison Operators

###Numerical Operators

DollarScript support the basic numerical operators +,-,/,*,% as well as #

###Logical Operators

DollarScript support the basic logical operators &&,||,! as well as the truthy operator `~`

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

```


### Pipe Operators
### Remaining Operators
## Imports &amp; Modules
###Import
###Module Locators
###UModules

##Builtin Functions

## Advanced Topics
### Concurrency & Threads




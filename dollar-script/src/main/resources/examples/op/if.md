Dollar does not have the concept of statements and expressions, just expressions. This means that you can use control flow in an expression such as:

```dollar

var a=1
var b= if a==1 2 else 3
b <=> 2

```

The `if` operator is separate from the `else` operator, it simply evaluates the condition supplied as the first argument. If that value is boolean and true it evaluates the second argument and returns it's value; otherwise it returns boolean false.

The `else` operator is a binary operator which evaluates the left-hand-side (usually the result of an `if` statement), if that has a value of false then the right-hand-side is evaluated and it's result returned, otherwise it returns the left-hand-side.

The combined effect of these two operators is to provide the usual if/else/else if/ control flow



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



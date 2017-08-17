
Support for some aspects of functional programming are included in Dollar. Primarily the idea of pure expressions using the `pure` operator. This signals that an expression or declaration is a pure expression or declaration.

In the example we're declaring reverse to be an expression that reverses two values from a supplied array. Because we declare it as `pure` the expression supplied must also be `pure`. To understand what a pure function is please see [http://en.wikipedia.org/wiki/Pure_function](http://en.wikipedia.org/wiki/Pure_function). Basically it prohibits the reading of external state or the setting of external state. We next swap `[2,1]` within a newly created pure expression, which is subsequently assigned to a. If reverse had not been declared pure it would not be allowed within the pure expression.


Note some builtin functions are not themselves pure and will trigger parser errors if you attempt to use them in a pure expression. Take DATE() for example which supplies an external state (the computers clock).

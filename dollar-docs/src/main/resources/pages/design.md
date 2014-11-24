#Design Goals

##Familiarity

The DollarScript language has been designed with the syntax of JavaScript and Java firmly in mind to provide familiarity to developers of those languages. So much so that JSON is valid DollarScript.

##Clarity

Confusing abbreviations are avoided, and although a rich variety of operators are available they are usually paired with an english language keyword for clarity.

##Concurrency

DollarScript is designed for concurrency. All structures are immutable (although they can have dynamic values) and all variables can only be reassigned from the thread in which they are defined. This provides all the concurrency safety needed for your application.

Any part of your application can be marked as having a parallel scope, which means that operations which take place within that scope can be executed in parallel. By default all operations take place on a single thread.


##Reactive


##Expressive

Everything in DollarScript is an expression.

Values can be assigned to a variable using the standard `=` assignment operator. A variable is declared using either 'var', 'const' or 'volatile'. 

`var` is used to mark a simple declaration of a mutable variable. E.g. `var a = 1; a= 2`. 

`const` is used to declare a readonly variable, since all values in Dollar are immutable this makes the variable both readonly and immutable.

`volatile` is used to declare the variable as mutable from multiple threads.

Although there are no compile type constraints in Dollar a runtime type system can be built using constraints. Constraints are declared at the time of variable assignment or declaration. A constraint once declared on a variable cannot be changed. The constraint is placed before the variable name at the time of declaration in parenthesis.
 
 Of course since the use of `(it is XXXX)` is very common, so Dollar provides a specific runtime type constraint that can be added in conjunction with other constraints. Simply prefix the assignment or decleration with `<XXXX>` where XXXX is the runtime type.
 
 Values can be also be marked for `export` as in Javascript.

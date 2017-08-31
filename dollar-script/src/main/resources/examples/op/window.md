The window operator provides a time window based view of a changing value.

The first part of the window operator is the expression this will be listened to for changes and it's value passed into the windowing process. 

The next part is the `over` clause which determines the duration over which changes are captured. Any change that is older than this duration is discarded.

Following this is the optional `period` clause which determines how often the the window is calculated and the window-expression evaluated. If it is not supplied it defaults to the value in the `over` clause.

Next is the optional `unless` clause which specifies which changes to ignore, if this clause is true then the change will be ignored.

Then the optional `until` clause which specifies a condition for when the windowing should stop completely.

Finally the window-expression is the expression which is evaluated with the following variables available:

* `count` a value that increments on every window-expression evaluation
* `collected` a list of values that have been windowed across the duration of the `over` clause and which have not been excluded by the `unless` clause. 

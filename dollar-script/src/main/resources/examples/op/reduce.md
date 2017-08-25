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


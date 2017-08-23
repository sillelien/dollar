Declares a variable to have a value, this is declarative and reactive such that saying `const a := b + 1` means that `a` always equals `b+1` no matter the value of b. The shorthand `def` is the same as `const <variable-name> :=` so `def a {b+1}` is the same as `const a := b + 1` but is syntactically better when declaring function like variables.

Declarations can also be marked as pure so that they can be used in pure scopes, this is done by prefixing the declaration with `pure`.

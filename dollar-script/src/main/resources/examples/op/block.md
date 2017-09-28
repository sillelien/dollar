A line block lies between `{` and `}` and is separated by either newlines or `;` characters.

```dollar

var myBlock = {
    "Hello "
    "World"
}

myBlock <=> "World"

const myBlock2 = {1;2}

myBlock2 <=> 2

```

When a line block is evaluated the result is the value of the last entry. For advanced users note that all lines will be evaluated, the value is just ignored. A line block behaves a lot like a function in an imperative language.

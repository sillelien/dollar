Operators

1000
&      + fix value
$       + variable reference

500
[]      + subscript
()      + parameterize
.       + member access

400 unary priority
++      + inc
--      + dec
-       + unary minus
+       - unary plus (no point!)
~       - NOT USED  - reserved - possibly regex
^       - NOT USED  - reserved
&       - NOT USED  - reserved - possibly to indicate parallelism preferred ?
`       - NOT USED  - rexerved - possibly Java strings
!       + logical negation
&       - attribute prefix


300 - /,*,%
*       + multiply
/       + divide
%       + modulus 


200 - +,-
+       + addition
-       + subtraction

100 - equivalence
==      + equals
!=      + not equals
<=>     + assert equivalence



80 - pipe
|       + pipe

80 cast
uri     + cast String to URI

70 - &&
60 - || 


50 - output
<$      + file in - change to (U)
$>      + file out
|>      + save
<|      + load - change to (U)
+>      + receive
&>      + notify
<+      + take  (U)

50 - control flow
?       + if
->      + causes
?->     + choose
?:      + default


30
:       + pair

2 - assignment
=       + assignment
:=      + declaration

sleep   + sleep keyword


0 - Line Prefix
@@  - stdout
!! - debug
?? - stderr
.: - assert

TODO

?+ ANY, ?* ALL

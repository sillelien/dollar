Operators

1000
<>      + fix
$       + variable

500
[]      + subscript
()      + parameterise
.       + member access

400 unary priority
++      + inc
--      + dec
+/-     - unary plus/minus
~       + NOT USED
^       + NOT USED
&       + NOT USED
`       + NOT USED
!       + logical negation
@       - attribute prefix


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
<$      + file out
$>      + file in
|>      + save
<|      + load
+>      + receive
&>      + notify
<+      + take  (B)

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
>>  - stdout
!! - debug
?? - stderr
=> - assert

Keywords


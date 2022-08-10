grammar Jepeto;

jepeto:
    function*
    mainFunc
    function*
    EOF
;

mainFunc:
    MAIN COLON
    { System.out.println("Main");}
    (functionCall {System.out.println("FunctionCall");} | print) SEMICOLON
;

print:
    PRINT
    {System.out.println("Built-in : " + $PRINT.text);}
    LPAR expression RPAR
;

function:
    FUNC FunctionName = IDENTIFIER
    {System.out.println("FunctionDec : " + $FunctionName.text);}
    LPAR arguments? RPAR COLON
    (functionBody | returnStatement)
;

arguments:
    ArgumentName = IDENTIFIER
    {System.out.println("ArgumentDec : " + $ArgumentName.text);}
    (COMMA arguments)?
;

functionBody:
    (LCURLY codeLine (codeLine)* RCURLY) |
    codeLine
;

codeLine:
    ifStatement | simpleCommand
;

simpleCommand:
    (
        {System.out.println("FunctionCall");} functionCall
        | returnStatement
        | print
    ) SEMICOLON
;

returnStatement:
    RETURN {System.out.println("Return");} (VOID | expression)
;

ifStatement:
    IF {System.out.println("Conditional : if");} expression COLON innerBlock
    (ELSE {System.out.println("Conditional : else");} COLON innerBlock)?
;

curlyBlockWithoutVarDeclaration:
    LCURLY
        codeLine*
    RCURLY
;

innerBlock:
    curlyBlockWithoutVarDeclaration | codeLine
;

anonymousFunc:
    LPAR {System.out.println("Anonymous Function");} arguments? RPAR ARROW
    LCURLY codeLine (codeLine)* RCURLY
    (LPAR callArguments? RPAR)*
;

functionCall:
    (IDENTIFIER call) | anonymousFunc
;

call:
    LPAR callArguments? RPAR call?
;

callArguments:
    ((expression) (COMMA callArgumentsWithoutAssign)?) |
    ((IDENTIFIER ASSIGN expression) (COMMA callArgumentsWithAssign)?)
;

callArgumentsWithoutAssign:
    (expression) (COMMA callArguments)?
;

callArgumentsWithAssign:
    (IDENTIFIER ASSIGN expression) (COMMA callArgumentsWithAssign)?
;

expression:
    expressionWithoutOr (OR expressionWithoutOr { System.out.println("Operator : " + $OR.text);})*
;

expressionWithoutOr:
    expressionWithoutAnd (AND expressionWithoutAnd { System.out.println("Operator : " + $AND.text);})*
;

expressionWithoutAnd:
    expressionWithoutEquality
    (equalityOperator expressionWithoutEquality { System.out.println("Operator : " + $equalityOperator.text);})*
;

expressionWithoutEquality:
    expressionWithoutComparison
    (comparisonOperator expressionWithoutComparison { System.out.println("Operator : " + $comparisonOperator.text);})*
;

expressionWithoutComparison:
    expressionWithoutAddSub
    (addSubOperator expressionWithoutAddSub { System.out.println("Operator : " + $addSubOperator.text);})*
;

expressionWithoutAddSub:
    expressionWithoutMulDiv
    (mulDivOperator expressionWithoutMulDiv { System.out.println("Operator : " + $mulDivOperator.text);})*
;

expressionWithoutMulDiv:
    prefixOperator expressionWithoutMulDiv { System.out.println("Operator : " + $prefixOperator.text);}
    | expressionWithoutPrefix
;

expressionWithoutPrefix:
    expressionWithoutConcate
    (concateOperator expressionWithoutConcate { System.out.println("Operator : " + $concateOperator.text);})*
;

expressionWithoutConcate:
    expressionWithoutPostfix (postfixOperator { System.out.println("Size");})?
;

expressionWithoutPostfix:
    arithmeticStatementWithoutBrackets ((LBRACK expression RBRACK) + expressionWithoutPostfix?)?
;

arithmeticStatementWithoutBrackets:
    LPAR expression RPAR | IDENTIFIER
    | functionCall | INTEGER_LITERAL | BOOL_LITERAL | STRING_LITERAL  | NULL
    | listLiteral
;

primitiveType: INT | STRING | BOOL;

equalityOperator: EQ | NE;

comparisonOperator: GT | LT;

addSubOperator: ADD | SUB;

mulDivOperator: MUL | DIV;

prefixOperator: SUB | NOT;

postfixOperator: DOT SIZE;

concateOperator: CONCATE;

variableType: primitiveType | IDENTIFIER;

listLiteral:
    LBRACK
        listLiteralElements?
    RBRACK
;

listLiteralElements:
    listLiteral | expression
    (COMMA listLiteralElements)*
;

// Literals
INTEGER_LITERAL: Digit+;
BOOL_LITERAL: TRUE | FALSE;
STRING_LITERAL: '"' (~["\r\n])* '"';

//Tokens
IN: 'in';
IF: 'if';
ELSE: 'else';
MAIN: 'main';
FUNC: 'func';
NULL: 'null';
PRINT: 'print';
RETURN: 'return';

TRUE: 'true';
FALSE: 'false';

INT: 'int';
BOOL: 'bool';
VOID: 'void';
//LIST: 'list';
STRING: 'string';

LPAR: '(';
RPAR: ')';
LBRACK: '[';
RBRACK: ']';
LCURLY: '{';
RCURLY: '}';

DOT: '.';
COMMA: ',';
COLON: ':';
HASHTAG: '#';
CONCATE: '::';
SEMICOLON: ';';
SIZE: 'size';

ADD: '+';
SUB: '-';
MUL: '*';
DIV: '/';
AND: 'and';
OR: 'or';
NOT: '~';
EQ: 'is';
NE: 'not';
GT: '>';
LT: '<';
ARROW: '->';
ASSIGN: '=';

IDENTIFIER: Letter (Letter | Digit)*;

// Whitespace and comments
WS: [ \t\r\n]+ -> skip;
COMMENT: '#' ~[\r\n]* -> skip;

// Fragments
fragment Digit: [0-9];
fragment Letter: [a-zA-Z_];
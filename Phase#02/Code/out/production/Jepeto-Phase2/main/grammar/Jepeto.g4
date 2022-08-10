grammar Jepeto;

@header{
    import main.ast.nodes.*;
    import main.ast.nodes.declaration.*;
    import main.ast.nodes.expression.*;
    import main.ast.nodes.expression.operators.*;
    import main.ast.nodes.expression.values.*;
    import main.ast.nodes.expression.values.primitive.*;
    import main.ast.nodes.statement.*;
    import java.util.*;
}

jepeto returns [Program jepetoProgram]:
 p = program
 {$jepetoProgram = $p.programRet;}
 EOF
 ;


program returns [Program programRet]:
 {
    $programRet = new Program();
    $programRet.setLine(1);
 }
 (
    fd = functionDeclaration
    { $programRet.addFunction($fd.functionDeclarationRet); }
 )*
 m = main
 { $programRet.setMain($m.mainDeclarationRet); }
 (
    fd = functionDeclaration
    { $programRet.addFunction($fd.functionDeclarationRet); }
 )*
 ;


functionDeclaration returns [FunctionDeclaration functionDeclarationRet]:
 f = FUNC
 {
    $functionDeclarationRet = new FunctionDeclaration();
    $functionDeclarationRet.setLine($f.getLine());
 }
 n = identifier
 { $functionDeclarationRet.setFunctionName($n.idRet); }
 fad = functionArgumentsDeclaration
 { $functionDeclarationRet.setArgs($fad.argsRet); }
 COLON
 b = body
 { $functionDeclarationRet.setBody($b.bodyRet); }
 ;


functionArgumentsDeclaration returns [ArrayList<Identifier> argsRet, int line]:
 l = LPAR
 {
    $argsRet = new ArrayList<Identifier>();
    $line = $l.line;

 }
 (
    idt = identifier
    { $argsRet.add($idt.idRet); }
    (
        COMMA
        idt2 = identifier
        { $argsRet.add($idt2.idRet); }
    )*
 )?
 RPAR
 ;


body returns [Statement bodyRet]:
 ss = singleStatement
 { $bodyRet = $ss.singleStatementRet; }
 | b = block
 { $bodyRet = $b.blockRet; }
 ;


main returns [MainDeclaration mainDeclarationRet]:
 m = MAIN
 {
    $mainDeclarationRet = new MainDeclaration();
    $mainDeclarationRet.setLine($m.line);
 }
 COLON
 (
    fcs = functionCallStatement
    { $mainDeclarationRet.setBody($fcs.functionCallStatementRet); }
    | ps = printStatement
    { $mainDeclarationRet.setBody($ps.printStatementRet); }
 )
 ;


functionCall returns [FunctionCall functionCallRet, int line]: // NOT SO SURE ABOUT THIS!
 ae = accessExpression
 (
    l = LPAR
    fa = functionArguments
    {
        $functionCallRet = new FunctionCall($ae.accessExpressionRet, $fa.argsRet, $fa.argsWithKeyRet);
        $functionCallRet.setLine($l.line);
        $line = $l.line;
    }
    RPAR
 )
 ;


functionArguments returns[ArrayList<Expression> argsRet, Map<Identifier, Expression> argsWithKeyRet]:
 sewc = splitedExpressionsWithComma //null set line??
 {
    $argsRet = new ArrayList<Expression> ();
    $argsRet = $sewc.argsRet;
    $argsWithKeyRet = new LinkedHashMap<Identifier, Expression> ();
 }
 |
 sewck = splitedExpressionsWithCommaAndKey //null set line??
 {
    $argsRet = new ArrayList<Expression> ();
    $argsWithKeyRet = new LinkedHashMap<Identifier, Expression> ();
    $argsWithKeyRet = $sewck.argsWithKeyRet;
 }
 ;


splitedExpressionsWithComma returns[ArrayList<Expression> argsRet]:
 { $argsRet = new ArrayList<Expression>(); }
 (
    exp = expression
    { $argsRet.add($exp.expressionRet); }
    (
        COMMA
        exp = expression
        { $argsRet.add($exp.expressionRet); }
    )*
 )?
 ;


splitedExpressionsWithCommaAndKey returns[Map <Identifier, Expression> argsWithKeyRet]:
 (
    idt = identifier
    ASSIGN
    exp = expression
    {
        $argsWithKeyRet = new LinkedHashMap<Identifier, Expression> ();
        $argsWithKeyRet.put($idt.idRet, $exp.expressionRet);
    }
    (
        COMMA
        idt = identifier
        ASSIGN
        exp = expression
        { $argsWithKeyRet.put($idt.idRet, $exp.expressionRet); }
    )*
 )?
 ;


functionCallStatement returns[Statement functionCallStatementRet]:
 fc = functionCall
 {
    $functionCallStatementRet = new FunctionCallStmt($fc.functionCallRet);
    $functionCallStatementRet.setLine($fc.line);
 }
 SEMICOLLON
 ;

returnStatement returns[ReturnStmt returnStatementRet]:
 r = RETURN
 {
    $returnStatementRet = new ReturnStmt();
    $returnStatementRet.setLine($r.line);
 }
 (
    expr = expression
    { $returnStatementRet.setReturnedExpr($expr.expressionRet); }
    |
    v = voidValue
    {
      $returnStatementRet.setReturnedExpr($v.voidValueRet);
    }
 )
 SEMICOLLON
 ;


ifStatement returns[ConditionalStmt ifStatementRet]:
 i = IF
 expr = expression
 COLON
 cb1 = conditionBody
 {
    $ifStatementRet = new ConditionalStmt($expr.expressionRet, $cb1.conditionalBodyRet);
    $ifStatementRet.setLine($i.line);
 }
 (
    ELSE
    COLON
    cb2 = conditionBody
    { $ifStatementRet.setElseBody($cb2.conditionalBodyRet); }
 )?
 ;


ifStatementWithReturn returns[ConditionalStmt ifStatementWithReturnRet]:
 i = IF
 expr = expression
 COLON
 b1 = body
 ELSE
 COLON
 b2 = body
 {
    $ifStatementWithReturnRet = new ConditionalStmt($expr.expressionRet, $b1.bodyRet);
    $ifStatementWithReturnRet.setLine($i.line);
    $ifStatementWithReturnRet.setElseBody($b2.bodyRet);
 }
 ;


printStatement returns[PrintStmt printStatementRet]:
 p = PRINT
 l = LPAR
 expr = expression
 {
    $printStatementRet = new PrintStmt($expr.expressionRet);
    $printStatementRet.setLine($l.line);
 }
 RPAR
 SEMICOLLON
 ;


statement returns[Statement statementRet]:
 is = ifStatement
 { $statementRet = $is.ifStatementRet; }
 |
 ps = printStatement
 { $statementRet = $ps.printStatementRet; }
 |
 fcs = functionCallStatement
 { $statementRet = $fcs.functionCallStatementRet; }
 |
 rs = returnStatement
 { $statementRet = $rs.returnStatementRet; }
 ;


singleStatement returns[Statement singleStatementRet]:
 rs = returnStatement
 { $singleStatementRet = $rs.returnStatementRet; }
 |
 ifswr = ifStatementWithReturn
 { $singleStatementRet = $ifswr.ifStatementWithReturnRet; }
 ;


block returns[BlockStmt blockRet]:
 l = LBRACE
 {
    $blockRet = new BlockStmt();
    $blockRet.setLine($l.line);
 }
 (
    (
        s = statement
        { $blockRet.addStatement($s.statementRet); }
    )*
    (
        rs = returnStatement
        { $blockRet.addStatement($rs.returnStatementRet); }
        |
        ifswr = ifStatementWithReturn
        { $blockRet.addStatement($ifswr.ifStatementWithReturnRet); }
    )
    (
        s = statement
        { $blockRet.addStatement($s.statementRet); }
    )*
 )
 RBRACE
 ;


conditionBody returns[Statement conditionalBodyRet]:
 (
    l = LBRACE
    {
        BlockStmt tempBlock = new BlockStmt();
        tempBlock.setLine($l.line);
    }
    (
        s = statement
        { tempBlock.addStatement($s.statementRet); }
    )*
    RBRACE
    { $conditionalBodyRet = tempBlock; }
 )
 |
 s = statement
 { $conditionalBodyRet = $s.statementRet; }
 ;


expression returns[Expression expressionRet]:
 ae1 = andExpression
 { $expressionRet = $ae1.andExpressionRet; }
 (
    or = OR
    ae2 = andExpression
    {
        BinaryOperator op = BinaryOperator.or;
        $expressionRet = new BinaryExpression($expressionRet, $ae2.andExpressionRet, op);
        $expressionRet.setLine($or.getLine());
    }
 )*
 ;


andExpression returns[Expression andExpressionRet]
    locals [BinaryOperator op]:
 ee1 = equalityExpression
 {
    $andExpressionRet = $ee1.equalityExpressionRet;
    $op = BinaryOperator.and;
 }
 (
    and = AND
    ee2 = equalityExpression
    {
        BinaryOperator op = BinaryOperator.and;
        $andExpressionRet = new BinaryExpression($andExpressionRet, $ee2.equalityExpressionRet, $op);
        $andExpressionRet.setLine($and.getLine());
    }
 )*
 ;


equalityExpression returns[Expression equalityExpressionRet]
 locals[BinaryOperator op, int line]:
 re1 = relationalExpression
 { $equalityExpressionRet = $re1.relationalExpressionRet; }
 (
    (
        eq = EQUAL
        {
            $op = BinaryOperator.eq;
            $line = $eq.getLine();
        }
        |
        neq = NOT_EQUAL
        {
            $op = BinaryOperator.neq;
            $line = $neq.getLine();
        }
    )
 re2 = relationalExpression
 {
    $equalityExpressionRet = new BinaryExpression($equalityExpressionRet, $re2.relationalExpressionRet, $op);
    $equalityExpressionRet.setLine($line);
 }
 )*
 ;


relationalExpression returns[Expression relationalExpressionRet]
 locals [BinaryOperator op, int line]:
 ae1 = additiveExpression
 { $relationalExpressionRet = $ae1.additiveExpressionRet; }
 (
    (
        gt = GREATER_THAN
        {
            $op = BinaryOperator.gt;
            $line = $gt.getLine();
        }
        |
        lt = LESS_THAN
        {
            $op = BinaryOperator.lt;
            $line = $lt.getLine();
        }
    )
 ae2 = additiveExpression
 {
    $relationalExpressionRet = new BinaryExpression($relationalExpressionRet, $ae2.additiveExpressionRet, $op);
    $relationalExpressionRet.setLine($line);
 }
 )*
 ;


additiveExpression returns[Expression additiveExpressionRet]
 locals[BinaryOperator op, int line]:
 me1 = multiplicativeExpression
 { $additiveExpressionRet = $me1.multiplicativeExpressionRet; }
 (
    (
        p = PLUS
        {
            $op = BinaryOperator.add;
            $line = $p.getLine();
        }
        |
        m = MINUS
        {
            $op = BinaryOperator.sub;
            $line = $m.getLine();
        }
    )
 me2 = multiplicativeExpression
 {
    $additiveExpressionRet = new BinaryExpression($additiveExpressionRet, $me2.multiplicativeExpressionRet, $op);
    $additiveExpressionRet.setLine($line);
 }
 )*
 ;


multiplicativeExpression returns[Expression multiplicativeExpressionRet]
 locals [BinaryOperator op, int line]:
 pe1 = preUnaryExpression
 { $multiplicativeExpressionRet = $pe1.preUnaryExpressionRet; }
 (
    (
        m = MULT
        {
            $op = BinaryOperator.mult;
            $line = $m.getLine();
        }
        |
        d = DIVIDE
        {
            $op = BinaryOperator.div;
            $line = $d.getLine();
        }
    )
 pe2 = preUnaryExpression
 {
    $multiplicativeExpressionRet = new BinaryExpression($multiplicativeExpressionRet, $pe2.preUnaryExpressionRet, $op);
    $multiplicativeExpressionRet.setLine($line);
 }
 )*
 ;


preUnaryExpression returns[Expression preUnaryExpressionRet]
 locals[UnaryOperator op, int line]:
 (
    (
        n = NOT
        {
            $op = UnaryOperator.not;
            $line = $n.getLine();
        }
        |
        m = MINUS
        {
            $op = UnaryOperator.minus;
            $line = $m.getLine();
        }
    )
    pue = preUnaryExpression
    {
        $preUnaryExpressionRet = new UnaryExpression($pue.preUnaryExpressionRet, $op);
        $preUnaryExpressionRet.setLine($line);
    }
 )
 |
 ae = appendExpression
 { $preUnaryExpressionRet = $ae.appendExpressionRet; }
 ;

appendExpression returns[Expression appendExpressionRet]:
 ae1 = accessExpression
 { $appendExpressionRet = $ae1.accessExpressionRet; }
 (
    a = APPEND
    ae2 = accessExpression
    {
        BinaryOperator op = BinaryOperator.append;
        $appendExpressionRet = new BinaryExpression($appendExpressionRet, $ae2.accessExpressionRet, op);
        $appendExpressionRet.setLine($a.getLine());
    }
 )*
 ;


accessExpression returns[Expression accessExpressionRet]:
 oe = otherExpression
 { $accessExpressionRet = $oe.otherExpressionRet; }
 (
    l = LPAR
    fa = functionArguments
    rp = RPAR
    {
        $accessExpressionRet = new FunctionCall($accessExpressionRet, $fa.argsRet, $fa.argsWithKeyRet);
        $accessExpressionRet.setLine($l.line);
    }
 )*
 (
    lb = LBRACK
    e = expression
    rb = RBRACK
    {
        $accessExpressionRet = new ListAccessByIndex($accessExpressionRet, $e.expressionRet);
        $accessExpressionRet.setLine($lb.getLine());
    }
 )*
 (
    se = sizeExpression
    {
        $accessExpressionRet = new ListSize($accessExpressionRet);
        $accessExpressionRet.setLine($se.line);
    } //setsize to setline!!
 )*
 ;


otherExpression returns[Expression otherExpressionRet]:
 v = values
 { $otherExpressionRet = $v.valuesRet; }
 | id = identifier
 { $otherExpressionRet = $id.idRet; }
 | af = anonymousFunction
 { $otherExpressionRet = $af.anonymousFunctionRet; }
 |LPAR (e = expression) RPAR
 { $otherExpressionRet =  $e.expressionRet; }
 ;


anonymousFunction returns[AnonymousFunction anonymousFunctionRet]:
 fad = functionArgumentsDeclaration
 {
    $anonymousFunctionRet = new AnonymousFunction($fad.argsRet);
    $anonymousFunctionRet.setLine($fad.line);
 }
 ARROW
 b = block
 { $anonymousFunctionRet.setBody($b.blockRet); }
 ;


sizeExpression returns[int line]:
 d = DOT
 SIZE
 { $line = $d.getLine(); }
 ;


values returns[Expression valuesRet]:
 b = boolValue
 {
    $valuesRet = new BoolValue($b.boolValueRet);
    $valuesRet.setLine($b.line);
 }
 | sv = STRING_VALUE
 {

    $valuesRet = new StringValue(($sv.text).substring(1, ($sv.text).length() - 1));
    $valuesRet.setLine($sv.getLine());
 }
 | iv = INT_VALUE
 {
    $valuesRet = new IntValue($iv.int);
    $valuesRet.setLine($iv.getLine());
 }
 | lv = listValue
 { $valuesRet = $lv.listValueRet; }
 ;

listValue returns[ListValue listValueRet]:
 l = LBRACK
 se = splitedExpressionsWithComma
 {
    $listValueRet = new ListValue($se.argsRet);
    $listValueRet.setLine($l.line);
 }
 RBRACK;

boolValue returns[boolean boolValueRet, int line]:
    t=TRUE
    {
        $boolValueRet = true;
        $line = $t.getLine();
    }
    | f=FALSE
    {
        $boolValueRet = false;
        $line = $f.getLine();
    }
    ;

voidValue returns[Value voidValueRet]:
 vo = VOID
 {
    $voidValueRet = new VoidValue();
    $voidValueRet.setLine($vo.getLine());
 };


identifier returns[Identifier idRet, int line]:
 id=IDENTIFIER
 {
    $idRet = new Identifier($id.text);
    $idRet.setLine($id.getLine());
    $line = $id.getLine();
 }
 ;


FUNC: 'func';
MAIN: 'main';
SIZE: 'size';

PRINT: 'print';
RETURN: 'return';
VOID: 'void';

IF: 'if';
ELSE: 'else';

PLUS: '+';
MINUS: '-';
MULT: '*';
DIVIDE: '/';

EQUAL: 'is';
NOT_EQUAL: 'not';
GREATER_THAN: '>';
LESS_THAN: '<';

AND: 'and';
OR: 'or';
NOT: '~';

APPEND: '::';

TRUE: 'true';
FALSE: 'false';

ARROW: '->';

ASSIGN: '=';

LPAR: '(';
RPAR: ')';
LBRACK: '[';
RBRACK: ']';
LBRACE: '{';
RBRACE: '}';

COMMA: ',';
DOT: '.';
COLON: ':';
SEMICOLLON: ';';

INT_VALUE: '0' | [1-9][0-9]*;
IDENTIFIER: [a-zA-Z_][A-Za-z0-9_]*;
STRING_VALUE: '"'~["]*'"';
COMMENT: ('#' ~( '\r' | '\n')*) -> skip;
WS: ([ \t\n\r]) -> skip;

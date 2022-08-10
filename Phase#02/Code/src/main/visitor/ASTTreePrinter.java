package main.visitor;

import main.ast.nodes.Program;
import main.ast.nodes.declaration.FunctionDeclaration;
import main.ast.nodes.declaration.MainDeclaration;
import main.ast.nodes.expression.*;
import main.ast.nodes.expression.values.ListValue;
import main.ast.nodes.expression.values.VoidValue;
import main.ast.nodes.expression.values.primitive.BoolValue;
import main.ast.nodes.expression.values.primitive.IntValue;
import main.ast.nodes.expression.values.primitive.StringValue;
import main.ast.nodes.statement.*;
import main.compileErrors.nameErrors.ArgumentNotDeclared;
import main.symbolTable.exceptions.ItemNotFoundException;
import main.symbolTable.items.VariableSymbolTableItem;

import java.util.ArrayList;
import java.util.Map;

public class ASTTreePrinter extends Visitor<Void> {

    public void messagePrinter(int line, String message){
        System.out.println("Line " + line + ": " + message);
    }

    @Override
    public Void visit(Program program) {
        messagePrinter(program.getLine(), program.toString());

        ArrayList<FunctionDeclaration> functions = program.getFunctions();
        MainDeclaration mainFunc = program.getMain();

        mainFunc.accept(this);

        if (functions != null) {
            for (FunctionDeclaration jepetoFunction : functions) {
                jepetoFunction.accept(this);
            }
        }
        //main

        return null;
    }

    @Override
    public Void visit(FunctionDeclaration funcDeclaration) {
        messagePrinter(funcDeclaration.getLine(), funcDeclaration.toString());

        Identifier functionName = funcDeclaration.getFunctionName();
        if (functionName != null) {
            functionName.accept(this);
        }

        ArrayList<Identifier> args = funcDeclaration.getArgs();
        if (args != null) {
            for (Identifier arg : args) {
                arg.accept(this);
            }
        }

        Statement bodyStmts = funcDeclaration.getBody();
        if (bodyStmts != null) {
            bodyStmts.accept(this);
        }

        return null;
    }

    @Override
    public Void visit(MainDeclaration mainDeclaration) {
        messagePrinter(mainDeclaration.getLine(), mainDeclaration.toString());

        Statement bodyStmts = mainDeclaration.getBody();
        if (bodyStmts != null) {
            bodyStmts.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(BlockStmt blockStmt) {
        messagePrinter(blockStmt.getLine(), blockStmt.toString());

        ArrayList<Statement> stmts = blockStmt.getStatements();
        if (stmts != null) {
            for (Statement stmt : stmts) {
                stmt.accept(this);
            }
        }
        return null;
    }

    @Override
    public Void visit(ConditionalStmt conditionalStmt) {
        messagePrinter(conditionalStmt.getLine(), conditionalStmt.toString());

        Expression condition = conditionalStmt.getCondition();
        if (condition != null) {
            condition.accept(this);
        }

        Statement thenBody = conditionalStmt.getThenBody();
        if (thenBody != null) {
            thenBody.accept(this);
        }

        Statement elseBody = conditionalStmt.getElseBody();
        if (elseBody != null) {
            elseBody.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(FunctionCallStmt funcCallStmt) {
        messagePrinter(funcCallStmt.getLine(), funcCallStmt.toString());

        FunctionCall functionCall = funcCallStmt.getFunctionCall();
        if (functionCall != null) {
            functionCall.accept(this);
        }

        return null;
    }

    @Override
    public Void visit(PrintStmt print) {
        messagePrinter(print.getLine(), print.toString());

        Expression arg = print.getArg();
        if (arg != null) {
            arg.accept(this);
        }

        return null;
    }

    @Override
    public Void visit(ReturnStmt returnStmt) {
        messagePrinter(returnStmt.getLine(), returnStmt.toString());

        Expression returnedExpr = returnStmt.getReturnedExpr();
        if (returnedExpr != null) {
            returnedExpr.accept(this);
        }

        return null;
    }

    @Override
    public Void visit(BinaryExpression binaryExpression) {
        messagePrinter(binaryExpression.getLine(), binaryExpression.toString());

        Expression firstOperand = binaryExpression.getFirstOperand();
        if (firstOperand != null) {
            firstOperand.accept(this);
        }

        Expression secondOperand = binaryExpression.getSecondOperand();
        if (secondOperand != null) {
            secondOperand.accept(this);
        }

        return null;
    }

    @Override
    public Void visit(UnaryExpression unaryExpression) {
        messagePrinter(unaryExpression.getLine(), unaryExpression.toString());

        Expression operand = unaryExpression.getOperand();
        if (operand != null) {
            operand.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(AnonymousFunction anonymousFunction) {
        messagePrinter(anonymousFunction.getLine(), anonymousFunction.toString());

        ArrayList<Identifier> args = anonymousFunction.getArgs();

        if (args != null) {
            for (Identifier arg : args) {
                arg.accept(this);
            }
        }

        Statement bodyStmts = anonymousFunction.getBody();
        if (bodyStmts != null) {
            bodyStmts.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(Identifier identifier) {
        messagePrinter(identifier.getLine(), identifier.toString());
        return null;
    }

    @Override
    public Void visit(ListAccessByIndex listAccessByIndex) {
        messagePrinter(listAccessByIndex.getLine(), listAccessByIndex.toString());

        Expression instance = listAccessByIndex.getInstance();
        if (instance != null) {
            instance.accept(this);
        }

        Expression index = listAccessByIndex.getIndex();
        if (index != null) {
            index.accept(this);
        }
        return null;
    }

    //Not Sure
    @Override
    public Void visit(ListSize listSize) {
        messagePrinter(listSize.getLine(), listSize.toString());

        Expression instance = listSize.getInstance();
        if (instance != null) {
            instance.accept(this);
        }

        return null;
    }

    @Override
    public Void visit(FunctionCall funcCall) {
        messagePrinter(funcCall.getLine(), funcCall.toString());

        Expression instance = funcCall.getInstance();
        if(instance != null) {
            instance.accept(this);
        }

        ArrayList<Expression> args = funcCall.getArgs();
        if(args != null && args.size() > 0) {
            for (Expression arg : args) {
                arg.accept(this);
            }
        }
        else {
            Map<Identifier, Expression> argsWithKey = funcCall.getArgsWithKey();
            for (Map.Entry<Identifier, Expression> entry : argsWithKey.entrySet()) {
                entry.getKey().accept(this);
                entry.getValue().accept(this);
            }
        }
        return null;
    }

    @Override
    public Void visit(ListValue listValue) {
        messagePrinter(listValue.getLine(), listValue.toString());

        ArrayList<Expression> elements = listValue.getElements();
        if(elements != null) {
            for (Expression element : elements) {
                element.accept(this);
            }
        }

        return null;
    }

    @Override
    public Void visit(IntValue intValue) {
        messagePrinter(intValue.getLine(), intValue.toString());
        return null;
    }

    @Override
    public Void visit(BoolValue boolValue) {
        messagePrinter(boolValue.getLine(), boolValue.toString());
        return null;
    }

    @Override
    public Void visit(StringValue stringValue) {
        messagePrinter(stringValue.getLine(), stringValue.toString());
        return null;
    }

    @Override
    public Void visit(VoidValue voidValue) {
        messagePrinter(voidValue.getLine(), voidValue.toString());
        return null;
    }
}
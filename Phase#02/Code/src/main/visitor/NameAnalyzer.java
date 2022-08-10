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

import main.symbolTable.SymbolTable;
import main.symbolTable.items.FunctionSymbolTableItem;
import main.symbolTable.exceptions.ItemAlreadyExistsException;

import main.compileErrors.nameErrors.DuplicateFunction;
import java.util.ArrayList;
import java.util.Map;

public class NameAnalyzer extends Visitor<Void> {
    @Override
    public Void visit(Program program) {
        SymbolTable.push(new SymbolTable());
        SymbolTable.root = SymbolTable.top;

        ArrayList<FunctionDeclaration> functions = program.getFunctions();
        MainDeclaration mainFunc = program.getMain();

        if (functions != null) {
            for (FunctionDeclaration jepetoFunction : functions) {
                jepetoFunction.accept(this);
            }
        }
        //main
        mainFunc.accept(this);
        return null;
    }

    @Override
    public Void visit(FunctionDeclaration functionDeclaration) {
        FunctionSymbolTableItem functionItem = new FunctionSymbolTableItem(functionDeclaration);
        SymbolTable.push(new SymbolTable());
        functionItem.setFunctionSymbolTable(SymbolTable.top);

        String name = functionDeclaration.getFunctionName().getName();

        try {
            SymbolTable.root.put(functionItem);
        } catch (ItemAlreadyExistsException e) {
            DuplicateFunction exception = new DuplicateFunction(functionDeclaration.getLine(), name);
            functionDeclaration.addError(exception);
            System.out.println(exception.getMessage());
            exception.handleException(functionDeclaration);
        }

        Identifier functionName = functionDeclaration.getFunctionName();
        if (functionName != null) {
            functionName.accept(this);
        }

        ArrayList<Identifier> args = functionDeclaration.getArgs();
        if (args != null) {
            for (Identifier arg : args) {
                arg.accept(this);
            }
        }

        Statement bodyStmts = functionDeclaration.getBody();
        if (bodyStmts != null) {
            bodyStmts.accept(this);
        }

        SymbolTable.pop();
        return null;
    }

    @Override
    public Void visit(MainDeclaration mainDeclaration) {
        Statement bodyStmts = mainDeclaration.getBody();
        if (bodyStmts != null) {
            bodyStmts.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(BlockStmt blockStmt) {

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

        FunctionCall functionCall = funcCallStmt.getFunctionCall();
        if (functionCall != null) {
            functionCall.accept(this);
        }

        return null;
    }

    @Override
    public Void visit(PrintStmt print) {

        Expression arg = print.getArg();
        if (arg != null) {
            arg.accept(this);
        }

        return null;
    }

    @Override
    public Void visit(ReturnStmt returnStmt) {

        Expression returnedExpr = returnStmt.getReturnedExpr();
        if (returnedExpr != null) {
            returnedExpr.accept(this);
        }

        return null;
    }

    @Override
    public Void visit(BinaryExpression binaryExpression) {

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

        Expression operand = unaryExpression.getOperand();
        if (operand != null) {
            operand.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(AnonymousFunction anonymousFunction) {

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
        return null;
    }

    @Override
    public Void visit(ListAccessByIndex listAccessByIndex) {

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

        Expression instance = listSize.getInstance();
        if (instance != null) {
            instance.accept(this);
        }

        return null;
    }

    @Override
    public Void visit(FunctionCall funcCall) {

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
        return null;
    }

    @Override
    public Void visit(BoolValue boolValue) {
        return null;
    }

    @Override
    public Void visit(StringValue stringValue) {
        return null;
    }

    @Override
    public Void visit(VoidValue voidValue) {
        return null;
    }
}
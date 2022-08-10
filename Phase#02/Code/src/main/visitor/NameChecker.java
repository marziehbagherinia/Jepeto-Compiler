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
import main.symbolTable.items.VariableSymbolTableItem;
import main.symbolTable.exceptions.ItemAlreadyExistsException;
import main.symbolTable.exceptions.ItemNotFoundException;

import main.compileErrors.nameErrors.DuplicateArgument;
import main.compileErrors.nameErrors.NameConflict;
import main.compileErrors.nameErrors.ArgumentNotDeclared;
import main.compileErrors.nameErrors.FunctionNotDeclared;
import main.compileErrors.nameErrors.VariableNotDeclared;

import java.util.ArrayList;
import java.util.Map;

public class NameChecker extends Visitor<Void> {
    Program root;
    private String currentFuncName;
    private String currentFuncCall;
    private boolean funcCallError;
    private boolean flag = false;
    public int numberOfError;

    private SymbolTable anonySymbolTable;
    private boolean anonyFlag = false;

    private SymbolTable getCurrentFuncSymbolTable(int type) {
        if (type == 0) {
            try {
                FunctionSymbolTableItem funcSymbolTableItem = (FunctionSymbolTableItem)SymbolTable.root.getItem(FunctionSymbolTableItem.START_KEY + this.currentFuncName);
                return funcSymbolTableItem.getFunctionSymbolTable();
            } catch (ItemNotFoundException ignored) {
                return null;
            }
        }
        else {
            try {
                FunctionSymbolTableItem funcSymbolTableItem = (FunctionSymbolTableItem)SymbolTable.root.getItem(FunctionSymbolTableItem.START_KEY + this.currentFuncCall);
                return funcSymbolTableItem.getFunctionSymbolTable();
            } catch (ItemNotFoundException ignored) {
                return null;
            }
        }

    }

    @Override
    public Void visit(Program program) {
        this.root = program;
        this.numberOfError = 0;

        ArrayList<FunctionDeclaration> functions = program.getFunctions();
        MainDeclaration mainFunc = program.getMain();

        if (functions != null) {
            for (FunctionDeclaration jepetoFunction : functions) {
                this.currentFuncName = jepetoFunction.getFunctionName().getName();
                jepetoFunction.accept(this);
            }
        }

        mainFunc.accept(this);

        return null;
    }

    @Override
    public Void visit(FunctionDeclaration functionDeclaration) {

        /*Identifier functionName = functionDeclaration.getFunctionName();

        if (functionName != null) {
            functionName.accept(this);
        }*/

        ArrayList<Identifier> args = functionDeclaration.getArgs();
        SymbolTable funcSymbolTable = this.getCurrentFuncSymbolTable(0);

        if (args != null) {
            for (Identifier arg : args) {
                //System.out.println("Test:   " + arg.getName());
                //boolean errored = false;
                try {
                    VariableSymbolTableItem variableSymbolTableItem = new VariableSymbolTableItem(arg);
                    funcSymbolTable.put(variableSymbolTableItem);
                    //errored = true;
                } catch (ItemAlreadyExistsException e) {
                    DuplicateArgument exception = new DuplicateArgument(arg.getLine(), arg.getName());
                    functionDeclaration.addError(exception);
                    System.out.println(exception.getMessage());
                    this.numberOfError += 1;
                }
                try {
                    FunctionSymbolTableItem funcSymbolTableItem = (FunctionSymbolTableItem)SymbolTable.root.getItem(FunctionSymbolTableItem.START_KEY + arg.getName());
                    NameConflict exception = new NameConflict(arg.getLine(), arg.getName());
                    functionDeclaration.addError(exception);
                    this.numberOfError += 1;
                    System.out.println(exception.getMessage());
                } catch (ItemNotFoundException ignored) {

                }
            }
        }

        /*if (args != null) {
            for (Identifier arg : args) {
                arg.accept(this);
            }
        }*/

        Statement bodyStmts = functionDeclaration.getBody();
        if (bodyStmts != null) {
            bodyStmts.accept(this);
        }

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
        this.anonySymbolTable = new SymbolTable();

        ArrayList<Identifier> args = anonymousFunction.getArgs();
        //SymbolTable funcSymbolTable = this.getCurrentFuncSymbolTable(0);

        if (args != null) {
            for (Identifier arg : args) {
                try {
                    VariableSymbolTableItem variableSymbolTableItem = new VariableSymbolTableItem(arg);
                    this.anonySymbolTable.put(variableSymbolTableItem);

                } catch (ItemAlreadyExistsException e) {
                    DuplicateArgument exception = new DuplicateArgument(arg.getLine(), arg.getName());
                    anonymousFunction.addError(exception);
                    System.out.println(exception.getMessage());
                    this.numberOfError += 1;
                }
                try {
                    FunctionSymbolTableItem funcSymbolTableItem = (FunctionSymbolTableItem)SymbolTable.root.getItem(FunctionSymbolTableItem.START_KEY + arg.getName());
                    NameConflict exception = new NameConflict(arg.getLine(), arg.getName());
                    anonymousFunction.addError(exception);
                    this.numberOfError += 1;
                    System.out.println(exception.getMessage());
                } catch (ItemNotFoundException ignored) {
                    return null;
                }
            }
        }

        Statement bodyStmts = anonymousFunction.getBody();
        if (bodyStmts != null) {
            this.anonyFlag = true;
            bodyStmts.accept(this);
            this.anonyFlag = false;
        }
        return null;
    }

    @Override
    public Void visit(Identifier identifier) {

        if (!this.flag) {
            SymbolTable funcSymbolTable = this.getCurrentFuncSymbolTable(0);

            if (anonyFlag) {
                funcSymbolTable = this.anonySymbolTable;
            }

            if (funcSymbolTable == null)
                return null;

            try {
                VariableSymbolTableItem varSymbolTableItem = (VariableSymbolTableItem)funcSymbolTable.getItem(VariableSymbolTableItem.START_KEY + identifier.getName());

            } catch (ItemNotFoundException ignored) {
                try {
                    FunctionSymbolTableItem funcSymbolTableItem = (FunctionSymbolTableItem)SymbolTable.root.getItem(FunctionSymbolTableItem.START_KEY + identifier.getName());

                } catch (ItemNotFoundException e) {
                    VariableNotDeclared exception = new VariableNotDeclared(identifier.getLine(), identifier.getName());
                    identifier.addError(exception);
                    System.out.println(exception.getMessage());
                    this.numberOfError += 1;
                }
            }
        }
        else {
            try {
                this.currentFuncCall = identifier.getName();
                FunctionSymbolTableItem funcSymbolTableItem = (FunctionSymbolTableItem)SymbolTable.root.getItem(FunctionSymbolTableItem.START_KEY + identifier.getName());
            } catch (ItemNotFoundException ignored) {
                FunctionNotDeclared exception = new FunctionNotDeclared(identifier.getLine(), identifier.getName());
                identifier.addError(exception);
                System.out.println(exception.getMessage());
                this.numberOfError += 1;
                this.funcCallError = true;
            }
            this.flag = false;
        }

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

        this.flag = true;
        this.funcCallError = false;
        Expression instance = funcCall.getInstance();

        if(instance != null) {
            instance.accept(this);
        }

        if (!this.funcCallError) {
            SymbolTable funcSymbolTable = this.getCurrentFuncSymbolTable(1);

            if (funcSymbolTable == null)
                return null;

            Map<Identifier, Expression> argsWithKey = funcCall.getArgsWithKey();
            for (Map.Entry<Identifier, Expression> entry : argsWithKey.entrySet()) {
                try {
                    VariableSymbolTableItem variableSymbolTableItem = (VariableSymbolTableItem)funcSymbolTable.getItem(VariableSymbolTableItem.START_KEY + entry.getKey().getName());

                } catch (ItemNotFoundException ignored) {
                    ArgumentNotDeclared exception = new ArgumentNotDeclared(funcCall.getLine(), entry.getKey().getName(), this.currentFuncCall);
                    funcCall.addError(exception);
                    System.out.println(exception.getMessage());
                    this.numberOfError += 1;
                }
            }
        }

        /*ArrayList<Expression> args = funcCall.getArgs();
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
        }*/
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
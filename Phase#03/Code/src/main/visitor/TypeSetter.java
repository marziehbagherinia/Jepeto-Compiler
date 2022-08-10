package main.visitor;

import main.ast.nodes.*;
import main.ast.nodes.declaration.*;
import main.ast.nodes.expression.*;
import main.ast.nodes.expression.values.*;
import main.ast.nodes.expression.values.primitive.*;
import main.ast.nodes.statement.*;
import main.ast.types.*;
import main.ast.types.functionPointer.FptrType;
import main.ast.types.single.BoolType;
import main.compileErrors.typeErrors.ConditionNotBool;
import main.compileErrors.typeErrors.ReturnValueNotMatchFunctionReturnType;
import main.compileErrors.typeErrors.UnsupportedTypeForPrint;
import main.symbolTable.*;
import main.symbolTable.exceptions.*;
import main.symbolTable.items.*;
import java.util.*;

public class TypeSetter  extends Visitor<Void> {
    private TypeInference typeInference = new TypeInference(this);
    private FunctionSymbolTableItem currentFunctionSTI = null;
    private ArrayList<String> functionOrder = new ArrayList<>();
    private ArrayList<FunctionDeclaration> functionDeclarations;

    public void setFunctionOrder(ArrayList<String> funcOrder) {this.functionOrder = funcOrder;};
    public void setCurrentFunSTI(FunctionSymbolTableItem FSTI){ this.currentFunctionSTI = FSTI; }

    @Override
    public Void visit(Program program) {
        MainDeclaration mainDeclaration = program.getMain();
        this.functionDeclarations = program.getFunctions();
        mainDeclaration.accept(this);
        return null;
    }

    @Override
    public Void visit(FunctionDeclaration funcDeclaration) {
        try {
            this.currentFunctionSTI = (FunctionSymbolTableItem) SymbolTable.root.getItem(FunctionSymbolTableItem.START_KEY +  funcDeclaration.getFunctionName().getName());
        } catch (ItemNotFoundException e) {
            return null;
        }
        funcDeclaration.getBody().accept(this);
        return null;
    }

    @Override
    public Void visit(MainDeclaration mainDeclaration) {
        mainDeclaration.getBody().accept(this);
        for (FunctionDeclaration value : functionDeclarations) {
            if (this.functionOrder.contains(value.getFunctionName().getName())) {
                try {
                    FunctionSymbolTableItem fsti = (FunctionSymbolTableItem) SymbolTable.root.getItem(FunctionSymbolTableItem.START_KEY + value.getFunctionName().getName());
                    System.out.print(value.getFunctionName().getName() + "\n");
                    System.out.print(fsti.getArgTypes() + "\n");
                    System.out.print(fsti.getReturnType() + "\n");
                } catch (ItemNotFoundException e) {
                    return null;
                }
            }
        }
        return null;
    }

    @Override
    public Void visit(BlockStmt blockStmt) {
        for(Statement statement : blockStmt.getStatements()) {
            statement.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(ConditionalStmt conditionalStmt) {
        Type conditionType = conditionalStmt.getCondition().accept(typeInference);
        conditionalStmt.getThenBody().accept(this);
        if(conditionalStmt.getElseBody() != null) {
            conditionalStmt.getElseBody().accept(this);
        }

        return null;
    }

    @Override
    public Void visit(FunctionCallStmt funcCallStmt) {
        funcCallStmt.getFunctionCall().accept(typeInference);
        return null;
    }

    @Override
    public Void visit(PrintStmt print) {
        Expression arg = print.getArg();
        Type argType = print.getArg().accept(typeInference);
        return null;
    }

    @Override
    public Void visit(ReturnStmt returnStmt) {
        Type returnType = returnStmt.getReturnedExpr().accept(typeInference);
        Type funReturnType = this.currentFunctionSTI.getReturnType();
        if((funReturnType == null && (!(returnType instanceof VoidType) || returnStmt.getReturnedExpr() instanceof VoidValue)) ||
           (funReturnType instanceof NoType && !(returnType instanceof NoType) && (!(returnType instanceof VoidType) || returnStmt.getReturnedExpr() instanceof VoidValue)))
            this.currentFunctionSTI.setReturnType(returnType);

        return null;
    }
}

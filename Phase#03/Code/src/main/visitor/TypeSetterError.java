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
import main.ast.types.single.IntType;
import main.ast.types.single.StringType;
import main.ast.types.list.ListType;
import main.compileErrors.typeErrors.ConditionNotBool;
import main.compileErrors.typeErrors.ReturnValueNotMatchFunctionReturnType;
import main.compileErrors.typeErrors.UnsupportedTypeForPrint;
import main.compileErrors.typeErrors.CantUseValueOfVoidFunction;
import main.symbolTable.*;
import main.symbolTable.exceptions.*;
import main.symbolTable.items.*;
import java.util.*;

public class TypeSetterError  extends Visitor<Void> {
    private TypeInferenceError typeInferenceError = new TypeInferenceError(this);
    private FunctionSymbolTableItem currentFunctionSTI = null;
    private ArrayList<String> functionOrder = new ArrayList<>();
    private ArrayList<FunctionDeclaration> functionDeclarations;

    public void setFunctionOrder(ArrayList<String> funcOrder) {this.functionOrder = funcOrder;};
    public void setCurrentFunSTI(FunctionSymbolTableItem FSTI){ this.currentFunctionSTI = FSTI; }

    private boolean FptrEqual(FptrType f1, FptrType f2){
        if(f1.getFunctionName().equals(f2.getFunctionName())){
            return true;
        }
        try{
            FunctionSymbolTableItem f1Symbol = (FunctionSymbolTableItem) SymbolTable.root.getItem(FunctionSymbolTableItem.START_KEY + f1.getFunctionName());
            FunctionSymbolTableItem f2Symbol = (FunctionSymbolTableItem) SymbolTable.root.getItem(FunctionSymbolTableItem.START_KEY + f2.getFunctionName());
            if(f1Symbol.getArgTypes().size() == f2Symbol.getArgTypes().size()){
                int i = 0;
                for (Type argType: f1Symbol.getArgTypes()){
                    if (argType.equals(f2Symbol.getArgTypes().get(i)))
                        i++;
                    else
                        return false;
                }
                if (f1Symbol.getReturnType().equals(f2Symbol.getReturnType()))
                    return true;
                else
                    return false;
            }
            else{
                return  false;
            }
        }catch(ItemNotFoundException ex){}
        return true;
    }

    @Override
    public Void visit(Program program) {
        //System.out.print("Program\n");
        MainDeclaration mainDeclaration = program.getMain();
        this.functionDeclarations = program.getFunctions();
        mainDeclaration.accept(this);
        return null;
    }

    @Override
    public Void visit(FunctionDeclaration funcDeclaration) {
        //System.out.print("FuncDec " + funcDeclaration.getFunctionName().getName() + "\n");
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
        //System.out.print("Main\n");
        mainDeclaration.getBody().accept(this);
        /*
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
        }*/
        return null;
    }

    @Override
    public Void visit(BlockStmt blockStmt) {
        //System.out.print("Blockstmt\n");
        for(Statement statement : blockStmt.getStatements()) {
            statement.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(ConditionalStmt conditionalStmt) {
        // System.out.print("visit conditional stmt\n");
        Type conditionType = conditionalStmt.getCondition().accept(typeInferenceError);
        if (conditionType instanceof VoidType) {
            CantUseValueOfVoidFunction exception = new CantUseValueOfVoidFunction(conditionalStmt.getLine());
            conditionalStmt.addError(exception);
        }
        else if(!((conditionType instanceof BoolType) || (conditionType instanceof NoType))){
            ConditionNotBool exception = new ConditionNotBool(conditionalStmt.getLine());
            conditionalStmt.addError(exception);
        }
        conditionalStmt.getThenBody().accept(this);
        if(conditionalStmt.getElseBody() != null) {
            conditionalStmt.getElseBody().accept(this);
        }

        return null;
    }

    @Override
    public Void visit(FunctionCallStmt funcCallStmt) {
        //System.out.println("FunCall Stmt");
        funcCallStmt.getFunctionCall().accept(typeInferenceError);
        return null;
    }

    @Override
    public Void visit(PrintStmt print) {
        //Expression arg = print.getArg();
        Type argType = print.getArg().accept(typeInferenceError);
        if (argType instanceof VoidType) {
            CantUseValueOfVoidFunction exception = new CantUseValueOfVoidFunction(print.getLine());
            print.addError(exception);
        }
        else if(!((argType instanceof IntType) || (argType instanceof StringType) || (argType instanceof BoolType) || (argType instanceof ListType))){
            UnsupportedTypeForPrint exception = new UnsupportedTypeForPrint(print.getLine());
            print.addError(exception);
        }

        return null;
    }

    @Override
    public Void visit(ReturnStmt returnStmt) {
        Type returnType = returnStmt.getReturnedExpr().accept(typeInferenceError);
        Type funReturnType = this.currentFunctionSTI.getReturnType();

        if((funReturnType == null && (!(returnType instanceof VoidType) || returnStmt.getReturnedExpr() instanceof VoidValue)) ||
           (funReturnType instanceof NoType && !(returnType instanceof NoType) && (!(returnType instanceof VoidType) || returnStmt.getReturnedExpr() instanceof VoidValue)))
            this.currentFunctionSTI.setReturnType(returnType);

        if (returnType instanceof VoidType && !(returnStmt.getReturnedExpr() instanceof VoidValue)) {
            CantUseValueOfVoidFunction exception = new CantUseValueOfVoidFunction((returnStmt.getLine()));
            returnStmt.addError(exception);
        }
        else if((funReturnType != null) && !(returnType.getClass().equals(funReturnType.getClass()))
                &&  !(returnType instanceof NoType) && !(funReturnType instanceof NoType)){
            ReturnValueNotMatchFunctionReturnType exception = new ReturnValueNotMatchFunctionReturnType((returnStmt.getLine()));
            returnStmt.addError(exception);
        }
        else if ((funReturnType instanceof  FptrType) && (returnType instanceof FptrType)) {
            if (!FptrEqual((FptrType) funReturnType, (FptrType) returnType)) {
                ReturnValueNotMatchFunctionReturnType exception = new ReturnValueNotMatchFunctionReturnType((returnStmt.getLine()));
                returnStmt.addError(exception);
            }
        }
        return null;
    }
}

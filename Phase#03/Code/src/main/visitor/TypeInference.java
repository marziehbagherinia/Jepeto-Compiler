package main.visitor;

import main.ast.nodes.declaration.*;
import main.ast.nodes.expression.*;
import main.ast.nodes.expression.operators.BinaryOperator;
import main.ast.nodes.expression.operators.UnaryOperator;
import main.ast.nodes.expression.values.*;
import main.ast.nodes.expression.values.primitive.*;
import main.ast.types.*;
import main.ast.types.functionPointer.FptrType;
import main.ast.types.list.ListType;
import main.ast.types.single.*;
import main.compileErrors.typeErrors.*;
import main.symbolTable.SymbolTable;
import main.symbolTable.exceptions.ItemNotFoundException;
import main.symbolTable.items.*;
import java.util.*;
import java.util.function.Function;

public class TypeInference extends Visitor<Type> {

    private boolean flag = false;
    private boolean inFunCall = false;
    private TypeSetter typeSetter = null;
    private FunctionDeclaration currentFunctionD;
    private FunctionSymbolTableItem currentFunctionSTI;
    private FunctionSymbolTableItem parentFunctionSTI;
    private ArrayList<String> functionOrder = new ArrayList<>();
    private Map<Identifier, Integer> visited_func = new HashMap<>();

    public TypeInference(TypeSetter typeSetter) {
        this.typeSetter = typeSetter;
    }

    private boolean FptrEqual(FptrType f1, FptrType f2){
        if(f1.getFunctionName().equals(f2.getFunctionName())) {
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
            else {
                return  false;
            }
        }catch(ItemNotFoundException ex) {

        }
        return true;
    }


    @Override
    public Type visit(BinaryExpression binaryExpression) {
        Expression left = binaryExpression.getFirstOperand();
        Expression right = binaryExpression.getSecondOperand();

        Type tl = left.accept(this);
        Type tr = right.accept(this);
        BinaryOperator operator =  binaryExpression.getBinaryOperator();


        if (operator.equals(BinaryOperator.and) || operator.equals(BinaryOperator.or)){
            if (tl instanceof BoolType && tr instanceof BoolType)
                return new BoolType();

            if ((tl instanceof NoType || tl instanceof BoolType) && (tr instanceof BoolType || tr instanceof NoType))
                return new NoType();
        }
        if (operator.equals(BinaryOperator.mult) || operator.equals(BinaryOperator.div) || operator.equals(BinaryOperator.add)
                || operator.equals(BinaryOperator.sub)) {
            if (tl instanceof IntType && tr instanceof IntType)
                return new IntType();

            if ((tl instanceof NoType || tl instanceof IntType) && (tr instanceof IntType || tr instanceof NoType))
                return new NoType();
        }
        if (operator.equals(BinaryOperator.gt) || operator.equals(BinaryOperator.lt)){
            if ((tl instanceof IntType && tr instanceof IntType))
                return new BoolType();

            if ((tl instanceof NoType || tl instanceof IntType) && (tr instanceof IntType || tr instanceof NoType))
                return new NoType();
        }
        if (operator.equals(BinaryOperator.eq) || operator.equals(BinaryOperator.neq)){
            if ((tl instanceof NoType) || (tr instanceof NoType)){
                return new NoType();
            }
            if (tl instanceof FptrType){
                if (tr instanceof FptrType){
                    if (FptrEqual((FptrType) tl, (FptrType) tr))
                        return new BoolType();
                    else
                        return new NoType();
                }
                else if (tr instanceof VoidType){
                    return new BoolType();
                }
            }
            else if(tl instanceof VoidType && tr instanceof FptrType){
                return new BoolType();
            }
            else if(tl.getClass().equals(tr.getClass())){
                if (!(tl instanceof ListType)){
                    return new BoolType();
                }
            }
        }
        if (operator.equals(BinaryOperator.append)){ //not so sure about this!
            if (tl instanceof NoType)
                return new NoType();
            if (tl instanceof ListType){
                Type listtype = ((ListType) tl).getType();
                if (listtype instanceof NoType){
                    ((ListType) tl).setType(tr);
                    return tl;
                }
                else if((listtype instanceof  IntType && tr instanceof IntType) || (listtype instanceof BoolType && tr instanceof BoolType) ||
                        (listtype instanceof  StringType && tr instanceof StringType)) {
                    return tl;
                }

                Type temp_1 = listtype;
                Type temp_2 = tr;
                while (temp_1 instanceof ListType && temp_2 instanceof ListType)
                {
                    temp_1 = ((ListType) temp_1).getType();
                    temp_2 = ((ListType) temp_2).getType();
                }

                if((temp_1 instanceof  IntType && temp_2 instanceof IntType) || (temp_1 instanceof BoolType && temp_2 instanceof BoolType) ||
                        (temp_1 instanceof  StringType && temp_2 instanceof StringType)) {
                    return tl;
                }
                if((temp_1 instanceof  NoType && (temp_2 instanceof IntType || temp_2 instanceof BoolType || temp_2 instanceof StringType)) ||
                        (temp_1 instanceof IntType || temp_1 instanceof BoolType || temp_1 instanceof StringType) && (temp_2 instanceof  NoType)) {
                    return tl;
                }
            }
        }

        return new NoType();
    }

    @Override
    public Type visit(UnaryExpression unaryExpression) {
        Expression opr = unaryExpression.getOperand();

        Type t = opr.accept(this);
        UnaryOperator operator =  unaryExpression.getOperator();
        if (operator.equals(UnaryOperator.not)){
            if (t instanceof BoolType)
                return new BoolType();
            if (t instanceof NoType)
                return new NoType();
        }
        if (operator.equals(UnaryOperator.minus)){
            if (t instanceof IntType)
                return new IntType();
            if (t instanceof NoType)
                return new NoType();
        }

        return new NoType();
    }

    @Override
    public Type visit(AnonymousFunction anonymousFunction) {
        try{
            FunctionSymbolTableItem fsti = (FunctionSymbolTableItem) SymbolTable.root.getItem(FunctionSymbolTableItem.START_KEY + anonymousFunction.getName());
            return new FptrType(anonymousFunction.getName());
        } catch (ItemNotFoundException ex) {
            //Unreachable
        }
        return new NoType();
    }

    @Override
    public Type visit(Identifier identifier) {

        try {
            FunctionSymbolTableItem fsti = (FunctionSymbolTableItem) SymbolTable.root.getItem(FunctionSymbolTableItem.START_KEY + identifier.getName());
            return new FptrType(identifier.getName());
        } catch (ItemNotFoundException ex) {
            if (this.inFunCall) {
                try {
                    if (this.parentFunctionSTI == null)
                        return null;

                    VariableSymbolTableItem var = (VariableSymbolTableItem) this.parentFunctionSTI.getFunctionSymbolTable().getItem(VariableSymbolTableItem.START_KEY + identifier.getName());
                    return var.getType();
                } catch (ItemNotFoundException ex3) {
                    return null;
                }
            }
            else {
                try {
                    if (this.currentFunctionSTI == null)
                        return null;

                    VariableSymbolTableItem var = (VariableSymbolTableItem) this.currentFunctionSTI.getFunctionSymbolTable().getItem(VariableSymbolTableItem.START_KEY + identifier.getName());
                    return var.getType();
                } catch (ItemNotFoundException ex2) {
                    return null;
                }
            }
        }
    }

    @Override
    public Type visit(ListAccessByIndex listAccessByIndex) {
        Type instanceType = listAccessByIndex.getInstance().accept(this);
        Type indexType = listAccessByIndex.getIndex().accept(this);

        if(instanceType instanceof ListType) {
            Type listtype = ((ListType) instanceType).getType();

            if(indexType instanceof IntType) {
                return listtype;
            }
        }
        return new NoType();
    }

    @Override
    public Type visit(ListSize listSize) {
        Expression list_instance = listSize.getInstance();
        Type instance_type = list_instance.accept(this);

        if (instance_type instanceof ListType)
            return new IntType();

        return new NoType();
    }

    @Override
    public Type visit(FunctionCall funcCall) {
        //System.out.print("1  FuncCall " + funcCall.getInstance().toString() + "\n");

        this.inFunCall = true;
        Type instanceType = funcCall.getInstance().accept(this);
        this.inFunCall = false;

        FunctionDeclaration prevfd = this.currentFunctionD;
        FunctionSymbolTableItem prevfsti = this.currentFunctionSTI;
        FunctionSymbolTableItem prevparent = this.parentFunctionSTI;

        try{
            if (!(instanceType instanceof FptrType))
                return new NoType();

            FunctionSymbolTableItem fsti = (FunctionSymbolTableItem) SymbolTable.root.getItem(FunctionSymbolTableItem.START_KEY + ((FptrType) instanceType).getFunctionName());

            if (!(functionOrder.contains(fsti.getFuncDeclaration().getFunctionName().getName())))
                functionOrder.add(fsti.getFuncDeclaration().getFunctionName().getName());

            if (!this.flag) {
                this.parentFunctionSTI = fsti;
                this.flag = true;
            }
            else {
                this.parentFunctionSTI = this.currentFunctionSTI;
            }
            this.currentFunctionSTI = fsti;
            this.currentFunctionD = fsti.getFuncDeclaration();

            //System.out.print("1  FuncCall " + funcCall.getLine() + " "+ this.currentFunctionD.getFunctionName().getName() + "\n");

        } catch (ItemNotFoundException ex) {
            return new NoType();
        }

        Type returnType = this.currentFunctionSTI.getReturnType();

        if ((returnType != null && !(returnType instanceof NoType) && !(returnType instanceof VoidType)) ||
            (visited_func.get(this.currentFunctionD.getFunctionName()) != null && visited_func.get(this.currentFunctionD.getFunctionName()) >= 2))
        {
            this.currentFunctionD = prevfd;
            this.currentFunctionSTI = prevfsti;
            this.parentFunctionSTI = prevparent;
            return returnType;
        }

        ArrayList<Identifier> args_2 = this.currentFunctionD.getArgs();
        int argIndex = 0;
        //System.out.println("Ars: " + funcCall.getLine() + " " + args_2 + "\n");

        for(Expression arg : funcCall.getArgs()) {
            inFunCall = true;
            Type idType = arg.accept(this);
            inFunCall = false;

            try {
                VariableSymbolTableItem var = (VariableSymbolTableItem) this.currentFunctionSTI.getFunctionSymbolTable().getItem(VariableSymbolTableItem.START_KEY + args_2.get(argIndex).getName());
                /*if(var.getType() instanceof NoType || var.getType() == null) {
                    var.setType(idType);
                    this.currentFunctionSTI.addArgType(idType);
                }*/

                if(var.getType() == null) {
                    var.setType(idType);
                    this.currentFunctionSTI.addArgType(idType);
                }
            } catch(ItemNotFoundException e) {
                this.currentFunctionD = prevfd;
                this.currentFunctionSTI = prevfsti;
                this.parentFunctionSTI = prevparent;
                return new NoType();
            }
            argIndex += 1;
        }

        if (funcCall.getArgsWithKey() != null) {
            ArrayList<Type> argTypes = new ArrayList<>();
            for(Map.Entry<Identifier, Expression> argsWithKey: funcCall.getArgsWithKey().entrySet()) {
                argTypes.add(null);
            }

            for(Map.Entry<Identifier, Expression> argsWithKey: funcCall.getArgsWithKey().entrySet()) {
                inFunCall = true;
                Type idType = argsWithKey.getValue().accept(this);
                inFunCall = false;
                int i = 0;
                int pos = 0;

                for (Identifier funDecArg : this.currentFunctionD.getArgs()){
                    if (funDecArg.getName().equals(argsWithKey.getKey().getName())){
                        pos = i;
                        break;
                    }
                    i = i + 1;
                }

                try {
                    VariableSymbolTableItem var = (VariableSymbolTableItem) this.currentFunctionSTI.getFunctionSymbolTable().getItem(VariableSymbolTableItem.START_KEY + argsWithKey.getKey().getName().replace("Identifier_", ""));

                    /*if(var.getType() instanceof NoType || var.getType() == null) {
                        var.setType(idType);
                        argTypes.set(pos, idType);
                    }*/

                    if(var.getType() == null) {
                        var.setType(idType);
                        argTypes.set(pos, idType);
                    }

                } catch(ItemNotFoundException e) {
                    this.currentFunctionD = prevfd;
                    this.currentFunctionSTI = prevfsti;
                    this.parentFunctionSTI = prevparent;
                    return new NoType();
                }
            }
            for(Type idType : argTypes) {
                this.currentFunctionSTI.addArgType(idType);
            }
        }

        visited_func.merge(this.currentFunctionD.getFunctionName(), 1, Integer::sum);

        this.typeSetter.setCurrentFunSTI(this.currentFunctionSTI);
        this.currentFunctionD.accept(typeSetter);
        returnType = this.currentFunctionSTI.getReturnType();

        this.currentFunctionD = prevfd;
        this.currentFunctionSTI = prevfsti;
        this.parentFunctionSTI = prevparent;

        this.typeSetter.setCurrentFunSTI(this.currentFunctionSTI);
        this.typeSetter.setFunctionOrder(this.functionOrder);

        if (returnType == null)
            return new NoType();

        return returnType;
    }

    @Override
    public Type visit(ListValue listValue) {
        Type first = new NoType();
        ArrayList <Type> elements_type = new ArrayList<>();

        for (Expression element : listValue.getElements())
            elements_type.add(element.accept(this));

        for (Type element : elements_type) {
            if (!(first instanceof NoType) && !(element instanceof NoType) && !(element.getClass().equals(first.getClass()))) {
                return new NoType();
            }
            if (first instanceof ListType && element instanceof ListType) {
                Type temp_1 = first;
                Type temp_2 = element;
                while (temp_1 instanceof ListType && temp_2 instanceof ListType) {
                    temp_1 = ((ListType) temp_1).getType();
                    temp_2 = ((ListType) temp_2).getType();
                }
                if (!(temp_1.getClass().equals(temp_2.getClass())) && !(temp_1 instanceof NoType) && !(temp_2 instanceof NoType)) {
                    return new NoType();
                }
            }
            if (!(element instanceof NoType) && !(element instanceof FptrType) && !(element instanceof VoidType) && element != null) {
                first = element;
            }
        }

        return new ListType(first);
    }

    @Override
    public Type visit(IntValue intValue) {
        return new IntType();
    }

    @Override
    public Type visit(BoolValue boolValue) {
        return new BoolType();
    }

    @Override
    public Type visit(StringValue stringValue) {
        return new StringType();
    }

    @Override
    public Type visit(VoidValue voidValue) {
        return new VoidType();
    }
}

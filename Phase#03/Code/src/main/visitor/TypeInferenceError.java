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
import main.compileErrors.nameErrors.DuplicateFunction;
import main.compileErrors.typeErrors.*;
import main.symbolTable.SymbolTable;
import main.symbolTable.exceptions.ItemNotFoundException;
import main.symbolTable.items.*;
import java.util.*;
import java.util.function.Function;

public class TypeInferenceError extends Visitor<Type> {

    private boolean flag = false;
    private boolean voidOrNot = false;
    private boolean inFunCall = false;
    private TypeSetterError typeSetterError = null;
    private FunctionDeclaration currentFunctionD;
    private FunctionSymbolTableItem currentFunctionSTI;
    private FunctionSymbolTableItem parentFunctionSTI;
    private ArrayList<String> functionOrder = new ArrayList<>();
    private Map<Identifier, Integer> visited_func = new HashMap<>();

    public TypeInferenceError(TypeSetterError typeSetterError) {
        this.typeSetterError = typeSetterError;
    }

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
                if (f1Symbol.getReturnType() instanceof VoidType || f2Symbol.getReturnType() instanceof VoidType) {
                    this.voidOrNot = true;
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
            if (tl instanceof FptrType) {
                if (tr instanceof FptrType){
                    this.voidOrNot = false;
                    if (FptrEqual((FptrType) tl, (FptrType) tr)) {
                        return new BoolType();
                    }
                    else {
                        if (this.voidOrNot) {
                            CantUseValueOfVoidFunction exception = new CantUseValueOfVoidFunction(binaryExpression.getLine());
                            binaryExpression.addError(exception);
                            return new NoType();
                        }
                    }
                }
                else if (tr instanceof VoidType){
                    CantUseValueOfVoidFunction exception = new CantUseValueOfVoidFunction(binaryExpression.getLine());
                    binaryExpression.addError(exception);
                    return new NoType();
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

        UnsupportedOperandType exception = new UnsupportedOperandType(binaryExpression.getLine(), operator.name());
        binaryExpression.addError(exception);

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
        UnsupportedOperandType exception = new UnsupportedOperandType(unaryExpression.getLine(), operator.name());
        unaryExpression.addError(exception);

        return new NoType();
    }

    @Override
    public Type visit(AnonymousFunction anonymousFunction) {
        try{
            FunctionSymbolTableItem fsti = (FunctionSymbolTableItem) SymbolTable.root.getItem(FunctionSymbolTableItem.START_KEY + anonymousFunction.getName());
            return new FptrType(anonymousFunction.getName());
        }catch (ItemNotFoundException ex){
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
                    //System.out.println("Id3 " + identifier.getName());
                    return var.getType();
                } catch (ItemNotFoundException ex2) {
                    return null;
                }
            }
        }
    }

    @Override
    public Type visit(ListAccessByIndex listAccessByIndex) {
        //Changed
        Type instanceType = listAccessByIndex.getInstance().accept(this);
        Type indexType = listAccessByIndex.getIndex().accept(this);

        if(instanceType instanceof ListType) {
            Type listtype = ((ListType) instanceType).getType();

            if((indexType instanceof IntType)){
                return listtype;
            }
            else if(!(indexType instanceof NoType)) {
                ListIndexNotInt exception = new ListIndexNotInt(listAccessByIndex.getLine());
                listAccessByIndex.addError(exception);
            }
        }
        else  {
            if(!(instanceType instanceof NoType)) {
                ListAccessByIndexOnNoneList exception = new ListAccessByIndexOnNoneList(listAccessByIndex.getLine());
                listAccessByIndex.addError(exception);
            }

            if(!(indexType instanceof NoType) && !(indexType instanceof IntType)) {
                ListIndexNotInt exception_2 = new ListIndexNotInt(listAccessByIndex.getLine());
                listAccessByIndex.addError(exception_2);
            }
        }
        return new NoType();
    }

    @Override
    public Type visit(ListSize listSize) {
        Expression list_instance = listSize.getInstance();
        Type instanceType = list_instance.accept(this);

        //System.out.println(instanceType.toString() + "Hi \n");
        if (instanceType instanceof ListType) {
            return new IntType();
        }
        else if(!(instanceType instanceof NoType)) {
            GetSizeOfNoneList exception = new GetSizeOfNoneList(listSize.getLine());
            listSize.addError(exception);
        }

        return new NoType();
    }

    @Override
    public Type visit(FunctionCall funcCall) {

        this.inFunCall = true;
        Type instanceType = funcCall.getInstance().accept(this);
        this.inFunCall = false;

        FunctionDeclaration prevfd = this.currentFunctionD;
        FunctionSymbolTableItem prevfsti = this.currentFunctionSTI;
        FunctionSymbolTableItem prevparent = this.parentFunctionSTI;

        try{
            if (!(instanceType instanceof FptrType)) {
                this.currentFunctionD = prevfd;
                this.currentFunctionSTI = prevfsti;
                this.parentFunctionSTI = prevparent;
                return new NoType();
            }

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
        ArrayList<Identifier> args_2 = this.currentFunctionD.getArgs();
        int argIndex = 0;

        for(Expression arg : funcCall.getArgs()) {
            inFunCall = true;
            Type idType = arg.accept(this);
            inFunCall = false;

            if (idType instanceof VoidType) {
                CantUseValueOfVoidFunction exception = new CantUseValueOfVoidFunction(funcCall.getLine());
                funcCall.addError(exception);
            }

            try {
                VariableSymbolTableItem var = (VariableSymbolTableItem) this.currentFunctionSTI.getFunctionSymbolTable().getItem(VariableSymbolTableItem.START_KEY + args_2.get(argIndex).getName());
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

        if (visited_func.get(this.currentFunctionD.getFunctionName()) != null && visited_func.get(this.currentFunctionD.getFunctionName()) >= 1) {
            this.currentFunctionD = prevfd;
            this.currentFunctionSTI = prevfsti;
            this.parentFunctionSTI = prevparent;
            return returnType;
        }

        visited_func.merge(this.currentFunctionD.getFunctionName(), 1, Integer::sum);

        this.typeSetterError.setCurrentFunSTI(this.currentFunctionSTI);
        this.currentFunctionD.accept(typeSetterError);
        returnType = this.currentFunctionSTI.getReturnType();

        this.currentFunctionD = prevfd;
        this.currentFunctionSTI = prevfsti;
        this.parentFunctionSTI = prevparent;

        this.typeSetterError.setCurrentFunSTI(this.currentFunctionSTI);
        this.typeSetterError.setFunctionOrder(this.functionOrder);

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
                ListElementsTypeNotMatch exception = new ListElementsTypeNotMatch(listValue.getLine());
                listValue.addError(exception);
                return new NoType();
            }
            if (first instanceof ListType && element instanceof ListType) {
                Type temp_1 = first;
                Type temp_2 = element;
                while (temp_1 instanceof ListType && temp_2 instanceof ListType) {
                    temp_1 = ((ListType) temp_1).getType();
                    temp_2 = ((ListType) temp_2).getType();
                }
                if (!(temp_1.getClass().equals(temp_2.getClass()))) {
                    ListElementsTypeNotMatch exception = new ListElementsTypeNotMatch(listValue.getLine());
                    listValue.addError(exception);
                    return new NoType();
                }
            }
            if (!(element instanceof NoType) && !(element instanceof FptrType) && !(element instanceof VoidType) && element != null) {
                //return new ListType(element);
                first = element;
            }
            if (element instanceof VoidType) {
                CantUseValueOfVoidFunction exception = new CantUseValueOfVoidFunction(listValue.getLine());
                listValue.addError(exception);
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

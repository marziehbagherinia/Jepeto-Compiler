package main.symbolTable.items;

import main.ast.nodes.declaration.FunctionDeclaration;
import main.ast.types.Type;
import main.symbolTable.SymbolTable;
import java.util.*;

public class FunctionSymbolTableItem extends SymbolTableItem {
    public static final String START_KEY = "Function_";
    private SymbolTable FunctionSymbolTable;
    private FunctionDeclaration funcDeclaration;
    private final ArrayList<Type> argTypes;
    private Type returnType;

    public FunctionSymbolTableItem(FunctionDeclaration funcDeclaration) {
        this.funcDeclaration = funcDeclaration;
        this.name = funcDeclaration.getFunctionName().getName();
        argTypes = new ArrayList<>();
    }

    public SymbolTable getFunctionSymbolTable() {
        return FunctionSymbolTable;
    }
    public void setFunctionSymbolTable(SymbolTable functionSymbolTable) {
        FunctionSymbolTable = functionSymbolTable;
    }

    public FunctionDeclaration getFuncDeclaration() {
        return funcDeclaration;
    }
    public void setFuncDeclaration(FunctionDeclaration funcDeclaration) {
        this.funcDeclaration = funcDeclaration;
    }

    public void addArgType(Type type) {
        argTypes.add(type);
    }

    public ArrayList<Type> getArgTypes() {
        return argTypes;
    }

    public Type getReturnType() {
        return returnType;
    }
    public void setReturnType(Type returnType) {
        this.returnType = returnType;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getKey() {
        return START_KEY + this.name;
    }
}

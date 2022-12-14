package main.symbolTable.items;



import main.ast.nodes.expression.Identifier;

public class VariableSymbolTableItem extends SymbolTableItem {
    public static String START_KEY = "Var_";

    public VariableSymbolTableItem(Identifier id) {
        this.name = id.getName();
    }

    public String getKey() {
        return START_KEY + this.name;
    }
}

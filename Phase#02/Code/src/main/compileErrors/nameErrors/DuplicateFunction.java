package main.compileErrors.nameErrors;
import main.ast.nodes.declaration.FunctionDeclaration;
import main.compileErrors.CompileError;
import main.symbolTable.SymbolTable;
import main.symbolTable.exceptions.ItemAlreadyExistsException;
import main.symbolTable.items.FunctionSymbolTableItem;

public class DuplicateFunction extends CompileError {
    public DuplicateFunction(int line, String functionName){
        super(line);
        message = "Duplicate function " + functionName;
    }
    public void handleException(FunctionDeclaration functionDeclaration) {
        String newName = functionDeclaration.getFunctionName().getName() + "_";
        functionDeclaration.getFunctionName().setName(newName);
        FunctionSymbolTableItem symbolTableActorItem = new FunctionSymbolTableItem(functionDeclaration);
        symbolTableActorItem.setFunctionSymbolTable(SymbolTable.top);
        try {
            SymbolTable.root.put(symbolTableActorItem);
        } catch (ItemAlreadyExistsException exception) {
            this.handleException(functionDeclaration);
        }
    }
}
package main.compileErrors.nameErrors;

import main.compileErrors.CompileError;

public class FunctionNotDeclared extends CompileError {
    public FunctionNotDeclared(int line, String functionName){
        super(line, "Function " + functionName + " not declared");
    }
}
package main.compileErrors.nameErrors;

import main.compileErrors.CompileError;

public class ArgumentNotDeclared extends CompileError {
    public ArgumentNotDeclared(int line, String argumentName, String functionName){
        super(line, "Argument " + argumentName + " not declared in function " + functionName);
    }
}


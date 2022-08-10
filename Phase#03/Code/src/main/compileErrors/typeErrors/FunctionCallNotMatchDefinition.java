package main.compileErrors.typeErrors;

import main.compileErrors.CompileError;

public class FunctionCallNotMatchDefinition extends CompileError {

    public FunctionCallNotMatchDefinition(int line) {
        super(line, "Args in function call do not match with definition");
    }

}

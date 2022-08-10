package main.compileErrors.typeErrors;

import main.compileErrors.CompileError;

public class ReturnValueNotMatchFunctionReturnType extends CompileError{

    public ReturnValueNotMatchFunctionReturnType(int line) {
        super(line, "Return value does not match with function return type");
    }

}

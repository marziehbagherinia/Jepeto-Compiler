package main.compileErrors.typeErrors;

import main.compileErrors.CompileError;

public class CallOnNoneFptrType extends CompileError {

    public CallOnNoneFptrType(int line) {
        super(line, "Calling a non-callable");
    }

}

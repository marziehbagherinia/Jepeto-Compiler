package main.compileErrors.typeErrors;

import main.compileErrors.CompileError;

public class ConditionNotBool extends CompileError {

    public ConditionNotBool(int line) {
        super(line, "Condition must be bool");
    }

}

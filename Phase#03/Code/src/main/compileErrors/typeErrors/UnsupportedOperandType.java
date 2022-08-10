package main.compileErrors.typeErrors;

import main.compileErrors.CompileError;

public class UnsupportedOperandType extends CompileError {

    public UnsupportedOperandType(int line, String operatorName) {
        super(line, "Unsupported operand type for operator " + operatorName);
    }

}

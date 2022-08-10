package main.compileErrors.typeErrors;

import main.compileErrors.CompileError;

public class ListElementsTypeNotMatch extends CompileError {
    public ListElementsTypeNotMatch(int line) {
        super(line, "Elements of the list have different types");
    }
}

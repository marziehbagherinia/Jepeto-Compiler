package main.compileErrors.typeErrors;

import main.compileErrors.CompileError;

public class ListIndexNotInt extends CompileError {

    public ListIndexNotInt(int line) {
        super(line, "List index is not an integer");
    }

}

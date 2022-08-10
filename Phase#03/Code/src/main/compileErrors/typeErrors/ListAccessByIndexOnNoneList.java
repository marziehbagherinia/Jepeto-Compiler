package main.compileErrors.typeErrors;

import main.compileErrors.CompileError;

public class ListAccessByIndexOnNoneList extends CompileError {

    public ListAccessByIndexOnNoneList(int line) {
        super(line, "Access by index on a non-list");
    }

}

package main.compileErrors.typeErrors;

import main.compileErrors.CompileError;

public class GetSizeOfNoneList extends CompileError {
    public GetSizeOfNoneList(int line) {
        super(line, "Get size of a non-list");
    }
}

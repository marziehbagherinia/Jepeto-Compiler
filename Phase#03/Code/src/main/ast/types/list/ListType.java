package main.ast.types.list;

import main.ast.types.Type;

import java.util.ArrayList;

public class ListType extends Type {
    private Type type;


    public ListType(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "ListType";
    }
}

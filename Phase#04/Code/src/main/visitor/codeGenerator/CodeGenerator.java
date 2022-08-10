package main.visitor.codeGenerator;

import main.ast.nodes.*;
import main.ast.nodes.declaration.*;
import main.ast.nodes.expression.*;
import main.ast.nodes.expression.operators.BinaryOperator;
import main.ast.nodes.expression.operators.UnaryOperator;
import main.ast.nodes.expression.values.*;
import main.ast.nodes.expression.values.primitive.*;
import main.ast.nodes.statement.*;
import main.ast.types.*;
import main.ast.types.functionPointer.*;
import main.ast.types.list.*;
import main.ast.types.single.*;
import main.symbolTable.SymbolTable;
import main.symbolTable.exceptions.ItemNotFoundException;
import main.symbolTable.items.FunctionSymbolTableItem;
import main.visitor.Visitor;
import main.visitor.type.ExpressionTypeChecker;

import javax.lang.model.type.NullType;
import java.io.*;
import java.util.*;

public class CodeGenerator extends Visitor<String> {
    private final String outputPath;
    private FileWriter mainFile;
    private final ExpressionTypeChecker expressionTypeChecker;
    private FunctionDeclaration curFuncDec;
    private FunctionDeclaration tempCurFuncDec;
    private FunctionSymbolTableItem tempFunc;

    private int labelNum;
    private int tempLabelNum;
    private int currentSlot;
    private int tempSlot;
    private String funCall_name;
    private String stack_size;
    private String locals_size;
    private Set<String> visited;
    private ArrayList<String> anonyVisited;
    private ArrayList<Identifier> local_vars;
    private ArrayList<Identifier> temp_local_vars;
    private ArrayList<AnonymousFunction> anonymousFuncs;
    private ArrayList<AnonymousFunction> tempAnonymousFuncs;

    public CodeGenerator(ExpressionTypeChecker expressionTypeChecker, Set<String> visited) {
        this.expressionTypeChecker = expressionTypeChecker;
        this.visited = visited;
        this.anonyVisited = new ArrayList<>();
        this.outputPath = "./output/";
        prepareOutputFolder();
        this.stack_size = "128";
        this.locals_size = "128";
        this.currentSlot = 0;
        this.local_vars = new ArrayList<>();
        this.anonymousFuncs = new ArrayList<>();
    }

    private void prepareOutputFolder() {
        String jasminPath = "utilities/jarFiles/jasmin.jar";
        String listClassPath = "utilities/codeGenerationUtilityClasses/List.j";
        String fptrClassPath = "utilities/codeGenerationUtilityClasses/Fptr.j";
        try{
            File directory = new File(this.outputPath);
            File[] files = directory.listFiles();
            if(files != null)
                for (File file : files)
                    file.delete();
            directory.mkdir();
        }
        catch(SecurityException e) {//unreachable
        }
        copyFile(jasminPath, this.outputPath + "jasmin.jar");
        copyFile(listClassPath, this.outputPath + "List.j");
        copyFile(fptrClassPath, this.outputPath + "Fptr.j");

        try {
            String path = outputPath + "Main.j";
            File file = new File(path);
            file.createNewFile();
            mainFile = new FileWriter(path);
        } catch (IOException e) {//unreachable
        }
    }

    private void copyFile(String toBeCopied, String toBePasted) {
        try {
            File readingFile = new File(toBeCopied);
            File writingFile = new File(toBePasted);
            InputStream readingFileStream = new FileInputStream(readingFile);
            OutputStream writingFileStream = new FileOutputStream(writingFile);
            byte[] buffer = new byte[1024];
            int readLength;
            while ((readLength = readingFileStream.read(buffer)) > 0)
                writingFileStream.write(buffer, 0, readLength);
            readingFileStream.close();
            writingFileStream.close();
        } catch (IOException e) {//never reached
        }
    }
    private String castObject(Type t) {
        return String.format("checkcast %s\n", getObjectType(t));
    }

    private void addCommand(String command) {
        try {
            command = String.join("\n\t\t", command.split("\n"));
            if(command.startsWith("Label_"))
                mainFile.write("\t" + command + "\n");
            else if(command.startsWith("."))
                mainFile.write(command + "\n");
            else
                mainFile.write("\t\t" + command + "\n");
            mainFile.flush();
        } catch (IOException e) {//unreachable

        }
    }

    private void addStaticMainMethod() {
        addCommand(".method public static main([Ljava/lang/String;)V");
        addCommand(".limit stack "+this.stack_size);
        addCommand(".limit locals "+this.stack_size);
        addCommand(String.format("new %s", "Main"));
        addCommand(String.format("invokespecial %s/<init>()V", "Main"));
        addCommand("return");
        addCommand(".end method");
        addCommand("");
    }

    private int slotOf(String identifier) {
        int index = 1;

        if (this.curFuncDec != null) {
            ArrayList<Identifier> args = this.curFuncDec.getArgs();

            for (Identifier arg: args) {
                if (arg.getName().equals(identifier)) {
                    return index;
                }
                index ++;
            }
        }

        for (Identifier arg: this.local_vars) {
            if (arg.getName().equals(identifier)) {
                return index;
            }
            index ++;
        }

        if (identifier.equals("")) {
            this.currentSlot++;
            return this.currentSlot + index;
        }
        return index;
    }

    private String getObjectType(Type t) {
        if (t instanceof IntType)
            return "java/lang/Integer";
        else if (t instanceof ListType)
            return "List";
        else if (t instanceof BoolType)
            return "java/lang/Boolean";
        else if (t instanceof StringType)
            return "java/lang/String";
        else if (t instanceof ClassType)
            return ((ClassType) t).getClassName().getName();
        else if (t instanceof FptrType)
            return "Fptr";
        else if (t instanceof VoidType)
            return "V";
        else
            return "ERROR Type";
    }

    private String makeTypeSignature(Type t) {
        return "L" + getObjectType(t) + ";";
    }

    private String makeReturnTypeSignature(FunctionSymbolTableItem func) {
        if (func.getReturnType() instanceof NullType || func.getReturnType() instanceof VoidType)
            return "V";
        else {
//            System.out.println(func.getReturnType().getClass().toString());
            return makeTypeSignature(func.getReturnType());
        }

        //return makeTypeSignature(func.getReturnType());
    }

    private String getPrimitiveType(Type t) {
        if (t instanceof IntType)
            return "I";
        else if (t instanceof BoolType)
            return "Z";
        else
            return makeTypeSignature(t);
    }

    private FunctionSymbolTableItem findFuncSymobolTableItem(FunctionDeclaration fptr) {
        try{
            FunctionSymbolTableItem func = (FunctionSymbolTableItem) SymbolTable.root.getItem(FunctionSymbolTableItem.START_KEY + fptr.getFunctionName().getName());
            return func;
        }catch (ItemNotFoundException e){
            return null;
        }
    }

    private int getLabel() {
        return ++this.labelNum;
    }

    private String toNonpremitive(Type t) {
        if(t instanceof IntType)
            return "invokestatic java/lang/Integer/valueOf(I)Ljava/lang/Integer;\n";
        if(t instanceof BoolType)
            return "invokestatic java/lang/Boolean/valueOf(Z)Ljava/lang/Boolean;\n";
        return "";
    }

    private String toPremitive(Type t) {
        if(t instanceof IntType)
            return "invokevirtual java/lang/Integer/intValue()I\n";
        if(t instanceof BoolType)
            return "invokevirtual java/lang/Boolean/booleanValue()Z\n";
        return "";
    }

    private void prapare_temp(FunctionSymbolTableItem func)
    {
        this.tempCurFuncDec = this.curFuncDec;
        this.tempFunc = this.findFuncSymobolTableItem(this.curFuncDec);
        this.tempLabelNum = this.labelNum;
        this.tempSlot = this.currentSlot;
        this.temp_local_vars = this.local_vars;
        this.tempAnonymousFuncs = this.anonymousFuncs;

        this.curFuncDec = func.getFuncDeclaration();
        this.expressionTypeChecker.setCurFunction(func);
        this.labelNum = 0;
        this.currentSlot = 0;
        this.local_vars = new ArrayList<>();
        this.anonymousFuncs = new ArrayList<>();
    }

    private void back_to_temp()
    {
        this.curFuncDec = this.tempCurFuncDec;
        this.expressionTypeChecker.setCurFunction(tempFunc);
        this.labelNum = this.tempLabelNum;
        this.currentSlot = this.tempSlot;
        this.local_vars = this.temp_local_vars;
        this.anonymousFuncs = this.tempAnonymousFuncs;
    }

    private void declareAnonymousMethod() {
        for (AnonymousFunction anonymousFunction: this.anonymousFuncs) {
            FunctionSymbolTableItem func;
            String methodName = anonymousFunction.getName();

            if (this.anonyVisited.contains(methodName))
                return;

            this.anonyVisited.add(methodName);

            try{
                func = (FunctionSymbolTableItem) SymbolTable.root.getItem(FunctionSymbolTableItem.START_KEY + methodName);
                prapare_temp(func);
                func.getFuncDeclaration().accept(this);
                back_to_temp();

            }catch (ItemNotFoundException e){
                func = null;
            }
        }
    }

    @Override
    public String visit(Program program) {
        program.getMain().accept(this);

        for (FunctionDeclaration funcDec: program.getFunctions()) {
            if (this.visited.contains(funcDec.getFunctionName().getName())) {
                FunctionSymbolTableItem func = this.findFuncSymobolTableItem(funcDec);
                this.curFuncDec = funcDec;
                this.expressionTypeChecker.setCurFunction(func);
                this.labelNum = 0;
                this.currentSlot = 0;
                this.local_vars = new ArrayList<>();
                this.anonymousFuncs = new ArrayList<>();

                funcDec.accept(this);

                if (this.anonymousFuncs.size() != 0)
                    this.declareAnonymousMethod();
            }
        }

        return null;
    }

    @Override
    public String visit(FunctionDeclaration funcDeclaration) {

        String methodName = funcDeclaration.getFunctionName().getName();
        FunctionSymbolTableItem func = this.findFuncSymobolTableItem(funcDeclaration);

        String argsSigniture = "";
        for (Map.Entry<String, Type> argsWithKey: func.getArgTypes().entrySet())
            argsSigniture = argsSigniture.concat(makeTypeSignature(argsWithKey.getValue()));

        String returnSigniture = makeReturnTypeSignature(func);
        addCommand(".method public "+methodName+"("+argsSigniture+")"+returnSigniture);
        addCommand(".limit locals 128");
        addCommand(".limit stack 128");

        funcDeclaration.getBody().accept(this);

        addCommand(".end method");
        addCommand("");

        return null;
    }

    @Override
    public String visit(MainDeclaration mainDeclaration) {
        this.currentSlot = 0;
        this.local_vars = new ArrayList<>();
        this.anonymousFuncs = new ArrayList<>();

        addCommand(".class public Main");
        addCommand(".super java/lang/Object");
        addCommand("");

        this.addStaticMainMethod();

        addCommand(".method public <init>()V");
        addCommand(".limit locals "+this.stack_size);
        addCommand(".limit stack "+this.locals_size);
        addCommand("aload 0");
        addCommand("invokenonvirtual java/lang/Object/<init>()V");

        mainDeclaration.getBody().accept(this);

        addCommand("return");
        addCommand(".end method");
        addCommand("");

        if (this.anonymousFuncs.size() != 0)
            this.declareAnonymousMethod();
        return null;
    }


    @Override
    public String visit(BlockStmt blockStmt) {
        //addCommand(";new inblock Statement");
        for (Statement stmt: blockStmt.getStatements())
            stmt.accept(this);

        return null;
    }

    @Override
    public String visit(ConditionalStmt conditionalStmt) {
        String scopeLabel = Integer.toString(getLabel());

        String thenStmt = "thenStmt_" + scopeLabel;
        String elseStmt = "elseStmt_" + scopeLabel;
        String afterStmt = "afterStmt_" + scopeLabel;

        Expression condition = conditionalStmt.getCondition();
        addCommand(condition.accept(this));
        addCommand("ifeq " + elseStmt);
        addCommand(thenStmt + ":");

        Statement thenBody = conditionalStmt.getThenBody();
        if (thenBody != null) {
            thenBody.accept(this);
        }

        addCommand("goto " + afterStmt);
        addCommand(elseStmt + ":");

        Statement elseBody = conditionalStmt.getElseBody();
        if (elseBody != null) {
            elseBody.accept(this);
        }

        addCommand(afterStmt + ":");

        return null;
    }

    @Override
    public String visit(FunctionCallStmt funcCallStmt) {
        this.expressionTypeChecker.setFunctioncallStmt(true);
        addCommand(funcCallStmt.getFunctionCall().accept(this));
        addCommand("pop");
        this.expressionTypeChecker.setFunctioncallStmt(false);
        return null;
    }

    @Override
    public String visit(PrintStmt print) {

        addCommand("getstatic java/lang/System/out Ljava/io/PrintStream;");
        Expression arg = print.getArg();

        Type argType = arg.accept(expressionTypeChecker);
        String command = arg.accept(this);

        if (command != null)
            addCommand(command);

/*        if (argType instanceof ListType)
        {
            addCommand("invokevirtual java/io/PrintStream/println(" + getPrimitiveType(new IntType()) + ")V");
            addCommand("\n");
            return null;
        }*/

        addCommand("invokevirtual java/io/PrintStream/println(" + getPrimitiveType(argType) + ")V");
        addCommand("\n");
        return null;
    }

    @Override
    public String visit(ReturnStmt returnStmt) {

        Type type = returnStmt.getReturnedExpr().accept(expressionTypeChecker);

        /*if(returnStmt.getLine() == 17)
        {
            System.out.println(returnStmt.getReturnedExpr().getClass().toString() + " " + type);
        }*/

        if (type instanceof NullType || type instanceof VoidType) {
            addCommand("");
            addCommand("return");
        } else {
            addCommand(returnStmt.getReturnedExpr().accept(this));
            if (type instanceof IntType || type instanceof BoolType) {
                addCommand(toNonpremitive(type));
            }
            addCommand("areturn");
            addCommand("return");
        }
        return null;
    }

    private String compareExpressions(String operator, String cmd) {
        String scopeLabel = Integer.toString(getLabel());

        String commands = "";
        String operatorBegin = operator + "_" +  scopeLabel;
        String operatorEnd = "end_" + operator + "_" + scopeLabel;
        String expEnd = "end_exp_" + scopeLabel;

        commands += "if_" + cmd + operator + " " + operatorEnd + "\n";
        commands += operatorBegin + ":\n";
        commands += "iconst_0\n";
        commands += "goto " + expEnd + "\n";
        commands += operatorEnd + ":\n";
        commands += "iconst_1\n";
        commands += expEnd + ":\n";

        return commands;
    }

    private String equalityExpressions(Expression expression, String operator) {
        Type expType = expression.accept(expressionTypeChecker);
        if (expType instanceof IntType || expType instanceof BoolType) {
            return compareExpressions(operator, "icmp");
        }
        return compareExpressions(operator, "acmp");
    }

    private String shortCircuit(BinaryExpression binaryExpression) {
        String commands = "";
        String scopeLabel = Integer.toString(getLabel());
        String jumpExpression = "jumpExpression_" + scopeLabel;
        String endExpression = "endExpression_" + scopeLabel;

        Expression firstOperand = binaryExpression.getFirstOperand();
        Expression secondOperand = binaryExpression.getSecondOperand();
        BinaryOperator binaryOperator = binaryExpression.getBinaryOperator();

        commands += firstOperand.accept(this) + "\n";
        if (binaryOperator == BinaryOperator.and) {
            commands += "ifeq " + jumpExpression + "\n";
        } else { // BinaryOperator.or
            commands += "ifne " + jumpExpression + "\n";
        }

        commands += secondOperand.accept(this) + "\n";
        commands += "goto " + endExpression + "\n";
        commands += jumpExpression + ":\n";

        if (binaryOperator == BinaryOperator.and) {
            commands += "iconst_0\n";
        } else { // BinaryOperator.or
            commands += "iconst_1\n";
        }

        commands += endExpression + ":\n";

        return commands;
    }


    @Override
    public String visit(BinaryExpression binaryExpression) {
        BinaryOperator operator = binaryExpression.getBinaryOperator();
        String commands = "";

        if (!(operator == BinaryOperator.or || operator == BinaryOperator.and || operator == BinaryOperator.append)) {
            commands += binaryExpression.getFirstOperand().accept(this) + "\n";
            commands += binaryExpression.getSecondOperand().accept(this) + "\n";
        }
        //eq, neq, gt, lt, add, sub, mult, div, and, or, append
        if (operator == BinaryOperator.add) {
            commands += "iadd\n";
        }
        else if (operator == BinaryOperator.sub) {
            commands += "isub\n";
        }
        else if (operator == BinaryOperator.mult) {
            commands += "imul\n";
        }
        else if (operator == BinaryOperator.div) {
            commands += "idiv\n";
        }
        else if((operator == BinaryOperator.gt) || (operator == BinaryOperator.lt)) {
            commands += compareExpressions(operator.toString(), "icmp");
        }
        else if((operator == BinaryOperator.eq) || (operator == BinaryOperator.neq)) {
            String operatorString = operator == BinaryOperator.eq ? "eq" : "ne";
            commands += equalityExpressions(binaryExpression.getFirstOperand(), operatorString);
        }
        else if(operator == BinaryOperator.and) {
            commands += shortCircuit(binaryExpression);
        }
        else if(operator == BinaryOperator.or) {
            commands += shortCircuit(binaryExpression);
        }
        else if(operator == BinaryOperator.append){
            Type tr = binaryExpression.getSecondOperand().accept(expressionTypeChecker);

            commands += binaryExpression.getFirstOperand().accept(this);
            commands += "dup\n";
            commands += "getfield List/elements Ljava/util/ArrayList;\n";
            commands += binaryExpression.getSecondOperand().accept(this);
            commands += toNonpremitive(tr);
            commands += "invokevirtual java/util/ArrayList/add(Ljava/lang/Object;)Z\n";
            commands += "pop\n\n";
            //commands += "invokevirtual List/addElement(Ljava/lang/Object;)V\n";

//            commands += castObject(tr);
        }

        return commands;
    }

    private String notOperator() {
        String commands = "";

        String scopeLabel = Integer.toString(getLabel());

        String setTrue = "setTrue_" + scopeLabel;
        String scopeEnd = "end_notOp_" + scopeLabel;

        commands += ("ifeq " + setTrue + "\n");
        commands += ("iconst_0\n");
        commands += ("goto " + scopeEnd + "\n");
        commands += (setTrue + ":\n");
        commands += ("iconst_1\n");
        commands += (scopeEnd + ":\n");

        return commands;
    }

    //not, minus
    @Override
    public String visit(UnaryExpression unaryExpression) {
        UnaryOperator operator = unaryExpression.getOperator();
        String commands = "";
        if(operator == UnaryOperator.minus) {
            commands += unaryExpression.getOperand().accept(this);
            commands += "ineg\n";
        }
        else if(operator == UnaryOperator.not) {
            commands += unaryExpression.getOperand().accept(this);
            commands += notOperator();
        }
        return commands;
    }

    @Override
    public String visit(AnonymousFunction anonymousFunction) {
        String commands = "";
        FunctionSymbolTableItem func;
        String methodName = anonymousFunction.getName();
        this.anonymousFuncs.add(anonymousFunction);

        commands += "\n";
        commands += "new Fptr\n";
        commands += "dup\n";
        commands += "aload 0\n";
        commands += "ldc \""+ anonymousFunction.getName() + "\"\n";
        commands += "invokespecial Fptr/<init>(Ljava/lang/Object;Ljava/lang/String;)V\n";

        return commands;
    }

    @Override
    public String visit(Identifier identifier) {
        String commands = "";
        Type type = identifier.accept(expressionTypeChecker);

        if (type instanceof FptrType) {
            this.funCall_name = identifier.getName();
            this.local_vars.add(identifier);
            commands += "\n";
            commands += "new Fptr\n";
            commands += "dup\n";
//            commands += funcCall.getInstance().accept(this);
            commands += "aload 0\n";
            commands += "ldc \""+ identifier.getName() + "\"\n";
            commands += "invokespecial Fptr/<init>(Ljava/lang/Object;Ljava/lang/String;)V\n";

            return commands;
        }

        int slotNumber = slotOf(identifier.getName());
        String primitiveTypeConverter = toPremitive(type);

        commands += "aload " + slotNumber + "\n";
        commands += !primitiveTypeConverter.equals("") ? primitiveTypeConverter + "\n" : "";
        return commands;
    }

    @Override
    public String visit(ListAccessByIndex listAccessByIndex) {
        String commands = "";
        Type memberType = listAccessByIndex.accept(this.expressionTypeChecker);
        commands += listAccessByIndex.getInstance().accept(this);
        commands += listAccessByIndex.getIndex().accept(this);
        commands += "invokevirtual List/getElement(I)Ljava/lang/Object;\n";
        if(memberType instanceof IntType || memberType instanceof BoolType) {
            if(memberType instanceof IntType)
                commands += "checkcast java/lang/Integer\n";
            else
                commands += "checkcast java/lang/Boolean\n";
            commands += toPremitive(memberType);
        }
        else if(memberType instanceof StringType)
            commands += "checkcast java/lang/String\n";
        else if(memberType instanceof ListType)
            commands += "checkcast List\n";
        else if(memberType instanceof FptrType)
            commands += "checkcast Fptr\n";
        else if(memberType instanceof ClassType)
            commands += "checkcast " + ((ClassType) memberType).getClassName().getName() + "\n";
        return commands;
    }

    @Override
    public String visit(ListSize listSize) {
        String commands = "";

        commands += listSize.getInstance().accept(this);
        commands += "getfield List/elements Ljava/util/ArrayList;";
        commands += "\n";
        commands += "invokevirtual java/util/ArrayList/size()I";
        commands += "\n";

        /*addCommand(listSize.getInstance().accept(this));
        addCommand(String.format(
                "getfield %s/%s %s",
                "List",
                "elements",
                "Ljava/util/ArrayList;"
                )
        );
        addCommand("\ngetfield List/elements Ljava/util/ArrayList;");
        addCommand("invokevirtual java/util/ArrayList/size()I");*/

        return commands;
    }

    private String getNewArrayList() {
        String commands = "";
        commands += "\n";
        commands += "new java/util/ArrayList\n";
        commands += "dup\n";
        commands += "invokespecial java/util/ArrayList/<init>()V\n";
        return commands;
    }

    @Override
    public String visit(FunctionCall funcCall) {
        String commands = "";
        commands += funcCall.getInstance().accept(this);

        int tempSlot = slotOf("");
        commands += getNewArrayList();
        commands += "astore " + tempSlot + "\n";

        for (Expression arg: funcCall.getArgs()) {
            Type argType = arg.accept(expressionTypeChecker);
//            System.out.println(arg.getClass().toString() + " " + funcCall.getLine());
            commands += "\n";
            commands += "aload " + tempSlot + "\n";
            commands+= arg.accept(this);

            if (argType instanceof IntType || argType instanceof BoolType)
                commands += toNonpremitive(argType);

            commands += "invokevirtual java/util/ArrayList/add(Ljava/lang/Object;)Z\n";
            commands += "pop\n";
        }

        if(funcCall.getArgsWithKey().size() != 0) {
            FunctionSymbolTableItem func;
            Type type = funcCall.getInstance().accept(expressionTypeChecker);
            try{
                func = (FunctionSymbolTableItem) SymbolTable.root.getItem(FunctionSymbolTableItem.START_KEY + ((FptrType) type).getFunctionName());

            }catch (ItemNotFoundException e){
                func = null;
            }

            Map<String, Expression> rtypesWithKey = new HashMap<>();

            for (Map.Entry<Identifier, Expression> argsWithKey: funcCall.getArgsWithKey().entrySet()) {
                rtypesWithKey.put(argsWithKey.getKey().getName(), argsWithKey.getValue());
            }

            assert func != null;
            for (Map.Entry<String, Type> ltype : func.getArgTypes().entrySet()) {
                if (rtypesWithKey.containsKey(ltype.getKey())) {
                    Type argType = rtypesWithKey.get(ltype.getKey()).accept(expressionTypeChecker);
                    commands += "\n";
                    commands += "aload " + tempSlot + "\n";
                    commands+= rtypesWithKey.get(ltype.getKey()).accept(this);

                    if (argType instanceof IntType || argType instanceof BoolType)
                        commands += toNonpremitive(argType);

                    commands += "invokevirtual java/util/ArrayList/add(Ljava/lang/Object;)Z\n";
                    commands += "pop\n";
                }
            }
        }

        commands += "\n";
        commands += "aload " + tempSlot + "\n";
        commands += "invokevirtual Fptr/invoke(Ljava/util/ArrayList;)Ljava/lang/Object;\n";

        Type t = funcCall.accept(expressionTypeChecker);
        commands += "\n";
        commands += castObject(t);
        commands += toPremitive(t);

        return commands;
    }

    @Override
    public String visit(ListValue listValue) {
        String commands = "";
        commands += "new List\n";
        commands += "dup\n";
        commands += "new java/util/ArrayList\n";
        commands += "dup\n";
        commands += "invokespecial java/util/ArrayList/<init>()V\n\n";
        for(Expression e : listValue.getElements()) {
            commands += "dup\n";
            commands += e.accept(this);
            String temp = toNonpremitive(e.accept(this.expressionTypeChecker));
            if(temp != null)
                commands += temp;
            commands += "checkcast java/lang/Object\n";
            commands += "invokevirtual java/util/ArrayList/add(Ljava/lang/Object;)Z\n";
            commands += "pop\n\n";
        }
        commands += "invokespecial List/<init>(Ljava/util/ArrayList;)V\n\n";
        return commands;
    }

    @Override
    public String visit(IntValue intValue) {
        return "ldc " + intValue.getConstant() + "\n";
    }

    @Override
    public String visit(BoolValue boolValue) {
        int value = 0;
        if (boolValue.getConstant()) {
            value = 1;
        }
        return "ldc " + value + "\n";
    }

    @Override
    public String visit(StringValue stringValue) {
        return "ldc \""+ stringValue.getConstant() + "\"\n";
    }

    @Override
    public String visit(VoidValue voidValue) {
        return null;
    }
}

package main;

import main.ast.nodes.Program;
import main.visitor.ASTTreePrinter;
import main.visitor.NameAnalyzer;
import main.visitor.NameChecker;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import parsers.*;

public class JepetoCompiler {
    public void compile(CharStream textStream) {
        JepetoLexer jepetoLexer = new JepetoLexer(textStream);
        CommonTokenStream tokenStream = new CommonTokenStream(jepetoLexer);
        JepetoParser jepetoParser = new JepetoParser(tokenStream);
        Program program = jepetoParser.jepeto().jepetoProgram;
        //program.accept(new ASTTreePrinter());

        program.accept(new NameAnalyzer());
        NameChecker nameChecker = new NameChecker();
        program.accept(nameChecker);

        if (nameChecker.numberOfError > 0) {
            //System.out.println("Line " + nameChecker.numberOfError);
            System.exit(1);
        } else {
            program.accept(new ASTTreePrinter());
        }
    }
}
package java2haxe;

import org.eclipse.gmt.modisco.java.AssertStatement;
import org.eclipse.gmt.modisco.java.Block;
import org.eclipse.gmt.modisco.java.BreakStatement;
import org.eclipse.gmt.modisco.java.CatchClause;
import org.eclipse.gmt.modisco.java.ConstructorInvocation;
import org.eclipse.gmt.modisco.java.ContinueStatement;
import org.eclipse.gmt.modisco.java.DoStatement;
import org.eclipse.gmt.modisco.java.EmptyStatement;
import org.eclipse.gmt.modisco.java.EnhancedForStatement;
import org.eclipse.gmt.modisco.java.Expression;
import org.eclipse.gmt.modisco.java.ExpressionStatement;
import org.eclipse.gmt.modisco.java.ForStatement;
import org.eclipse.gmt.modisco.java.IfStatement;
import org.eclipse.gmt.modisco.java.LabeledStatement;
import org.eclipse.gmt.modisco.java.ReturnStatement;
import org.eclipse.gmt.modisco.java.Statement;
import org.eclipse.gmt.modisco.java.SuperConstructorInvocation;
import org.eclipse.gmt.modisco.java.SwitchCase;
import org.eclipse.gmt.modisco.java.SwitchStatement;
import org.eclipse.gmt.modisco.java.SynchronizedStatement;
import org.eclipse.gmt.modisco.java.ThrowStatement;
import org.eclipse.gmt.modisco.java.TryStatement;
import org.eclipse.gmt.modisco.java.TypeDeclarationStatement;
import org.eclipse.gmt.modisco.java.VariableDeclarationFragment;
import org.eclipse.gmt.modisco.java.VariableDeclarationStatement;
import org.eclipse.gmt.modisco.java.WhileStatement;

/**
 * This class implements the statement generation
 */
public class StatementSupport extends ExpressionSupport
{

    //
    // Statement Printing
    //

    protected void printStatement(HaxeWriter out, Statement statement)
    {
        if (!(statement instanceof Block))
        {
            printComments(out, statement.getComments());
        }

        if (statement instanceof AssertStatement)
        {
            printAssertStatement(out, (AssertStatement) statement);
        }
        else if (statement instanceof Block)
        {
            printBlock(out, (Block) statement);
        }
        else if (statement instanceof BreakStatement)
        {
            printBreakStatement(out, (BreakStatement) statement);
        }
        else if (statement instanceof ConstructorInvocation)
        {
            printConstructorInvocation(out, (ConstructorInvocation) statement);
        }
        else if (statement instanceof ContinueStatement)
        {
            printContinueStatement(out, (ContinueStatement) statement);
        }
        else if (statement instanceof DoStatement)
        {
            printDoStatement(out, (DoStatement) statement);
        }
        else if (statement instanceof EmptyStatement)
        {
            printEmptyStatement(out, (EmptyStatement) statement);
        }
        else if (statement instanceof EnhancedForStatement)
        {
            printEnhancedForStatement(out, (EnhancedForStatement) statement);
        }
        else if (statement instanceof ExpressionStatement)
        {
            printExpressionStatement(out, (ExpressionStatement) statement);
        }
        else if (statement instanceof ForStatement)
        {
            printForStatement(out, (ForStatement) statement);
        }
        else if (statement instanceof IfStatement)
        {
            printIfStatement(out, (IfStatement) statement);
        }
        else if (statement instanceof LabeledStatement)
        {
            printLabeledStatement(out, (LabeledStatement) statement);
        }
        else if (statement instanceof ReturnStatement)
        {
            printReturnStatement(out, (ReturnStatement) statement);
        }
        else if (statement instanceof SuperConstructorInvocation)
        {
            printSuperConstructorInvocation(out,
                    (SuperConstructorInvocation) statement);
        }
        else if (statement instanceof SwitchStatement)
        {
            printSwitchStatement(out, (SwitchStatement) statement);
        }
        else if (statement instanceof SynchronizedStatement)
        {
            printSynchronizedStatement(out, (SynchronizedStatement) statement);
        }
        else if (statement instanceof ThrowStatement)
        {
            printThrowStatement(out, (ThrowStatement) statement);
        }
        else if (statement instanceof TryStatement)
        {
            printTryStatement(out, (TryStatement) statement);
        }
        else if (statement instanceof TypeDeclarationStatement)
        {
            printTypeDeclarationStatement(out,
                    (TypeDeclarationStatement) statement);
        }
        else if (statement instanceof VariableDeclarationStatement)
        {
            printVariableDeclarationStatement(out,
                    (VariableDeclarationStatement) statement);
        }
        else if (statement instanceof WhileStatement)
        {
            printWhileStatement(out, (WhileStatement) statement);
        }
    }

    protected void printWhileStatement(HaxeWriter out, WhileStatement statement)
    {
        out.printIndent();
        out.print("while(");
        printExpression(out, statement.getExpression());
        out.println(")");
        if (!(statement.getBody() instanceof Block))
        {
            out.indent();
        }
        printStatement(out, statement.getBody());
        if (!(statement.getBody() instanceof Block))
        {
            out.outend();
        }
    }

    protected void printVariableDeclarationStatement(HaxeWriter out,
            VariableDeclarationStatement statement)
    {
        for (VariableDeclarationFragment fragment : statement.getFragments())
        {
            out.printIndent();
            out.print("var ");
            out.print(fragment.getName());
            out.print(" : ");
            printTypeAccess(out, statement.getType());

            if (fragment.getInitializer() != null)
            {
                out.print(" = ");
                printExpression(out, fragment.getInitializer());
            }

            out.println(";");
        }
    }

    protected void printTypeDeclarationStatement(HaxeWriter out,
            TypeDeclarationStatement statement)
    {
        error(out, "class declaration statements are not supported", false);
    }

    protected void printTryStatement(HaxeWriter out, TryStatement statement)
    {
        out.printIndent();
        out.println("try");
        printBlock(out, statement.getBody());

        for (CatchClause c : statement.getCatchClauses())
        {
            printComments(out, c.getComments());
            out.printIndent();
            out.print("catch(");
            printSingleVariableDeclaration(out, c.getException());
            out.println(")");

            printBlock(out, c.getBody());
        }

        if (statement.getFinally() != null)
        {
            warn(out, "TODO: workaround for finally needed", false);
            out.printIndent();
            out.println("finally");
            printBlock(out, statement.getFinally());
        }
    }

    protected void printThrowStatement(HaxeWriter out, ThrowStatement statement)
    {
        out.printIndent();
        out.print("throw ");
        printExpression(out, statement.getExpression());
        out.println(";");
    }

    protected void printSynchronizedStatement(HaxeWriter out,
            SynchronizedStatement statement)
    {
        error(out, "Synchronized statement not supported", false);
        out.printIndent();
        out.print("/*synchronized(*/");
        printExpression(out, statement.getExpression());
        out.print("/*)*/");
        printBlock(out, statement.getBody());
    }

    protected void printSwitchStatement(HaxeWriter out,
            SwitchStatement statement)
    {
        out.printIndent();
        out.print("switch(");
        printExpression(out, statement.getExpression());
        out.println(")");

        out.printIndent();
        out.println("{");
        out.indent();

        for (int i = 0; i < statement.getStatements().size(); i++)
        {
            Statement s = statement.getStatements().get(i);
            if (s instanceof SwitchCase)
            {
                SwitchCase switchCase = (SwitchCase) s;
                out.printIndent();
                if (switchCase.isDefault())
                {
                    out.println("default:");
                }
                else
                {
                    out.print("case ");
                    printExpression(out, switchCase.getExpression());

                    while ((i < (statement.getStatements().size() - 1))
                            && (statement.getStatements().get(i + 1) instanceof SwitchCase)
                            && !((SwitchCase) (statement.getStatements()
                                    .get(i + 1))).isDefault())
                    {
                        out.print(", ");
                        printExpression(out, ((SwitchCase) statement
                                .getStatements().get(i++)).getExpression());
                    }

                    out.println(": ");
                }
            }
            else if (s instanceof BreakStatement)
            {
                // skip break if it is the last statement or the next statement
                // is a switchcase
                if ((i < (statement.getStatements().size() - 1))
                        && !(statement.getStatements().get(i + 1) instanceof SwitchCase))
                {
                    printStatement(out, s);
                }
            }
            else
            {
                printStatement(out, s);
            }
        }

        out.outend();
        out.printIndent();
        out.println("}");

    }

    protected void printSuperConstructorInvocation(HaxeWriter out,
            SuperConstructorInvocation statement)
    {
        out.printIndent();
        out.print("super(");

        for (int i = 0; i < statement.getArguments().size(); i++)
        {
            if (i > 0)
            {
                out.print(", ");
            }

            printExpression(out, statement.getArguments().get(i));
        }

        out.println(");");
    }

    protected void printReturnStatement(HaxeWriter out,
            ReturnStatement statement)
    {
        out.printIndent();
        if (statement.getExpression() == null)
        {
            out.println("return;");
        }
        else
        {
            out.print("return ");
            printExpression(out, statement.getExpression());
            out.println(";");
        }
    }

    protected void printLabeledStatement(HaxeWriter out,
            LabeledStatement statement)
    {
        error(out, "statement labels are not supported", false);
        printStatement(out, statement.getBody());
    }

    protected void printIfStatement(HaxeWriter out, IfStatement statement)
    {
        out.printIndent();
        out.print("if(");
        printExpression(out, statement.getExpression());
        out.println(")");
        if (!(statement.getThenStatement() instanceof Block))
        {
            out.indent();
        }
        printStatement(out, statement.getThenStatement());
        if (!(statement.getThenStatement() instanceof Block))
        {
            out.outend();
        }

        if (statement.getElseStatement() != null)
        {
            printComments(out, statement.getElseStatement().getComments());
            out.printIndent();
            out.print("else ");
            if (statement.getElseStatement() instanceof IfStatement)
            {
                printStatement(out, statement.getElseStatement());
            }
            else if (statement.getElseStatement() instanceof Block)
            {
                out.println();
                printStatement(out, statement.getElseStatement());
            }
            else
            {
                out.println();
                out.indent();
                printStatement(out, statement.getElseStatement());
                out.outend();
            }
        }
    }

    protected void printForStatement(HaxeWriter out, ForStatement statement)
    {
        // thanks to David Holan for the clean pattern of converting for loops
        /*
         * { <init>; if ( <condition> ) do {
         * 
         * <body> } while ( function(){ <increment>; return false; }() ||
         * <condition> ); }
         */
        out.printIndent();
        out.println("{");
        out.indent();
        {
            // <init>
            for (Expression e : statement.getInitializers())
            {
                out.printIndent();
                printExpression(out, e);
                out.println(";");
            }

            out.printIndent();
            out.print("if(");
            if (statement.getExpression() != null)
            {
                printExpression(out, statement.getExpression());
            }
            else
            {
                out.print(true);
            }
            out.println(") do");
            out.printIndent();

            // <body>
            out.println("{");
            out.indent();
            {
                printStatement(out, statement.getBody());
            }
            out.outend();
            out.printIndent();
            out.print("} while( function() { ");
            // <increment>
            for (Expression e : statement.getUpdaters())
            {
                printExpression(out, e);
                out.print("; ");
            }
            out.print("; return false; }() || ");
            if (statement.getExpression() != null)
            {
                printExpression(out, statement.getExpression());
            }
            else
            {
                out.print("true");
            }
            out.println(");");

        }
        out.outend();
        out.printIndent();
        out.println("}");
    }

    protected void printExpressionStatement(HaxeWriter out,
            ExpressionStatement statement)
    {
        out.printIndent();
        printExpression(out, statement.getExpression());
        out.println(";");
    }

    protected void printEnhancedForStatement(HaxeWriter out,
            EnhancedForStatement statement)
    {
        out.printIndent();
        out.print("for(");
        out.print(statement.getParameter().getName());
        out.print(" in ");
        printExpression(out, statement.getExpression());
        out.println(")");

        if (!(statement.getBody() instanceof Block))
        {
            out.indent();
        }
        printStatement(out, statement.getBody());
        if (!(statement.getBody() instanceof Block))
        {
            out.outend();
        }
    }

    protected void printEmptyStatement(HaxeWriter out, EmptyStatement statement)
    {
        // we don't need those
    }

    protected void printDoStatement(HaxeWriter out, DoStatement statement)
    {
        out.printIndent();
        out.println("do");
        if (!(statement.getBody() instanceof Block))
        {
            out.indent();
        }
        printStatement(out, statement.getBody());
        if (!(statement.getBody() instanceof Block))
        {
            out.outend();
        }
        out.print("while(");
        printExpression(out, statement.getExpression());
        out.println(");");
    }

    protected void printContinueStatement(HaxeWriter out,
            ContinueStatement statement)
    {
        if (statement.getLabel() != null)
        {
            error(out, "labeled continues are not supported", false);
        }
        out.printIndent();
        out.println("continue;");
    }

    protected void printConstructorInvocation(HaxeWriter out,
            ConstructorInvocation statement)
    {
        error(out, "constructor overloads are not supported", false);
        out.printIndent();
        printAbstractMethodInvokation(out, statement, "this");
        out.println(";");
    }

    protected void printBreakStatement(HaxeWriter out, BreakStatement statement)
    {
        if (statement.getLabel() != null)
        {
            error(out, "labeled breaks are not supported", false);
        }
        out.printIndent();
        out.println("break;");
    }

    protected void printAssertStatement(HaxeWriter out,
            AssertStatement statement)
    {
        error(out, "assert statements not supported", false);
    }

    protected void printBlock(HaxeWriter out, Block body)
    {
        out.printIndent();
        out.println("{");
        out.indent();
        printComments(out, body.getComments());
        for (Statement stmt : body.getStatements())
        {
            printStatement(out, stmt);
        }

        out.outend();
        out.printIndent();
        out.println("}");
    }
}

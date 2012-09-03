package java2haxe;

import org.eclipse.gmt.modisco.java.AbstractMethodInvocation;
import org.eclipse.gmt.modisco.java.Annotation;
import org.eclipse.gmt.modisco.java.ArrayAccess;
import org.eclipse.gmt.modisco.java.ArrayCreation;
import org.eclipse.gmt.modisco.java.ArrayInitializer;
import org.eclipse.gmt.modisco.java.ArrayLengthAccess;
import org.eclipse.gmt.modisco.java.ArrayType;
import org.eclipse.gmt.modisco.java.Assignment;
import org.eclipse.gmt.modisco.java.BooleanLiteral;
import org.eclipse.gmt.modisco.java.CastExpression;
import org.eclipse.gmt.modisco.java.CharacterLiteral;
import org.eclipse.gmt.modisco.java.ClassInstanceCreation;
import org.eclipse.gmt.modisco.java.ConditionalExpression;
import org.eclipse.gmt.modisco.java.Expression;
import org.eclipse.gmt.modisco.java.FieldAccess;
import org.eclipse.gmt.modisco.java.InfixExpression;
import org.eclipse.gmt.modisco.java.InstanceofExpression;
import org.eclipse.gmt.modisco.java.MethodInvocation;
import org.eclipse.gmt.modisco.java.NullLiteral;
import org.eclipse.gmt.modisco.java.NumberLiteral;
import org.eclipse.gmt.modisco.java.ParenthesizedExpression;
import org.eclipse.gmt.modisco.java.PostfixExpression;
import org.eclipse.gmt.modisco.java.PrefixExpression;
import org.eclipse.gmt.modisco.java.SingleVariableAccess;
import org.eclipse.gmt.modisco.java.StringLiteral;
import org.eclipse.gmt.modisco.java.SuperFieldAccess;
import org.eclipse.gmt.modisco.java.SuperMethodInvocation;
import org.eclipse.gmt.modisco.java.ThisExpression;
import org.eclipse.gmt.modisco.java.TypeAccess;
import org.eclipse.gmt.modisco.java.TypeLiteral;
import org.eclipse.gmt.modisco.java.VariableDeclarationExpression;
import org.eclipse.gmt.modisco.java.VariableDeclarationFragment;

/**
 * This class implements the generation of the expression tree.
 */
public class ExpressionSupport extends BasicElementSupport
{

    //
    // Expression Printing
    //

    protected void printExpression(HaxeWriter out, Expression ex)
    {
        if (ex instanceof Annotation)
        {
            printAnnotation(out, (Annotation) ex);
        }
        else if (ex instanceof ArrayInitializer)
        {
            printArrayInitializer(out, (ArrayInitializer) ex);
        }
        else if (ex instanceof ArrayLengthAccess)
        {
            printArrayLengthAccess(out, (ArrayLengthAccess) ex);
        }
        else if (ex instanceof BooleanLiteral)
        {
            printBooleanLiteral(out, (BooleanLiteral) ex);
        }
        else if (ex instanceof ClassInstanceCreation)
        {
            printClassInstanceCreation(out, (ClassInstanceCreation) ex);
        }
        else if (ex instanceof ArrayAccess)
        {
            printArrayAccess(out, (ArrayAccess) ex);
        }
        else if (ex instanceof ArrayCreation)
        {
            printArrayCreation(out, (ArrayCreation) ex);
        }
        else if (ex instanceof Assignment)
        {
            printAssignment(out, (Assignment) ex);
        }
        else if (ex instanceof ConditionalExpression)
        {
            printConditionalExpression(out, (ConditionalExpression) ex);
        }
        else if (ex instanceof CastExpression)
        {
            printCastExpression(out, (CastExpression) ex);
        }
        else if (ex instanceof CharacterLiteral)
        {
            printCharacterLiteral(out, (CharacterLiteral) ex);
        }
        else if (ex instanceof InfixExpression)
        {
            printInfixExpression(out, (InfixExpression) ex);
        }
        else if (ex instanceof FieldAccess)
        {
            printFieldAccess(out, (FieldAccess) ex);
        }
        else if (ex instanceof InstanceofExpression)
        {
            printInstanceofExpression(out, (InstanceofExpression) ex);
        }
        else if (ex instanceof MethodInvocation)
        {
            printMethodInvocation(out, (MethodInvocation) ex);
        }
        else if (ex instanceof TypeLiteral)
        {
            printTypeLiteral(out, (TypeLiteral) ex);
        }
        else if (ex instanceof PostfixExpression)
        {
            printPostfixExpression(out, (PostfixExpression) ex);
        }
        else if (ex instanceof ParenthesizedExpression)
        {
            printParenthesizedExpression(out, (ParenthesizedExpression) ex);
        }
        else if (ex instanceof NumberLiteral)
        {
            printNumberLiteral(out, (NumberLiteral) ex);
        }
        else if (ex instanceof NullLiteral)
        {
            printNullLiteral(out, (NullLiteral) ex);
        }
        else if (ex instanceof PrefixExpression)
        {
            printPrefixExpression(out, (PrefixExpression) ex);
        }
        else if (ex instanceof StringLiteral)
        {
            printStringLiteral(out, (StringLiteral) ex);
        }
        else if (ex instanceof SuperFieldAccess)
        {
            printSuperFieldAccess(out, (SuperFieldAccess) ex);
        }
        else if (ex instanceof VariableDeclarationExpression)
        {
            printVariableDeclarationExpression(out,
                    (VariableDeclarationExpression) ex);
        }
        else if (ex instanceof ThisExpression)
        {
            printThisExpression(out, (ThisExpression) ex);
        }
        else if (ex instanceof SuperMethodInvocation)
        {
            printSuperMethodInvocation(out, (SuperMethodInvocation) ex);
        }
        else if (ex instanceof SingleVariableAccess)
        {
            printSingleVariableAccess(out, (SingleVariableAccess) ex);
        }
        else if (ex instanceof TypeAccess)
        {
            printTypeAccess(out, (TypeAccess) ex);
        }
    }

    protected void printSingleVariableAccess(HaxeWriter out,
            SingleVariableAccess ex)
    {
        out.print(ex.getVariable().getName());
    }

    protected void printSuperMethodInvocation(HaxeWriter out,
            SuperMethodInvocation ex)
    {
        out.print("super.");
        printAbstractMethodInvokation(out, ex, ex.getMethod().getName());
    }

    protected void printThisExpression(HaxeWriter out, ThisExpression ex)
    {
        out.print("this");
    }

    protected void printVariableDeclarationExpression(HaxeWriter out,
            VariableDeclarationExpression ex)
    {
        out.print("var ");
        for (VariableDeclarationFragment f : ex.getFragments())
        {
            out.print(f.getName());
            out.print(" : ");
            printTypeAccess(out, ex.getType());

            if (f.getInitializer() != null)
            {
                out.print(" = ");
                printExpression(out, f.getInitializer());
            }
        }
    }

    protected void printSuperFieldAccess(HaxeWriter out, SuperFieldAccess ex)
    {
        out.print("super.");
        out.print(ex.getField().getVariable().getName());
    }

    protected void printStringLiteral(HaxeWriter out, StringLiteral ex)
    {
        out.print("\"");
        out.print(ex.getEscapedValue());
        out.print("\"");
    }

    protected void printPrefixExpression(HaxeWriter out, PrefixExpression ex)
    {
        out.print(ex.getOperator().getLiteral());
        printExpression(out, ex.getOperand());
    }

    protected void printNullLiteral(HaxeWriter out, NullLiteral ex)
    {
        out.print("null");
    }

    protected void printNumberLiteral(HaxeWriter out, NumberLiteral ex)
    {
        if (ex.getTokenValue().toLowerCase().endsWith("f")
                || ex.getTokenValue().toLowerCase().endsWith("l"))
        {
            out.print(ex.getTokenValue().substring(0,
                    ex.getTokenValue().length() - 1));
        }
        else
        {
            out.print(ex.getTokenValue());
        }
    }

    protected void printParenthesizedExpression(HaxeWriter out,
            ParenthesizedExpression ex)
    {
        out.print("(");

        printExpression(out, ex.getExpression());

        out.print(")");
    }

    protected void printPostfixExpression(HaxeWriter out, PostfixExpression ex)
    {
        printExpression(out, ex.getOperand());
        out.print(ex.getOperator().getLiteral());
    }

    protected void printTypeLiteral(HaxeWriter out, TypeLiteral ex)
    {
        printTypeAccess(out, ex.getType());
    }

    protected void printMethodInvocation(HaxeWriter out, MethodInvocation ex)
    {
        if (ex.getExpression() != null)
        {
            printExpression(out, ex.getExpression());
            out.print(".");
        }
        printAbstractMethodInvokation(out, ex, ex.getMethod().getName());
    }

    protected void printInstanceofExpression(HaxeWriter out,
            InstanceofExpression ex)
    {
        out.print("Std.is(");
        printExpression(out, ex.getLeftOperand());
        out.print(", ");
        printTypeAccess(out, ex.getRightOperand());
        out.print(")");
    }

    protected void printFieldAccess(HaxeWriter out, FieldAccess ex)
    {
        printExpression(out, ex.getExpression());
        out.print(".");
        out.print(ex.getField().getVariable().getName());
    }

    protected void printInfixExpression(HaxeWriter out, InfixExpression ex)
    {
        printExpression(out, ex.getLeftOperand());
        out.print(" ");
        out.print(ex.getOperator().getLiteral());
        out.print(" ");
        printExpression(out, ex.getRightOperand());
    }

    protected void printCharacterLiteral(HaxeWriter out, CharacterLiteral ex)
    {
        out.print("\"");
        out.print(ex.getEscapedValue());
        out.print("\".code");
    }

    protected void printCastExpression(HaxeWriter out, CastExpression ex)
    {
        out.print("cast(");
        printExpression(out, ex.getExpression());
        out.print(", ");
        printTypeAccess(out, ex.getType());
        out.print(")");
    }

    protected void printConditionalExpression(HaxeWriter out,
            ConditionalExpression ex)
    {
        printExpression(out, ex.getExpression());
        out.print(" ? ");
        printExpression(out, ex.getThenExpression());
        out.print(" : ");
        printExpression(out, ex.getElseExpression());
    }

    protected void printAssignment(HaxeWriter out, Assignment ex)
    {
        printExpression(out, ex.getLeftHandSide());

        switch (ex.getOperator())
        {
            case ASSIGN:
                out.print(" = ");
                printExpression(out, ex.getRightHandSide());
                break;
            // haxe does not support shorthand-assignments, we need to duplicate
            // the lefhandside
            case BIT_AND_ASSIGN:
                out.print(" = (");
                printExpression(out, ex.getLeftHandSide());
                out.print(") & ");
                printExpression(out, ex.getRightHandSide());
                break;
            case BIT_OR_ASSIGN:
                out.print(" = (");
                printExpression(out, ex.getLeftHandSide());
                out.print(") | ");
                printExpression(out, ex.getRightHandSide());
                break;
            case BIT_XOR_ASSIGN:
                out.print(" = (");
                printExpression(out, ex.getLeftHandSide());
                out.print(") ^ ");
                printExpression(out, ex.getRightHandSide());
                break;
            case DIVIDE_ASSIGN:
                out.print(" = (");
                printExpression(out, ex.getLeftHandSide());
                out.print(") / ");
                printExpression(out, ex.getRightHandSide());
                break;
            case LEFT_SHIFT_ASSIGN:
                out.print(" = (");
                printExpression(out, ex.getLeftHandSide());
                out.print(") << ");
                printExpression(out, ex.getRightHandSide());
                break;
            case MINUS_ASSIGN:
                out.print(" = (");
                printExpression(out, ex.getLeftHandSide());
                out.print(") - ");
                printExpression(out, ex.getRightHandSide());
                break;
            case PLUS_ASSIGN:
                out.print(" = (");
                printExpression(out, ex.getLeftHandSide());
                out.print(") + ");
                printExpression(out, ex.getRightHandSide());
                break;
            case REMAINDER_ASSIGN:
                out.print(" = (");
                printExpression(out, ex.getLeftHandSide());
                out.print(") % ");
                printExpression(out, ex.getRightHandSide());
                break;
            case RIGHT_SHIFT_SIGNED_ASSIGN:
                out.print(" = (");
                printExpression(out, ex.getLeftHandSide());
                out.print(") >> ");
                printExpression(out, ex.getRightHandSide());
                break;
            case RIGHT_SHIFT_UNSIGNED_ASSIGN:
                out.print(" = (");
                printExpression(out, ex.getLeftHandSide());
                out.print(") >>> ");
                printExpression(out, ex.getRightHandSide());
                break;
            case TIMES_ASSIGN:
                out.print(" = (");
                printExpression(out, ex.getLeftHandSide());
                out.print(") * ");
                printExpression(out, ex.getRightHandSide());
                break;
        }
    }

    protected void printArrayCreation(HaxeWriter out, ArrayCreation ex)
    {
        // ArrayUtils.create2DArray(1,2);
        out.print("ArrayUtils.create");
        out.print(((ArrayType) ex.getType().getType()).getDimensions());
        out.print("DArray<");
        printTypeAccess(out,
                ((ArrayType) ex.getType().getType()).getElementType());
        out.print(">(");

        if (ex.getInitializer() == null)
        {
            out.print("null, ");
        }
        else
        {
            printExpression(out, ex.getInitializer());
        }

        for (int i = 0; i < ex.getDimensions().size(); i++)
        {
            if (i > 0)
            {
                out.print(", ");
            }
            printExpression(out, ex.getDimensions().get(i));
        }
        out.print(")");

    }

    protected void printArrayAccess(HaxeWriter out, ArrayAccess ex)
    {
        out.print("[");
        printExpression(out, ex.getIndex());
        out.print("]");
    }

    protected void printClassInstanceCreation(HaxeWriter out,
            ClassInstanceCreation ex)
    {
        out.print("new ");

        printTypeAccess(out, ex.getType());
        out.print("(");

        for (int i = 0; i < ex.getArguments().size(); i++)
        {
            if (i > 0)
            {
                out.print(", ");
            }
            printExpression(out, ex.getArguments().get(i));
        }

        out.print(")");
    }

    protected void printBooleanLiteral(HaxeWriter out, BooleanLiteral ex)
    {
        out.print(ex.isValue() ? "true" : "false");
    }

    protected void printArrayLengthAccess(HaxeWriter out, ArrayLengthAccess ex)
    {
        out.print(".length");
    }

    protected void printArrayInitializer(HaxeWriter out, ArrayInitializer ex)
    {
        out.print("[");

        for (int i = 0; i < ex.getExpressions().size(); i++)
        {
            if (i > 0)
            {
                out.print(", ");
            }
            printExpression(out, ex.getExpressions().get(i));
        }

        out.print("]");
    }

    protected void printAnnotation(HaxeWriter out, Annotation ex)
    {
        // no annotation generation
    }

    protected void printAbstractMethodInvokation(HaxeWriter out,
            AbstractMethodInvocation invoke, String methodName)
    {
        out.print(methodName);
        if (invoke.getTypeArguments().size() > 0)
        {
            error(out, "generic methods are not supported", false);
        }
        printTypeArguments(out, invoke.getTypeArguments());
        out.print("(");

        int paramIndex = 0;
        for (int i = 0; i < invoke.getArguments().size(); i++)
        {
            if (i > 0)
            {
                out.print(", ");
            }
            boolean hasParam = paramIndex < invoke.getMethod().getParameters()
                    .size();
            if (hasParam
                    && invoke.getMethod().getParameters().get(paramIndex)
                            .isVarargs())
            {
                out.print("Reflect.makeVarArgs(");
                printExpression(out, invoke.getArguments().get(i));
                out.print(")");
            }
            else
            {
                if (!hasParam)
                {
                    warn(out, "Detected varargs of core method", false);
                }
                printExpression(out, invoke.getArguments().get(i));
                paramIndex++;
            }
        }
        out.print(")");
    }
}

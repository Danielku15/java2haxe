package java2haxe;

import org.eclipse.emf.common.util.EList;
import org.eclipse.gmt.modisco.java.Comment;
import org.eclipse.gmt.modisco.java.SingleVariableDeclaration;

/**
 * This class implements some general code generation stuff like comments
 */
public class BasicElementSupport extends TypeNameSupport
{
    protected void printComments(HaxeWriter out, EList<Comment> comments)
    {
        for (Comment c : comments)
        {
            printComment(out, c);
        }
    }

    protected void printComment(HaxeWriter out, Comment c)
    {
        String[] lines = c.getContent().split("\\r?\\n");
        for (String l : lines)
        {
            out.printIndent();
            out.println(l);
        }
    }

    protected void printSingleVariableDeclaration(HaxeWriter out,
            SingleVariableDeclaration singleVariableDeclaration)
    {
        if (singleVariableDeclaration.isVarargs())
        {
            out.print(singleVariableDeclaration.getName());
            out.print(" : Array<");
            printTypeAccess(out, singleVariableDeclaration.getType());
            out.print(">");
        }
        else
        {
            out.print(singleVariableDeclaration.getName());
            out.print(" : ");
            printTypeAccess(out, singleVariableDeclaration.getType());
        }
    }

}

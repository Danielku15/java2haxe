package java2haxe;

/**
 * This class implements some logging functionality for the code generator.
 */
public class LogSupport
{
    public void warn(HaxeWriter out, String s, boolean inline)
    {
        log(out, "WARN: " + s, inline);
    }

    public void error(HaxeWriter out, String s, boolean inline)
    {
        log(out, "ERR: " + s, inline);
    }

    private void log(HaxeWriter out, String s, boolean inline)
    {
        if (out != null)
        {
            if (inline)
            {
                out.print("/* " + s + " */ ");
            }
            else
            {
                out.println();
                out.printIndent();
                out.println("/* " + s + " */");
            }
        }
        System.out.println(s);
    }
}

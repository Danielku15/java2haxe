package java2haxe;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class HaxeWriter extends PrintWriter
{
    private int _indent;

    public HaxeWriter(File file) throws FileNotFoundException
    {
        super(file);
    }

    public void indent()
    {
        _indent++;
    }

    public void outend()
    {
        _indent--;
        if (_indent < 0)
        {
            _indent = 0;
        }
    }

    public void printIndent()
    {
        for (int i = 0; i < _indent * 4; i++)
        {
            print(" ");
        }
    }
}

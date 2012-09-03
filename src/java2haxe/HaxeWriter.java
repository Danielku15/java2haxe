/*
 * This file is part of java2haxe.
 *
 *  java2haxe is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  alphaTab is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with java2haxe.  If not, see <http://www.gnu.org/licenses/>.
 */
package java2haxe;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 * This writer provides some utility methods for writing Haxe source code.
 */
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

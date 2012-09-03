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

/**
 * This small command-line utility allows the conversion of a XMI file created
 * by MoDisco to Haxe source. Simply convert any Java project to a XMI file
 * using the MoDisco plugin and run this converter to get Haxe source.
 */
public class Converter
{
    public static void main(String[] args)
    {
        if (args.length != 2)
        {
            printHelp();
            return;
        }

        try
        {
            // initialize the processor
            MoDiscoXmiToHaxeProcessor processor = new MoDiscoXmiToHaxeProcessor();
            processor.setInputXmi(args[0]);
            processor.setTargetDir(args[1]);
            processor.run();
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }

    private static void printHelp()
    {
        System.out.println("Java2Haxe Converter");
        System.out.println("About:");
        System.out
                .println("  This small command-line utility allows the conversion of a XMI file created");
        System.out
                .println("  by MoDisco to Haxe source. Simply convert any Java project to a XMI file using");
        System.out
                .println("  the MoDisco plugin and run this converter to get Haxe source.");
        System.out.println("Usage:");
        System.out.println("  java2haxe.Converter [xmi-file] [output-dir]");
    }
}

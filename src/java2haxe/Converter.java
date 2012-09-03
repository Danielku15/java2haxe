package java2haxe;

/**
 * This small command-line utility allows the conversion of a XMI file created
 * by Modisc to Haxe source. Simply convert any Java project to a XMI file using
 * the Modisc plugin and run this converter to get Haxe source.
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
            ModiscXmiToHaxeProcessor processor = new ModiscXmiToHaxeProcessor();
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
                .println("  by Modisc to Haxe source. Simply convert any Java project to a XMI file using");
        System.out
                .println("  the Modisc plugin and run this converter to get Haxe source.");
        System.out.println("Usage:");
        System.out.println("  java2haxe.Converter [xmi-file] [output-dir]");
    }
}

package sharpen.haxe.test;

public class TestSubClass2 extends Exception
{
    public TestSubClass2()
    {
        super("Test");
    }

    public TestSubClass2(String arg0, Throwable arg1, boolean arg2, boolean arg3)
    {
        super(arg0, arg1, arg2, arg3);
    }

    public TestSubClass2(String arg0, Throwable arg1)
    {
        super(arg0, arg1);
    }

    public TestSubClass2(String arg0)
    {
        super(arg0);
    }

    public TestSubClass2(Throwable arg0)
    {
        super(arg0);
    }
}

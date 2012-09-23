package sharpen.haxe.test;

public class TestSubClass extends TestClass
{
    public TestSubClass()
    {
        super(3);
    }

    public TestSubClass(String s)
    {
        super(Integer.parseInt(s));
    }

    public TestSubClass(int i)
    {
        this(Integer.toString(i));
    }
}

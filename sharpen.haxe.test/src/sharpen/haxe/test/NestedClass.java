package sharpen.haxe.test;

public class NestedClass
{
    private int a;

    public NestedClass()
    {
        new NonStatic().test();
        new Static().test();
    }

    private class NonStatic
    {
        private void test()
        {
            System.out.println(a);
        }
    }

    private static class Static
    {
        private void test()
        {
            System.out.println("Test");
        }
    }
}

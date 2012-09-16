package sharpen.haxe.test;

public class AnonymousTest
{
    public void test01()
    {
        new TestInterface()
        {
            @Override
            public int test2(String param)
            {
                return 0;
            }

            @Override
            public void test1()
            {
            }
        };
    }

    private int _a;

    public void test02()
    {
        new TestInterface()
        {
            @Override
            public int test2(String param)
            {
                return _a;
            }

            @Override
            public void test1()
            {
            }
        };
    }

    public void test03()
    {
        final int a = 7;
        new TestInterface()
        {
            @Override
            public int test2(String param)
            {
                return a;
            }

            @Override
            public void test1()
            {
            }
        };
    }
}

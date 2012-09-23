package sharpen.haxe.test;

import java.util.ArrayList;

import sharpen.haxe.test.NestedInterface.NestedInterface2;

public class TestUsage
{
    public TestUsage()
    {
        TestInterface a;
        TestSubInterface b;
        NestedInterface c;
        NestedInterface2 d;

        // creation of native class
        new ArrayList<String>();
        new ArrayList<String>(5);

        // creation of own classes
        new TestClass();
        new TestClass(3);
        new TestClass("Test");
    }
}

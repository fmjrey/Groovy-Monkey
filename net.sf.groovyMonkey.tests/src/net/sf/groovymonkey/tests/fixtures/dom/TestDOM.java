package net.sf.groovymonkey.tests.fixtures.dom;

public class TestDOM
{
    public static volatile String string = "";
    public TestDOM() {}
    
    public void callDOM( final String string )
    {
        TestDOM.string = string;
    }
}

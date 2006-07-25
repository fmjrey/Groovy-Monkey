package net.sf.groovymonkey.tests.fixtures.dom;

public class TestDOM
{
    private static volatile String string = "";
    public TestDOM() {}
    
    public void callDOM( final String string )
    {
        synchronized( TestDOM.class )
        {
            TestDOM.string = string;
        }
    }
    public synchronized static String string()
    {
        return string;
    }
    public String getString()
    {
        return string();
    }
}

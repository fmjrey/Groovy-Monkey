package net.sf.groovymonkey.tests.fixtures.dom;

public class AnotherDOM
{
    private static volatile String string = "";
    public AnotherDOM() {}
    
    public void callDOM( final String string )
    {
        synchronized( AnotherDOM.class )
        {
            AnotherDOM.string = string;
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

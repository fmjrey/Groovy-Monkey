package net.sf.groovymonkey.tests;

public class TestInclude
{
    public TestInclude() {}
    
    public void callDOM( final String string )
    throws Exception
    {
        final Class clase = Class.forName( "net.sf.groovymonkey.tests.fixtures.dom.TestDOM" );
        final Object instance = clase.newInstance();
        clase.getMethod( "callDOM", new Class[] { String.class } ).invoke( instance, new Object[] { string } );
    }
}

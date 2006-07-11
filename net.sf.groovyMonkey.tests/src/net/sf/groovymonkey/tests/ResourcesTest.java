package net.sf.groovymonkey.tests;
import java.util.List;
import junit.textui.TestRunner;
import net.sf.groovyMonkey.dom.resources.File;
import net.sf.groovyMonkey.dom.resources.Line;
import net.sf.groovyMonkey.dom.resources.Resources;
import net.sf.groovyMonkey.dom.resources.ResourcesDOMFactory;

public class ResourcesTest 
extends TestCaseAbstract
{
    public static void main( final String[] args )
    {
        TestRunner.run( ResourcesTest.class );
    }
    @Override
    public void setUp() 
    throws Exception
    {
        super.setUp();
        setUpJavaProject();
        final String content = "Hi my name is joe.. and I work at the butt factory \nthe other day.. my boss came up to me and said.. hey joe.. hey joe... are you busy? heck no...\n";
        javaProject.makeFile( "/blahs", "blahblah.txt", content );
    }
    @Override
    public void tearDown() 
    throws Exception
    {
        super.tearDown();
    }
    public void testFilesMatching() 
    throws Exception
    {
        final Resources resources = ( Resources )new ResourcesDOMFactory().getDOMroot();
        final Object[] result = resources.filesMatching( ".*\\.java" );
        assertEquals( 1, result.length );
        assertTrue( result[ 0 ] instanceof File );
        assertEquals( "TestInclude.java", ( ( File )result[ 0 ] ).getEclipseObject().getName() );
    }
    public void testGetLines() 
    throws Exception
    {
        final Resources resources = ( Resources )new ResourcesDOMFactory().getDOMroot();
        final Object[] result = resources.filesMatching( ".*\\.txt" );
        final File file = ( File )result[ 0 ];
        final List< Line > lines = file.getLines();
        assertEquals( 2, lines.size() );
        assertEquals( "the other day.. my boss came up to me and said.. hey joe.. hey joe... are you busy? heck no...", lines.get( 1 ).getString() );
    }
}

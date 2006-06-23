package net.sf.groovymonkey.tests;
import static net.sf.groovymonkey.tests.Activator.bundle;
import static org.apache.commons.lang.StringUtils.removeStart;
import java.io.File;
import junit.framework.TestCase;
import net.sf.groovymonkey.tests.fixtures.projects.TestJavaProject;
import net.sf.groovymonkey.tests.fixtures.projects.TestMonkeyProject;

public class TestCaseAbstract
extends TestCase
{   
    public static final String MONKEY_TEST_SCRIPTS = "/monkeyTestScripts/";
    protected TestMonkeyProject monkeyProject;
    protected TestJavaProject javaProject;
    protected String bundleLocation;
    
    public TestCaseAbstract()
    {
        super();
    }
    public TestCaseAbstract( final String name )
    {
        super( name );
    }
    @Override
    protected void setUp() 
    throws Exception
    {
         super.setUp();
         monkeyProject = new TestMonkeyProject( "TestMonkeyProject" );
         javaProject = new TestJavaProject( "TestJavaProject" );
         bundleLocation = new File( ".", removeStart( bundle().getLocation(), "update@" ) ).getCanonicalPath();
    }
    @Override
    protected void tearDown() 
    throws Exception
    {
         super.tearDown();
         monkeyProject.dispose();
         javaProject.dispose();
    }
}

package net.sf.groovymonkey.tests;
import static net.sf.groovymonkey.tests.Activator.bundle;
import static org.apache.commons.io.FileUtils.copyDirectory;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.lang.StringUtils.removeStart;
import static org.eclipse.core.resources.IncrementalProjectBuilder.FULL_BUILD;
import java.io.File;
import java.io.InputStream;
import junit.framework.TestCase;
import net.sf.groovymonkey.tests.fixtures.dom.AnotherDOM;
import net.sf.groovymonkey.tests.fixtures.dom.TestDOM;
import net.sf.groovymonkey.tests.fixtures.projects.TestJavaProject;
import net.sf.groovymonkey.tests.fixtures.projects.TestMonkeyProject;
import org.apache.commons.io.IOUtils;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import utmj.threaded.RetriedAssert;

public class TestCaseAbstract
extends TestCase
{   
    public static final String MONKEY_TEST_SCRIPTS = "/monkeyTestScripts/";
    protected TestMonkeyProject monkeyProject;
    protected TestJavaProject javaProject;
    protected String bundleLocation;
    protected InputStream javaFileInput = null;
    
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
         bundleLocation = new File( removeStart( bundle().getLocation(), "update@" ) ).getCanonicalPath();
    }
    protected void setUpJavaProject() 
    throws Exception
    {
        javaFileInput = bundle().getResource( MONKEY_TEST_SCRIPTS + getClass().getSimpleName() + "/" + "TestInclude.java" ).openStream();
        final String outputLocation = bundleLocation + File.separator + "bin";
        final IFolder destination = javaProject.createFolder( "externalLib" );
        copyDirectory( new File( outputLocation ), destination.getLocation().toFile(), true );
        javaProject.addWorkspacePathToClasspath( javaProject.project().getName() + "/externalLib" );
        javaProject.project().refreshLocal( IResource.DEPTH_INFINITE, null );
        javaProject.createJavaType( "net.sf.groovymonkey.tests", "TestInclude.java", IOUtils.toString( javaFileInput, "ISO-8859-1" ) );
        javaProject.project().build( FULL_BUILD, null );
    }
    @Override
    protected void tearDown() 
    throws Exception
    {
         super.tearDown();
         closeQuietly( javaFileInput );
         monkeyProject.dispose();
         javaProject.dispose();
    }
    protected void assertTestDOMEquals() 
    throws Exception
    {
        new RetriedAssert( 60000, 500 )
        {
            @Override
            public void run() throws Exception
            {
                assertEquals( "expected: '" + getName() + "', actual: '" + TestDOM.string() + "'", getName(), TestDOM.string() );
            }
        }.start();
    }
    protected void assertAnotherDOMEquals() 
    throws Exception
    {
        new RetriedAssert( 60000, 500 )
        {
            @Override
            public void run() throws Exception
            {
                assertEquals( getName(), AnotherDOM.string() );
            }
        }.start();
    }
}

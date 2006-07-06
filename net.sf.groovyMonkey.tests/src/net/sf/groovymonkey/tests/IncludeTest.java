package net.sf.groovymonkey.tests;
import static net.sf.groovymonkey.tests.Activator.bundle;
import static net.sf.groovymonkey.tests.fixtures.projects.TestMonkeyProject.MONKEY_EXT;
import static net.sf.groovymonkey.tests.fixtures.projects.TestMonkeyProject.runMonkeyScript;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.io.IOUtils.toString;
import java.io.File;
import java.io.InputStream;
import net.sf.groovymonkey.tests.fixtures.dom.TestDOM;
import org.apache.commons.io.FileUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;

public class IncludeTest 
extends TestCaseAbstract
{
    private InputStream javaFileInput = null;
    private InputStream scriptFileInput = null;
    private IFile script = null;
    
    public IncludeTest( final String name )
    {
        super( name );
    }
    @Override
    protected void setUp() 
    throws Exception
    {
        super.setUp();
        setUpJavaProject();
        scriptFileInput = bundle().getResource( MONKEY_TEST_SCRIPTS + IncludeTest.class.getSimpleName() + "/" + getName() + MONKEY_EXT ).openStream();
        script = monkeyProject.makeMonkeyScript( getName(), scriptFileInput );
        new TestDOM().callDOM( "" );
    }
    private void setUpJavaProject() 
    throws Exception
    {
        javaFileInput = bundle().getResource( MONKEY_TEST_SCRIPTS + IncludeTest.class.getSimpleName() + "/" + "TestInclude.java" ).openStream();
        final String outputLocation = bundleLocation + File.separator + "bin";
        final IFolder destination = javaProject.createFolder( "externalLib" );
        FileUtils.copyDirectory( new File( outputLocation ), destination.getLocation().toFile(), true );
        javaProject.addWorkspacePathToClasspath( javaProject.project().getName() + "/externalLib" );
        javaProject.project().refreshLocal( IProject.DEPTH_INFINITE, null );
        javaProject.createJavaType( "net.sf.groovymonkey.tests", "TestInclude.java", toString( javaFileInput, "ISO-8859-1" ) );
        javaProject.project().build( IncrementalProjectBuilder.FULL_BUILD, null );
    }
    @Override
    protected void tearDown() 
    throws Exception
    {
        super.tearDown();
        closeQuietly( javaFileInput );
        closeQuietly( scriptFileInput );
    }
    public void testIncludeInBeanshell()
    throws Exception
    {
        runMonkeyScript( script );
        assertEquals( getName(), TestDOM.string() );
    }
    public void testIncludeInDefault()
    throws Exception
    {
        runMonkeyScript( script );
        assertEquals( getName(), TestDOM.string() );
    }
    public void testIncludeInGroovy()
    throws Exception
    {
        runMonkeyScript( script );
        assertEquals( getName(), TestDOM.string() );
    }
    public void testIncludeInPython()
    throws Exception
    {
        runMonkeyScript( script );
        assertEquals( getName(), TestDOM.string() );
    }
    public void testIncludeInRuby()
    throws Exception
    {
        runMonkeyScript( script );
        assertEquals( getName(), TestDOM.string() );
    }
    public void testIncludeInTcl()
    throws Exception
    {
        runMonkeyScript( script );
        assertEquals( getName(), TestDOM.string() );
    }
}

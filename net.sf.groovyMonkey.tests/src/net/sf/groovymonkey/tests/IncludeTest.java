package net.sf.groovymonkey.tests;
import static net.sf.groovymonkey.tests.Activator.bundle;
import static net.sf.groovymonkey.tests.fixtures.projects.TestMonkeyProject.MONKEY_EXT;
import static net.sf.groovymonkey.tests.fixtures.projects.TestMonkeyProject.runMonkeyScript;
import static org.apache.commons.io.IOUtils.closeQuietly;
import java.io.InputStream;
import net.sf.groovymonkey.tests.fixtures.dom.TestDOM;
import org.eclipse.core.resources.IFile;

public class IncludeTest 
extends TestCaseAbstract
{
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
    @Override
    protected void tearDown() 
    throws Exception
    {
        super.tearDown();
        closeQuietly( scriptFileInput );
    }
    public void testIncludeInBeanshell()
    throws Exception
    {
        runMonkeyScript( script );
        assertTestDOMEquals();
    }
    public void testIncludeInDefault()
    throws Exception
    {
        runMonkeyScript( script );
        assertTestDOMEquals();
    }
    public void testIncludeInGroovy()
    throws Exception
    {
        runMonkeyScript( script );
        assertTestDOMEquals();
    }
//    public void testIncludeInPython()
//    throws Exception
//    {
//        runMonkeyScript( script );
//        assertEquals( getName(), TestDOM.string() );
//    }
    public void testIncludeInRuby()
    throws Exception
    {
        runMonkeyScript( script );
        assertTestDOMEquals();
    }
//    public void testIncludeInTcl()
//    throws Exception
//    {
//        runMonkeyScript( script );
//        assertEquals( getName(), TestDOM.string() );
//    }
}

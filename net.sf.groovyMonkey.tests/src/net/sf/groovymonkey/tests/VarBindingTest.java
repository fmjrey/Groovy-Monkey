package net.sf.groovymonkey.tests;
import static net.sf.groovymonkey.tests.Activator.bundle;
import static net.sf.groovymonkey.tests.fixtures.projects.TestMonkeyProject.MONKEY_EXT;
import static net.sf.groovymonkey.tests.fixtures.projects.TestMonkeyProject.runMonkeyScript;
import static org.apache.commons.io.IOUtils.closeQuietly;
import java.io.InputStream;
import net.sf.groovymonkey.tests.fixtures.dom.TestDOM;
import org.eclipse.core.resources.IFile;

public class VarBindingTest 
extends TestCaseAbstract
{
    private InputStream input = null;
    private IFile script = null;
    
    public VarBindingTest( final String name )
    {
        super( name );
    }
    @Override
    protected void setUp() 
    throws Exception
    {
        super.setUp();
        new TestDOM().callDOM( "" );
        input = bundle().getResource( MONKEY_TEST_SCRIPTS + VarBindingTest.class.getSimpleName() + "/" + getName() + MONKEY_EXT ).openStream();
        script = monkeyProject.makeMonkeyScript( getName(), input );
    }
    @Override
    protected void tearDown() 
    throws Exception
    {
        super.tearDown();
        closeQuietly( input );
    }
    public void testBeanshellRuntime()
    throws Exception
    {
        runMonkeyScript( script );
        assertTestDOMEquals();
        assertAnotherDOMEquals();
    }
    public void testDefaultRuntime()
    throws Exception
    {
        runMonkeyScript( script );
        assertTestDOMEquals();
        assertAnotherDOMEquals();
    }
    public void testGroovyRuntime()
    throws Exception
    {
        runMonkeyScript( script );
        assertTestDOMEquals();
        assertAnotherDOMEquals();
    }
    public void testPythonRuntime()
    throws Exception
    {
        runMonkeyScript( script );
        assertTestDOMEquals();
    }
    public void testRubyRuntime()
    throws Exception
    {
        runMonkeyScript( script );
        assertTestDOMEquals();
        assertAnotherDOMEquals();
    }
}

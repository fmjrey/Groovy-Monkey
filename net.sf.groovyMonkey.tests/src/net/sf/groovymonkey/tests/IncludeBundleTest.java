package net.sf.groovymonkey.tests;
import static net.sf.groovymonkey.tests.Activator.bundle;
import static net.sf.groovymonkey.tests.fixtures.projects.TestMonkeyProject.MONKEY_EXT;
import static net.sf.groovymonkey.tests.fixtures.projects.TestMonkeyProject.runMonkeyScript;
import static org.apache.commons.io.IOUtils.closeQuietly;
import java.io.InputStream;
import net.sf.groovymonkey.tests.fixtures.dom.TestDOM;
import org.eclipse.core.resources.IFile;

/**
 * These unit tests don't pass yet.... haven't figured out how to get BundleClassLoaderAdapter to work.
 */
public class IncludeBundleTest 
extends TestCaseAbstract
{
    private InputStream scriptFileInput = null;
    private IFile script = null;
    
    public IncludeBundleTest( final String name )
    {
        super( name );
    }
    @Override
    protected void setUp() 
    throws Exception
    {
        super.setUp();
        scriptFileInput = bundle().getResource( MONKEY_TEST_SCRIPTS + IncludeBundleTest.class.getSimpleName() + "/" + getName() + MONKEY_EXT ).openStream();
        script = monkeyProject.makeMonkeyScript( getName(), scriptFileInput );
        TestDOM.string = "";
    }
    @Override
    protected void tearDown() 
    throws Exception
    {
        super.tearDown();
        closeQuietly( scriptFileInput );
    }
    public void testIncludeInBundleBeanshell()
    throws Exception
    {
        runMonkeyScript( script );
        assertEquals( getName(), TestDOM.string );
    }
    public void testIncludeInBundleDefault()
    throws Exception
    {
        runMonkeyScript( script );
        assertEquals( getName(), TestDOM.string );
    }
    public void testIncludeInBundleGroovy()
    throws Exception
    {
        runMonkeyScript( script );
        assertEquals( getName(), TestDOM.string );
    }
    public void testIncludeInBundleJavascript()
    throws Exception
    {
        runMonkeyScript( script );
        assertEquals( getName(), TestDOM.string );
    }
    public void testIncludeInBundleRuby()
    throws Exception
    {
        runMonkeyScript( script );
        assertEquals( getName(), TestDOM.string );
    }
    public void testIncludeInBundleTcl()
    throws Exception
    {
        runMonkeyScript( script );
        assertEquals( getName(), TestDOM.string );
    }
}

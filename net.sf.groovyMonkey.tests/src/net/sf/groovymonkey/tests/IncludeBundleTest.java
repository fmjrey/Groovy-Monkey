package net.sf.groovymonkey.tests;
import static net.sf.groovymonkey.tests.Activator.bundle;
import static net.sf.groovymonkey.tests.fixtures.projects.TestMonkeyProject.MONKEY_EXT;
import static net.sf.groovymonkey.tests.fixtures.projects.TestMonkeyProject.runMonkeyScript;
import static org.apache.commons.io.IOUtils.closeQuietly;
import java.io.InputStream;
import net.sf.groovymonkey.tests.fixtures.dom.TestDOM;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.jobs.ILock;
import org.eclipse.core.runtime.jobs.Job;

/**
 * These unit tests don't pass yet.... haven't figured out how to get BundleClassLoaderAdapter to work.
 */
public class IncludeBundleTest 
extends TestCaseAbstract
{
    private static final ILock lock = Job.getJobManager().newLock();
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
        lock.acquire();
        scriptFileInput = bundle().getResource( MONKEY_TEST_SCRIPTS + IncludeBundleTest.class.getSimpleName() + "/" + getName() + MONKEY_EXT ).openStream();
        script = monkeyProject.makeMonkeyScript( getName(), scriptFileInput );
        new TestDOM().callDOM( "" );
    }
    @Override
    protected void tearDown() 
    throws Exception
    {
        try
        {
            super.tearDown();
            closeQuietly( scriptFileInput );
        }
        finally
        {
            lock.release();
        }
    }
    public void testIncludeInBundleBeanshell()
    throws Exception
    {
        assertEquals( getName(), runMonkeyScript( script ) );
        assertTestDOMEquals();
    }
    public void testIncludeInBundleDefault()
    throws Exception
    {
        assertEquals( getName(), runMonkeyScript( script ) );
        assertTestDOMEquals();
    }
    public void testIncludeInBundleGroovy()
    throws Exception
    {
        assertEquals( getName(), runMonkeyScript( script ) );
        assertTestDOMEquals();
    }
    public void testIncludeInBundlePython()
    throws Exception
    {
        assertEquals( getName(), runMonkeyScript( script ) );
        assertTestDOMEquals();
    }
    public void testIncludeInBundleRuby()
    throws Exception
    {
        assertEquals( getName(), runMonkeyScript( script ) );
        assertTestDOMEquals();
    }
}

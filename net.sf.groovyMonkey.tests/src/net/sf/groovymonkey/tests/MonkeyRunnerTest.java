package net.sf.groovymonkey.tests;
import static net.sf.groovyMonkey.GroovyMonkeyPlugin.FILE_EXTENSION;
import static net.sf.groovymonkey.tests.Activator.bundle;
import static net.sf.groovymonkey.tests.fixtures.projects.TestMonkeyProject.MONKEY_EXT;
import static net.sf.groovymonkey.tests.fixtures.projects.TestMonkeyProject.runMonkeyScript;
import static org.apache.commons.io.IOUtils.closeQuietly;
import java.io.InputStream;
import net.sf.groovymonkey.tests.fixtures.dom.TestDOM;
import org.eclipse.core.resources.IFile;

public class MonkeyRunnerTest 
extends TestCaseAbstract
{
    private InputStream input = null;
    private InputStream included = null;
    private IFile script = null;
    
    public MonkeyRunnerTest( final String name )
    {
        super( name );
    }
    @Override
    protected void setUp() 
    throws Exception
    {
        super.setUp();
        new TestDOM().callDOM( "" );
        input = bundle().getResource( MONKEY_TEST_SCRIPTS + MonkeyRunnerTest.class.getSimpleName() + "/" + getName() + MONKEY_EXT ).openStream();
        included = bundle().getResource( MONKEY_TEST_SCRIPTS + MonkeyRunnerTest.class.getSimpleName() + "/monkeyRunner" + MONKEY_EXT ).openStream();
        script = monkeyProject.makeMonkeyScript( getName(), input );
        monkeyProject.makeFile( "includedScripts", "monkeyRunner" + FILE_EXTENSION, included );
    }
    @Override
    protected void tearDown() 
    throws Exception
    {
        super.tearDown();
        closeQuietly( input );
        closeQuietly( included );
    }
    /**
     * This unit tests demonstrates that we can use the MonkeyRunner DOM to run
     * another script providing it arguments via the map attribute and that we can 
     * check status coming back as well using the passed map attribute returnedValue.
     * @throws Exception
     */
    public void testMonkeyRunner()
    throws Exception
    {
        runMonkeyScript( script );
        assertEquals( getName(), TestDOM.string() );
    }
    public void testMonkeyRunnerJob()
    throws Exception
    {
        runMonkeyScript( script );
        assertEquals( getName(), TestDOM.string() );
    }
}

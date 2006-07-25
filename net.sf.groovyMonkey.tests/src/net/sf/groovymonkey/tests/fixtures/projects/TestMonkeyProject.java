package net.sf.groovymonkey.tests.fixtures.projects;
import static net.sf.groovyMonkey.GroovyMonkeyPlugin.FILE_EXTENSION;
import static net.sf.groovyMonkey.GroovyMonkeyPlugin.MONKEY_DIR;
import static net.sf.groovyMonkey.dom.Utilities.activeWindow;
import static org.apache.commons.io.IOUtils.toInputStream;
import java.io.InputStream;
import java.util.Map;
import net.sf.groovyMonkey.RunMonkeyScript;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

public class TestMonkeyProject 
extends TestProject
{
    public static String MONKEY_PATH = MONKEY_DIR + "/";
    public static String MONKEY_EXT = FILE_EXTENSION;

    public static Object runMonkeyScript( final IFile script )
    {
        final RunMonkeyScript monkeyScript = new RunMonkeyScript( script, activeWindow(), true );
        return monkeyScript.runScript( null );
    }
    public static Object runMonkeyScript( final IFile script,
                                          final Map< String, Object > map )
    {
        final RunMonkeyScript monkeyScript = new RunMonkeyScript( script, activeWindow(), map, true );
        return monkeyScript.runScript( null );
    }
    public TestMonkeyProject( final String name ) 
    throws CoreException
    {
        super( name );
        createFolder( MONKEY_PATH );
    }
    public void runMonkeyScript( final String projectPath )
    {
        runMonkeyScript( MONKEY_PATH + project().getFile( projectPath ) );
    }
    public IFile makeMonkeyScript( final String scriptName, 
                                   final InputStream input ) 
    throws CoreException
    {
        return makeFile( MONKEY_PATH, scriptName.endsWith( MONKEY_EXT ) ? scriptName : scriptName + MONKEY_EXT, input );
    }
    public IFile makeMonkeyScript( final String scriptName, 
                                   final String contents ) 
    throws CoreException
    {
        return makeMonkeyScript( scriptName, toInputStream( contents ) );
    }
    }

package net.sf.groovymonkey.tests.fixtures.projects;
import static org.apache.commons.io.IOUtils.toInputStream;
import static org.eclipse.ui.PlatformUI.getWorkbench;
import java.io.InputStream;
import net.sf.groovyMonkey.RunMonkeyScript;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IWorkbenchWindow;

public class TestMonkeyProject 
extends TestProject
{
    public static String MONKEY_PATH = "monkey/";
    public static String MONKEY_EXT = ".em";
    public static IWorkbenchWindow workbenchWindow()
    {
        return getWorkbench().getActiveWorkbenchWindow();
    }
    public static void runMonkeyScript( final IFile script )
    {
        final RunMonkeyScript monkeyScript = new RunMonkeyScript( script, workbenchWindow(), true );
        monkeyScript.runScript( null );
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

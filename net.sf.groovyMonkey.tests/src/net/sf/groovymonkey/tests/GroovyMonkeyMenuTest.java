package net.sf.groovymonkey.tests;
import static net.sf.groovyMonkey.GroovyMonkeyPlugin.MENU_PATH;
import static net.sf.groovyMonkey.GroovyMonkeyPlugin.getDefault;
import static net.sf.groovyMonkey.preferences.PreferenceInitializer.MONKEY_MENU_NAME;
import static org.eclipse.ui.PlatformUI.getWorkbench;
import junit.textui.TestRunner;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.WorkbenchWindow;

public class GroovyMonkeyMenuTest 
extends TestCaseAbstract
{
    private MenuManager manager = null;
    
    public static void main( final String[] args )
    {
        TestRunner.run( GroovyMonkeyMenuTest.class );
    }
    @Override
    public void setUp() 
    throws Exception
    {
        super.setUp();
        final IWorkbenchWindow window = getWorkbench().getActiveWorkbenchWindow();
        manager = (( WorkbenchWindow )window).getMenuManager();
    }
    @Override
    public void tearDown() 
    throws Exception
    {
        super.tearDown();
    }
    public void testDefaultMenu() 
    throws Exception
    {
        final MenuManager menu = ( MenuManager )manager.findMenuUsingPath( MENU_PATH );
        assertNotNull( menu );
        assertEquals( getDefault().getPreferenceStore().getDefaultString( MONKEY_MENU_NAME ), menu.getMenuText() );
    }
}

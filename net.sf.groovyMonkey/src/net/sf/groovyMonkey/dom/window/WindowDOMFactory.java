package net.sf.groovyMonkey.dom.window;
import net.sf.groovyMonkey.dom.IMonkeyDOMFactory;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

public class WindowDOMFactory 
implements IMonkeyDOMFactory
{
    public WindowDOMFactory()
    {
    }
    public Object getDOMroot()
    {
        if( Display.getCurrent() == null )
            throw new RuntimeException( "Error not in UI Thread." );
        return PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    }
}

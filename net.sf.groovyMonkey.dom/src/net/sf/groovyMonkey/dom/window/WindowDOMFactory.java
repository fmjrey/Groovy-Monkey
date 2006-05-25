package net.sf.groovyMonkey.dom.window;
import org.eclipse.eclipsemonkey.dom.IMonkeyDOMFactory;
import org.eclipse.ui.PlatformUI;

public class WindowDOMFactory 
implements IMonkeyDOMFactory
{
    public WindowDOMFactory()
    {
    }
    public Object getDOMroot()
    {
        return PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    }
}

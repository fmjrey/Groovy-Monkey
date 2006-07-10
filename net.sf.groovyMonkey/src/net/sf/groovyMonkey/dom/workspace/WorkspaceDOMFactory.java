package net.sf.groovyMonkey.dom.workspace;
import static org.eclipse.core.resources.ResourcesPlugin.getWorkspace;
import net.sf.groovyMonkey.dom.IMonkeyDOMFactory;

public class WorkspaceDOMFactory 
implements IMonkeyDOMFactory
{
    public Object getDOMroot()
    {
        return getWorkspace();
    }
}

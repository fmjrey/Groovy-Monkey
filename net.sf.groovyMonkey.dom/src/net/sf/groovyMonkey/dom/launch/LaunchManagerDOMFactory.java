package net.sf.groovyMonkey.dom.launch;
import org.eclipse.eclipsemonkey.dom.IMonkeyDOMFactory;

public class LaunchManagerDOMFactory 
implements IMonkeyDOMFactory
{
    public LaunchManagerDOMFactory()
    {
    }
    public Object getDOMroot()
    {
        return new LaunchManagerDOM();
    }

}

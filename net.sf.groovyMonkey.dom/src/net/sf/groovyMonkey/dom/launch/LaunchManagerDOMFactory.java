package net.sf.groovyMonkey.dom.launch;
import net.sf.groovyMonkey.dom.IMonkeyDOMFactory;

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

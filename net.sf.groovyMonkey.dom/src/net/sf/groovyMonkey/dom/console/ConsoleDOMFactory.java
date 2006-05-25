package net.sf.groovyMonkey.dom.console;
import org.eclipse.eclipsemonkey.dom.IMonkeyDOMFactory;

public class ConsoleDOMFactory 
implements IMonkeyDOMFactory
{
    public Object getDOMroot()
    {
        return new ConsoleDOM();
    }
}

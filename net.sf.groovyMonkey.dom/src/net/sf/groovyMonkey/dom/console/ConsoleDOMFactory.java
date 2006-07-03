package net.sf.groovyMonkey.dom.console;
import net.sf.groovyMonkey.dom.IMonkeyDOMFactory;

public class ConsoleDOMFactory 
implements IMonkeyDOMFactory
{
    public Object getDOMroot()
    {
        return new ConsoleDOM();
    }
}

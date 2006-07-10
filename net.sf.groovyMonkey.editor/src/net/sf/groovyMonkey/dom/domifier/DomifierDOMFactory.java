package net.sf.groovyMonkey.dom.domifier;
import net.sf.groovyMonkey.dom.IMonkeyDOMFactory;

public class DomifierDOMFactory 
implements IMonkeyDOMFactory
{
    public Object getDOMroot()
    {
        return new Domifier();
    }
}

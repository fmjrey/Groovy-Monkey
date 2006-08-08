package net.sf.groovymonkey.dom.cvsdom;
import net.sf.groovyMonkey.dom.IMonkeyDOMFactory;

public class DOMFactory 
implements IMonkeyDOMFactory
{
    public DOMFactory() {}
    public Object getDOMroot()
    {
        return new CVSDOM();
    }
}

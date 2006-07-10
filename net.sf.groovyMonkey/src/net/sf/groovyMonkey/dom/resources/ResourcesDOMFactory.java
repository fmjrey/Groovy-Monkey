package net.sf.groovyMonkey.dom.resources;
import net.sf.groovyMonkey.dom.IMonkeyDOMFactory;

public class ResourcesDOMFactory 
implements IMonkeyDOMFactory
{
    public Object getDOMroot()
    {
        return new Resources();
    }
}

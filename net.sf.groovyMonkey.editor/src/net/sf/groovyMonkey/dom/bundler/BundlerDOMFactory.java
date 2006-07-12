package net.sf.groovyMonkey.dom.bundler;
import net.sf.groovyMonkey.dom.IMonkeyDOMFactory;

public class BundlerDOMFactory 
implements IMonkeyDOMFactory
{
    public Object getDOMroot()
    {
        return new Bundler();
    }
}

package net.sf.groovymonkey.tests.fixtures.dom;
import net.sf.groovyMonkey.dom.IMonkeyDOMFactory;

public class AnotherDOMFactory 
implements IMonkeyDOMFactory
{
    public AnotherDOMFactory() {}

    public Object getDOMroot()
    {
        return new AnotherDOM();
    }
}

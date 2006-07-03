package net.sf.groovymonkey.tests.fixtures.dom;
import net.sf.groovyMonkey.dom.IMonkeyDOMFactory;

public class TestDOMFactory 
implements IMonkeyDOMFactory
{
    public TestDOMFactory() {}

    public Object getDOMroot()
    {
        return new TestDOM();
    }
}

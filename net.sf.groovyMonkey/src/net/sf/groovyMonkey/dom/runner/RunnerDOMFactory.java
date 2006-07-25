package net.sf.groovyMonkey.dom.runner;
import net.sf.groovyMonkey.dom.IMonkeyDOMFactory;

public class RunnerDOMFactory 
implements IMonkeyDOMFactory
{
    public RunnerDOMFactory() {}

    public Object getDOMroot()
    {
        return new RunnerDOM();
    }
}

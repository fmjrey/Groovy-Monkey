package net.sf.groovyMonkey.dom.runner;
import net.sf.groovyMonkey.dom.IMonkeyDOMFactory;

public class RunnerDOM 
implements IMonkeyDOMFactory
{
    public RunnerDOM()
    {
    }
    public Object getDOMroot()
    {
        return new MonkeyRunner();
    }
}

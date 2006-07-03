package net.sf.groovyMonkey.dom;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

/**
 * This dom is just a placeholder to add the monitor binding to the outline view.
 * It does not actually provide the object that is put in the binding.  The Eclipse 
 * Job in RunMonkeyScript.run() actually does that.
 */
public class MonitorDOM 
implements IMonkeyDOMFactory
{
    public MonitorDOM() {}

    public Object getDOMroot()
    {
        return new SubProgressMonitor( new NullProgressMonitor(), 1 );
    }
}

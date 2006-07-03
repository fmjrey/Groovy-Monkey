package net.sf.groovyMonkey.dom;
import static java.util.Collections.synchronizedMap;
import java.util.LinkedHashMap;

/**
 * This dom is just a placeholder to add the vars binding to the outline view.
 * It does not actually provide the object that is put in the binding.  The 
 * setBinding() method in MonkeyScript actually does that.
 */
public class VarsDOM 
implements IMonkeyDOMFactory
{
    public VarsDOM() {}

    public Object getDOMroot()
    {
        return synchronizedMap( new LinkedHashMap< String, Object >() );
    }
}

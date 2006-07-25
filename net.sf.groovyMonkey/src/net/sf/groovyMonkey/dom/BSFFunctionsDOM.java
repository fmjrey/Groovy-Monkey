package net.sf.groovyMonkey.dom;
import org.apache.bsf.util.BSFFunctions;

/**
 * This dom is just a placeholder to add the vars binding to the outline view.
 * It does not actually provide the object that is put in the binding.  The 
 * setBinding() method in MonkeyScript actually does that.
 */
public class BSFFunctionsDOM 
implements IMonkeyDOMFactory
{
    public BSFFunctionsDOM() {}

    public Object getDOMroot()
    {
        return new BSFFunctions( null, null );
    }
}

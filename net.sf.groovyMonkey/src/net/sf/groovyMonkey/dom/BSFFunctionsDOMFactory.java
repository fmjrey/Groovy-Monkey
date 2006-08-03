package net.sf.groovyMonkey.dom;
import org.apache.bsf.util.BSFFunctions;

/**
 * This dom is just a placeholder to add the bsf binding to the outline view.
 * It does not actually provide the object that is put in the binding.  The 
 * BSF Engine implementation actually does that.
 */
public class BSFFunctionsDOMFactory 
implements IMonkeyDOMFactory
{
    public BSFFunctionsDOMFactory() {}

    public Object getDOMroot()
    {
        return new BSFFunctions( null, null );
    }
}

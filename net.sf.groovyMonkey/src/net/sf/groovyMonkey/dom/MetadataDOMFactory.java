package net.sf.groovyMonkey.dom;
import net.sf.groovyMonkey.ScriptMetadata;

/**
 * This dom is just a placeholder to add the metadata binding to the outline view.
 * It does not actually provide the object that is put in the binding.  The 
 * run() method in MonkeyScript actually does that.
 */
public class MetadataDOMFactory 
implements IMonkeyDOMFactory
{
    public MetadataDOMFactory() {}

    public Object getDOMroot()
    {
        return new ScriptMetadata();
    }
}

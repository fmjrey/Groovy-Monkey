package groovy.jface;
import net.sf.groovyMonkey.dom.IMonkeyDOMFactory;

public class JFaceDOMFactory
implements IMonkeyDOMFactory
{
    public Object getDOMroot()
    {
        return new JFaceBuilder();
    }

}

/*
 * Created on Feb 20, 2004
 */
package groovy.jface.factory;
import static groovy.swt.SwtUtils.getParentShell;
import groovy.jface.impl.WizardDialogImpl;
import groovy.jface.impl.WizardImpl;
import groovy.swt.factory.AbstractSwtFactory;
import groovy.swt.factory.SwtFactory;
import java.util.Map;

/**
 * @author <a href="mailto:ckl@dacelo.nl">Christiaan ten Klooster </a>
 * @version $Revision$
 */
public class WizardDialogFactory 
extends AbstractSwtFactory 
implements SwtFactory
{
    /*
     * @see groovy.swt.factory.SwtFactory#newInstance(java.util.Map,
     *      java.lang.Object)
     */
    @Override
    public Object newInstance( final Map properties, 
                               final Object parent )
    {
        return new WizardDialogImpl( getParentShell( parent ), new WizardImpl() );
    }
}

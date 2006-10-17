package groovy.jface.factory;
import static groovy.swt.SwtUtils.getParentShell;
import groovy.jface.impl.PreferenceDialogImpl;
import groovy.swt.InvalidParentException;
import groovy.swt.factory.AbstractSwtFactory;
import groovy.swt.factory.SwtFactory;
import java.util.Map;
import org.codehaus.groovy.GroovyException;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.swt.widgets.Shell;

/**
 * @author <a href="mailto:ckl@dacelo.nl">Christiaan ten Klooster </a>
 * @version $Revision$
 */
public class PreferencesDialogFactory 
extends AbstractSwtFactory 
implements SwtFactory
{
    /*
     * @see groovy.swt.impl.SwtFactory#newInstance(java.util.Map,
     *      java.lang.Object)
     */
    @Override
    public Object newInstance( final Map properties, 
                               final Object parent ) 
    throws GroovyException
    {
        final Shell parentShell = getParentShell( parent );
        if( parent != null )
            return new PreferenceDialogImpl( parentShell, new PreferenceManager() );
        throw new InvalidParentException( "applicationWindow or shell" );
    }
}

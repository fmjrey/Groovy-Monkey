package groovy.swt.factory;
import groovy.jface.factory.ActionImpl;
import java.util.Map;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionManager;

/**
 * @author <a href="mailto:ckl@dacelo.nl">Christiaan ten Klooster </a>
 * @version $Revision$
 */
public class ActionFactory 
extends AbstractSwtFactory 
implements SwtFactory
{
    /*
     * @see groovy.swt.impl.SwtFactory#newInstance(java.util.Map,
     *      java.lang.Object)
     */
    @Override
    public Object newInstance( final Map< String, Object > properties, 
                               final Object parent )
    {
        final Action action = new ActionImpl();
        setBeanProperties( action, properties );
        if( parent instanceof IContributionManager )
        {
            final IContributionManager contributionManager = ( IContributionManager )parent;
            contributionManager.add( action );
        }
        return action;
    }
}

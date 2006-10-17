package groovy.swt.factory;
import static org.eclipse.swt.widgets.Display.getCurrent;
import java.util.Map;

public class TrayFactory 
extends AbstractSwtFactory 
implements SwtFactory
{
    @Override
    public Object newInstance( final Map properties, 
                               final Object parent )
    {
        return getCurrent().getSystemTray();
    }
}

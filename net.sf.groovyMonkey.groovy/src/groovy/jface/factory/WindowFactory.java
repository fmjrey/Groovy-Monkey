/*
 * Created on Feb 28, 2004
 */
package groovy.jface.factory;
import groovy.swt.InvalidParentException;
import groovy.swt.convertor.PointConverter;
import groovy.swt.factory.SwtFactory;
import groovy.swt.factory.WidgetFactory;
import java.util.List;
import java.util.Map;
import org.codehaus.groovy.GroovyException;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

/**
 * @author <a href:ckl at dacelo.nl">Christiaan ten Klooster </a>
 * @version $Revision$
 */
public class WindowFactory 
extends WidgetFactory 
implements SwtFactory
{
    /**
     * @param beanClass
     */
    public WindowFactory( final Class beanClass )
    {
        super( beanClass );
    }
    /*
     * @see groovy.swt.factory.AbstractSwtFactory#newInstance(java.util.Map,
     *      java.lang.Object)
     */
    @Override
    public Object newInstance( final Map properties, Object parent ) 
    throws GroovyException
    {
        if( parent == null )
            parent = new Shell();
        if( !( parent instanceof Shell ) )
            throw new InvalidParentException( "shell" );
        final Window window = ( Window )createWidget( parent );
        if( window != null )
        {
            // set title of Window
            final String title = ( String )properties.remove( "title" );
            if( title != null )
                window.getShell().setText( title );
            // set size of Window
            final List size = ( List )properties.remove( "size" );
            if( size != null )
            {
                final Point point = PointConverter.getInstance().parse( size );
                window.getShell().setSize( point );
            }
            // set location of Window
            final List location = ( List )properties.remove( "location" );
            if( location != null )
            {
                final Point point = PointConverter.getInstance().parse( location );
                window.getShell().setLocation( point );
            }
        }
        setBeanProperties( window, properties );
        return window;
    }
}

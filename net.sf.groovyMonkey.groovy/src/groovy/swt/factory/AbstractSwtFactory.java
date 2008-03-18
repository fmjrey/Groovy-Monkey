/*
 * Created on Feb 15, 2004
 *
 */
package groovy.swt.factory;

import groovy.swt.convertor.ColorConverter;
import groovy.swt.convertor.PointConverter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.codehaus.groovy.GroovyException;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Control;

/**
 * @author <a href="mailto:ckl@dacelo.nl">Christiaan ten Klooster </a>
 * @version $Revision$
 */
public abstract class AbstractSwtFactory 
implements SwtFactory
{

    public abstract Object newInstance(Map<String,Object> properties, Object parent) throws GroovyException;

    /**
     * set the properties
     *
     * @param bean
     * @param properties
     */
    protected void setBeanProperties(Object bean, Map<?,?> properties) {

        if (bean instanceof Control) {
            Control control = (Control) bean;

            // set size of widget
            Object size = properties.remove("size");
            if (size != null) {
                setSize(control, size);
            }

            // set background of widget
            Object colorValue = properties.remove("background");
            if (colorValue != null) {
                Color background = getColor(control, colorValue);
                control.setBackground(background);
            }

            // set foreground of widget
            colorValue = properties.remove("foreground");
            if (colorValue != null) {
                Color foreground = getColor(control, colorValue);
                control.setForeground(foreground);
            }
        }
        for( final Iterator< ? > iter = properties.entrySet().iterator(); iter.hasNext(); ) 
        {
            final Map.Entry< ?, ? > entry = ( Map.Entry< ?, ? >) iter.next();
            final String property = entry.getKey().toString();
            final Object value = entry.getValue();
            try 
            {
                InvokerHelper.setProperty( bean, property, value );

            }
            catch( final Exception e ) 
            {
                throw new RuntimeException( e );
            }
        }
    }

    protected Color getColor(Control control, Object colorValue) {
        Color color = null;
        if (colorValue != null) {
            RGB rgb = null;
            if (colorValue instanceof Color) {
                color = (Color) colorValue;
            } else if (colorValue instanceof List) {
                rgb = ColorConverter.getInstance().parse((List<?>) colorValue);
                color = new Color(control.getDisplay(), rgb);
            } else {
                rgb = ColorConverter.getInstance().parse(colorValue.toString());
                color = new Color(control.getDisplay(), rgb);
            }
        }
        return color;
    }

    protected void setSize(Control control, Object size) {
        Point point = null;
        if (size != null) {
            if (size instanceof Point) {
                point = (Point) size;
            } else if (size instanceof List) {
                point = PointConverter.getInstance().parse((List<?>) size);
            }
            control.setSize(point);
        }
    }
}
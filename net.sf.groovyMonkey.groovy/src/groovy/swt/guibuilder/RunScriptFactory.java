/*
 * Created on Mar 17, 2004
 */
package groovy.swt.guibuilder;
import static groovy.swt.SwtUtils.disposeChildren;
import static groovy.swt.SwtUtils.getParentWidget;
import static java.util.logging.Logger.getLogger;
import groovy.lang.Binding;
import groovy.lang.MissingPropertyException;
import groovy.swt.factory.AbstractSwtFactory;
import groovy.swt.factory.SwtFactory;
import groovy.util.ResourceException;
import groovy.util.ScriptException;
import java.util.Map;
import java.util.logging.Logger;
import org.codehaus.groovy.GroovyException;
import org.eclipse.swt.widgets.Composite;

/**
 * Run another script
 * 
 * @author <a href:ckl at dacelo.nl">Christiaan ten Klooster </a> $Id:
 *         RunScriptFactory.java 35 2006-07-03 16:59:58 +0000 (Mon, 03 Jul 2006)
 *         jervin $
 */
public class RunScriptFactory 
extends AbstractSwtFactory 
implements SwtFactory
{
    /** the logger */
    public static final Logger log = getLogger( RunScriptFactory.class.getName() );
    /** the builder */
    private ApplicationGuiBuilder guiBuilder;
    /**
     * @param scriptEngine
     */
    public RunScriptFactory( final ApplicationGuiBuilder guiBuilder )
    {
        this.guiBuilder = guiBuilder;
    }
    /*
     * @see groovy.swt.factory.AbstractSwtFactory#newInstance(java.util.Map,
     *      java.lang.Object)
     */
    @Override
    public Object newInstance( final Map properties, 
                               final Object parent ) 
    throws GroovyException
    {
        // get src
        final String src = ( String )properties.remove( "src" );
        if( src == null )
            throw new MissingPropertyException( "src", RunScriptFactory.class );
        // get binding
        Binding binding = ( Binding )properties.remove( "binding" );
        if( binding == null )
            binding = new Binding();
        // get parent composite
        Composite parentComposite = null;
        final Object obj = properties.remove( "parent" );
        if( obj != null )
        {
            parentComposite = ( Composite )getParentWidget( obj, properties );
            if( parentComposite == null && parent instanceof Composite )
                parentComposite = ( Composite )parent;
        }
        else
            parentComposite = ( Composite )getParentWidget( guiBuilder.getCurrent(), properties );
        guiBuilder.setCurrent( parentComposite );
        // dispose children
        final Boolean rebuild = ( Boolean )properties.remove( "rebuild" );
        if( parentComposite != null && rebuild != null && rebuild.booleanValue() )
            disposeChildren( parentComposite );
        // run script
        Object result;
        try
        {
            result = runScript( src, parentComposite, binding );
        }
        catch( final Exception e )
        {
            throw new GroovyException( e.getMessage() );
        }
        if( result == null )
            throw new NullPointerException( "Script returns null: " + src );
        return result;
    }
    /**
     * @param widget
     * @param script
     * @param parent
     * @return
     * @throws ScriptException
     * @throws ResourceException
     */
    private Object runScript( final String script, 
                              final Composite parent, 
                              final Binding binding ) 
    throws ResourceException, ScriptException
    {
        // script binding
        binding.setVariable( "guiBuilder", guiBuilder );
        if( parent != null )
            binding.setVariable( "parent", parent );
        final Object obj = guiBuilder.getScriptEngine().run( script, binding );
        // layout widget
        if( parent != null )
            parent.layout();
        else if( obj != null && obj instanceof Composite )
            ( ( Composite )obj ).layout();
        return obj;
    }
}

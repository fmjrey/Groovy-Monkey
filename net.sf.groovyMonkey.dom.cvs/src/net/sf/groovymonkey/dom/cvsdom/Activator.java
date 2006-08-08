package net.sf.groovymonkey.dom.cvsdom;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class Activator 
extends AbstractUIPlugin
{
    public static final String PLUGIN_ID = "net.sf.groovyMonkey.dom.cvs";
    private static Activator plugin;

    public Activator()
    {
        plugin = this;
    }
    @Override
    public void start( final BundleContext context ) 
    throws Exception
    {
        super.start( context );
    }
    @Override
    public void stop( final BundleContext context ) 
    throws Exception
    {
        plugin = null;
        super.stop( context );
    }
    public static Activator getDefault()
    {
        return plugin;
    }
    public static Activator plugin()
    {
        return plugin;
    }
}

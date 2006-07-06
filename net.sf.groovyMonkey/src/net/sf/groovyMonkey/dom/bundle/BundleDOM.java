package net.sf.groovyMonkey.dom.bundle;
import java.util.Set;
import net.sf.groovyMonkey.GroovyMonkeyPlugin;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.PlatformAdmin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public class BundleDOM
{
    public Bundle bundle()
    {
        return GroovyMonkeyPlugin.bundle();
    }
    public BundleContext context()
    {
        return GroovyMonkeyPlugin.context();
    }
    public PlatformAdmin platformAdmin()
    {
        return GroovyMonkeyPlugin.platformAdmin();
    }
    public BundleDescription bundleDescription()
    {
        return GroovyMonkeyPlugin.bundleDescription();
    }
    public BundleDescription bundleDescription( final long id )
    {
        return GroovyMonkeyPlugin.bundleDescription( id );
    }
    public BundleDescription bundleDescription( final String name )
    {
        return GroovyMonkeyPlugin.bundleDescription( name );
    }
    public static Set< String > getAllRequiredBundles()
    {
        return GroovyMonkeyPlugin.getAllRequiredBundles();
    }
    public static Set< String > getAllRequiredBundles( final long id )
    {
        return GroovyMonkeyPlugin.getAllRequiredBundles( id );
    }
    public static Set< String > getAllRequiredBundles( final String name )
    {
        return GroovyMonkeyPlugin.getAllRequiredBundles( name );
    }
    public static Set< String > getAllReexportedBundles( final long id )
    {
        return GroovyMonkeyPlugin.getAllReexportedBundles( id );
    }
    public static Set< String > getAllReexportedBundles( final String name )
    {
        return GroovyMonkeyPlugin.getAllReexportedBundles( name );
    }
}

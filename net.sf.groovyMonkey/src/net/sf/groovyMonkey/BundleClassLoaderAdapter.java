package net.sf.groovyMonkey;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import net.sf.groovyMonkey.util.MapUtil;
import org.apache.commons.lang.ArrayUtils;
import org.osgi.framework.Bundle;

@SuppressWarnings("unchecked")
public class BundleClassLoaderAdapter
extends ClassLoader
{
    private final Bundle[] bundles;
    private final Map< String, URL > cachedResources = MapUtil.map();
    private final Map< String, Class< ? > > cachedClasses = MapUtil.map();
    public BundleClassLoaderAdapter( final Bundle... bundles )
    {
        this.bundles = ( Bundle[] )ArrayUtils.clone( bundles );
    }
    public BundleClassLoaderAdapter( final ClassLoader parent,
                                     final Bundle... bundles )
    {
        super( parent );
        this.bundles = ( Bundle[] )ArrayUtils.clone( bundles );
    }
    @Override
    public Class< ? > loadClass( final String name )
    throws ClassNotFoundException
    {
        try
        {
            return askBundles( name );
        }
        catch( final ClassNotFoundException cnfe ) {}
        return super.loadClass( name );
    }
    private Class< ? > askBundles( final String name )
    throws ClassNotFoundException
    {
        synchronized( cachedClasses )
        {
            if( cachedClasses.containsKey( name ) )
                return cachedClasses.get( name );
            for( final Bundle bundle: bundles )
            {
                try
                {
                    final Class< ? > clase = bundle.loadClass( name );
                    if( clase == null )
                        continue;
                    cachedClasses.put( name, clase );
                    return clase;
                }
                catch( final ClassNotFoundException cnfe ) {}
            }
            throw new ClassNotFoundException( "Class " + name + " not found in bundles: " + ArrayUtils.toString( bundles ) );
        }
    }
    @Override
    protected Class< ? > findClass( final String name )
    throws ClassNotFoundException
    {
        return askBundles( name );
    }
    @Override
    protected URL findResource( final String name )
    {
        synchronized( cachedResources )
        {
            if( cachedResources.containsKey( name ) )
                return cachedResources.get( name );
            for( final Bundle bundle : bundles )
            {
                final URL resource = bundle.getResource( name );
                if( resource == null )
                    continue;
                cachedResources.put( name, resource );
                return resource;
            }
            return super.findResource( name );
        }
    }
    @Override
    protected Enumeration< URL > findResources( final String name )
    throws IOException
    {
        for( final Bundle bundle : bundles )
        {
            try
            {
                final Enumeration< URL > resource = bundle.getResources( name );
                if( resource != null )
                    return resource;
            }
            catch( final IOException ioe ) {}
        }
        return super.findResources( name );
    }
    @Override
    public String toString()
    {
        return "bundles: " + ArrayUtils.toString( bundles );
    }
}

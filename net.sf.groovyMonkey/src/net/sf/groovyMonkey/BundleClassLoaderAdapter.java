package net.sf.groovyMonkey;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import org.apache.commons.lang.ArrayUtils;
import org.osgi.framework.Bundle;

public class BundleClassLoaderAdapter 
extends ClassLoader
{
    private final Bundle[] bundles;
    public BundleClassLoaderAdapter( final Bundle... bundles )
    {
        this.bundles = bundles;
    }
    public BundleClassLoaderAdapter( final ClassLoader parent,
                                     final Bundle... bundles )
    {
        super( parent );
        this.bundles = bundles;
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
        for( final Bundle bundle: bundles )
        {
            try
            {
                return bundle.loadClass( name );
            }
            catch( final ClassNotFoundException cnfe ) {}
        }
        throw new ClassNotFoundException( "Class " + name + " not found in bundles: " + bundles );
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
        for( final Bundle bundle : bundles )
        {
            final URL resource = bundle.getResource( name );
            if( resource != null )
                return resource;
        }
        return super.findResource( name );
    }
    @SuppressWarnings("unchecked")
    @Override
    protected Enumeration< URL > findResources( final String name ) 
    throws IOException
    {
        for( final Bundle bundle : bundles )
        {
            try
            {
                final Enumeration< URL > resource = ( Enumeration< URL > )bundle.getResources( name );
                if( resource != null )
                    return resource;
            }
            catch( final IOException ioe ) {}
        }
        return super.findResources( name );
    }
    public String toString()
    {
        return "bundles: " + ArrayUtils.toString( bundles );
    }
}

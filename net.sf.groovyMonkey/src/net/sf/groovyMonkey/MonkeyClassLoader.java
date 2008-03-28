/*******************************************************************************
 * Copyright (c) 2006 Eclipse Foundation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bjorn Freeman-Benson - initial implementation
 *     Ward Cunningham - initial implementation
 *******************************************************************************/

package net.sf.groovyMonkey;
import static java.lang.reflect.AccessibleObject.setAccessible;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import net.sf.groovyMonkey.util.ListUtil;
import net.sf.groovyMonkey.util.MapUtil;

@SuppressWarnings("unchecked")
public class MonkeyClassLoader 
extends ClassLoader 
{
    private final List< ClassLoader > loaders = ListUtil.array();
    private final Map< String, Class< ? > > clases = MapUtil.hashMap();
    private final Map< String, URL > resources = MapUtil.hashMap();
    
    
    public MonkeyClassLoader( final ClassLoader parentLoader )
    {
        super( parentLoader );
    }
    public MonkeyClassLoader()
    {
        super();
    }
    public void add( final ClassLoader loader ) 
    {
        loaders.add( loader );
    }
    public List< ClassLoader > getLoaders()
    {
        return ListUtil.array( loaders );
    }
    @Override
    public Class< ? > loadClass( final String name ) 
    throws ClassNotFoundException 
    {
        if( clases.containsKey( name ) )
            return clases.get( name );
        for( final ClassLoader loader : loaders )
        {
            try
            {
                if( loader == null )
                    continue;
                final Class< ? > clase = loader.loadClass( name );
                if( clase != null )
                {
                    clases.put( name, clase );
                    return clase;
                }
            }
            catch( final ClassNotFoundException e )
            {
                // continue the loop
            }
            catch( final Exception e )
            {
                throw new RuntimeException( e );
            }
        }
        final Class< ? > clase = super.loadClass( name );
        if( clase != null )
            return clase;
        throw new ClassNotFoundException( name );
    }
    @Override
    protected synchronized Class< ? > loadClass( final String name, 
                                                 final boolean resolve ) 
    throws ClassNotFoundException
    {
        if( clases.containsKey( name ) )
            return clases.get( name );
        for( final ClassLoader loader : loaders )
        {
            try
            {
                if( loader == null )
                    continue;
                final Class< ? > clase = ( Class< ? > )invoke( loader, "loadClass", name, resolve );
                if( clase != null )
                {
                    clases.put( name, clase );
                    return clase;
                }
            }
            catch( final Throwable e )
            {
                throw new RuntimeException( e );
            }
        }
        final Class< ? > clase = super.loadClass( name, resolve );
        if( clase != null )
            return clase;
        throw new ClassNotFoundException( name );
    }
    @Override
    protected Class< ? > findClass( final String name ) 
    throws ClassNotFoundException
    {
        if( clases.containsKey( name ) )
            return clases.get( name );
        for( final ClassLoader loader : loaders )
        {
            if( loader == null )
                continue;
            Class< ? > clase = ( Class< ? > )invoke( loader, "findClass", name );
            if( clase != null )
            {
                clases.put( name, clase );
                return clase;
            }
            clase = ( Class< ? > )invoke( loader, "findLocalClass", name );
            if( clase != null )
            {
                clases.put( name, clase );
                return clase;
            }
        }
        try
        {
            return super.findClass( name );
        }
        catch( final ClassNotFoundException cnfe )
        {
            throw new ClassNotFoundException( "Could not find class: " + name + ". " + cnfe.getMessage(), cnfe );
        }
    }
    private Object invoke( final ClassLoader loader, 
                           final String methodName, 
                           final String name )
    {
        try
        {
            final Method method = loader.getClass().getDeclaredMethod( methodName, new Class[] { String.class } );
            setAccessible( new Method[] { method }, true );
            return method.invoke( loader, name );
        }
        catch( final SecurityException se )
        {
        }
        catch( final NoSuchMethodException e )
        {
        }
        catch( final IllegalArgumentException e )
        {
        }
        catch( final IllegalAccessException e )
        {
        }
        catch( final InvocationTargetException e )
        {
        }
        return null;
    }
    private Object invoke( final ClassLoader loader, 
                           final String methodName, 
                           final String name,
                           final boolean resolve )
    {
        try
        {
            final Method method = loader.getClass().getDeclaredMethod( methodName, new Class[]{ String.class, boolean.class } );
            setAccessible( new Method[]{ method }, true );
            return method.invoke( loader, name, resolve );
        }
        catch( final SecurityException se )
        {
        }
        catch( final NoSuchMethodException e )
        {
        }
        catch( final IllegalArgumentException e )
        {
        }
        catch( final IllegalAccessException e )
        {
        }
        catch( final InvocationTargetException e )
        {
        }
        return null;
    }
    @Override
    protected URL findResource( final String name )
    {
        if( resources.containsKey( name ) )
            return resources.get( name );
        for( final ClassLoader loader : loaders )
        {
            if( loader == null )
                continue;
            URL url = ( URL )invoke( loader, "findResource", name );
            if( url != null )
            {
                resources.put( name, url );
                return url;
            }
        }
        return super.findResource( name );
    }
    
    @Override
    public URL getResource( final String resName )
    {
        final URL url = findResource( resName );
        if( url != null )
            return url;
        return super.getResource( resName );
    }
    @Override
    protected Enumeration< URL > findResources( final String name ) 
    throws IOException
    {
        final Map< String, URL > resourceMap = MapUtil.linkedMap();
        for( final ClassLoader loader : loaders )
        {
            if( loader == null )
                continue;
            final Enumeration< URL > enumeration = ( Enumeration< URL > )invoke( loader, "findResources", name );
            while( enumeration.hasMoreElements() )
            {
                final URL element = enumeration.nextElement();
                if( !resourceMap.containsKey( element.getPath() ) )
                    resourceMap.put( element.getPath(), element );
            }
        }
        final Enumeration< URL > enumeration = super.findResources( name );
        while( enumeration.hasMoreElements() )
        {
            final URL element = enumeration.nextElement();
            if( !resourceMap.containsKey( element.getPath() ) )
                resourceMap.put( element.getPath(), element );
        }
        return new Vector< URL >( resourceMap.values() ).elements();
    }
}

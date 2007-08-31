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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MonkeyClassLoader 
extends ClassLoader 
{
	private final List< ClassLoader > loaders = new ArrayList< ClassLoader >();
	private final Map< String, Class<?> > clases = new HashMap< String, Class<?> >();
    
	public MonkeyClassLoader()
    {
        super();
    }
    public MonkeyClassLoader( final ClassLoader parent )
    {
        super( parent );
    }
    public void add( final ClassLoader loader ) 
    {
		loaders.add( loader );
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
                final Class<?> clase = loader.loadClass( name );
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
            catch( final Throwable e )
            {
                throw new RuntimeException( e );
            }
		}
        final Class<?> clase = super.loadClass( name );
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
                final Class<?> clase = invoke( loader, "loadClass", name, resolve );
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
        final Class<?> clase = super.loadClass( name, resolve );
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
            Class< ? > clase = invoke( loader, "findClass", name );
            if( clase != null )
            {
                clases.put( name, clase );
                return clase;
            }
            clase = invoke( loader, "findLocalClass", name );
            if( clase != null )
            {
                clases.put( name, clase );
                return clase;
            }
        }
        return super.findClass( name );
    }
    private Class< ? > invoke( final ClassLoader loader, 
                               final String methodName, 
                               final String name )
    {
        try
        {
            final Method method = loader.getClass().getDeclaredMethod( methodName, new Class<?>[] { String.class } );
            setAccessible( new Method[] { method }, true );
            final Object object = method.invoke( loader, name );
            if( object == null || !( object instanceof Class ) )
                return null;
            return ( Class<?> )object;
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
    private Class< ? > invoke( final ClassLoader loader, 
                               final String methodName, 
                               final String name,
                               final boolean resolve )
    {
        try
        {
            final Method method = loader.getClass().getDeclaredMethod( methodName, new Class<?>[]{ String.class, boolean.class } );
            setAccessible( new Method[]{ method }, true );
            final Object object = method.invoke( loader, name, resolve );
            if( object == null || !( object instanceof Class ) )
                return null;
            return ( Class<?> )object;
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
        return super.findResource( name );
    }
    @Override
    protected Enumeration< URL > findResources( final String name ) 
    throws IOException
    {
        return super.findResources( name );
    }
    
}

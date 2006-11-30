package net.sf.groovyMonkey.util;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

public class SetUtil
{
	public static < T > Set< T > treeSet( final T... objects )
    {
        final Set< T > list = new TreeSet< T >();
        add( list, objects );
        return list;
    }
    public static < T > Set< T > treeSet( final Collection< T > collection )
    {
        final Set< T > list = new TreeSet< T >();
        if( collection != null && collection.size() > 0 )
            list.addAll( collection );
        return list;
    }
	public static < T > Set< T > hashSet( final T... objects )
    {
        final Set< T > list = new HashSet< T >();
        add( list, objects );
        return list;
    }
    public static < T > Set< T > hashSet( final Collection< T > collection )
    {
        final Set< T > list = new HashSet< T >();
        if( collection != null && collection.size() > 0 )
            list.addAll( collection );
        return list;
    }
    public static < T > Set< T > linkedSet( final T... objects )
    {
        final Set< T > list = new LinkedHashSet< T >();
        add( list, objects );
        return list;
    }
    public static < T > Set< T > linkedSet( final Collection< T > collection )
    {
        final Set< T > list = new LinkedHashSet< T >();
        if( collection != null && collection.size() > 0 )
            list.addAll( collection );
        return list;
    }
    public static < T > Set< T > set( final Collection< T > collection )
    {
        final Set< T > list = new HashSet< T >();
        if( collection != null && collection.size() > 0 )
            list.addAll( collection );
        return list;
    }
    public static < T > Set< T > set( final T... objects )
    {
        return hashSet( objects );
    }
    public static < T > void add( final Set< T > set,
            					  final T... objects )
    {
    	for( final T object : objects )
    		set.add( object );
    }
    public static < T > void setAdd( final Set< T > set,
			  				         final T... objects )
    {
    	add( set, objects );
    }
}

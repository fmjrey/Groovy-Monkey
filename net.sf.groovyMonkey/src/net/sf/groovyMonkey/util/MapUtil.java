package net.sf.groovyMonkey.util;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MapUtil
{
    private static final class Entry < K, V >
    implements Map.Entry< K, V >
    {
        private final K key;
        private final V value;

        public Entry( final K key,
                      final V value )
        {
            super();
            this.key = key;
            this.value = value;
        }
        public K getKey()
        {
            return key;
        }
        public K key()
        {
            return getKey();
        }
        public V getValue()
        {
            return value;
        }
        public V value()
        {
            return getValue();
        }
        public V setValue( V value )
        {
            throw new IllegalAccessError( "value is final" );
        }
    }
    public static < K, V > List< Map.Entry< K, V > > entries( final List< K > keys,
                                                              final List< V > values )
    {
        if( keys == null || keys.size() == 0 )
            return ListUtil.list();
        final List< Map.Entry< K, V > > entries = ListUtil.list();
        final Iterator< V > valueIterator = values != null ? values.iterator() : null;
        for( final K key : keys )
        {
            final Map.Entry< K, V > entry;
            if( valueIterator == null || !valueIterator.hasNext() )
                entry = new Entry< K, V >( key, null );
            else
                entry = new Entry< K, V >( key, valueIterator.next() );
            entries.add( entry );
        }
        return entries;
    }
	public static < K, V > Map< K, V > treeMap( final Map.Entry< K, V >... objects )
    {
        final Map< K, V > list = new TreeMap< K, V >();
        add( list, objects );
        return list;
    }
	public static < K, V > Map< K, V > hashMap( final Map.Entry< K, V >... objects )
    {
        final Map< K, V > list = new HashMap< K, V >();
        add( list, objects );
        return list;
    }
    public static < K, V > Map< K, V > linkedMap( final Map.Entry< K, V >... objects )
    {
        final Map< K, V > list = new LinkedHashMap< K, V >();
        add( list, objects );
        return list;
    }
    public static < K, V > Map< K, V > table( final Map.Entry< K, V >... objects )
    {
        final Map< K, V > list = new Hashtable< K, V >();
        add( list, objects );
        return list;
    }
    public static < K, V > Map< K, V > map( final Map.Entry< K, V >... objects )
    {
        return hashMap( objects );
    }
    public static < K, V > void add( final Map< K, V > set,
                                     final Map.Entry< K, V >... objects )
    {
    	for( final Map.Entry< K, V > object : objects )
    		set.put( object.getKey(), object.getValue() );
    }
    public static < K, V > void mapAdd( final Map< K, V > set,
                                        final Map.Entry< K, V >... objects )
    {
    	add( set, objects );
    }
}

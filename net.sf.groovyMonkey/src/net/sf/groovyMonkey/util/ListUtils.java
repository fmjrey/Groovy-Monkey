package net.sf.groovyMonkey.util;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang.ObjectUtils;

public class ListUtils
{
    public static < T > List< T > transformed( final Transformer< T > transformer,
                                               final List list )
    {
        return transformed( transformer, list.toArray() );
    }
    public static < T > List< T > transformed( final Transformer< T > transformer,
                                               final Object... objects )
    {
        final List< T > list = new ArrayList< T >()
        {
            private static final long serialVersionUID = 4888742388896207408L;
            private final Transformer< T > trans = transformer;
            @Override
            public void add( int index, T element )
            {
                super.add( index, trans.transform( element ) );
            }
            @Override
            public boolean add( T o )
            {
                return super.add( trans.transform( o ) );
            }
            @Override
            public boolean addAll( Collection< ? extends T > c )
            {
                final List< T > list = new ArrayList< T >();
                for( final T t : c )
                    list.add( trans.transform( t ) );
                return super.addAll( list );
            }
            @Override
            public boolean addAll( int index, Collection< ? extends T > c )
            {
                final List< T > list = new ArrayList< T >();
                for( final T t : c )
                    list.add( trans.transform( t ) );
                return super.addAll( index, list );
            }
            @Override
            public boolean contains( Object elem )
            {
                return super.contains( trans.transform( elem ) );
            }
            @Override
            public int indexOf( Object elem )
            {
                return super.indexOf( trans.transform( elem ) );
            }
            @Override
            public int lastIndexOf( Object elem )
            {
                return super.lastIndexOf( trans.transform( elem ) );
            }
            @Override
            public boolean remove( Object o )
            {
                // TODO Auto-generated method stub
                return super.remove( trans.transform( o ) );
            }
            @Override
            public T set( int index, T element )
            {
                return super.set( index, trans.transform( element ) );
            }
            @Override
            public boolean containsAll( Collection< ? > c )
            {
                final List< T > list = new ArrayList< T >();
                for( final Object t : c )
                    list.add( trans.transform( t ) );
                return super.containsAll( list );
            }
            @Override
            public boolean removeAll( Collection< ? > c )
            {
                final List< T > list = new ArrayList< T >();
                for( final Object t : c )
                    list.add( trans.transform( t ) );
                return super.removeAll( list );
            }
            @Override
            public boolean retainAll( Collection< ? > c )
            {
                final List< T > list = new ArrayList< T >();
                for( final Object t : c )
                    list.add( trans.transform( t ) );
                return super.retainAll( list );
            }

        };
        for( final Object object : objects )
            list.add( transformer.transform( object ) );
        return list;
    }
    public static < T > List< T > array( final T... objects )
    {
        final List< T > list = new ArrayList< T >();
        add( list, objects );
        return list;
    }
    public static < T > List< T > linked( final T... objects )
    {
        final List< T > list = new LinkedList< T >();
        add( list, objects );
        return list;
    }
    public static < T > List< T > list( final T... objects )
    {
        return array( objects );
    }
    public static < T extends Comparable< T > > List< T > treeList( final T... objects )
    {
        final List< T > list = new TreeList< T >();
        add( list, objects );
        return list;
    }
    public static < T > void add( final List< T > list,
                                  final T... objects )
    {
        for( final T object : objects )
            list.add( object );
    }
    public static < T > void listAdd( final List< T > list,
            						  final T... objects )
    {
    	add( list, objects );
    }
    public static < E extends Object > List< String > caseless( final E... objects )
    {
        final Transformer< String > transformer = new Transformer< String >()
        {
            public String transform( final Object input )
            {
                return ObjectUtils.toString( input ).toLowerCase();
            }
        };
        return transformed( transformer, objects );
    }
    public static List< String > caseless( final List list )
    {
        return caseless( list.toArray() );
    }
}

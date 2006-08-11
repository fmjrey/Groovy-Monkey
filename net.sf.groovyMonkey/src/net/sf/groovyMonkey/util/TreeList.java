package net.sf.groovyMonkey.util;
import static java.util.Collections.binarySearch;
import static org.apache.commons.collections.ComparatorUtils.NATURAL_COMPARATOR;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.ListIterator;

public class TreeList< T extends Comparable< T > > 
extends LinkedList< T >
{
    private static final long serialVersionUID = -1353224017415189717L;
    protected final Comparator< T > comparator;
    
    @SuppressWarnings("unchecked")
    public TreeList()
    {
        comparator = ( Comparator< T > )NATURAL_COMPARATOR;
    }
    public TreeList( final Comparator< T > comparator )
    {
        this.comparator = comparator;
    }
    public TreeList( final Collection< T > collection )
    {
        this();
        addAll( collection );
    }
    @Override
    public final boolean add( final T object )
    {
        final int insertionPointTemp = fastIndexOf( object );
        final int insertionPoint = insertionPointTemp >= 0 ? insertionPointTemp : -( insertionPointTemp + 1 );
        final ListIterator< T > i = super.listIterator( insertionPoint );
        while( i.hasNext() )
        {
            final T element = i.next();
            final boolean isGreater;
            if( comparator == null )
            {
                isGreater = element.compareTo( object ) > 0;
            }
            else
                isGreater = comparator.compare( element, object ) > 0;
            if( isGreater )
            {
                i.previous();
                break;
            }
        }
        i.add( object );
        return true;
    }
    @Override
    public final boolean addAll( final Collection< ? extends T > collection )
    {
        for( final T t : collection )
            add( t );
        return true;
    }
    public final Comparator< T > comparator()
    {
        return comparator;
    }
    public final int fastIndexOf( final T key )
    {
        return binarySearch( this, key, comparator );
    }
    @Override
    public final ListIterator< T > listIterator( final int index ) 
    throws IndexOutOfBoundsException
    {
        return new ListIterator< T >()
        {
            private final ListIterator< T > listIterator = TreeList.super.listIterator( index );

            public void add( final T notUsed )
            {
                error();
            }
            public boolean hasNext()
            {
                return listIterator.hasNext();
            }
            public boolean hasPrevious()
            {
                return listIterator.hasPrevious();
            }
            public T next()
            {
                return listIterator.next();
            }
            public int nextIndex()
            {
                return listIterator.nextIndex();
            }
            public T previous()
            {
                return listIterator.previous();
            }
            public int previousIndex()
            {
                return listIterator.previousIndex();
            }
            public void remove()
            {
                listIterator.remove();
            }
            public void set( final T notUsed )
            {
                error();
            }
        };
    }
    @Override
    public final void add( final int notUsed, 
                           final T notUsed2 ) 
    throws UnsupportedOperationException
    {
        error();
    }
    @Override
    public final T set( final int notUsed, 
                        final T notUsed2 ) 
    throws UnsupportedOperationException
    {
        error();
        throw new AssertionError( "Should never be reached" );
    }
    @Override
    public final void addFirst( final T notUsed ) 
    throws UnsupportedOperationException
    {
        error();
    }
    @Override
    public final void addLast( final T notUsed ) 
    throws UnsupportedOperationException
    {
        error();
    }
    @Override
    public final boolean addAll( final int notUsed, 
                                 final Collection< ? extends T > notUsed2 ) 
    throws UnsupportedOperationException
    {
        error();
        throw new AssertionError( "Should never be reached" );
    }
    private static void error() 
    throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException( "Objects can only be added to a TreeList< T > using add( Object ) or addAll( Collection )" );
    }
}

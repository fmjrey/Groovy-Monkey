package net.sf.groovyMonkey;
import static net.sf.groovyMonkey.GroovyMonkeyPlugin.PLUGIN_ID;
import static org.apache.commons.lang.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang.builder.HashCodeBuilder.reflectionHashCode;

import java.util.Map;
import java.util.TreeMap;

public class DOMDescriptor
implements Comparable< DOMDescriptor >
{
    public final String url;
    public final String pluginName;
    public final Map< String, String > map = new TreeMap< String, String >();

    public DOMDescriptor( final String url, 
                          final String pluginName,
                          final Map< String, String > map ) 
    {
        this.url = url;
        this.pluginName = pluginName;
        if( map != null && !map.isEmpty() )
            this.map.putAll( map );
    }
    @Override
    public String toString()
    {
        final StringBuffer domUpdateURL = new StringBuffer( getDOMUpdateSite() );
        if( !map.isEmpty() )
            domUpdateURL.append( "[ " );
        for( final String scriptVarName : map.keySet() )
            domUpdateURL.append( map.get( scriptVarName ) ).append( ":" ).append( scriptVarName ).append( ", " );
        if( !map.isEmpty() )
        {
            domUpdateURL.deleteCharAt( domUpdateURL.length() - 1 ); // Removing that last appended comma
            domUpdateURL.append( "]" );
        }
        return domUpdateURL.toString();
    }
    public String getDOMUpdateSite()
    {
        if( pluginName.equals( PLUGIN_ID ) )
            return "Default";
        return url.endsWith( "/" ) ? url + pluginName : url + "/" + pluginName;
    }
    @Override
    public boolean equals( final Object obj )
    {
        return reflectionEquals( this, obj );
    }
    @Override
    public int hashCode()
    {
        return reflectionHashCode( this );
    }
    public int compareTo( final DOMDescriptor descriptor )
    {
        if( descriptor == null )
            return 1;
        if( toString().compareTo( descriptor.toString() ) == 0 )
            return 0;
        if( toString().equals( "Default" ) )
            return 1;
        return toString().compareTo( descriptor.toString() );
    }
}

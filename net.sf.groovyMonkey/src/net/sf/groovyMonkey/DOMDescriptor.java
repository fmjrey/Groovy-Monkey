package net.sf.groovyMonkey;
import static net.sf.groovyMonkey.GroovyMonkeyPlugin.PLUGIN_ID;
import static org.apache.commons.lang.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang.builder.HashCodeBuilder.reflectionHashCode;

public class DOMDescriptor
implements Comparable< DOMDescriptor >
{
    public final String url;
    public final String pluginName;

    public DOMDescriptor( final String url, 
                          final String pluginName ) 
    {
        this.url = url;
        this.pluginName = pluginName;
    }
    @Override
    public String toString()
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

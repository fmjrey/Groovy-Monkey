package net.sf.groovyMonkey;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class DOMDescriptor
{
    public final String url;
    public final String plugin_name;

    public DOMDescriptor( final String url, 
                          final String plugin_name ) 
    {
        this.url = url;
        this.plugin_name = plugin_name;
    }
    @Override
    public String toString()
    {
        if( plugin_name.equals( GroovyMonkeyPlugin.PLUGIN_ID ) )
            return "Default";
        return url.endsWith( "/" ) ? url + plugin_name : url + "/" + plugin_name;
    }
    @Override
    public boolean equals( final Object obj )
    {
        return EqualsBuilder.reflectionEquals( this, obj );
    }
    @Override
    public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode( this );
    }
}

package net.sf.groovyMonkey;
import static net.sf.groovyMonkey.GroovyMonkeyPlugin.PLUGIN_ID;
import static org.apache.commons.lang.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang.builder.HashCodeBuilder.reflectionHashCode;

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
        if( plugin_name.equals( PLUGIN_ID ) )
            return "Default";
        return url.endsWith( "/" ) ? url + plugin_name : url + "/" + plugin_name;
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
}

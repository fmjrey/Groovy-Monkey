package net.sf.groovyMonkey.dom.project;
import static java.lang.Integer.parseInt;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.split;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class Version
{
    private int major;
    private int minor;
    private int service;
    private String qualifier = "";
    
    public Version( final String version )
    {
        final String[] split = split( version, '.' );
        if( split == null || split.length < 3 )
            throw new IllegalArgumentException( "Error version: " + version + " is not a valid Bundle-Version string" );
        try
        {
            major = parseInt( split[ 0 ] );
            minor = parseInt( split[ 1 ] );
            service = parseInt( split[ 2 ] );
        }
        catch( final NumberFormatException nfe )
        {
            throw new IllegalArgumentException( "Error version: " + version + " is not a valid Bundle-Version string", nfe );
        }
        if( split.length > 3 )
        {
            for( int i = 3; i < split.length; i++ )
            {
                qualifier += split[ i ];
                if( i < split.length - 1 )
                    qualifier += ".";
            }
        }
        else
            qualifier = "";
    }
    public Version incrementMajor()
    {
        major++;
        return this;
    }
    public Version incrementMinor()
    {
        minor++;
        return this;
    }
    public Version incrementService()
    {
        service++;
        return this;
    }
    @Override
    public boolean equals( final Object obj )
    {
        if( !( obj instanceof Version ) )
            return false;
        final Version version = ( Version )obj;
        return new EqualsBuilder().append( major, version.major )
                                  .append( minor, version.minor )
                                  .append( service, version.service )
                                  .isEquals();
    }
    @Override
    public int hashCode()
    {
        return new HashCodeBuilder( 13, 41 ).append( major )
                                            .append( minor )
                                            .append( service )
                                            .toHashCode();
    }
    @Override
    public String toString()
    {
        return major + "." + minor + "." + service + ( isNotBlank( qualifier ) ? "." + qualifier : "" );
    }
}

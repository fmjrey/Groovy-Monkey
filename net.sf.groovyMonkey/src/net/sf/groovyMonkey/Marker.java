package net.sf.groovyMonkey;
import static org.apache.commons.lang.StringUtils.defaultString;
import static org.apache.commons.lang.Validate.notNull;
import static org.apache.commons.lang.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang.builder.HashCodeBuilder.reflectionHashCode;
import org.eclipse.core.resources.IMarker;

public class Marker
{
    private final Tags.Type tag;
    private final int severity;
    private final String message;
    private int lineNumber = 0;
    private int charStart = -1;
    private int charEnd = -1;
    
    public static Marker error( final Tags.Type tag,
                                final String message, 
                                final int... location )
    {
        return marker( tag, IMarker.SEVERITY_ERROR, message, location );
    }
    public static Marker warning( final Tags.Type tag,
                                  final String message, 
                                  final int... location )
    {
        return marker( tag, IMarker.SEVERITY_WARNING, message, location );
    }
    public static Marker info( final Tags.Type tag,
                               final String message, 
                               final int... location )
    {
        return marker( tag, IMarker.SEVERITY_INFO, message, location );
    }
    private static Marker marker( final Tags.Type tag,
                                  final int severity, 
                                  final String message, 
                                  final int... location )
    {
        notNull( tag );
        if( location == null || location.length == 0 )
            return new Marker( tag, severity, message );
        if( location.length == 1 )
            return new Marker( tag, severity, message, location[ 0 ] );
        return new Marker( tag, severity, message, location[ 0 ], location[ 1 ], location[ 2 ] );
    }
    public Marker( final Tags.Type tag,
                   final int severity, 
                   final String message )
    {
        notNull( tag );
        this.tag = tag;
        this.severity = severity;
        this.message = defaultString( message );
    }
    public Marker( final Tags.Type tag,
                   final int severity, 
                   final String message,
                   final int lineNumber )
    {
        this( tag, severity, message );
        this.lineNumber = lineNumber;
    }
    public Marker( final Tags.Type tag,
                   final int severity, 
                   final String message,
                   final int lineNumber,
                   final int charStart,
                   final int charEnd )
    {
        this( tag, severity, message, lineNumber );
        this.charStart = charStart;
        this.charEnd = charEnd;
    }
    public Tags.Type tag()
    {
        return getTag();
    }
    public Tags.Type getTag()
    {
        return tag;
    }
    public int charEnd()
    {
        return getCharEnd();
    }
    public int getCharEnd()
    {
        return charEnd;
    }
    public int charStart()
    {
        return getCharStart();
    }
    public int getCharStart()
    {
        return charStart;
    }
    public int lineNumber()
    {
        return getLineNumber();
    }
    public int getLineNumber()
    {
        return lineNumber;
    }
    public String message()
    {
        return getMessage();
    }
    public String getMessage()
    {
        return message;
    }
    public int severity()
    {
        return getSeverity();
    }
    public int getSeverity()
    {
        return severity;
    }
    @Override
    public int hashCode()
    {
        return reflectionHashCode( this );
    }
    @Override
    public boolean equals( final Object obj )
    {
        return reflectionEquals( this, obj );
    }   
}

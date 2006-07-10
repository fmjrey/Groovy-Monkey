package net.sf.groovyMonkey.dom.project;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Version
{
    String version;

    public Version( final String v )
    {
        version = v;
    }
    public Version increment_third_digit()
    {
        final Pattern p = Pattern.compile( "(\\d+\\.\\d+\\.)(\\d+)(.*)" );
        final Matcher m = p.matcher( version );
        if( m.matches() )
        {
            final int n = Integer.parseInt( m.group( 2 ) );
            version = m.group( 1 ) + ( n + 1 ) + m.group( 3 );
        }
        return this;
    }
    @Override
    public String toString()
    {
        return version;
    }
}

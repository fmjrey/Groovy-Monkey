package net.sf.groovyMonkey.util;
import org.apache.commons.lang.StringUtils;

public class StringUtil
{
    public static String removeFirst( final String string, 
                                      final String remove )
    {
        if( StringUtils.isEmpty( string ) || StringUtils.isEmpty( remove ) )
            return string;
        return StringUtils.replace( string, remove, "", 1 );
    }
}

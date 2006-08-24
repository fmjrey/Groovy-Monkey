package net.sf.groovyMonkey;
import java.util.EnumMap;
import java.util.Map;

public class Tags
{
    public enum Type
    {
        PATH, MENU, KUDOS, LICENSE, DOM, LISTENER, LANG, INCLUDE, INCLUDE_BUNDLE, JOB, EXEC_MODE
    }
    public static final Map< Type, String > Tags = new EnumMap< Type, String >( Type.class );
    static
    {
        Tags.put( Type.PATH, "Script-Path:" );
        Tags.put( Type.MENU, "Menu:" );
        Tags.put( Type.KUDOS, "Kudos:" );
        Tags.put( Type.LICENSE, "License:" );
        Tags.put( Type.DOM, "DOM:" );
        Tags.put( Type.LISTENER, "Listener:" );
        Tags.put( Type.LANG, "LANG:" );
        Tags.put( Type.INCLUDE, "Include:" );
        Tags.put( Type.INCLUDE_BUNDLE, "Include-Bundle:" );
        Tags.put( Type.JOB, "Job:" );
        Tags.put( Type.EXEC_MODE, "Exec-Mode:" );
    }
    public static final String get( final Type type )
    {
        return Tags.get( type );
    }
    public static final String getTag( final Type type )
    {
        return get( type );
    }
    public static final String getTagText( final Type type )
    {
        return " * " + getTag( type ) + " ";
    }
}

package net.sf.groovyMonkey.lang;

public class CompilationException
extends Exception
{
    private static final long serialVersionUID = -1339637392743307406L;
    public CompilationException()
    {
        super();
    }
    public CompilationException( final String message, 
                                 final Throwable cause )
    {
        super( message, cause );
    }
    public CompilationException( final String message )
    {
        super( message );
    }
    public CompilationException( final Throwable cause )
    {
        super( cause );
    }

}

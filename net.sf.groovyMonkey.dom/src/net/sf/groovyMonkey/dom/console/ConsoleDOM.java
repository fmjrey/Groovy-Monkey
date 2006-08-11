package net.sf.groovyMonkey.dom.console;
import static org.eclipse.ui.console.ConsolePlugin.getDefault;
import java.io.IOException;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

public class ConsoleDOM
{
    private static final String NAME = "Monkey";
    private MessageConsoleStream out;
    private final MessageConsole console;

    public ConsoleDOM()
    {
        this( NAME );
    }
    public ConsoleDOM( final String name )
    {
        for( final IConsole console : manager().getConsoles() )
        {
            if( !console.getName().equals( NAME ) || !( console instanceof MessageConsole ) )
                continue;
            this.console = ( MessageConsole )console;
            manager().showConsoleView( this.console );
            this.out = this.console.newMessageStream();
            return;
        }
        console = new MessageConsole( name, null );
        out = console.newMessageStream();
        manager().addConsoles( new IConsole[]{ console } );
        manager().showConsoleView( console );
    }
    public IConsoleManager manager()
    {
        return getDefault().getConsoleManager();
    }
    public ConsoleDOM print( final String msg )
    {
        out.print( msg );
        return this;
    }
    public ConsoleDOM println( final String msg )
    {
        out.println( msg );
        return this;
    }
    public ConsoleDOM println()
    {
        out.println();
        return this;
    }
    public ConsoleDOM clear()
    {
        console.clearConsole();
        try
        {
            out.flush();
            out.close();
        }
        catch( final IOException e ) {}
        out = console.newMessageStream();
        return this;
    }
}

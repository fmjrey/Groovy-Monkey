package net.sf.groovyMonkey.dom.console;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

public class ConsoleDOM
{
    private static final String NAME = "Monkey";
    private final MessageConsoleStream out;
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
        return ConsolePlugin.getDefault().getConsoleManager();
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

}

package net.sf.groovyMonkey.editor.actions;
import static net.sf.groovyMonkey.GroovyMonkeyPlugin.ICON_PATH;
import static net.sf.groovyMonkey.GroovyMonkeyPlugin.getImageDescriptor;
import net.sf.groovyMonkey.editor.ScriptContentProvider;
import net.sf.groovyMonkey.editor.ScriptResourceChangeListener;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;

public class FlatOutlineViewAction 
extends Action
{
    private final ScriptContentProvider provider;
    private final ScriptResourceChangeListener listener;
    private IAction otherAction;
    
    public FlatOutlineViewAction( final ScriptContentProvider provider,
                                  final ScriptResourceChangeListener listener )
    {
        super( "Flat Layout", AS_CHECK_BOX );
        setChecked( false );
        setImageDescriptor( getImageDescriptor( ICON_PATH + "flatLayout.gif" ) );
        this.provider = provider;
        this.listener = listener;
        
    }
    public FlatOutlineViewAction setOtherAction( final IAction otherAction )
    {
        this.otherAction = otherAction;
        return this;
    }
    @Override
    public void run()
    {
        provider.setViewLayout( isChecked() );
        if( otherAction != null )
            otherAction.setChecked( !isChecked() );
        listener.updateViewer();
    }
}

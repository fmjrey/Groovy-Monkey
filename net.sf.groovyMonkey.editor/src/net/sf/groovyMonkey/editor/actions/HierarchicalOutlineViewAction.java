package net.sf.groovyMonkey.editor.actions;
import static net.sf.groovyMonkey.GroovyMonkeyPlugin.ICON_PATH;
import net.sf.groovyMonkey.GroovyMonkeyPlugin;
import net.sf.groovyMonkey.editor.ScriptContentProvider;
import net.sf.groovyMonkey.editor.ScriptResourceChangeListener;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;

public class HierarchicalOutlineViewAction 
extends Action
{
    private final ScriptContentProvider provider;
    private final ScriptResourceChangeListener listener;
    private IAction otherAction;
    
    public HierarchicalOutlineViewAction( final ScriptContentProvider provider,
                                          final ScriptResourceChangeListener listener )
    {
        super( "Hierarchical Layout", AS_CHECK_BOX );
        setChecked( true );
        setImageDescriptor( GroovyMonkeyPlugin.getImageDescriptor( ICON_PATH + "hierarchicalLayout.gif" ) );
        this.provider = provider;
        this.listener = listener;
    }
    public HierarchicalOutlineViewAction setOtherAction( final IAction otherAction )
    {
        this.otherAction = otherAction;
        return this;
    }
    @Override
    public void run()
    {
        provider.setViewLayout( !isChecked() );
        if( otherAction != null )
            otherAction.setChecked( !isChecked() );
        listener.updateViewer();
    }
}

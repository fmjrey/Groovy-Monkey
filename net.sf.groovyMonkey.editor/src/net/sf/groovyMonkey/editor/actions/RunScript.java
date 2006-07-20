package net.sf.groovyMonkey.editor.actions;
import static net.sf.groovyMonkey.dom.Utilities.activeWindow;
import net.sf.groovyMonkey.RunMonkeyScript;
import net.sf.groovyMonkey.editor.ScriptEditor;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

public class RunScript 
extends Action 
implements IObjectActionDelegate
{
    private ScriptEditor targetEditor = null;
    private IStructuredSelection selection = null;
    
    public RunScript()
    {
    }
    public RunScript( final ScriptEditor targetEditor )
    {
        this.targetEditor = targetEditor;
        setText( "Run Script" );
        setToolTipText( "Run Groovy Monkey Script" );
    }
    public void run( final IAction action )
    {
        run();
    }
    @Override
    public void run()
    {
        final IFile script = getTargetScript();
        if( script == null )
            return;
        new RunMonkeyScript( script, activeWindow() ).run( false );
    }
    private IFile getTargetScript()
    {
        if( targetEditor != null )
            return ( IFile )targetEditor.getAdapter( IFile.class );
        if( selection != null )
        {
            final Object selected = selection.getFirstElement();
            if( !( selected instanceof IFile ) )
                return null;
            return ( IFile )selected;
        }
        return null;
    }
    public void selectionChanged( final IAction action, 
                                  final ISelection selection )
    {
        if( !( selection instanceof IStructuredSelection ) )
            return;
        this.selection = ( IStructuredSelection )selection;
    }
    public void setActivePart( final IAction action, 
                               final IWorkbenchPart targetPart )
    {
    }
}

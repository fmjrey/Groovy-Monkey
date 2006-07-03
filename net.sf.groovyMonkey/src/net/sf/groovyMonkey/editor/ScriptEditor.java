package net.sf.groovyMonkey.editor;
import static org.eclipse.ui.texteditor.ITextEditorActionConstants.GROUP_EDIT;
import net.sf.groovyMonkey.actions.AddDOM;
import net.sf.groovyMonkey.actions.RunScript;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

public class ScriptEditor 
extends TextEditor
{
    private ScriptContentOutlinePage outline = null;
    
    public ScriptEditor()
    {
        super();
    }
    @Override
    protected void createActions()
    {
        super.createActions();
        setAction( "addDOM", new AddDOM( this ) );
        setAction( "runScript", new RunScript( this ) );
    }
    @Override
    public Object getAdapter( final Class adapter )
    {
        if( IContentOutlinePage.class.equals( adapter ) )
        {
            if( outline != null )
                return outline;
            outline = new ScriptContentOutlinePage();
            if( getEditorInput() != null )
                outline.setInput( getEditorInput() );
            return outline;
        }
        if( IFile.class.equals( adapter ) )
        {
            return getEditorInput().getAdapter( adapter );
        }
        return super.getAdapter( adapter );
    }
    @Override
    protected void editorContextMenuAboutToShow( final IMenuManager menu )
    {
        super.editorContextMenuAboutToShow( menu );
        menu.appendToGroup( GROUP_EDIT, new Separator( GROUP_EDIT ) ); 
        menu.appendToGroup( GROUP_EDIT, getAction( "addDOM" ) );
        menu.appendToGroup( GROUP_EDIT, getAction( "runScript" ) );
    }
}

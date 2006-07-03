package net.sf.groovyMonkey.editor;
import net.sf.groovyMonkey.actions.AddDOM;
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
        final IAction action = new AddDOM( this );
        setAction( "addDOM", action );
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
        final IAction action = getAction( "addDOM" );
        final String editGroup = "group.edit";
        menu.appendToGroup( ITextEditorActionConstants.GROUP_EDIT, new Separator( editGroup ) ); 
        menu.appendToGroup( editGroup, action );
    }
}

package net.sf.groovyMonkey.editor;
import static java.util.ResourceBundle.getBundle;
import static net.sf.groovyMonkey.GroovyMonkeyPlugin.PLUGIN_ID;
import static org.eclipse.ui.texteditor.ITextEditorActionConstants.GROUP_EDIT;
import static org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS;
import net.sf.groovyMonkey.editor.actions.AddBundle;
import net.sf.groovyMonkey.editor.actions.AddDOM;
import net.sf.groovyMonkey.editor.actions.AddInclude;
import net.sf.groovyMonkey.editor.actions.PublishScriptForBlogger;
import net.sf.groovyMonkey.editor.actions.PublishScriptForEmail;
import net.sf.groovyMonkey.editor.actions.PublishScriptForText;
import net.sf.groovyMonkey.editor.actions.PublishScriptForWiki;
import net.sf.groovyMonkey.editor.actions.RunScript;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.eclipse.ui.texteditor.ContentAssistAction;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

public class ScriptEditor
extends AbstractDecoratedTextEditor
{
    public static final String ADD_GROUP = GROUP_EDIT + "/Add To Script";
    private ScriptContentOutlinePage outline = null;

    public ScriptEditor()
    {
        super();
        setSourceViewerConfiguration( new Configuration( this ) );
        setDocumentProvider( new ScriptDocumentProvider() );
    }
    @Override
    protected void createActions()
    {
        super.createActions();
        final Action action = new ContentAssistAction( getBundle( PLUGIN_ID + ".editor.messages" ), "ContentAssistProposal.", this );
        action.setActionDefinitionId( CONTENT_ASSIST_PROPOSALS );
        setAction( "ContentAssistProposal", action );
        markAsStateDependentAction( "ContentAssistProposal", true );
        setAction( "addBundle", new AddBundle( this ) );
        setAction( "addDOM", new AddDOM( this ) );
        setAction( "addInclude", new AddInclude( this ) );
        setAction( "runScript", new RunScript( this ) );
        setAction( "publishForBlogger", new PublishScriptForBlogger( this ) );
        setAction( "publishForEmail", new PublishScriptForEmail( this ) );
        setAction( "publishForText", new PublishScriptForText( this ) );
        setAction( "publishForWiki", new PublishScriptForWiki( this ) );
    }
    @SuppressWarnings("unchecked")
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
        menu.appendToGroup( GROUP_EDIT, getAction( "addBundle" ) );
        menu.appendToGroup( GROUP_EDIT, getAction( "addDOM" ) );
        menu.appendToGroup( GROUP_EDIT, getAction( "addInclude" ) );
        menu.appendToGroup( GROUP_EDIT, getAction( "runScript" ) );
        final MenuManager publishMenu = new MenuManager( "Copy for Publication", "publish" );
        menu.appendToGroup( GROUP_EDIT, publishMenu );
        publishMenu.add( getAction( "publishForBlogger" ) );
        publishMenu.add( getAction( "publishForEmail" ) );
        publishMenu.add( getAction( "publishForText" ) );
        publishMenu.add( getAction( "publishForWiki" ) );
    }
}

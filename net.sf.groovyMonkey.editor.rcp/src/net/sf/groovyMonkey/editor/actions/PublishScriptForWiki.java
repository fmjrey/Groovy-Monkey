package net.sf.groovyMonkey.editor.actions;
import static net.sf.groovyMonkey.util.ListUtil.array;
import java.util.List;
import net.sf.groovyMonkey.editor.ScriptEditor;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;

/**
 * This class is meant as a delegate that invokes the PublishScriptForWiki action from within the Editor.
 */
public class PublishScriptForWiki
extends Action
{
	private final ScriptEditor editor;

	public PublishScriptForWiki( final ScriptEditor editor )
	{
		this.editor = editor;
		setText( "as Wiki (indented)" );
        setToolTipText( "Publish script in wiki format" );
	}
	@Override
	public void run()
	{
		final net.sf.groovyMonkey.actions.PublishScriptForWiki publishAction = new net.sf.groovyMonkey.actions.PublishScriptForWiki()
		{
			@Override
			protected List< IFile > getScripts()
			{
				return array( ( IFile )editor.getAdapter( IFile.class ) );
			}

		};
		publishAction.run( this );
	}
}

package net.sf.groovyMonkey.editor.actions;
import static net.sf.groovyMonkey.util.ListUtil.array;
import java.util.List;
import net.sf.groovyMonkey.editor.ScriptEditor;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;

/**
 * This class is meant as a delegate that invokes the PublishScriptForBlogger action from within the Editor.
 */
public class PublishScriptForBlogger
extends Action
{
	private final ScriptEditor editor;

	public PublishScriptForBlogger( final ScriptEditor editor )
	{
		this.editor = editor;
		setText( "as Blogger (HTML)" );
        setToolTipText( "Publish script in blogging format" );
	}
	@Override
	public void run()
	{
		final net.sf.groovyMonkey.actions.PublishScriptForBlogger publishAction = new net.sf.groovyMonkey.actions.PublishScriptForBlogger()
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

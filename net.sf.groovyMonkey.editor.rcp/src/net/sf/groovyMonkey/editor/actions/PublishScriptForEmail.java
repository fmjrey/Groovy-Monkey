package net.sf.groovyMonkey.editor.actions;
import static net.sf.groovyMonkey.util.ListUtil.array;
import java.util.List;
import net.sf.groovyMonkey.editor.ScriptEditor;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;

/**
 * This class is meant as a delegate that invokes the PublishScriptForEmail action from within the Editor.
 */
public class PublishScriptForEmail
extends Action
{
	private final ScriptEditor editor;

	public PublishScriptForEmail( final ScriptEditor editor )
	{
		this.editor = editor;
		setText( "as Email (wrapped text)" );
		setToolTipText( "Publish script for transmitting as email" );
	}
	@Override
	public void run()
	{
		final net.sf.groovyMonkey.actions.PublishScriptForEmail publishAction = new net.sf.groovyMonkey.actions.PublishScriptForEmail()
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

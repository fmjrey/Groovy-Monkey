package net.sf.groovyMonkey.preferences;
import static net.sf.groovyMonkey.GroovyMonkeyPlugin.getDefault;
import static net.sf.groovyMonkey.preferences.PreferenceInitializer.MONKEY_MENU_NAME;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class GroovyMonkeyPreferencePage 
extends FieldEditorPreferencePage 
implements IWorkbenchPreferencePage
{
    public GroovyMonkeyPreferencePage()
    {
        super( GRID );
        setPreferenceStore( getDefault().getPreferenceStore() );
        setDescription( "Groovy Monkey Preferences Page" );
    }
    public void init( final IWorkbench workbench )
    {
    }
    @Override
    protected void createFieldEditors()
    {
        addField( new StringFieldEditor( MONKEY_MENU_NAME, "&Main Toolbar Menu Name", getFieldEditorParent() ) );
    }
}

package net.sf.groovyMonkey.preferences;
import net.sf.groovyMonkey.GroovyMonkeyPlugin;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

public class PreferenceInitializer 
extends AbstractPreferenceInitializer
{
    public static final String MONKEY_MENU_NAME = "net.sf.groovyMonkey.menuName";
    @Override
    public void initializeDefaultPreferences()
    {
        final IPreferenceStore store = GroovyMonkeyPlugin.getDefault().getPreferenceStore();
        store.setDefault( MONKEY_MENU_NAME, "Groovy Monkey" );
    }
}

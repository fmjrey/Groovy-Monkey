package net.sf.groovyMonkey.lang;
import static net.sf.groovyMonkey.dom.Utilities.key;
import static org.apache.bsf.BSFManager.isLanguageRegistered;
import static org.apache.bsf.BSFManager.registerScriptingEngine;
import static org.apache.commons.lang.StringUtils.defaultString;
import static org.apache.commons.lang.StringUtils.trim;
import static org.apache.commons.lang.Validate.noNullElements;
import static org.apache.commons.lang.Validate.notNull;
import java.util.HashMap;
import java.util.Map;
import net.sf.groovyMonkey.ScriptMetadata;
import org.eclipse.core.resources.IFile;

public abstract class MonkeyScriptFactoryAbstract
implements IMonkeyScriptFactory
{
    private static final Map< String, Map< String, IMonkeyScript > > runners = new HashMap< String, Map< String, IMonkeyScript >>();
    protected String languageName;
    protected String fileExtension;
    protected String scriptEngine;

    public MonkeyScriptFactoryAbstract()
    {
        init();
        noNullElements( new Object[] { languageName, fileExtension, scriptEngine } );
        if( !isLanguageRegistered( languageName ) )
            registerScriptingEngine( languageName, scriptEngine, new String[]{ fileExtension } );
    }
    /**
     * The subclass must define the languageName, fileExtension and scriptEngine
     *  attributes in this method.
     */
    protected abstract void init();
    public synchronized void changed( final IFile script )
    {
        if( script == null )
            return;
        runners().remove( key( script ) );
    }
    private synchronized Map< String, IMonkeyScript > runners()
    {
        final Map< String, IMonkeyScript > map = runners.get( name() );
        if( map != null )
            return map;
        final Map< String, IMonkeyScript > langMap = new HashMap< String, IMonkeyScript >();
        runners.put( languageName, langMap );
        return langMap;
    }
    public synchronized void clearCachedScripts()
    {
        runners.clear();
    }
    public String getName()
    {
        return languageName;
    }
    public String name()
    {
        return getName();
    }
    public boolean isLang( final String name )
    {
        return defaultString( trim( name ) ).equalsIgnoreCase( name() );
    }
    public IMonkeyScript runner( final ScriptMetadata metadata,
                                 final Map< String, Object > map )
    {
        return getRunner( metadata, map );
    }
    protected abstract IMonkeyScript script( final ScriptMetadata metadata );
    public IMonkeyScript getRunner( final ScriptMetadata metadata,
                                    final Map< String, Object > map )
    {
        notNull( metadata );
        notNull( metadata.getFile() );
        final String scriptPath = key( metadata.getFile() );
        final Map< String, IMonkeyScript > runners = runners();
        if( runners.containsKey( scriptPath ) )
            return runners.get( scriptPath ).setBinding( metadata, map );
        final IMonkeyScript runner = script( metadata ).setBinding( metadata, map );
        runners.put( scriptPath, runner );
        return runner;
    }
}

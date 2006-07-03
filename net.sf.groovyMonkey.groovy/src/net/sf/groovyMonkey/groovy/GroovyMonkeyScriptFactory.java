package net.sf.groovyMonkey.groovy;
import net.sf.groovyMonkey.ScriptMetadata;
import net.sf.groovyMonkey.lang.IMonkeyScript;
import net.sf.groovyMonkey.lang.IMonkeyScriptFactory;
import net.sf.groovyMonkey.lang.MonkeyScript;
import net.sf.groovyMonkey.lang.MonkeyScriptFactoryAbstract;

public class GroovyMonkeyScriptFactory 
extends MonkeyScriptFactoryAbstract
implements IMonkeyScriptFactory
{
    public static final String NAME = "Groovy";
    public static final String EXTENSION = "groovy";
    public static final String SCRIPT_ENGINE_CLASS = "org.codehaus.groovy.bsf.GroovyEngine";
    
    public GroovyMonkeyScriptFactory()
    {
        super();
    }
    @Override
    protected void init()
    {
        this.languageName = NAME;
        this.fileExtension = EXTENSION;
        this.scriptEngine = SCRIPT_ENGINE_CLASS;
    }
    @Override
    protected IMonkeyScript script( final ScriptMetadata metadata )
    {
        return new MonkeyScript( languageName, fileExtension, metadata.getFile(), false );
    }
}

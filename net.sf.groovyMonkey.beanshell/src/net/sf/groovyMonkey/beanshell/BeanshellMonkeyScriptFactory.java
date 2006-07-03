package net.sf.groovyMonkey.beanshell;
import net.sf.groovyMonkey.ScriptMetadata;
import net.sf.groovyMonkey.lang.IMonkeyScript;
import net.sf.groovyMonkey.lang.IMonkeyScriptFactory;
import net.sf.groovyMonkey.lang.MonkeyScript;
import net.sf.groovyMonkey.lang.MonkeyScriptFactoryAbstract;

public class BeanshellMonkeyScriptFactory 
extends MonkeyScriptFactoryAbstract
implements IMonkeyScriptFactory
{
    public static final String NAME = "Beanshell";
    public static final String EXTENSION = "bsh";
    public static final String SCRIPT_ENGINE_CLASS = "bsh.util.BeanShellBSFEngine";

    public BeanshellMonkeyScriptFactory()
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

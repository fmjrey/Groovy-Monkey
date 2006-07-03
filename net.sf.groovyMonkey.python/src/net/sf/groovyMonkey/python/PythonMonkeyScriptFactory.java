package net.sf.groovyMonkey.python;
import net.sf.groovyMonkey.ScriptMetadata;
import net.sf.groovyMonkey.lang.IMonkeyScript;
import net.sf.groovyMonkey.lang.IMonkeyScriptFactory;
import net.sf.groovyMonkey.lang.MonkeyScript;
import net.sf.groovyMonkey.lang.MonkeyScriptFactoryAbstract;

public class PythonMonkeyScriptFactory
extends MonkeyScriptFactoryAbstract
implements IMonkeyScriptFactory
{
    public static final String NAME = "Python";
    public static final String EXTENSION = "py";
    public static final String SCRIPT_ENGINE_CLASS = "org.apache.bsf.engines.jython.JythonEngine";
    
    public PythonMonkeyScriptFactory()
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
        return new MonkeyScript( languageName, fileExtension, metadata.getFile(), true );
    }
}

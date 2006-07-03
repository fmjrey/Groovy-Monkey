package net.sf.groovyMonkey.tcl;
import net.sf.groovyMonkey.ScriptMetadata;
import net.sf.groovyMonkey.lang.IMonkeyScript;
import net.sf.groovyMonkey.lang.IMonkeyScriptFactory;
import net.sf.groovyMonkey.lang.MonkeyScript;
import net.sf.groovyMonkey.lang.MonkeyScriptFactoryAbstract;

public class TclMonkeyScriptFactory 
extends MonkeyScriptFactoryAbstract
implements IMonkeyScriptFactory
{
    public static final String NAME = "Tcl";
    public static final String EXTENSION = "tcl";
    public static final String SCRIPT_ENGINE_CLASS = "org.apache.bsf.engines.jacl.JaclEngine";
    
    public TclMonkeyScriptFactory()
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

package net.sf.groovyMonkey.lang;
import java.util.Map;
import net.sf.groovyMonkey.ScriptMetadata;
import org.eclipse.core.resources.IFile;

public interface IMonkeyScript
{
    public IMonkeyScript setBinding( final ScriptMetadata metadata,
                                     final Map< String, Object > map );
    public IFile getScript();
    public IFile script();
    /**
     * Runs the script with the given variable bindings and classloaders as given in the 
     * DOMs provided in the script metadata.
     * @param binding
     * @param classLoaders
     * @param script
     */
    public void run()
    throws CompilationException;
}

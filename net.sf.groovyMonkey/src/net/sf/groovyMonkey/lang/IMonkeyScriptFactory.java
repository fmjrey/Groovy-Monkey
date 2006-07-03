package net.sf.groovyMonkey.lang;
import java.util.Map;
import net.sf.groovyMonkey.ScriptMetadata;
import org.eclipse.core.resources.IFile;

public interface IMonkeyScriptFactory
{
   /**
    * @return The name of the language the extension represents.  Should be the same as
    * the language metadata tag in the monkey script and the name tag in the extension xml.
    * Language names do not consider case significant.
    */
   public String getName();
   public String name();
   /**
    * @param fileName
    * @return true if the language extension is recognized by this runner as belonging to it.
    */
   public boolean isLang( final String name );
   
   /**
    * Lets the factory know that the file has changed and therefore could be rebuilt or
    * marked to be rebuilt at the next opportunity.
    * @param script
    */
   public void changed( final IFile script );
   
   public void clearCachedScripts();
   
   public IMonkeyScript getRunner( final ScriptMetadata metadata,
                                   final Map< String, Object > map );
   public IMonkeyScript runner( final ScriptMetadata metadata,
                                final Map< String, Object > map );
}

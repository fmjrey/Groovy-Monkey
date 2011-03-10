package net.sf.groovyMonkey.lang;
import static java.lang.Thread.currentThread;
import static java.security.AccessController.doPrivileged;
import static net.sf.groovyMonkey.GroovyMonkeyPlugin.context;
import static net.sf.groovyMonkey.ScriptMetadata.stripMetadata;
import static net.sf.groovyMonkey.dom.Utilities.getContents;
import static net.sf.groovyMonkey.dom.Utilities.getExtensionGlobalVariables;
import static org.apache.bsf.BSFException.REASON_IO_ERROR;
import static org.apache.bsf.BSFException.REASON_OTHER_ERROR;
import static org.apache.commons.lang.StringUtils.removeEnd;
import static org.apache.commons.lang.StringUtils.removeStart;
import static org.apache.commons.lang.StringUtils.substringBeforeLast;
import static org.apache.commons.lang.Validate.noNullElements;
import static org.apache.commons.lang.Validate.notNull;
import static org.eclipse.core.resources.ResourcesPlugin.getWorkspace;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;
import net.sf.groovyMonkey.BundleClassLoaderAdapter;
import net.sf.groovyMonkey.DOMDescriptor;
import net.sf.groovyMonkey.MonkeyClassLoader;
import net.sf.groovyMonkey.ScriptMetadata;
import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.osgi.framework.Bundle;

public class MonkeyScript
implements IMonkeyScript
{
    private final Map< String, Object > binding = new LinkedHashMap< String, Object >();
    private MonkeyClassLoader loader = null;
    protected ScriptMetadata metadata;
    protected final String languageName;
    protected final String fileNameExtension;
    protected final IFile scriptFile;
    protected final boolean stripMetadata;
    protected final Map< String, Object > map = new HashMap< String, Object >();
    private String toString;

    public MonkeyScript( final String languageName,
                         final String fileNameExtension,
                         final IFile scriptFile,
                         final boolean stripMetadata )
    {
        this.loader = doPrivileged( new PrivilegedAction< MonkeyClassLoader >()
        {
            public MonkeyClassLoader run()
            {
                return new MonkeyClassLoader();
            }
        } );
        noNullElements( new Object[] { languageName, fileNameExtension, scriptFile } );
        this.languageName = languageName;
        this.scriptFile = scriptFile;
        this.fileNameExtension = fileNameExtension;
        this.stripMetadata = stripMetadata;
        setToString();
    }
    private void setToString()
    {
        toString = MonkeyScript.class.getSimpleName() + "<" + languageName + ", " + scriptFile.getName() + ">";
    }
    @Override
    public String toString()
    {
        return toString;
    }
    public IFile getScript()
    {
        return this.scriptFile;
    }
    public IFile script()
    {
        return getScript();
    }
    public IMonkeyScript setBinding( final ScriptMetadata metadata,
                                     final Map< String, Object > map )
    {
        notNull( metadata );
        this.metadata = metadata;
        this.map.clear();
        if( map != null )
        {
            for( final Entry< String, Object > entry : map.entrySet() )
            {
                String resolvedName = entry.getKey();
                for( final DOMDescriptor descriptor : metadata.getDOMs() )
                {
                    if( descriptor.map.containsKey( entry.getKey() ) )
                        resolvedName = descriptor.map.get( entry.getKey() );
                }
                this.map.put( resolvedName, entry.getValue() );
            }
        }
        setBinding();
        return this;
    }
    private void setBinding()
    {
        binding.clear();
        final Map< String, Object > vars = getExtensionGlobalVariables( this.metadata );
        vars.putAll( map );
        binding.putAll( vars );
        loader = new MonkeyClassLoader( getClass().getClassLoader() );
        for( final Object object : binding.values() )
            if( object != null && object.getClass().getClassLoader() != null )
                loader.add( object.getClass().getClassLoader() );
        addIncludes( loader, metadata );
        return;
    }
    public static void addIncludes( final MonkeyClassLoader loader,
                                    final ScriptMetadata metadata )
    {
        final List< URL > includes = getIncludes( metadata );
        if( includes.size() > 0 )
        {
            loader.add( doPrivileged( new PrivilegedAction< URLClassLoader > ()
            {
                public URLClassLoader run()
                {
                    return new URLClassLoader( includes.toArray( new URL[ 0 ] ), loader );
                }
            } ) );
        }
        final List< Bundle > includedBundles = getIncludedBundles( metadata );
        if( includedBundles.size() > 0 )
        {
            loader.add( doPrivileged( new PrivilegedAction< BundleClassLoaderAdapter > ()
            {
                public BundleClassLoaderAdapter run()
                {
                    return new BundleClassLoaderAdapter( loader, includedBundles.toArray( new Bundle[ 0 ] ) );
                }
            } ) );
        }
    }
    public static List< URL > getIncludes( final ScriptMetadata metadata  )
    {
        final List< URL > includes = new ArrayList< URL >();
        for( final String include : metadata.getIncludes() )
        {
            final IFile ifile = getWorkspace().getRoot().getFile(new Path(include));
            try
            {
                final URI uri = ifile.getRawLocationURI();
                final File file = new File( uri );
                final URL url = file.isDirectory() ? new URL( addSlash( uri.toString() ) ): uri.toURL();
                includes.add( url );
            }
            catch( final MalformedURLException e )
            {
                throw new RuntimeException( e );
            }
        }
        return includes;
    }
    public static List< Bundle > getIncludedBundles( final ScriptMetadata metadata )
    {
        return getBundles( metadata.getIncludedBundles().toArray( new String[ 0 ] ) );
    }
    public static List< Bundle > getBundles( final String... bundleIDs )
    {
        final List< Bundle > bundles = new ArrayList< Bundle >();
        for( final String bundleID : bundleIDs )
        {
            for( final Bundle bundle : context().getBundles() )
            {
                final String bundleName = bundle.getSymbolicName();
                if( bundleName.trim().equals( bundleID.trim() ) )
                {
                    bundles.add( bundle );
                    break;
                }
            }
        }
        return bundles;
    }
    public static String addSlash( final String string )
    {
        return removeEnd(  string, "/" ) + "/";
    }
    public static String workspaceURI()
    {
        final String workspaceURI = getWorkspace().getRoot().getLocationURI().toString();
        return removeEnd( workspaceURI, "/" );
    }
    public Object run()
    throws BSFException
    {
        notNull( metadata );
        final ClassLoader oldLoader = currentThread().getContextClassLoader();
        currentThread().setContextClassLoader( loader );
        try
        {
            final BSFManager manager = new BSFManager();
            manager.setClassLoader( loader );
            manager.loadScriptingEngine( languageName );
            for( final String varName : binding.keySet() )
            {
                // bsf binding provided by BSF Engine implementation
                if( varName.equals( "bsf" ) )
                    continue;
                // metadata binding is provided by this object and not
                //  by DOM Factory.
                if( varName.equals( "metadata" ) )
                {
                    manager.declareBean( varName, metadata, ScriptMetadata.class );
                    continue;
                }
                if( binding.get( varName ) == null )
                    continue;
                manager.declareBean( varName, binding.get( varName ), binding.get( varName ).getClass() );
            }
            final String script = stripMetadata ? stripMetadata( getContents( scriptFile ) ) : getContents( scriptFile );
            final String scriptName = substringBeforeLast( scriptFile.getName(), "." ) + "." + fileNameExtension;
            return manager.apply( languageName, scriptName, 1, 1, script, new Vector< Object >(), new Vector< Object >() );
        }
        catch( final CoreException e )
        {
            throw new BSFException( REASON_OTHER_ERROR, e.getMessage(), e );
        }
        catch( final IOException e )
        {
            throw new BSFException( REASON_IO_ERROR, e.getMessage(), e );
        }
        finally
        {
            currentThread().setContextClassLoader( oldLoader );
        }
    }
}

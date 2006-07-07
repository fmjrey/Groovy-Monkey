package net.sf.groovyMonkey;
import static net.sf.groovyMonkey.GroovyMonkeyPlugin.FILE_EXTENSION;
import static net.sf.groovyMonkey.GroovyMonkeyPlugin.PLUGIN_ID;
import static org.apache.commons.lang.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.join;
import static org.apache.commons.lang.StringUtils.split;
import static org.apache.commons.lang.StringUtils.strip;
import static org.apache.commons.lang.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang.builder.HashCodeBuilder.reflectionHashCode;
import static org.eclipse.ui.PlatformUI.getWorkbench;
import static org.eclipse.update.search.UpdateSearchRequest.createDefaultSiteSearchCategory;
import static org.eclipse.update.ui.UpdateManagerUI.openInstaller;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.update.search.UpdateSearchRequest;
import org.eclipse.update.search.UpdateSearchScope;
import org.eclipse.update.ui.UpdateJob;
import org.osgi.framework.Bundle;

public class ScriptMetadata 
{
    public static final DOMDescriptor DEFAULT_DOM = new DOMDescriptor( "http://groovy-monkey.sourceforge.net/update/plugins", PLUGIN_ID );
    public enum JobModes
    {
        Job, UIJob, WorkspaceJob
    }
    public enum ExecModes
    {
        Background, Foreground
    }
    public static final String DEFAULT_LANG = "Groovy";
    public static final JobModes DEFAULT_JOB = JobModes.Job;
    public static final ExecModes DEFAULT_MODE = ExecModes.Background;
	private IFile file;
	private String menuName = "";
    private String kudos = "";
    private String license = "EPL 1.0";
	private String lang = DEFAULT_LANG;
	private final List< DOMDescriptor > doms = new ArrayList< DOMDescriptor >();
	private final List< Subscription > subscriptions = new ArrayList< Subscription >();
    private final Set< String > includes = new LinkedHashSet< String >();
    private final Set< String > includedBundles = new LinkedHashSet< String >();
    private JobModes jobMode = DEFAULT_JOB;
    private ExecModes execMode = DEFAULT_MODE; 
    
    public void setJobMode( final String jobMode )
    {
        for( final JobModes mode : JobModes.values() )
        {
            if( equalsIgnoreCase( mode.toString(), jobMode.trim() ) )
            {
                this.jobMode = mode;
                break;
            }
        }
    }
    public JobModes getJobMode()
    {
        return jobMode;
    }
    public void setExecMode( final String execMode )
    {
        for( final ExecModes mode : ExecModes.values() )
        {
            if( equalsIgnoreCase( mode.toString(), execMode.trim() ) )
            {
                this.execMode = mode;
                break;
            }
        }
    }
    public ExecModes getExecMode()
    {
        return execMode;
    }
    public boolean isBackground()
    {
        return execMode.equals( ExecModes.Background );
    }
    public boolean isForeground()
    {
        return execMode.equals( ExecModes.Foreground );
    }
    public void addInclude( final String include )
    {
        this.includes.add( include );
    }
	public Set< String > getIncludes()
    {
        return includes;
    }
    public void addIncludedBundle( final String bundleID )
    {
        this.includedBundles.add( bundleID );
    }
    public Set< String > getIncludedBundles()
    {
        return includedBundles;
    }
    public void setMenuName(String string) {
		this.menuName = string;
	}

	public void setFile(IFile file) {
		this.file = file;
	}

	public IFile getFile() {
		return file;
	}

	public String getMenuName() {
		return menuName;
	}

    public void addDOM( final DOMDescriptor dom )
    {
        doms.add( dom );
    }
    
	public List< DOMDescriptor > getDOMs() {
		return doms;
	}

	public String getReasonableFilename()
    {
        if( file != null )
            return file.getName();
        if( isNotBlank( menuName ) )
        {
            String result = menuName;
            result = result.replaceAll( " ", "_" );
            final Pattern illegalChars = Pattern.compile( "[^\\p{Alnum}_-]" );
            final Matcher match = illegalChars.matcher( result );
            result = match.replaceAll( "" );
            if( !result.equals( "" ) )
                return result + FILE_EXTENSION;
        }
        return "script" + FILE_EXTENSION;
    }
    public boolean containsDOM_by_plugin( final String pluginID )
    {
        for( final DOMDescriptor dom : doms )
        {
            if( StringUtils.equals( dom.plugin_name, pluginID ) )
                return true;
        }
        return false;
    }

	public boolean ensure_doms_are_loaded(IWorkbenchWindow window) {
		String missing_plugin_names = "";
		URLtoPluginMap missing_urls = new URLtoPluginMap();
		for (Iterator iter = doms.iterator(); iter.hasNext();) {
			DOMDescriptor element = (DOMDescriptor) iter.next();
			Bundle b = Platform.getBundle(element.plugin_name);
			if (b == null) {
				missing_plugin_names += "     " + element.plugin_name + "\n";
				missing_urls.add(element);
			} else if (b.getState() == Bundle.UNINSTALLED) {
				missing_plugin_names += "     " + element.plugin_name + "\n";
			}
		}
		if (missing_plugin_names.length() > 0) {
			missing_plugin_names = missing_plugin_names.substring(0,
					missing_plugin_names.length() - 1);
			String choice = notifyMissingDOMs(missing_plugin_names);
			if (choice.startsWith("Install")) {
				launchUpdateInstaller(missing_urls);
			}
			if (choice.startsWith("Edit")) {
				openEditor();
			}
			return false;
		}
		return true;
	}
	@Override
    public boolean equals( final Object obj )
    {
	    return reflectionEquals( this, obj );
    }
    @Override
    public int hashCode()
    {
        return reflectionHashCode( this );
    }

    class URLtoPluginMap
    {
        final Map< String, Set< String > > map = new HashMap< String, Set< String > >();

        String getPluginNames( String url )
        {
            Set ids = ( Set )map.get( url );
            String idstr = "";
            for( Iterator iterator = ids.iterator(); iterator.hasNext(); )
            {
                String id = ( String )iterator.next();
                idstr += id + ", ";
            }
            idstr = idstr.substring( 0, idstr.length() - 2 );
            return idstr;
        }
        void add( DOMDescriptor domdesc )
        {
            Set< String > ids = map.get( domdesc.url );
            if( ids == null )
                ids = new HashSet< String >();
            ids.add( domdesc.plugin_name );
            map.put( domdesc.url, ids );
        }
    }

	private void openEditor() {
		try {
			IWorkbenchPage page = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage();
			IEditorDescriptor desc = PlatformUI.getWorkbench()
					.getEditorRegistry().getDefaultEditor(file.getName());
			if (desc == null) {
				desc = PlatformUI.getWorkbench().getEditorRegistry()
						.getDefaultEditor("foo.txt");
			}
			page.openEditor(new FileEditorInput(file), desc.getId());
		} catch (PartInitException x) {
			MessageDialog.openError(null, "Eclipse::PartInitException",
					"Unable to open editor on " + file.getName() + " due to "
							+ x.toString());
		}
	}
	private void launchUpdateInstaller( final URLtoPluginMap missingUrls ) 
    {
		final UpdateSearchScope scope = new UpdateSearchScope();
        for( final String url : missingUrls.map.keySet() )
        {
            final String id = missingUrls.getPluginNames( url );
            final String plural = id.indexOf( "," ) >= 0 ? "s" : "";
            final String description = "Site providing DOM" + plural + " ( " + id + " )";
			try 
            {
				scope.addSearchSite( description, new URL( url ), new String[ 0 ] );
			} 
            catch( final MalformedURLException x ) {}
		}
		final UpdateSearchRequest request = new UpdateSearchRequest( createDefaultSiteSearchCategory(), scope );
		final UpdateJob job = new UpdateJob( "Install Eclipse Monkey DOMs", request );
        final Shell shell = getWorkbench().getActiveWorkbenchWindow().getShell();
		openInstaller( shell, job );
	}

	private String notifyMissingDOMs(String missing_plugin_names) {
		String plural = (missing_plugin_names.indexOf("\n") >= 0 ? "s" : "");
		String[] choices = new String[] { "Cancel Script", "Edit Script",
				"Install Plug-in" + plural };
		MessageDialog dialog = new MessageDialog(null, "Missing DOM" + plural,
				null, "The script "
						+ this.file.getName()
						+ " requires "
						+ (missing_plugin_names.indexOf("\n") >= 0 ? "these"
								: "this") + " missing DOM plug-in" + plural
						+ ":\n" + missing_plugin_names, MessageDialog.WARNING,
				choices, 2);
		int result = dialog.open();
		String choice = choices[result];
		return choice;
	}
    public String toHeader()
    {
        final StringBuffer buffer = new StringBuffer();
        buffer.append( "/*" ).append( "\n" );
        buffer.append( " * Menu: " + getMenuName() ).append( "\n" );
        buffer.append( " * Kudos: " + getKudos() ).append( "\n" );
        buffer.append( " * License: " + getLicense() ).append( "\n" );
        if( !getLang().equals( DEFAULT_LANG ) )
            buffer.append( " * LANG: " + getLang() ).append( "\n" );
        if( !getJobMode().equals( DEFAULT_JOB ) )
            buffer.append( " * Job: " + getJobMode() ).append( "\n" );
        if( !getExecMode().equals( DEFAULT_MODE ) )
            buffer.append( " * Exec-Mode: " + getExecMode() ).append( "\n" );
        for( final DOMDescriptor dom : getDOMs() )
        {
            if( dom.equals( DEFAULT_DOM ) )
                continue;
            buffer.append( " * DOM: " + dom ).append( "\n" );
        }
        for( final String include : getIncludes() )
            buffer.append( " * Include: " + include ).append( "\n" );
        for( final String include : getIncludedBundles() )
            buffer.append( " * Include-Bundle: " + include ).append( "\n" );
        buffer.append( "*/" ).append( "\n" );
        buffer.append( "\n" );
        return buffer.toString();
    }
	public static ScriptMetadata getScriptMetadata( final String contents ) 
    {
		final ScriptMetadata metadata = new ScriptMetadata();
        metadata.addDOM( DEFAULT_DOM );
        Pattern pattern = Pattern.compile( "^\\s*\\/\\*.*?\\*\\/", Pattern.DOTALL );
        Matcher matcher = pattern.matcher( contents );
        if( !matcher.find() )
            return metadata; // no meta-data comment - do nothing

        final String comment = matcher.group();
        pattern = Pattern.compile( "Menu:\\s*((\\p{Graph}| )+)", Pattern.DOTALL );
        matcher = pattern.matcher( comment );
        if( matcher.find() )
            metadata.setMenuName( matcher.group( 1 ) );
        
        pattern = Pattern.compile( "Kudos:\\s*((\\p{Graph}| )+)", Pattern.DOTALL );
        matcher = pattern.matcher( comment );
        if( matcher.find() )
            metadata.setKudos( matcher.group( 1 ) );
        
        pattern = Pattern.compile( "License:\\s*((\\p{Graph}| )+)", Pattern.DOTALL );
        matcher = pattern.matcher( comment );
        if( matcher.find() )
            metadata.setLicense( matcher.group( 1 ) );
        
        pattern = Pattern.compile( "DOM:\\s*(\\p{Graph}+)\\/((\\p{Alnum}|\\.)+)", Pattern.DOTALL );
        matcher = pattern.matcher( comment );
        while( matcher.find() )
            metadata.addDOM( new DOMDescriptor( matcher.group( 1 ), matcher.group( 2 ) ) );
        
        pattern = Pattern.compile( "Listener:", Pattern.DOTALL );
        matcher = pattern.matcher( comment );
        while( matcher.find() )
            metadata.subscriptions.add( new Subscription( "workspace", "addResourceChangeListener" ) );
        
        pattern = Pattern.compile( "LANG:\\s*((\\p{Graph}| )+)", Pattern.DOTALL );
        matcher = pattern.matcher( comment );
        if( matcher.find() )
            metadata.setLang( matcher.group( 1 ) );
        
        pattern = Pattern.compile( "Include:\\s*((\\p{Graph}| )+)", Pattern.DOTALL );
        matcher = pattern.matcher( comment );
        if( matcher.find() )
            metadata.addInclude( matcher.group( 1 ) );

        pattern = Pattern.compile( "Include-Bundle:\\s*((\\p{Graph}| )+)", Pattern.DOTALL );
        matcher = pattern.matcher( comment );
        if( matcher.find() )
            metadata.addIncludedBundle( matcher.group( 1 ) );
        
        pattern = Pattern.compile( "Job:\\s*((\\p{Graph}| )+)", Pattern.DOTALL );
        matcher = pattern.matcher( comment );
        if( matcher.find() )
            metadata.setJobMode( matcher.group( 1 ) );
        
        pattern = Pattern.compile( "Exec-Mode:\\s*((\\p{Graph}| )+)", Pattern.DOTALL );
        matcher = pattern.matcher( comment );
        if( matcher.find() )
            metadata.setExecMode( matcher.group( 1 ) );
        
        return metadata;
	}
    public static List< String > getMetadataLines( final String contents )
    {
        final String[] lines = split( contents, "\r\n" );
        final List< String > code = new ArrayList< String >();
        boolean started = false;
        boolean finished = false;
        for( final String line : lines )
        {
            if( strip( line ).startsWith( "/*" ) )
            {
                started = true;
                code.add( line );
                continue;
            }
            if( strip( line ).endsWith( "*/" ) )
            {
                finished = true;
                code.add( line );
                continue;
            }
            if( started && !finished )
            {
                code.add( line );
                continue;
            }
        }
        return code;
    }
	public static String stripMetadata( final String contents )
    {
	    final String[] lines = split( contents, "\r\n" );
        final List< String > code = new ArrayList< String >();
        boolean started = false;
        boolean finished = false;
        for( final String line : lines )
        {
            if( strip( line ).startsWith( "/*" ) )
            {
                started = true;
                continue;
            }
            if( strip( line ).endsWith( "*/" ) )
            {
                finished = true;
                continue;
            }
            if( started && !finished )
                continue;
            code.add( line );
        }
        return join( code.toArray( new String[ 0 ] ), "\n" );
    }
	public void subscribe() {
		for (int i = 0; i < subscriptions.size(); i++) {
			Subscription subscription = (Subscription) subscriptions.get(i);
			subscription.subscribe();
		}
	}

	public void unsubscribe() {
		for (int i = 0; i < subscriptions.size(); i++) {
			Subscription subscription = (Subscription) subscriptions.get(i);
			subscription.unsubscribe();
		}
	}
	public String getLang() 
    {
		return lang;
	}
	public void setLang( final String lang ) 
    {
		this.lang = isNotBlank( lang ) ? lang : DEFAULT_LANG;
	}
    public String getKudos()
    {
        return kudos;
    }
    public void setKudos( String kudos )
    {
        this.kudos = kudos;
    }
    public String getLicense()
    {
        return license;
    }
    public void setLicense( String license )
    {
        this.license = license;
    }
}

class Subscription {

	private final String addMethodName;
	private Object listenerProxy;
	private Method removeMethod;
	private Object source;

	public Subscription(String source, String addMethodName) {
		this.addMethodName = addMethodName;
	}

	public void subscribe() {
		source = ResourcesPlugin.getWorkspace();
		try {
			subscribe(source, addMethodName);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}

	}

	public void unsubscribe() {
		try {
			removeMethod.invoke(source, new Object[] {listenerProxy});
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	private void subscribe(Object foo, String methodName)
			throws InstantiationException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {
		InvocationHandler listener = new GenericListener();
		Method addMethod = findAddMethod(foo, methodName);
		// TODO what if null is returned?

		Class listenerType = addMethod.getParameterTypes()[0];
		listenerProxy = Proxy.newProxyInstance(listenerType
						.getClassLoader(), new Class[] { listenerType }, listener);
		addMethod.invoke(foo, new Object[] { listenerProxy });
	}

	private Method findAddMethod(Object source, String methodName) {
		Method methods[] = source.getClass().getMethods();
		for (int i = 0; i < methods.length; i++) {
			Method m = methods[i];
			if (!(m.getName().equals(methodName)))
				continue;
			if (m.getParameterTypes().length != 1)
				continue;
			if (findRemoveMethod(source, methodName, m.getParameterTypes()) == null)
				continue;

			return m;
		}
		return null;
	}

	private Method findRemoveMethod(Object source, String methodName,
			Class[] parameterTypes) {
		String removeMethodName = "remove" + methodName.substring(3);
		try {
			removeMethod = source.getClass()
					.getMethod(removeMethodName, parameterTypes);
			return removeMethod;
		} catch (Exception e) {
			return null;
		}
	}

	class GenericListener implements InvocationHandler {

		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
			System.out.println(method.getName() + " did it " + args[0]);
			return null;
		}

	}
}

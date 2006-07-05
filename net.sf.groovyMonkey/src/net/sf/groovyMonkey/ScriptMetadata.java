/*******************************************************************************
 * Copyright (c) 2005 Eclipse Foundation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bjorn Freeman-Benson - initial implementation
 *     Ward Cunningham - initial implementation
 *******************************************************************************/

package net.sf.groovyMonkey;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.join;
import static org.apache.commons.lang.StringUtils.split;
import static org.apache.commons.lang.StringUtils.strip;
import static org.apache.commons.lang.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang.builder.HashCodeBuilder.reflectionHashCode;
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
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.update.search.UpdateSearchRequest;
import org.eclipse.update.search.UpdateSearchScope;
import org.eclipse.update.ui.UpdateJob;
import org.eclipse.update.ui.UpdateManagerUI;
import org.osgi.framework.Bundle;

public class ScriptMetadata 
{
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
	private String menuName;
	private String scopeName;
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
            if( mode.toString().equalsIgnoreCase( jobMode.trim() ) )
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
            if( mode.toString().equalsIgnoreCase( execMode.trim() ) )
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

	public String getScopeName() {
		return scopeName;
	}

	public void setScopeName(String s) {
		scopeName = s;
	}

    public void addDOM( final DOMDescriptor dom )
    {
        doms.add( dom );
    }
    
	public List< DOMDescriptor > getDOMs() {
		return doms;
	}

	public String getReasonableFilename() {
		if (file != null)
			return file.getName();
		if (menuName != null && !menuName.equals("")) {
			String result = menuName;
			result = result.replaceAll(" ", "_");
			Pattern illegalChars = Pattern.compile("[^\\p{Alnum}_-]");
			Matcher match = illegalChars.matcher(result);
			result = match.replaceAll("");
			if (!result.equals(""))
				return result + ".em";
		}
		return "script.em";
	}

	public boolean containsDOM_by_plugin(String plugin_id) {
		for (Iterator iter = doms.iterator(); iter.hasNext();) {
			DOMDescriptor element = (DOMDescriptor) iter.next();
			if (element.plugin_name.equals(plugin_id))
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

    class URLtoPluginMap {
		Map< String, Set< String > > map = new HashMap< String, Set< String > >();

		Iterator iterator() {
			return map.keySet().iterator();
		}

		String getPluginNames(String url) {
			Set ids = (Set) map.get(url);
			String idstr = "";
			for (Iterator iterator = ids.iterator(); iterator.hasNext();) {
				String id = (String) iterator.next();
				idstr += id + ", ";
			}
			idstr = idstr.substring(0, idstr.length() - 2);
			return idstr;
		}

		void add(DOMDescriptor domdesc) {
			Set< String > ids = map.get(domdesc.url);
			if (ids == null)
				ids = new HashSet< String >();
			ids.add(domdesc.plugin_name);
			map.put(domdesc.url, ids);
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

	private void launchUpdateInstaller(URLtoPluginMap missing_urls) {
		UpdateSearchScope scope = new UpdateSearchScope();
		String[] skips = {};
		for (Iterator iter = missing_urls.iterator(); iter.hasNext();) {
			String url = (String) iter.next();
			try {
				String idstr = missing_urls.getPluginNames(url);
				String plural2 = idstr.indexOf(",") >= 0 ? "s" : "";
				scope.addSearchSite("Site providing DOM" + plural2 + " ("
						+ idstr + ")", new URL(url), skips);
			} catch (MalformedURLException x) {
				// ignore
			}
		}
		UpdateSearchRequest request = new UpdateSearchRequest(
				UpdateSearchRequest.createDefaultSiteSearchCategory(), scope);
		UpdateJob job = new UpdateJob("Install Eclipse Monkey DOMs", request);
		Shell shell = Workbench.getInstance().getWorkbenchWindows()[0]
				.getShell();
		UpdateManagerUI.openInstaller(shell, job);
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
	public static ScriptMetadata getScriptMetadata( final String contents ) 
    {
		final ScriptMetadata metadata = new ScriptMetadata();
        metadata.addDOM( new DOMDescriptor( "http://groovy-monkey.sourceforge.net/update/plugins", GroovyMonkeyPlugin.PLUGIN_ID ) );
        Pattern pattern = Pattern.compile( "^\\s*\\/\\*.*?\\*\\/", Pattern.DOTALL );
        Matcher matcher = pattern.matcher( contents );
        if( !matcher.find() )
            return metadata; // no meta-data comment - do nothing

        final String comment = matcher.group();
        pattern = Pattern.compile( "Menu:\\s*((\\p{Graph}| )+)", Pattern.DOTALL );
        matcher = pattern.matcher( comment );
        if( matcher.find() )
            metadata.setMenuName( matcher.group( 1 ) );
        
        pattern = Pattern.compile( "Scope:\\s*((\\p{Graph}| )+)", Pattern.DOTALL );
        matcher = pattern.matcher( comment );
        if( matcher.find() )
            metadata.setScopeName( matcher.group( 1 ) );
        
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
        while( matcher.find() )
            metadata.addInclude( matcher.group( 1 ) );

        pattern = Pattern.compile( "Include-Bundle:\\s*((\\p{Graph}| )+)", Pattern.DOTALL );
        matcher = pattern.matcher( comment );
        while( matcher.find() )
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

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
import static java.lang.Thread.currentThread;
import static net.sf.groovyMonkey.EclipseMonkeyPlugin.scopeStore;
import static net.sf.groovyMonkey.EclipseMonkeyPlugin.scriptStore;
import static net.sf.groovyMonkey.UpdateMonkeyActionsResourceChangeListener.createTheMonkeyMenu;
import static net.sf.groovyMonkey.dom.Utilities.SCRIPT_NAME;
import static net.sf.groovyMonkey.dom.Utilities.getFileContents;
import static net.sf.groovyMonkey.dom.Utilities.key;
import static net.sf.groovyMonkey.dom.Utilities.state;
import static net.sf.groovyMonkey.lang.MonkeyScript.addIncludes;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.eclipse.core.runtime.Platform.getExtensionRegistry;
import static org.mozilla.javascript.Context.javaToJS;
import static org.mozilla.javascript.ScriptableObject.putProperty;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import net.sf.groovyMonkey.ScriptMetadata.ExecModes;
import net.sf.groovyMonkey.dom.IMonkeyDOMFactory;
import net.sf.groovyMonkey.dom.Utilities;
import net.sf.groovyMonkey.lang.CompilationException;
import net.sf.groovyMonkey.lang.IMonkeyScriptFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.progress.UIJob;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.WrappedException;

public class RunMonkeyScript 
{
    public static StoredScript last_run = null;
	IWorkbenchWindow window;
	IFile file;
	ClassLoader old_classloader;
	MonkeyClassLoader classloader;
    private final boolean throwError;    
    private volatile Map< String, Object > map = Collections.synchronizedMap( new HashMap< String, Object >() );
    private StoredScript storedScript = null;
    
	public RunMonkeyScript( final IFile file, 
                            final IWorkbenchWindow window ) 
    {
	    this( file, window, false );
	}
    public RunMonkeyScript( final IFile file, 
                            final IWorkbenchWindow window, 
                            final boolean throwError ) 
    {
        this( file, window, null, throwError );
    }
    public RunMonkeyScript( final IFile file, 
                            final IWorkbenchWindow window,
                            final Map< String, Object > map,
                            final boolean throwError ) 
    {
        this.window = window;
        this.file = file;
        this.throwError = throwError;
        this.map = map != null ? map : this.map;
    }
	public void run() 
    {
        final String fileName = key( file );
        storedScript = ( StoredScript )scriptStore().get( fileName );
        if( storedScript.metadata.getExecMode() == ExecModes.UIJob )
        {
            runUIJob();
            return;
        }
        if( storedScript.metadata.getExecMode() == ExecModes.WorkspaceJob )
        {
            runWorkspaceJob();
            return;
        }
        runJob();
	}
    private void runUIJob()
    {
        final UIJob job = new UIJob( "Script: " + file.getName() )
        {
            @Override
            public IStatus runInUIThread( final IProgressMonitor monitor )
            {
                runScript( monitor );
                monitor.worked( 1 );
                monitor.done();
                return Status.OK_STATUS;
            }
        };
        exec( job );
    }
    private void runWorkspaceJob()
    {
        final WorkspaceJob job = new WorkspaceJob( "Script: " + file.getName() )
        {
            @Override
            public IStatus runInWorkspace( final IProgressMonitor monitor )
            throws CoreException
            {
                runScript( monitor );
                monitor.worked( 1 );
                monitor.done();
                return Status.OK_STATUS;
            }
        };
        exec( job );
    }
    private void runJob()
    {
        final Job job = new Job( "Script: " + file.getName() )
        {
            @Override
            public IStatus run( final IProgressMonitor monitor )
            {
                runScript( monitor );
                monitor.worked( 1 );
                monitor.done();
                return Status.OK_STATUS;
            }
        };
        exec( job );
    }
    private void exec( final Job job )
    {
        job.setUser( true );
        job.schedule();
        join( job );
    }
    private void join( final Job job )
    {
        // There is no use joining a job from within the same UI Thread
        if( job instanceof UIJob && Display.getCurrent() != null )
            return;
        try
        {
            job.join();
        }
        catch( final InterruptedException e ) {}
    }
    public void runScript( final IProgressMonitor progressMonitor )
    {
        final IProgressMonitor monitor = progressMonitor != null ? progressMonitor : new NullProgressMonitor();
        monitor.beginTask( file.getName(), 1 );
        this.map.put( "monitor", new SubProgressMonitor( monitor, 1, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK ) );
        try
        {
            if( storedScript == null )
            {
                final String fileName = key( file );
                storedScript = ( StoredScript )scriptStore().get( fileName );
            }
            if( isBlank( storedScript.metadata.getLang() ) || storedScript.metadata.getLang().equalsIgnoreCase( "javascript" ) )
                runJavaScript();
            else
                runOtherScript();
        }
        catch( final Throwable x )
        {
            error( x, x.getMessage() );
        }
        finally
        {
            monitor.done();
        }
    }
    private void runJavaScript()
    {
		try {
			Context cx = Context.enter();

			Scriptable sharedScope = null;
			if (!storedScript.metadata.ensure_doms_are_loaded(window))
				return;

			String sharedScopeName = storedScript.metadata.getScopeName();
			if (sharedScopeName != null) {
				final Map< String, Scriptable > scopeStore = scopeStore();
				sharedScope = (Scriptable) scopeStore.get(sharedScopeName);
				if (sharedScope == null) {
					sharedScope = cx.initStandardObjects();
					scopeStore.put(sharedScopeName, sharedScope);
				}
			}
			defineDynamicVariables(file);
			defineClassLoader( storedScript.metadata );
			try {
				boolean needs_compiling = storedScript.compiledScope == null;
				if (needs_compiling) {
					storedScript.compiledScope = cx.initStandardObjects();
					if (sharedScope != null)
						storedScript.compiledScope.setParentScope(sharedScope);
				}
				defineStandardGlobalVariables(storedScript.compiledScope );
				defineExtensionGlobalVariables(storedScript.compiledScope,
						storedScript.metadata);

				if (needs_compiling) {
					String contents = getFileContents(file);
					storedScript.compiledScript = cx.compileString(contents,
							key( file ), 1, null);
					storedScript.compiledScript.exec(cx,
							storedScript.compiledScope);
				}

				Object fObj = storedScript.compiledScope.get("main",
						storedScript.compiledScope);
				if (!(fObj instanceof Function)) {
					throw new EvaluatorException(
							"function main() is not defined in ", key( file ), 0,
							"", 0);
				} else {
					Object functionArgs[] = {};
					Function f = (Function) fObj;
					f.call(cx, storedScript.compiledScope,
							storedScript.compiledScope, functionArgs);
				}

			} finally {
				undefineClassLoader();
				undefineDynamicVariables(file);
			}
		} catch (WrappedException x) {
			error(x, x.getWrappedException().toString());
		} catch (EvaluatorException x) {
			error(x, x.lineSource() + "\n" + x.details());
		} catch (RhinoException x) {
			error(x, x.details());
		} catch (IOException x) {
			error(x, x.toString());
		} catch (CoreException x) {
			error(x, x.toString());
		} finally {
			Context.exit();
			last_run = storedScript;
			createTheMonkeyMenu();
		}
    }
    private void runOtherScript()
    {
        try
        {
            defineDynamicVariables( file );
            if( !storedScript.metadata.ensure_doms_are_loaded( window ) )
                return;
            final String scriptLang = storedScript.metadata.getLang();
            final Map< String, IMonkeyScriptFactory > factories = getFactories();
            boolean found = false;
            for( final String language : factories.keySet() )
            {
                final IMonkeyScriptFactory factory = factories.get( language );
                if( !factory.isLang( scriptLang ) )
                    continue;
                found = true;
                factory.runner( storedScript.metadata, map ).run();
            }
            if( !found )
                error( "No factory for language: " + scriptLang,
                       scriptLang + " not found. Available are: " + factories.keySet(),
                       new Exception() );
        }
        catch( final CompilationException x )
        {
            error( x, x.getMessage() );
        }
        finally
        {
            last_run = storedScript;
            createTheMonkeyMenu();
            undefineDynamicVariables( file );
        }
    }
    
	private void defineDynamicVariables(IFile file) {
		state().begin(file);
		state().set(SCRIPT_NAME,file.getFullPath().toPortableString());
	}

	private void undefineDynamicVariables(IFile file) {
		state().end(file);
	}

	private void defineStandardGlobalVariables(Scriptable scope) {
		Object wrappedWindow = javaToJS(window, scope);
		putProperty(scope, "window", wrappedWindow);
		// Object wrappedWorkspace = Context.javaToJS(ResourcesPlugin
		// .getWorkspace(), scope);
		// ScriptableObject.putProperty(scope, "workspace", wrappedWorkspace);
	}

	private void defineExtensionGlobalVariables(Scriptable scope,
			ScriptMetadata metadata) {
		IExtensionRegistry registry = getExtensionRegistry();
		IExtensionPoint point = registry
				.getExtensionPoint("net.sf.groovyMonkey.dom");
		if (point != null) {
			IExtension[] extensions = point.getExtensions();
			for (int i = 0; i < extensions.length; i++) {
				IExtension extension = extensions[i];
				IConfigurationElement[] configurations = extension
						.getConfigurationElements();
				for (int j = 0; j < configurations.length; j++) {
					IConfigurationElement element = configurations[j];
					try {
						IExtension declaring = element.getDeclaringExtension();
                        String declaring_plugin_id = declaring.getContributor().getName();
						if (metadata.containsDOM_by_plugin(declaring_plugin_id)) {
							String variableName = element
									.getAttribute("variableName");
							Object object = element
									.createExecutableExtension("class");
							IMonkeyDOMFactory factory = (IMonkeyDOMFactory) object;
							Object rootObject = factory.getDOMroot();
							ClassLoader rootLoader = rootObject.getClass().getClassLoader();
							classloader.add(rootLoader);
							Object wrappedRoot = javaToJS(rootObject,
									scope);
							ScriptableObject.putProperty(scope, variableName,
									wrappedRoot);
						}
					} catch (InvalidRegistryObjectException x) {
						// ignore bad extensions
					} catch (CoreException x) {
						// ignore bad extensions
					}
				}
			}
		}
	}
	
    public static Map< String, IMonkeyScriptFactory > getFactories()
    {
        final Map< String, IMonkeyScriptFactory > factories = new HashMap< String, IMonkeyScriptFactory >();
        final IExtensionRegistry registry = getExtensionRegistry();
        final IExtensionPoint point = registry.getExtensionPoint( "net.sf.groovyMonkey.lang" );
        if( point == null )
            return factories;
        final IExtension[] extensions = point.getExtensions();
        for( final IExtension extension : extensions )
        {
            for( final IConfigurationElement element : extension.getConfigurationElements() )
            {
                final String languageName = element.getAttribute( "name" );
                if( factories.containsKey( languageName ) )
                    // Ignoring duplicate extensions.
                    continue;
                try
                {
                    final IMonkeyScriptFactory factory = ( IMonkeyScriptFactory )element.createExecutableExtension( "factory" );
                    factories.put( languageName, factory );
                }
                catch( final CoreException e )
                {
                    // Ignoring bad extensions
                    continue;
                }   
            }
        }
        return factories;
    }
    
	private void error(RhinoException x, String string) {
		error( x.getClass().getName(), string + "\n" + x.sourceName() + " #" + x.lineNumber(), x );
	}

	private void error(Throwable x, String string) {
        error( x.getClass().getName(), string, x.getCause() != null ? x.getCause() : x );
	}
	private void error( final String title, 
                        final String message,
                        final Throwable exception )
    {
        if( throwError )
            throw new RuntimeException( title + ". " + message, exception );
        Utilities.error( title, message, exception );
    }
	private void defineClassLoader( final ScriptMetadata metadata ) 
    {
		classloader = new MonkeyClassLoader( RunMonkeyScript.class.getClassLoader() );
        addIncludes( classloader, metadata );
		old_classloader = currentThread().getContextClassLoader();
		classloader.add(old_classloader);
		currentThread().setContextClassLoader(classloader);
	}
	private void undefineClassLoader() 
    {
		currentThread().setContextClassLoader(old_classloader);
	}
}

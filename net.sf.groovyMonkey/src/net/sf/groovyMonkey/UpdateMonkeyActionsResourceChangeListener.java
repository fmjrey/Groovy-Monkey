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
import static net.sf.groovyMonkey.GroovyMonkeyPlugin.addScript;
import static net.sf.groovyMonkey.GroovyMonkeyPlugin.clearScripts;
import static net.sf.groovyMonkey.GroovyMonkeyPlugin.removeScript;
import static net.sf.groovyMonkey.RunMonkeyScript.getScriptFactories;
import static net.sf.groovyMonkey.ScriptMetadata.getScriptMetadata;
import static net.sf.groovyMonkey.dom.Utilities.contents;
import static net.sf.groovyMonkey.dom.Utilities.isMonkeyScript;
import static org.eclipse.core.resources.IResourceDelta.ADDED;
import static org.eclipse.core.resources.IResourceDelta.CHANGED;
import static org.eclipse.core.resources.IResourceDelta.CONTENT;
import static org.eclipse.core.resources.IResourceDelta.MOVED_FROM;
import static org.eclipse.core.resources.IResourceDelta.MOVED_TO;
import static org.eclipse.core.resources.IResourceDelta.REMOVED;
import static org.eclipse.core.resources.IResourceDelta.REPLACED;
import static org.eclipse.core.resources.ResourcesPlugin.getWorkspace;
import static org.eclipse.ui.PlatformUI.getWorkbench;
import java.io.IOException;
import net.sf.groovyMonkey.actions.RecreateMonkeyMenuAction;
import net.sf.groovyMonkey.lang.IMonkeyScriptFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IWorkbenchWindow;

public class UpdateMonkeyActionsResourceChangeListener 
implements IResourceChangeListener
{
    public void resourceChanged( final IResourceChangeEvent event )
    {
        final Boolean changes[] = new Boolean[ 1 ];
        changes[ 0 ] = new Boolean( false );
        final IResourceDeltaVisitor visitor = new IResourceDeltaVisitor()
        {
            private void foundAChange()
            {
                changes[ 0 ] = new Boolean( true );
            }
            public boolean visit( final IResourceDelta delta )
            {
                if( !( delta.getResource() instanceof IFile ) )
                    return true;
                final String fullPath = delta.getFullPath().toString();
                if( isMonkeyScript( fullPath ) )
                {
                    final IFile file = ( IFile )delta.getResource();
                    switch( delta.getKind() )
                    {
                        case ADDED:
                            processNewOrChangedScript( fullPath, file );
                            foundAChange();
                            break;
                        case REMOVED:
                            processRemovedScript( fullPath, file );
                            foundAChange();
                            break;
                        case CHANGED:
                            if( ( delta.getFlags() & MOVED_FROM ) != 0 )
                            {
                                processRemovedScript( delta.getMovedFromPath().toString(), file );
                                processNewOrChangedScript( fullPath, file );
                                foundAChange();
                            }
                            if( ( delta.getFlags() & MOVED_TO ) != 0 )
                            {
                                processRemovedScript( fullPath, file );
                                processNewOrChangedScript( delta.getMovedToPath().toString(), file );
                                foundAChange();
                            }
                            if( ( delta.getFlags() & REPLACED ) != 0 )
                            {
                                processNewOrChangedScript( fullPath, file );
                                foundAChange();
                            }
                            if( ( delta.getFlags() & CONTENT ) != 0 )
                            {
                                processNewOrChangedScript( fullPath, file );
                                foundAChange();
                            }
                            break;
                    }
                }
                return true;
            }
        };
        try
        {
            event.getDelta().accept( visitor );
        }
        catch( final CoreException x )
        {
            // log an error in the error log
        }
        final boolean anyMatches = ( changes[ 0 ] ).booleanValue();
        if( anyMatches )
            createTheMonkeyMenu();
    }
    private void processNewOrChangedScript( final String name, 
                                            final IFile file )
    {
        ScriptMetadata metadata;
        try
        {
            metadata = getMetadataFrom( file );
        }
        catch( final CoreException x )
        {
            metadata = new ScriptMetadata();
            metadata.setFile( file );
            // log an error in the error log
        }
        catch( final IOException x )
        {
            metadata = new ScriptMetadata();
            metadata.setFile( file );
            // log an error in the error log
        }
        addScript( name, metadata );
        for( final IMonkeyScriptFactory factory : getScriptFactories().values() )
            factory.changed( file );
    }
    private void processRemovedScript( final String name, final IFile file )
    {
        removeScript( name );
        for( final IMonkeyScriptFactory factory : getScriptFactories().values() )
            factory.changed( file );
    }
    public void rescanAllFiles()
    {
        clearScripts();
        for( final IMonkeyScriptFactory factory : getScriptFactories().values() )
            factory.clearCachedScripts();
        final IWorkspace workspace = getWorkspace();
        for( final IProject project : workspace.getRoot().getProjects() )
        {
            final IResourceVisitor visitor = new IResourceVisitor()
            {
                public boolean visit( final IResource resource ) 
                throws CoreException
                {
                    if( !( resource instanceof IFile ) )
                        return true;
                    final IFile file = ( IFile )resource;
                    if( isMonkeyScript( file ) )
                        processNewOrChangedScript( file.getFullPath().toString(), file );
                    return true;
                }
                
            };
            try
            {
                project.accept( visitor );
            }
            catch( final CoreException x )
            {
                // ignore folders we cannot access
            }
        }
    }
    private ScriptMetadata getMetadataFrom( final IFile file ) 
    throws CoreException, IOException
    {
        final String contents = contents( file );
        final ScriptMetadata metadata = getScriptMetadata( contents );
        metadata.setFile( file );
        return metadata;
    }
    public static void createTheMonkeyMenu()
    {
        final IWorkbenchWindow[] windows = getWorkbench().getWorkbenchWindows();
        for( final IWorkbenchWindow window : windows )
        {
            window.getShell().getDisplay().asyncExec( new Runnable()
            {
                public void run()
                {
                    final RecreateMonkeyMenuAction action = new RecreateMonkeyMenuAction();
                    action.init( window );
                    action.run( null );
                }
            } );
        }
    }
}

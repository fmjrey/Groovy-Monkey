/*******************************************************************************
 * Copyright (c) 2006 Eclipse Foundation
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
import static java.util.regex.Pattern.DOTALL;
import static java.util.regex.Pattern.compile;
import static net.sf.groovyMonkey.dom.Utilities.activeWindow;
import static net.sf.groovyMonkey.dom.Utilities.error;
import static org.apache.commons.lang.StringUtils.defaultString;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.eclipse.core.resources.ResourcesPlugin.getWorkspace;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;

public class Subscription
{
    private final ScriptMetadata metadata;
    private final String filter;
    private final IWorkspace source;
    private IResourceChangeListener listener;

    public Subscription( final ScriptMetadata metadata,
                         final String filter )
    {
        this.metadata = metadata;
        this.filter = defaultString( filter );
        this.source = getWorkspace();
    }
    public String getFilter()
    {
        return filter;
    }
    public void subscribe()
    {
        this.listener = new ResourceChangeListener( metadata, filter );
        source.addResourceChangeListener( listener );
    }
    public void unsubscribe()
    {
        source.removeResourceChangeListener( listener );
    }
    class ResourceChangeListener
    implements IResourceChangeListener
    {
        private final ScriptMetadata metadata;
        private final String filter;
        
        public ResourceChangeListener( final ScriptMetadata metadata,
                                final String filter )
        {
            this.metadata = metadata;
            this.filter = filter;
        }
        public boolean checkFilter( final IResourceChangeEvent event )
        {
            if( isBlank( filter ) )
                return true;
            final Pattern pattern = compile( filter, DOTALL );
            if( event.getResource() != null )
            {
                final IResource resource = event.getResource();
                final Matcher matcher = pattern.matcher( resource.getFullPath().toOSString() );
                if( matcher.find() )
                    return true;
            }
            if( event.getDelta() != null )
            {
                final Boolean[] found = { false };
                final IResourceDeltaVisitor visitor = new IResourceDeltaVisitor()
                {
                    public boolean visit( final IResourceDelta delta ) 
                    throws CoreException
                    {
                        if( found[ 0 ] )
                            return false;
                        if( !( delta.getResource() instanceof IFile ) )
                            return true;
                        final Matcher matcher = pattern.matcher( delta.getFullPath().toOSString() );
                        found[ 0 ] = matcher.find();
                        return true;
                    }
                };
                try
                {
                    event.getDelta().accept( visitor );
                }
                catch( final CoreException e )
                {
                    error( "Error filtering ResourceChangeEvent", "" + event, e );
                }
                return found[ 0 ];
            }
            return true;
        }
        public void resourceChanged( final IResourceChangeEvent event )
        {
            if( event == null )
                return;
            if( !checkFilter( event ) )
                return;
            final Map< String, Object > map = new HashMap< String, Object >();
            map.put( "event", event );
            final RunMonkeyScript script = new RunMonkeyScript( metadata.getFile(), activeWindow(), map, true );
            script.run();
        }
    }
}

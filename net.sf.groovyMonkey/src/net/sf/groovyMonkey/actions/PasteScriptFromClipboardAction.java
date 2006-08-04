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
package net.sf.groovyMonkey.actions;
import static java.util.regex.Pattern.DOTALL;
import static java.util.regex.Pattern.compile;
import static net.sf.groovyMonkey.GroovyMonkeyPlugin.EXAMPLES_PROJECT;
import static net.sf.groovyMonkey.GroovyMonkeyPlugin.FILE_EXTENSION;
import static net.sf.groovyMonkey.GroovyMonkeyPlugin.MONKEY_DIR;
import static net.sf.groovyMonkey.GroovyMonkeyPlugin.PUBLISH_AFTER_MARKER;
import static net.sf.groovyMonkey.GroovyMonkeyPlugin.PUBLISH_BEFORE_MARKER;
import static net.sf.groovyMonkey.ScriptMetadata.getScriptMetadata;
import static org.eclipse.core.resources.ResourcesPlugin.getWorkspace;
import static org.eclipse.jface.dialogs.MessageDialog.openInformation;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sf.groovyMonkey.ScriptMetadata;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.RTFTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.views.navigator.ResourceNavigator;

public class PasteScriptFromClipboardAction 
implements IWorkbenchWindowActionDelegate, IObjectActionDelegate
{
    class TextAndRTF
    {
        String text;
        String rtf;
    }
    private IStructuredSelection selection = null;
    private Shell shell;
    private IWorkbenchWindow window;
    
    public PasteScriptFromClipboardAction() {}

    public void run( final IAction action )
    {
        final TextAndRTF text = getTextFromClipboard();
        final Collection< String > scripts = extractScriptsFromText( text );
        for( final String script : scripts )
            try
            {
                final String scriptText = collapseEscapedNewlines( script );
                final IFolder destination = findDestinationFor( scriptText );
                final IFile file = createScriptFile( destination, scriptText );
                highlightNewScriptInNavigator( file );
            }
            catch( final CoreException x )
            {
                openInformation( shell, "Groovy Monkey", "Unable to create the Examples project due to " + x );
            }
            catch( final IOException x )
            {
                openInformation( shell, "Groovy Monkey", "Unable to create the Examples project due to " + x );
            }
        if( scripts.isEmpty() )
            openInformation( shell, "Groovy Monkey", "Can't find any scripts on clipboard - make sure you include the Jabberwocky-inspired markers at the beginning and ending of the script" );
    }
    public String collapseEscapedNewlines( final String input )
    {
        final Pattern pattern = compile( "\\\\(\n|(\r\n?))" );
        final Matcher match = pattern.matcher( input );
        final String result = match.replaceAll( "" );
        return result;
    }
    private void highlightNewScriptInNavigator( final IFile file ) 
    throws PartInitException
    {
        if( window == null )
            return;
        window.getActivePage().showView( "org.eclipse.ui.views.ResourceNavigator" );
        final IViewReference[] refs = window.getActivePage().getViewReferences();
        for( final IViewReference reference : refs )
        {
            if( reference.getId().equals( "org.eclipse.ui.views.ResourceNavigator" ) )
            {
                final ResourceNavigator nav = ( ResourceNavigator )reference.getView( true );
                final IStructuredSelection sel = new StructuredSelection( file );
                nav.selectReveal( sel );
            }
        }
    }
    private IFolder findDestinationFor( final String script ) 
    throws CoreException
    {
        if( selection != null && selection.getFirstElement() instanceof IFolder )
        {
            final IFolder element = ( IFolder )selection.getFirstElement();
            if( element.getName().equals( MONKEY_DIR ) )
                return element;
        }
        final IWorkspace workspace = getWorkspace();
        final IProject[] projects = workspace.getRoot().getProjects();
        IProject project = null;
        for( final IProject p : projects )
        {
            if( p.getName().equals( EXAMPLES_PROJECT ) )
            {
                project = p;
                break;
            }
        }
        if( project == null )
        {
            project = workspace.getRoot().getProject( EXAMPLES_PROJECT );
            project.create( null );
            project.open( null );
        }
        final IFolder folder = project.getFolder( MONKEY_DIR );
        if( !folder.exists() )
            folder.create( IResource.NONE, true, null );
        return folder;
    }
    private IFile createScriptFile( final IFolder destination, 
                                    final String script ) 
    throws CoreException, IOException
    {
        final ScriptMetadata metadata = getScriptMetadata( script );
        String basename = metadata.getReasonableFilename();
        final int ix = basename.lastIndexOf( "." );
        if( ix > 0 )
            basename = basename.substring( 0, ix );
        final IResource[] members = destination.members( 0 );
        final Pattern suffix = compile( basename + "(-(\\d+))?\\" + FILE_EXTENSION );
        int maxsuffix = -1;
        for( final IResource resource : members )
        {
            if( resource instanceof IFile )
            {
                final IFile file = ( IFile )resource;
                final String filename = file.getName();
                final Matcher match = suffix.matcher( filename );
                if( match.matches() )
                    if( match.group( 2 ) == null )
                        maxsuffix = Math.max( maxsuffix, 0 );
                    else
                    {
                        final int n = Integer.parseInt( match.group( 2 ) );
                        maxsuffix = Math.max( maxsuffix, n );
                    }
            }
        }
        final String filename = maxsuffix == -1 ? basename + FILE_EXTENSION : basename + "-" + ( maxsuffix + 1 ) + FILE_EXTENSION;
        final ByteArrayInputStream stream = new ByteArrayInputStream( script.getBytes() );
        final IFile file = destination.getFile( filename );
        file.create( stream, false, null );
        stream.close();
        return file;
    }
    private TextAndRTF getTextFromClipboard()
    {
        final TextAndRTF result = new TextAndRTF();
        final Clipboard clipboard = new Clipboard( shell.getDisplay() );
        try
        {
            final TextTransfer textTransfer = TextTransfer.getInstance();
            final RTFTransfer rtfTransfer = RTFTransfer.getInstance();
            result.text = ( String )clipboard.getContents( textTransfer );
            result.rtf = ( String )clipboard.getContents( rtfTransfer );
            return result;
        }
        finally
        {
            clipboard.dispose();
        }
    }
    private List< String > extractScriptsFromText( final TextAndRTF text )
    {
        final List< String > result = new ArrayList< String >();
        final Pattern pattern = Pattern.compile( PUBLISH_BEFORE_MARKER + "\\s*(.*?)\\s*" + PUBLISH_AFTER_MARKER, DOTALL );
        final Pattern crpattern = Pattern.compile( "\r\n?" );
        if( text.text != null )
        {
            final Matcher matcher = pattern.matcher( text.text );
            while( matcher.find() )
            {
                String string = matcher.group( 1 );
                final Matcher crmatch = crpattern.matcher( string );
                string = crmatch.replaceAll( "\n" );
                if( string.indexOf( "\n" ) >= 0 )
                    result.add( string );
            }
        }
        if( result.isEmpty() && text.rtf != null )
        {
            final Matcher matcher = pattern.matcher( text.rtf );
            final Pattern escapesPattern = compile( "\\\\(.)" );
            while( matcher.find() )
            {
                String string = matcher.group( 1 );
                string = string.replaceAll( "\\\\line", "\n" );
                final Matcher escapes = escapesPattern.matcher( string );
                string = escapes.replaceAll( "$1" );
                final Matcher crmatch = crpattern.matcher( string );
                string = crmatch.replaceAll( "\n" );
                if( string.indexOf( "\n" ) >= 0 )
                    result.add( string );
            }
        }
        return result;
    }
    public void selectionChanged( final IAction action, 
                                  final ISelection selection )
    {
        this.selection = ( IStructuredSelection )selection;
    }
    public void dispose() {}
    public void init( final IWorkbenchWindow window )
    {
        shell = window.getShell();
        this.window = window;
    }
    public void setActivePart( final IAction action, 
                               final IWorkbenchPart targetPart )
    {
        shell = targetPart.getSite().getShell();
        window = null;
    }
}

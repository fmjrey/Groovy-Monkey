/*******************************************************************************
 * Copyright (c) 2005, 2006 Eclipse Foundation
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
import static java.lang.Math.max;
import static java.util.regex.Pattern.DOTALL;
import static java.util.regex.Pattern.compile;
import static net.sf.groovyMonkey.GroovyMonkeyPlugin.FILE_EXTENSION;
import static net.sf.groovyMonkey.GroovyMonkeyPlugin.MONKEY_DIR;
import static net.sf.groovyMonkey.GroovyMonkeyPlugin.PUBLISH_AFTER_MARKER;
import static net.sf.groovyMonkey.GroovyMonkeyPlugin.PUBLISH_BEFORE_MARKER;
import static net.sf.groovyMonkey.GroovyMonkeyPlugin.SCRIPTS_PROJECT;
import static net.sf.groovyMonkey.ScriptMetadata.getScriptMetadata;
import static net.sf.groovyMonkey.dom.Utilities.closeEditor;
import static net.sf.groovyMonkey.dom.Utilities.createFolder;
import static net.sf.groovyMonkey.dom.Utilities.openEditor;
import static net.sf.groovyMonkey.dom.Utilities.shell;
import static net.sf.groovyMonkey.util.ListUtil.array;
import static org.apache.commons.lang.ArrayUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.removeStart;
import static org.apache.commons.lang.StringUtils.substringAfter;
import static org.apache.commons.lang.StringUtils.substringAfterLast;
import static org.apache.commons.lang.StringUtils.substringBeforeLast;
import static org.eclipse.core.resources.IResource.NONE;
import static org.eclipse.core.resources.ResourcesPlugin.getWorkspace;
import static org.eclipse.jface.dialogs.MessageDialog.openInformation;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sf.groovyMonkey.ScriptMetadata;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
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
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
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
        {
            try
            {
                final String scriptText = collapseEscapedNewlines( script );
                final ScriptMetadata metadata = getScriptMetadata( scriptText );
                final IFolder destination = getDestinationFor( metadata );
                final IFile file = createScriptFile( destination, metadata, scriptText );
                highlightNewScriptInNavigator( file );
                openEditor( file );
            }
            catch( final CoreException x )
            {
                openInformation( shell, "Groovy Monkey", "Unable to create the " + SCRIPTS_PROJECT  + " project or create Monkey script due to " + x );
            }
            catch( final IOException x )
            {
                openInformation( shell, "Groovy Monkey", "Unable to create the " + SCRIPTS_PROJECT + " project or create a Monkey script due to " + x );
            }
        }
        if( scripts.isEmpty() )
            openInformation( shell, "Groovy Monkey", "Can't find any scripts on clipboard - make sure you include the Jabberwocky-inspired markers at the beginning and ending of the script" );
    }
    public static String collapseEscapedNewlines( final String input )
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
    private IProject getProject( final String fullPathString )
    throws CoreException
    {
        final String pathString = removeStart( fullPathString, "/" );
        final String path = pathString.indexOf( "/" ) != -1 ? pathString.substring( 0, pathString.indexOf( "/" ) ) : pathString;
        return getProject( getWorkspace().getRoot().getProject( path ) );
    }
    private IProject getProject( final IResource resource )
    throws CoreException
    {
        final IProject project = resource.getProject();
        if( !project.exists() )
            project.create( null );
        if( !project.isOpen() )
            project.open( null );
        return project;
    }
    /**
     * This method will potentially modify the scriptPath and file attributes of the passed metadata object.
     * @param metadata
     * @return
     * @throws CoreException
     */
    private IFolder getDestinationFor( final ScriptMetadata metadata )
    throws CoreException
    {
    	final IFolder folder = findDestinationFor( metadata );
    	final IFile file = folder.getFile( substringAfterLast( metadata.scriptPath(), "/" ) );
    	final SelectScriptPathDialog dialog = new SelectScriptPathDialog( shell(), new WorkbenchLabelProvider(), new WorkbenchContentProvider(), file );
    	dialog.setInitialSelection( folder );
    	dialog.setInput( getWorkspace().getRoot() );
        final int returnCode = dialog.open();
        if( returnCode != Window.OK )
            return folder;
        final Object[] results = dialog.getResult();
        if( isEmpty( results ) )
        	return folder;
        final IFile result = ( IFile )results[ 0 ];
        metadata.setScriptPath( result.getFullPath().toString() );
        metadata.setFile( result );
    	return getFolderForPath( result.getFullPath().toString() );
    }
    private IFolder findDestinationFor( final ScriptMetadata metadata )
    throws CoreException
    {
        if( selection != null && selection.getFirstElement() instanceof IFolder )
        {
            final IFolder folder = ( IFolder )selection.getFirstElement();
            getProject( folder );
            if( !folder.exists() )
                folder.create( NONE, true, null );
            return folder;
        }
        if( isNotBlank( metadata.scriptPath() ) )
            return getFolderForPath( metadata.scriptPath() );
        final IProject project = getProject( getWorkspace().getRoot().getProject( SCRIPTS_PROJECT ) );
        final IFolder folder = project.getFolder( MONKEY_DIR );
        if( !folder.exists() )
            folder.create( IResource.NONE, true, null );
        return folder;
    }

	private IFolder getFolderForPath( final String scriptPath )
	throws CoreException
	{
		final String path = substringBeforeLast( scriptPath, "/" );
		final IProject project = getProject( path );
		final String folderPath = substringAfter( removeStart( path, "/" ), "/" );
		if( isBlank( folderPath ) )
		    return ( IFolder )project.getAdapter( IFolder.class );
		return project.getFolder( folderPath );
	}
    private IFile createScriptFile( final IFolder destination,
                                    final ScriptMetadata metadata,
                                    final String script )
    throws CoreException, IOException
    {
        final String defaultName = substringAfterLast( metadata.scriptPath(), "/" );
        String basename = defaultName;
        final int ix = basename.lastIndexOf( "." );
        if( ix > 0 )
            basename = basename.substring( 0, ix );
        createFolder( destination );
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
                {
                    if( file.exists() && file.getName().equals( defaultName ) )
                    {
                        final MessageDialog dialog = new MessageDialog( shell(), "Overwrite?", null, "Overwrite existing script: " + file.getName() + " ?", MessageDialog.WARNING, new String[] { "Yes", "No" }, 0 );
                        if( dialog.open() == 0 )
                        {
                            file.delete( true, null );
                            closeEditor( file );
                            maxsuffix = -1;
                            basename = substringBeforeLast( file.getName(), "." );
                            break;
                        }
                    }
                    if( match.group( 2 ) == null )
                        maxsuffix = max( maxsuffix, 0 );
                    else
                    {
                        final int n = Integer.parseInt( match.group( 2 ) );
                        maxsuffix = max( maxsuffix, n );
                    }
                }
            }
        }
        final String filename = maxsuffix == -1 ? basename + FILE_EXTENSION : basename + "-" + ( maxsuffix + 1 ) + FILE_EXTENSION;
        final IFile file = destination.getFile( filename );
        final ByteArrayInputStream stream = new ByteArrayInputStream( script.getBytes() );
        file.create( stream, true, null );
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
        final List< String > result = array();
        final Pattern pattern = compile( PUBLISH_BEFORE_MARKER + "\\s*(.*?)\\s*" + PUBLISH_AFTER_MARKER, DOTALL );
        final Pattern crpattern = compile( "\r\n?" );
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

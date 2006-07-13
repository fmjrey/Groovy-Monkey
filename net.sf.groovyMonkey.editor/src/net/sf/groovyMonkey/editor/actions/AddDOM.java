package net.sf.groovyMonkey.editor.actions;
import static net.sf.groovyMonkey.ScriptMetadata.getMetadataLines;
import static net.sf.groovyMonkey.ScriptMetadata.getScriptMetadata;
import static net.sf.groovyMonkey.ScriptMetadata.stripMetadata;
import static net.sf.groovyMonkey.dom.Utilities.error;
import static net.sf.groovyMonkey.dom.Utilities.getContents;
import static net.sf.groovyMonkey.dom.Utilities.getDOMPlugins;
import static net.sf.groovyMonkey.dom.Utilities.getUpdateSiteForDOMPlugin;
import static org.apache.commons.lang.StringUtils.join;
import static org.eclipse.core.resources.IResource.DEPTH_ONE;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.sf.groovyMonkey.ScriptMetadata;
import net.sf.groovyMonkey.editor.ScriptEditor;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

public class AddDOM 
extends Action
implements IObjectActionDelegate
{
    private ScriptEditor targetEditor = null;
    private IStructuredSelection selection = null;
    
    public AddDOM()
    {
    }
    public AddDOM( final ScriptEditor targetEditor )
    {
        this.targetEditor = targetEditor;
        setText( "Add DOM to Script" );
        setToolTipText( "Add existing DOM to Monkey Script" );
    }
    public void run( final IAction action )
    {
        run();
    }
    @Override
    public void run()
    {
        final IFile script = getTargetScript();
        if( script == null )
            return;
        try
        {
            final IEditorPart editor = getEditor( script );
            if( editor.isDirty() )
            {
                final SaveEditorDialog dialog = new SaveEditorDialog( getShell(), script );
                final int returnCode = dialog.open();
                if( returnCode != Window.OK )
                    return;
                saveChangesInEditor( editor );
            }
            final Set< String > selectedDOMPlugins = openSelectDOMsDialog( getUnusedDOMs( script ) );
            if( selectedDOMPlugins.size() == 0 )
                return;
            addDOMsToScript( script, selectedDOMPlugins );
        }
        catch( final CoreException e )
        {
            error( "IO Error", "Error getting the contents of: " + script.getName() + ". " + e, e );
        }
        catch( final IOException e )
        {
            error( "IO Error", "Error getting the contents of: " + script.getName() + ". " + e, e );
        }
    }
    private void addDOMsToScript( final IFile script, 
                                  final Set< String > selectedDOMPlugins ) 
    throws CoreException, IOException
    {
        final List< String > metadata = getMetadataLines( getContents( script ) );
        for( final String selectedDOMPlugin : selectedDOMPlugins )
            metadata.add( metadata.size() - 1, " * DOM: " + getUpdateSiteForDOMPlugin( selectedDOMPlugin ) );
        final String contents = join( metadata.toArray( new String[ 0 ] ), "\n" ) + "\n" + stripMetadata( getContents( script ) );
        script.setContents( new ByteArrayInputStream( contents.getBytes() ), true, false, null );
        script.refreshLocal( DEPTH_ONE, null );
    }
    private Set< String > openSelectDOMsDialog( final Set< String > availableDOMPlugins )
    {
        if( availableDOMPlugins == null || availableDOMPlugins.size() == 0 )
            return new LinkedHashSet< String >();
        final AddDOMDialog dialog = new AddDOMDialog( getShell(), availableDOMPlugins );
        final int returnCode = dialog.open();
        if( returnCode != Window.OK )
            return new LinkedHashSet< String >();
        return dialog.selectedDOMPlugins();
    }
    private Set< String > getUnusedDOMs( final IFile script ) 
    throws CoreException, IOException
    {
        final ScriptMetadata data = getScriptMetadata( getContents( script ) );
        final Set< String > availableDOMPlugins = getDOMPlugins();
        for( final Iterator< String > iterator = availableDOMPlugins.iterator(); iterator.hasNext(); )
        {
            final String pluginID = iterator.next();
            if( data.containsDOMByPlugin( pluginID ) )
                iterator.remove();
        }
        return availableDOMPlugins;
    }
    private void saveChangesInEditor( final IEditorPart editor )
    {
        final boolean[] done = new boolean[ 1 ];
        final boolean[] cancelled = new boolean[ 1 ];
        final IProgressMonitor monitor = new NullProgressMonitor()
        {
            @Override
            public void done()
            {
                done[ 0 ] = true;
            }
            @Override
            public void setCanceled( final boolean cancel )
            {
                cancelled[ 0 ] = cancel;
            }
        };
        editor.doSave( monitor );
        while( !done[ 0 ] && !cancelled[ 0 ] )
        {
            try
            {
                Thread.sleep( 500 );
            }
            catch( final InterruptedException e ) {}
        }
    }
    private Shell getShell()
    {
        return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
    }
    private IEditorPart getEditor( final IFile script  )
    throws PartInitException
    {
        if( targetEditor != null )
            return targetEditor;
        return PlatformUI.getWorkbench()
                         .getActiveWorkbenchWindow()
                         .getActivePage()
                         .openEditor( new FileEditorInput( script ), "net.sf.groovyMonkey.editor.ScriptEditor" );
    }
    private IFile getTargetScript()
    {
        if( targetEditor != null )
            return ( IFile )targetEditor.getAdapter( IFile.class );
        if( selection != null )
        {
            final Object selected = selection.getFirstElement();
            if( !( selected instanceof IFile ) )
                return null;
            return ( IFile )selected;
        }
        return null;
    }
    public void selectionChanged( final IAction action, 
                                  final ISelection selection )
    {
        if( !( selection instanceof IStructuredSelection ) )
            return;
        this.selection = ( IStructuredSelection )selection;
    }
    public void setActivePart( final IAction action, 
                               final IWorkbenchPart targetPart )
    {
    }
}

package net.sf.groovyMonkey.editor.actions;
import static net.sf.groovyMonkey.GroovyMonkeyPlugin.PLUGIN_ID;
import static net.sf.groovyMonkey.ScriptMetadata.getScriptMetadata;
import static net.sf.groovyMonkey.ScriptMetadata.refreshScriptMetadata;
import static net.sf.groovyMonkey.dom.Utilities.activePage;
import static net.sf.groovyMonkey.dom.Utilities.error;
import static net.sf.groovyMonkey.dom.Utilities.shell;
import static org.eclipse.core.resources.ResourcesPlugin.getWorkspace;
import static org.eclipse.core.runtime.IStatus.ERROR;
import static org.eclipse.core.runtime.IStatus.OK;
import static org.eclipse.jdt.core.JavaCore.NATURE_ID;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;
import net.sf.groovyMonkey.ScriptMetadata;
import net.sf.groovyMonkey.editor.ScriptEditor;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.part.FileEditorInput;

public class AddInclude 
extends Action
implements IObjectActionDelegate
{
    private ScriptEditor targetEditor = null;
    private IStructuredSelection selection = null;
    
    public AddInclude()
    {
    }
    public AddInclude( final ScriptEditor targetEditor )
    {
        this.targetEditor = targetEditor;
        setText( "Add Include to Script" );
        setToolTipText( "Add Jars/Folders to Script Classpath" );
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
                final SaveEditorDialog dialog = new SaveEditorDialog( shell(), script );
                final int returnCode = dialog.open();
                if( returnCode != Window.OK )
                    return;
                saveChangesInEditor( editor );
            }
            final Set< String > selected = openSelectDialog( getIncludes( script ) );
            if( selected.size() == 0 )
                return;
            addIncludesToScript( script, selected );
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
    private void addIncludesToScript( final IFile script, 
                                      final Set< String > includes ) 
    throws CoreException, IOException
    {
        final ScriptMetadata metadata = getScriptMetadata( script );
        for( final String include : includes )
            metadata.addInclude( include );
        refreshScriptMetadata( script, metadata );
    }
    private Set< String > openSelectDialog( final Set< String > alreadyIncluded )
    {
        final Set< String > included = alreadyIncluded != null ? alreadyIncluded : new TreeSet< String >();
        final ISelectionStatusValidator validator = new ISelectionStatusValidator()
        {
            public IStatus validate( final Object[] selection )
            {
                if( selection == null || selection.length == 0 )
                    return new Status( OK, PLUGIN_ID, 0, "Nothing selected", null );
                for( final Object selected : selection )
                {
                    if( selected instanceof IFile )
                    {
                        final IFile file = ( IFile )selected;
                        if( !file.getName().toLowerCase().endsWith( ".jar" ) )
                            return new Status( ERROR, PLUGIN_ID, 0, "Error file: " + file.getFullPath() + " does not have a Jar extension" , null );
                        continue;
                    }
                    if( selected instanceof IFolder )
                    {
                        final IFolder folder = ( IFolder )selected;
                        try
                        {
                            if( !folder.getProject().hasNature( NATURE_ID ) )
                                return new Status( ERROR, PLUGIN_ID, 0, "Error folder: " + folder.getFullPath() + " is not in a Java Project", null );
                            final IJavaProject project = JavaCore.create( folder.getProject() );
                            if( project.getOutputLocation().equals( folder.getFullPath() ) )
                                continue;
                            boolean ok = false;
                            for( final IClasspathEntry entry : project.getResolvedClasspath( false ) )
                            {
                                if( !folder.getFullPath().equals( entry.getOutputLocation() ) 
                                    && !folder.getFullPath().equals( entry.getPath() ) )
                                    continue;
                                ok = true;
                                break;
                            }
                            if( !ok )
                                return new Status( ERROR, PLUGIN_ID, 0, "Error folder: " + folder.getFullPath() + " is not in the Java Project's classpath", null );
                        }
                        catch( final CoreException e )
                        {
                            return new Status( ERROR, PLUGIN_ID, 0, "Error folder: " + folder.getFullPath() + " is not in an open/existing project? " + e, e );                            
                        }
                        continue;
                    }
                    return new Status( ERROR, PLUGIN_ID, 0, "Error selected: " + selected + " is not an IFile or IFolder", null );
                }
                return new Status( OK, PLUGIN_ID, 0, "", null );
            }   
        };
        final ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog( shell(), new WorkbenchLabelProvider(), new WorkbenchContentProvider() );
        dialog.setTitle( "Add Folders/Jars to Script Classpath" );
        dialog.setInput( getWorkspace().getRoot() );
        dialog.setValidator( validator );
        final int returnCode = dialog.open();
        if( returnCode != Window.OK )
            return new TreeSet< String >();
        final Set< String > set = new TreeSet< String >();
        for( final Object include : dialog.getResult() )
        {
            if( included.contains( "" + (( IResource )include).getFullPath() ) )
                continue;
            set.add( "" + (( IResource )include).getFullPath() );
        }
        return set;
    }
    private Set< String > getIncludes( final IFile script ) 
    throws CoreException, IOException
    {
        return getScriptMetadata( script ).getIncludes();
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
    private IEditorPart getEditor( final IFile script )
    throws PartInitException
    {
        if( targetEditor != null )
            return targetEditor;
        return activePage().openEditor( new FileEditorInput( script ), 
                                        ScriptEditor.class.getName() );
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

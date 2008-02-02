package net.sf.groovyMonkey.editor.actions;
import static net.sf.groovyMonkey.GroovyMonkeyPlugin.bundleDescription;
import static net.sf.groovyMonkey.GroovyMonkeyPlugin.context;
import static net.sf.groovyMonkey.GroovyMonkeyPlugin.icon;
import static net.sf.groovyMonkey.ScriptMetadata.getScriptMetadata;
import static net.sf.groovyMonkey.ScriptMetadata.refreshScriptMetadata;
import static net.sf.groovyMonkey.dom.Utilities.activePage;
import static net.sf.groovyMonkey.dom.Utilities.error;
import static net.sf.groovyMonkey.dom.Utilities.getAllAvailableBundles;
import static net.sf.groovyMonkey.dom.Utilities.getContents;
import static net.sf.groovyMonkey.dom.Utilities.shell;
import static net.sf.groovyMonkey.editor.ScriptContentProvider.getBundles;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import net.sf.groovyMonkey.ScriptMetadata;
import net.sf.groovyMonkey.dom.Utilities;
import net.sf.groovyMonkey.editor.ScriptEditor;
import net.sf.groovyMonkey.util.SetUtil;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.TwoPaneElementSelector;
import org.eclipse.ui.part.FileEditorInput;

public class AddBundle 
extends Action
implements IObjectActionDelegate
{
    private ScriptEditor targetEditor = null;
    private IStructuredSelection selection = null;
    
    public AddBundle()
    {
    }
    public AddBundle( final ScriptEditor targetEditor )
    {
        this.targetEditor = targetEditor;
        setText( "Add Bundle to Script" );
        setToolTipText( "Add existing Bundle to Monkey Script" );
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
            final Set< String > selectedBundles = openSelectBundlesDialog( getUnusedBundles( script ) );
            if( selectedBundles.size() == 0 )
                return;
            addBundlesToScript( script, selectedBundles );
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
    private void addBundlesToScript( final IFile script, 
                                     final Set< String > selectedBundles ) 
    throws CoreException, IOException
    {
        final ScriptMetadata metadata = getScriptMetadata( script );
        for( final String bundle : selectedBundles )
            metadata.addIncludedBundle( bundle );
        refreshScriptMetadata( script, metadata );
    }
    private Set< String > openSelectBundlesDialog( final Set< String > availablePlugins )
    {
        if( availablePlugins == null || availablePlugins.size() == 0 )
            return SetUtil.treeSet();
        
        final ILabelProvider elementRenderer = new LabelProvider()
        {
            @Override
            public Image getImage( final Object element )
            {
                final String bundleName = ObjectUtils.toString( element, "" );
                if( StringUtils.isBlank( bundleName ) )
                    return icon( "plugin_obj.gif" );
                final BundleDescription description = bundleDescription( bundleName );
                if( description != null && description.getHost() != null )
                    return icon( "frgmt_obj.gif" );
                return icon( "plugin_obj.gif" );
            }            
        };
        final ILabelProvider qualifierRenderer = new LabelProvider()
        {
            @Override
            public Image getImage( final Object element )
            {
                final String bundleName = ObjectUtils.toString( element, "" );
                if( StringUtils.isBlank( bundleName ) )
                    return icon( "plugin_obj.gif" );
                final BundleDescription description = bundleDescription( bundleName );
                if( description != null && description.getHost() != null )
                    return icon( "frgmt_obj.gif" );
                return icon( "plugin_obj.gif" );
            }
            @Override
            public String getText( final Object element )
            {
                final String bundleName = ObjectUtils.toString( element, "" );
                if( StringUtils.isBlank( bundleName ) )
                    return "<Blank>";
                final BundleDescription description = bundleDescription( bundleName );
                if( description == null )
                    return super.getText( element );
                if( description.getHost() != null )
                {
                    final BundleDescription hostDescription = bundleDescription( description.getHost().getName() );
                    try
                    {
                        return ObjectUtils.toString( description ) + " -> " + ObjectUtils.toString( hostDescription ) + " - " + FileLocator.toFileURL( context().getBundle( hostDescription.getBundleId() ).getResource( "/" ) );
                    }
                    catch( final Exception e )
                    {
                        throw new RuntimeException( e );
                    }
                }
                try
                {
                    return ObjectUtils.toString( description ) + " - " + FileLocator.toFileURL( context().getBundle( description.getBundleId() ).getResource( "/" ) );
                }
                catch( final Exception e )
                {
                    throw new RuntimeException( e );
                }
            }            
        };
        final TwoPaneElementSelector selector = new TwoPaneElementSelector( Utilities.shell(), elementRenderer, qualifierRenderer );
        selector.setTitle( "Select Bundle to add:" );
        try
        {
            selector.setElements( availablePlugins.toArray() );
        }
        catch( final Exception e )
        {
            throw new RuntimeException( e );
        }
        if( selector.open() != Window.OK )
            return SetUtil.treeSet();
        final String selectedBundle = ObjectUtils.toString( selector.getFirstResult(), "" );
        if( StringUtils.isBlank( selectedBundle ) )
            return SetUtil.treeSet();
        return SetUtil.treeSet( selectedBundle );
        
//        final AddDialog dialog = createAddBundleDialog( shell(), availablePlugins );
//        final int returnCode = dialog.open();
//        if( returnCode != Window.OK )
//            return new TreeSet< String >();
//        return dialog.selected();
    }
    private Set< String > getUnusedBundles( final IFile script ) 
    throws CoreException, IOException
    {
        final ScriptMetadata data = getScriptMetadata( getContents( script ) );
        final Set< String > installedBundles = getAllAvailableBundles();
        final Set< String > alreadyIncludedBundles = getBundles( data );
        for( final Iterator< String > iterator = installedBundles.iterator(); iterator.hasNext(); )
        {
            final String pluginID = iterator.next();
            if( alreadyIncludedBundles.contains( pluginID ) )
                iterator.remove();
        }
        return installedBundles;
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
            catch( final InterruptedException e ) 
            {
                Thread.currentThread().interrupt();
            }
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

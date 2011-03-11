package net.sf.groovyMonkey.editor.views;
import static net.sf.groovyMonkey.dom.Utilities.getDOMExtensionPoint;
import static net.sf.groovyMonkey.dom.Utilities.getDOMPlugins;
import static net.sf.groovyMonkey.dom.Utilities.getUpdateSiteForDOMPlugin;
import static net.sf.groovyMonkey.dom.Utilities.imageDescriptor;
import static org.apache.commons.lang.StringUtils.remove;
import static org.eclipse.core.runtime.Platform.getExtensionRegistry;
import static org.eclipse.jface.dialogs.MessageDialog.openInformation;
import static org.eclipse.swt.SWT.H_SCROLL;
import static org.eclipse.swt.SWT.MULTI;
import static org.eclipse.swt.SWT.V_SCROLL;
import static org.eclipse.swt.widgets.Display.getCurrent;
import static org.eclipse.swt.widgets.Display.getDefault;
import static org.eclipse.ui.ISharedImages.IMG_OBJS_INFO_TSK;
import static org.eclipse.ui.IWorkbenchActionConstants.MB_ADDITIONS;
import java.util.Set;
import java.util.TreeSet;
import net.sf.groovyMonkey.DOMDescriptor;
import net.sf.groovyMonkey.editor.ScriptContentProvider;
import net.sf.groovyMonkey.editor.ScriptLabelProvider;
import net.sf.groovyMonkey.editor.ScriptContentProvider.ClassDescriptor;
import net.sf.groovyMonkey.editor.ScriptContentProvider.FieldDescriptor;
import net.sf.groovyMonkey.editor.ScriptContentProvider.MethodDescriptor;
import org.eclipse.core.runtime.IExtensionDelta;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.IRegistryChangeListener;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;

public class DOMView
extends ViewPart
implements IRegistryChangeListener
{
    private TreeViewer viewer;
    //private DrillDownAdapter drillDownAdapter;
    private Action action1;
    private Action action2;
    private Action doubleClickAction;

    static class NameSorter
    extends ViewerSorter
    {
        @Override
        public int category( final Object element )
        {
            if( element instanceof ClassDescriptor )
                return 0;
            if( element instanceof FieldDescriptor )
                return 1;
            if( element instanceof MethodDescriptor )
                return 2;
            return super.category( element );
        }
    }

    public DOMView()
    {
    }
    /**
     * This is a callback that will allow us to create the viewer and initialize
     * it.
     */
    @Override
    public void createPartControl( final Composite parent )
    {
        viewer = new TreeViewer( parent, MULTI | H_SCROLL | V_SCROLL );
        //drillDownAdapter = new DrillDownAdapter( viewer );
        final ScriptContentProvider contentProvider = new ScriptContentProvider()
        {
            @Override
            public Object[] getElements( final Object inputElement )
            {
                final Set< String > list = getDOMPlugins();
                final Set< DOMDescriptor > doms = new TreeSet< DOMDescriptor >();
                for( final String pluginID : list )
                    doms.add( new DOMDescriptor( remove( getUpdateSiteForDOMPlugin( pluginID ), pluginID ), pluginID, null ) );
                return doms.toArray( new DOMDescriptor[ 0 ] );
            }
            @Override
            public void inputChanged( final Viewer viewer,
                                      final Object oldInput,
                                      final Object newInput )
            {
            }
        };
        viewer.setContentProvider( contentProvider );
        viewer.setLabelProvider( new ScriptLabelProvider() );
        viewer.setSorter( new NameSorter() );
        viewer.setInput( getViewSite() );
        makeActions();
        hookContextMenu();
        hookSelectionListener();
        hookDoubleClickAction();
        contributeToActionBars();
        getExtensionRegistry().addRegistryChangeListener( this );
    }
	private void hookSelectionListener()
	{
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) 
			{
				if(event.getSelection().isEmpty()) return; 
				if(event.getSelection() instanceof DOMDescriptor) 
				{
				}
			}
		});
		
	}
    private void hookContextMenu()
    {
        final MenuManager menuMgr = new MenuManager( "#PopupMenu" );
        menuMgr.setRemoveAllWhenShown( true );
        menuMgr.addMenuListener( new IMenuListener()
        {
            public void menuAboutToShow( final IMenuManager manager )
            {
                fillContextMenu( manager );
            }
        } );
        final Menu menu = menuMgr.createContextMenu( viewer.getControl() );
        viewer.getControl().setMenu( menu );
        getSite().registerContextMenu( menuMgr, viewer );
    }
    private void contributeToActionBars()
    {
        final IActionBars bars = getViewSite().getActionBars();
        fillLocalPullDown( bars.getMenuManager() );
        fillLocalToolBar( bars.getToolBarManager() );
    }
    private void fillLocalPullDown( final IMenuManager manager )
    {
        manager.add( action1 );
        manager.add( new Separator() );
        manager.add( action2 );
    }
    private void fillContextMenu( final IMenuManager manager )
    {
        manager.add( action1 );
        manager.add( action2 );
        manager.add( new Separator() );
        //drillDownAdapter.addNavigationActions( manager );
        // Other plug-ins can contribute there actions here
        manager.add( new Separator( MB_ADDITIONS ) );
    }
    private void fillLocalToolBar( final IToolBarManager manager )
    {
        manager.add( action1 );
        manager.add( action2 );
        manager.add( new Separator() );
        //drillDownAdapter.addNavigationActions( manager );
    }
    private void makeActions()
    {
        action1 = new Action()
        {
            @Override
            public void run()
            {
                showMessage( "Action 1 executed" );
            }
        };
        action1.setText( "Action 1" );
        action1.setToolTipText( "Action 1 tooltip" );
        action1.setImageDescriptor( imageDescriptor( IMG_OBJS_INFO_TSK ) );
        action2 = new Action()
        {
            @Override
            public void run()
            {
                showMessage( "Action 2 executed" );
            }
        };
        action2.setText( "Action 2" );
        action2.setToolTipText( "Action 2 tooltip" );
        action2.setImageDescriptor( imageDescriptor( IMG_OBJS_INFO_TSK ) );
        doubleClickAction = new Action()
        {
            @Override
            public void run()
            {
                final ISelection selection = viewer.getSelection();
                final Object obj = ( ( IStructuredSelection )selection ).getFirstElement();
                showMessage( "Double-click detected on " + obj.toString() );
            }
        };
    }
    private void hookDoubleClickAction()
    {
        viewer.addDoubleClickListener( new IDoubleClickListener()
        {
            public void doubleClick( final DoubleClickEvent event )
            {
                doubleClickAction.run();
            }
        } );
    }
    private void showMessage( final String message )
    {
        openInformation( viewer.getControl().getShell(), "Installed DOMs", message );
    }
    /**
     * Passing the focus request to the viewer's control.
     */
    @Override
    public void setFocus()
    {
        viewer.getControl().setFocus();
    }
    @Override
    public void dispose()
    {
        super.dispose();
        getExtensionRegistry().removeRegistryChangeListener( this );
    }
    private void updateViewer()
    {
        if( getCurrent() == null )
        {
            final Runnable runnable = new Runnable()
            {
                public void run()
                {
                    updateViewer();
                }
            };
            getDefault().syncExec( runnable );
            return;
        }
        viewer.setInput( viewer.getInput() );
    }
    public void registryChanged( final IRegistryChangeEvent event )
    {
        if( event == null || event.getExtensionDeltas() == null )
            return;
        for( final IExtensionDelta delta : event.getExtensionDeltas() )
        {
            if( !getDOMExtensionPoint().getUniqueIdentifier().equals( delta.getExtensionPoint().getUniqueIdentifier() ) )
                continue;
            updateViewer();
            break;
        }
    }
}

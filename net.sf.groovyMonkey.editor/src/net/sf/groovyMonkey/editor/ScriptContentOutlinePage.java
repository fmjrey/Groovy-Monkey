package net.sf.groovyMonkey.editor;
import static org.eclipse.core.resources.IResourceChangeEvent.POST_CHANGE;
import static org.eclipse.core.resources.ResourcesPlugin.getWorkspace;
import net.sf.groovyMonkey.editor.actions.FlatOutlineViewAction;
import net.sf.groovyMonkey.editor.actions.HierarchicalOutlineViewAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

public class ScriptContentOutlinePage 
extends ContentOutlinePage
{
    private IEditorInput editorInput = null;
    private ScriptResourceChangeListener listener;
    
    @Override
    public void createControl( final Composite parent )
    {
        super.createControl( parent );
        final TreeViewer viewer = getTreeViewer();
        final ScriptContentProvider contentProvider = new ScriptContentProvider();
        viewer.setContentProvider( contentProvider );
        final ILabelProvider labelProvider = new ScriptLabelProvider();
        viewer.setLabelProvider( labelProvider );
        if( editorInput != null )
            viewer.setInput( editorInput );
        listener = new ScriptResourceChangeListener( viewer, contentProvider );
        getWorkspace().addResourceChangeListener( listener, POST_CHANGE );
        viewer.addDoubleClickListener( new ScriptOutlineDoubleClickAction() );
        final IMenuManager manager = getSite().getActionBars().getMenuManager();
        final HierarchicalOutlineViewAction hierarchy = new HierarchicalOutlineViewAction( contentProvider, listener );
        final FlatOutlineViewAction flat = new FlatOutlineViewAction( contentProvider, listener );
        hierarchy.setOtherAction( flat );
        flat.setOtherAction( hierarchy );
        manager.add( hierarchy );
        manager.add( flat );
    }
    public void setInput( final IEditorInput editorInput )
    {
        this.editorInput = editorInput;
    }
    @Override
    public void dispose()
    {
        super.dispose();
        getWorkspace().removeResourceChangeListener( listener );        
    }
}

package net.sf.groovyMonkey.editor.actions;
import java.util.LinkedHashSet;
import java.util.Set;
import net.sf.groovyMonkey.DOMDescriptor;
import net.sf.groovyMonkey.editor.ScriptContentProvider;
import net.sf.groovyMonkey.editor.ScriptLabelProvider;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;

public class AddDOMDialog 
extends Dialog
implements ISelectionChangedListener
{
    private final Set< String > availableDOMPlugins;
    private final Set< String > selectedDOMPlugins = new LinkedHashSet< String >();
    private TableViewer table;
    private TreeViewer details;
    
    public AddDOMDialog( final Shell parentShell,
                         final Set< String > availableDOMPlugins )
    {
        super( parentShell );
        this.availableDOMPlugins = availableDOMPlugins;
        setShellStyle( getShellStyle() | SWT.RESIZE );
    }
    protected void configureShell( final Shell newShell )
    {
        super.configureShell( newShell );
        newShell.setText( "Add DOMs: " );
    }
    protected Control createDialogArea( final Composite parent )
    {
        final GridLayout layout = new GridLayout( 1, false );
        parent.setLayout( layout );
        table = new TableViewer( parent, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.SINGLE | SWT.RESIZE );
        table.setContentProvider( new ArrayContentProvider() );
        final LabelProvider labelProvider = new LabelProvider()
        {
            final ScriptLabelProvider provider = new ScriptLabelProvider();
            @Override
            public Image getImage( final Object element )
            {
                if( !( element instanceof String ) )
                    return super.getImage( element );
                return provider.getImage( new DOMDescriptor( "", "" + element ) );
            }
            
        };
        table.setLabelProvider( labelProvider );
        table.setInput( availableDOMPlugins );
        table.addSelectionChangedListener( this );
        table.getControl().setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
        details = new TreeViewer( parent, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL | SWT.RESIZE );
        details.setLabelProvider( new ScriptLabelProvider() );
        final ScriptContentProvider contentProvider = new ScriptContentProvider()
        {
            @Override
            public Object[] getElements( final Object inputElement )
            {
                return new Object[] { new DOMDescriptor( "", "" + inputElement ) };
            }
            @Override
            public void inputChanged( final Viewer viewer, 
                                      final Object oldInput, 
                                      final Object newInput )
            {
            }
        };
        details.setContentProvider( contentProvider );
        details.getControl().setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
        return parent;
    }
    protected void okPressed()
    {
        for( final TableItem item : table.getTable().getItems() )
            if( item.getChecked() )
                selectedDOMPlugins.add( item.getText() );
        super.okPressed();
    }
    public Set< String > selectedDOMPlugins()
    {
        return selectedDOMPlugins;
    }
    public void selectionChanged( final SelectionChangedEvent event )
    {
        if( !( event.getSelection() instanceof IStructuredSelection ) )
            return;
        final IStructuredSelection selection = ( IStructuredSelection )event.getSelection();
        details.setInput( selection.getFirstElement() );
    }
}

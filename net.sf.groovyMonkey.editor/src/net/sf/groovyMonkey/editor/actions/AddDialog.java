package net.sf.groovyMonkey.editor.actions;
import static net.sf.groovyMonkey.GroovyMonkeyPlugin.getDefault;
import static net.sf.groovyMonkey.dom.Utilities.getDOMPlugins;
import java.util.Set;
import java.util.TreeSet;
import net.sf.groovyMonkey.DOMDescriptor;
import net.sf.groovyMonkey.editor.ScriptContentProvider;
import net.sf.groovyMonkey.editor.ScriptLabelProvider;
import net.sf.groovyMonkey.util.SetUtil;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
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

public class AddDialog 
extends Dialog
implements ISelectionChangedListener
{
    public enum Text
    {
        BUNDLE, DOM;
        @Override
        public String toString()
        {
            return "Add " + name() + "(s): ";
        }
    };
    public static final String DIALOG_SETTINGS_SECTION = AddDialog.class.getName();
    private final Text text;
    private final String dialogSettingsSection;
    private final Set< String > available;
    private final Set< String > selected = new TreeSet< String >();
    private TableViewer table;
    private TreeViewer details;
    
    public static AddDialog createAddDOMDialog( final Shell parent, 
                                                final Set< String > available )
    {
        return new AddDialog( parent, Text.DOM, available );
    }
    public static AddDialog createAddBundleDialog( final Shell parent, 
                                                   final Set< String > available )
    {
        return new AddDialog( parent, Text.BUNDLE, available );
    }
    public AddDialog( final Shell parentShell,
                      final Text text,
                      final Set< String > available )
    {
        super( parentShell );
        this.text = text;
        dialogSettingsSection = DIALOG_SETTINGS_SECTION + "." + text.name();
        this.available = SetUtil.treeSet( available );
        setShellStyle( getShellStyle() | SWT.RESIZE );
    }
    @Override
    protected IDialogSettings getDialogBoundsSettings()
    {
        final IDialogSettings settings = getDefault().getDialogSettings();
        if( settings.getSection( dialogSettingsSection ) == null )
            settings.addNewSection( dialogSettingsSection );
        return settings.getSection( dialogSettingsSection );
    }
    @Override
    protected int getDialogBoundsStrategy()
    {
        return super.getDialogBoundsStrategy();
    }
    @Override
    protected void configureShell( final Shell newShell )
    {
        super.configureShell( newShell );
        newShell.setText( "" + text );
    }
    @Override
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
                if( !getDOMPlugins().contains( "" + element ) )
                    return provider.getImage( new ScriptContentProvider.BundleDescriptor( "" + element, null ) );
                return provider.getImage( new DOMDescriptor( "", "" + element, null ) );
            }
            
        };
        table.setLabelProvider( labelProvider );
        table.setInput( available );
        table.addSelectionChangedListener( this );
        table.getControl().setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
        details = new TreeViewer( parent, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL | SWT.RESIZE );
        details.setLabelProvider( new ScriptLabelProvider() );
        final ScriptContentProvider contentProvider = new ScriptContentProvider()
        {
            @Override
            public Object[] getElements( final Object inputElement )
            {
                return new Object[] { new DOMDescriptor( "", "" + inputElement, null ) };
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
    @Override
    protected void okPressed()
    {
        for( final TableItem item : table.getTable().getItems() )
            if( item.getChecked() )
                selected.add( item.getText() );
        super.okPressed();
    }
    public Set< String > selected()
    {
        return SetUtil.treeSet( selected );
    }
    public void selectionChanged( final SelectionChangedEvent event )
    {
        if( !( event.getSelection() instanceof IStructuredSelection ) )
            return;
        final IStructuredSelection selection = ( IStructuredSelection )event.getSelection();
        details.setInput( selection.getFirstElement() );
    }
}

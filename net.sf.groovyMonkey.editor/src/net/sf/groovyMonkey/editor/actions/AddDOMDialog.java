package net.sf.groovyMonkey.editor.actions;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

public class AddDOMDialog 
extends Dialog
{
    private final List< String > availableDOMPlugins;
    private final List< String > selectedDOMPlugins = new ArrayList< String >();
    private Table table;
    
    public AddDOMDialog( final Shell parentShell,
                         final List< String > availableDOMPlugins )
    {
        super( parentShell );
        this.availableDOMPlugins = availableDOMPlugins;
    }
    protected void configureShell( final Shell newShell )
    {
        super.configureShell( newShell );
        newShell.setText( "Add DOMs: " );
    }
    protected Control createDialogArea( final Composite parent )
    {
        final Composite composite = new Composite( parent, SWT.NONE );
        final GridLayout layout = new GridLayout( 2, false );
        composite.setLayout( layout );
        table = new Table( composite, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL );
        for( final String plugin : availableDOMPlugins )
        {
            final TableItem item = new TableItem( table, SWT.NONE );
            item.setText( plugin );
        }
        return composite;
    }
    protected void okPressed()
    {
        for( final TableItem item : table.getItems() )
            if( item.getChecked() )
                selectedDOMPlugins.add( item.getText() );
        super.okPressed();
    }
    public List< String > selectedDOMPlugins()
    {
        return selectedDOMPlugins;
    }
}

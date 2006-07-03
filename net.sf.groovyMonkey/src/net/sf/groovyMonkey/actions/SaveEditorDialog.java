package net.sf.groovyMonkey.actions;
import static org.eclipse.jface.resource.JFaceResources.TEXT_FONT;
import static org.eclipse.jface.resource.JFaceResources.getFontRegistry;
import static org.eclipse.swt.SWT.BORDER;
import static org.eclipse.swt.SWT.COLOR_WHITE;
import static org.eclipse.swt.SWT.NONE;
import static org.eclipse.swt.layout.GridData.CENTER;
import static org.eclipse.swt.layout.GridData.END;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class SaveEditorDialog 
extends Dialog
{
    private final IFile script;
    
    public SaveEditorDialog( final Shell parentShell,
                             final IFile script )
    {
        super( parentShell );
        this.script = script;
    }
    protected void configureShell( final Shell newShell )
    {
        super.configureShell( newShell );
        newShell.setText( "Save changes and continue?" );
    }
    protected Control createDialogArea( final Composite parent )
    {
        final Composite composite = new Composite( parent, NONE );
        final GridLayout layout = new GridLayout( 2, false );
        composite.setLayout( layout );

        final Label label = new Label( composite, NONE | BORDER );
        label.setBackground( getShell().getDisplay().getSystemColor( COLOR_WHITE ) );
        label.setText( "Editor has unsaved changes. \nSave changes to script: " + script.getName() + " and continue? " );
        label.setLayoutData( new GridData( END, CENTER, false, false ) );
        label.setFont( getFontRegistry().get( TEXT_FONT ) );
        return composite;
    }
}

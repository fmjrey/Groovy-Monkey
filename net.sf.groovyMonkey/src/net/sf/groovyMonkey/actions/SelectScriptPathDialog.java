package net.sf.groovyMonkey.actions;
import static net.sf.groovyMonkey.GroovyMonkeyPlugin.FILE_EXTENSION;
import static net.sf.groovyMonkey.GroovyMonkeyPlugin.MONKEY_DIR;
import static net.sf.groovyMonkey.GroovyMonkeyPlugin.PLUGIN_ID;
import static org.eclipse.core.runtime.IStatus.ERROR;
import static org.eclipse.core.runtime.IStatus.WARNING;
import static org.eclipse.swt.SWT.BORDER;
import static org.eclipse.swt.layout.GridData.FILL_BOTH;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;

public class SelectScriptPathDialog
extends ElementTreeSelectionDialog
implements ISelectionChangedListener, ModifyListener
{
	public class Validator
	implements ISelectionStatusValidator
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
		         	final IPath projectPath = file.getProjectRelativePath();
		         	if( projectPath.segment( 0 ) == null || !projectPath.segment( 0 ).equals( MONKEY_DIR ) )
						return new Status( WARNING, PLUGIN_ID, 0, "file is not under the " + MONKEY_DIR + " subdirectory of its project" , null );
		            if( !file.getName().toLowerCase().endsWith( FILE_EXTENSION ) )
		            	return new Status( WARNING, PLUGIN_ID, 0, "file does not have a " + FILE_EXTENSION + " extension" , null );
		            if( file.exists() )
		            	return new Status( WARNING, PLUGIN_ID, 0, "file exists... will overwrite...", null );
		            continue;
		        }
		        if( selected instanceof IFolder )
		        {
					// Testing to see if the folder is in a subdirectory of the project's monkey directory
					final IPath projectPath = (( IFolder )selected).getProjectRelativePath();
					if( projectPath.segment( 0 ) == null || !projectPath.segment( 0 ).equals( MONKEY_DIR ) )
						return new Status( WARNING, PLUGIN_ID, 0, "folder is not under the " + MONKEY_DIR + " subdirectory of its project" , null );
		            continue;
		        }
		        if( selected instanceof IProject )
		        {
		        	continue;
		        }
		        return new Status( ERROR, PLUGIN_ID, 0, "" + selected + " is not an IFile/IFolder/IProject", null );
		    }
		    return new Status( OK, PLUGIN_ID, 0, "", null );
		}
	}

	private Label label;
	private Text text;
	private IFile selectedFile;
	private ISelectionStatusValidator validator;

	public SelectScriptPathDialog( final Shell parent,
								   final ILabelProvider labelProvider,
								   final ITreeContentProvider contentProvider,
								   final IFile initialFileSelected )
	{
		super( parent, labelProvider, contentProvider );
		System.out.println( "SelectScriptPathDialog" );
		setAllowMultiple( false );
		validator = new Validator();
		setValidator( validator );
		setTitle( "Save Script To" );
		this.selectedFile = initialFileSelected;
	}
	protected Control createDialogArea( final Composite parent )
	{
        final Composite composite = ( Composite )super.createDialogArea( parent );
        getTreeViewer().addSelectionChangedListener( this );
        label = new Label( composite, SWT.NULL );
		label.setText( "Project: " + ( getFirstResult() != null ? (( IResource )getFirstResult()).getProject().getName() : "" ) );
		label.setVisible( true );
		GridData gd = new GridData( FILL_BOTH );
		gd.widthHint = convertWidthInCharsToPixels( 55 );
		label.setLayoutData( gd );
		applyDialogFont( label );
		text = new Text( composite, BORDER );
		text.setText( "" + getFirstResult() );
		text.setVisible( true );
		text.setEnabled( true );
		gd = new GridData( FILL_BOTH );
		gd.widthHint = convertWidthInCharsToPixels( 110 );
		text.setLayoutData( gd );
		text.addModifyListener( this );
		applyDialogFont( text );
        return composite;
    }
    public void selectionChanged( final SelectionChangedEvent event )
    {
    	final Object select = (( IStructuredSelection )event.getSelection()).getFirstElement();
    	System.out.println( "selectionChanged(): event: " + event.toString() );
    	if( !validator.validate( new Object[] { select } ).isOK() )
    		return;
		if( !( select instanceof IFile ) && !( select instanceof IFolder ) && !( select instanceof IProject ) )
			return;
		if( select instanceof IProject )
			selectedFile = (( IProject )select).getFile( selectedFile.getProjectRelativePath().toString() );
		if( select instanceof IFolder )
			selectedFile = (( IFolder )select).getFile( selectedFile.getName() );
		if( select instanceof IFile )
			selectedFile = ( IFile )select;
		label.setText( "Project: " + selectedFile.getProject().getName() );
		label.setVisible( true );
		text.setText( "" + selectedFile.getProjectRelativePath() );
		text.setVisible( true );
		text.setEnabled( true );
		updateOKStatus();
	}
	public void modifyText( final ModifyEvent event )
	{
		selectedFile = selectedFile.getProject().getFile( text.getText() );
		updateOKStatus();
	}
	public Object[] getResult()
	{
		return new Object[] { selectedFile };
	}
}


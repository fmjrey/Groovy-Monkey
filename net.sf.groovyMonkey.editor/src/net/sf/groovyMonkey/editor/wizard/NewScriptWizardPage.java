package net.sf.groovyMonkey.editor.wizard;
import static net.sf.groovyMonkey.GroovyMonkeyPlugin.FILE_EXTENSION;
import static net.sf.groovyMonkey.GroovyMonkeyPlugin.FILE_EXTENSION_WILDCARD;
import static net.sf.groovyMonkey.GroovyMonkeyPlugin.MONKEY_DIR;
import static net.sf.groovyMonkey.GroovyMonkeyPlugin.SCRIPT_SUFFIX;
import static net.sf.groovyMonkey.RunMonkeyScript.getScriptFactories;
import static net.sf.groovyMonkey.ScriptMetadata.DEFAULT_JOB;
import static net.sf.groovyMonkey.ScriptMetadata.DEFAULT_LANG;
import static net.sf.groovyMonkey.ScriptMetadata.DEFAULT_MODE;
import static org.apache.commons.lang.StringUtils.capitalize;
import static org.apache.commons.lang.StringUtils.defaultString;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.eclipse.core.resources.IResource.FOLDER;
import static org.eclipse.core.resources.IResource.PROJECT;
import static org.eclipse.core.resources.ResourcesPlugin.getWorkspace;
import static org.eclipse.jface.window.Window.OK;
import static org.eclipse.swt.SWT.BORDER;
import static org.eclipse.swt.SWT.DROP_DOWN;
import static org.eclipse.swt.SWT.NULL;
import static org.eclipse.swt.SWT.PUSH;
import static org.eclipse.swt.SWT.SINGLE;
import static org.eclipse.swt.layout.GridData.FILL_HORIZONTAL;
import java.util.Map;
import java.util.Set;
import net.sf.groovyMonkey.ScriptMetadata;
import net.sf.groovyMonkey.lang.IMonkeyScriptFactory;
import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

/**
 * The "New" wizard page allows setting the container for the new file as well
 * as the file name. The page will only accept file name without the extension
 * OR with the extension that matches the expected one (mpe).
 */
public class NewScriptWizardPage 
extends WizardPage
{
    private Text containerText;
    private Text fileText;
    private Text menuText;
    private Text kudosText;
    private Combo langCombo;
    private Combo jobModeCombo;
    private Combo execModeCombo;
    private final ISelection selection;

    /**
     * Constructor for SampleNewWizardPage.
     * 
     * @param pageName
     */
    public NewScriptWizardPage( final ISelection selection )
    {
        super( "newScriptWizardPage" );
        setTitle( "New Groovy Monkey Script Wizard" );
        setDescription( "This wizard creates a new file with " + FILE_EXTENSION_WILDCARD + " extension that can be opened by a multi-page editor." );
        this.selection = selection;
    }
    /**
     * @see IDialogPage#createControl(Composite)
     */
    public void createControl( final Composite parent )
    {
        final Composite container = new Composite( parent, NULL );
        final GridLayout layout = new GridLayout();
        container.setLayout( layout );
        layout.numColumns = 3;
        layout.verticalSpacing = 9;
        
        final Label containerLabel = new Label( container, NULL );
        containerLabel.setText( "&Container:" );
        containerText = new Text( container, BORDER | SINGLE );
        GridData gd = new GridData( FILL_HORIZONTAL );
        containerText.setLayoutData( gd );
        containerText.addModifyListener( new ModifyListener()
        {
            public void modifyText( final ModifyEvent e )
            {
                dialogChanged();
            }
        } );
        
        final Button button = new Button( container, PUSH );
        button.setText( "Browse..." );
        button.addSelectionListener( new SelectionAdapter()
        {
            @Override
            public void widgetSelected( final SelectionEvent e )
            {
                handleBrowse();
            }
        } );
        
        final Label fileLabel = new Label( container, NULL );
        fileLabel.setText( "&File name:" );
        fileText = new Text( container, BORDER | SINGLE );
        gd = new GridData( FILL_HORIZONTAL );
        fileText.setLayoutData( gd );
        fileText.addModifyListener( new ModifyListener()
        {
            public void modifyText( final ModifyEvent e )
            {
                dialogChanged();
            }
        } );
        new Label( container, NULL );
        
        final Label menuLabel = new Label( container, NULL );
        menuLabel.setText( "&Menu:" );
        menuText = new Text( container, BORDER | SINGLE );
        gd = new GridData( FILL_HORIZONTAL );
        menuText.setLayoutData( gd );
        menuText.addModifyListener( new ModifyListener()
        {
            public void modifyText( final ModifyEvent e )
            {
                dialogChanged();
            }
        } );
        new Label( container, NULL );
        
        final Label kudosLabel = new Label( container, NULL );
        kudosLabel.setText( "&Kudos:" );
        kudosText = new Text( container, BORDER | SINGLE );
        gd = new GridData( FILL_HORIZONTAL );
        kudosText.setLayoutData( gd );
        kudosText.addModifyListener( new ModifyListener()
        {
            public void modifyText( final ModifyEvent e )
            {
                dialogChanged();
            }
        } );
        new Label( container, NULL );
        
        final Label langLabel = new Label( container, NULL );
        langLabel.setText( "&Lang:" );
        langCombo = new Combo( container, DROP_DOWN );
        gd = new GridData( FILL_HORIZONTAL );
        langCombo.setLayoutData( gd );
        langCombo.addModifyListener( new ModifyListener()
        {
            public void modifyText( final ModifyEvent e )
            {
                dialogChanged();
            }
        } );
        new Label( container, NULL );
        
        final Label jobLabel = new Label( container, NULL );
        jobLabel.setText( "&Job:" );
        jobModeCombo = new Combo( container, DROP_DOWN );
        gd = new GridData( FILL_HORIZONTAL );
        jobModeCombo.setLayoutData( gd );
        jobModeCombo.addModifyListener( new ModifyListener()
        {
            public void modifyText( final ModifyEvent e )
            {
                dialogChanged();
            }
        } );
        new Label( container, NULL );
        
        final Label execLabel = new Label( container, NULL );
        execLabel.setText( "&Exec-Mode:" );
        execModeCombo = new Combo( container, DROP_DOWN );
        gd = new GridData( FILL_HORIZONTAL );
        execModeCombo.setLayoutData( gd );
        execModeCombo.addModifyListener( new ModifyListener()
        {
            public void modifyText( final ModifyEvent e )
            {
                dialogChanged();
            }
        } );
        new Label( container, NULL );
        
        initialize();
        dialogChanged();
        setControl( container );
    }
    public Map< String, IMonkeyScriptFactory > getScriptLanguages()
    {
        return new CaseInsensitiveMap( getScriptFactories() );
    }
    /**
     * Tests if the current workbench selection is a suitable container to use.
     */
    private void initialize()
    {
        for( final ScriptMetadata.ExecModes mode : ScriptMetadata.ExecModes.values() )
            execModeCombo.add( mode.toString() );
        execModeCombo.setText( DEFAULT_MODE.toString() );
        
        for( final ScriptMetadata.JobModes mode : ScriptMetadata.JobModes.values() )
            jobModeCombo.add( mode.toString() );
        jobModeCombo.setText( DEFAULT_JOB.toString() );
        
        menuText.setText( "New Groovy Monkey Script" );
        
        final Set< String > languages = getScriptLanguages().keySet();
        for( final String language : languages )
            langCombo.add( capitalize( language ) );
        langCombo.setText( capitalize( DEFAULT_LANG ) );
        
        kudosText.setText( System.getProperty( "user.name" ) );
        
        if( !( selection instanceof IStructuredSelection ) || selection.isEmpty() )
        {
            fileText.setText( "new_file" + FILE_EXTENSION );
            return;
        }
        final IStructuredSelection ssel = ( IStructuredSelection )selection;
        if( ssel.size() > 1 )
            return;
        final Object obj = ssel.getFirstElement();
        if( obj instanceof IAdaptable )
        {
            final IProject project = (( IResource )(( IAdaptable )obj).getAdapter( IResource.class )).getProject();
            final IFolder folder = getMonkeyFolder( project );
            containerText.setText( folder.getFullPath().toString() );
        }
        fileText.setText( "new_file" + FILE_EXTENSION );
    }
    private IFolder getMonkeyFolder( final IProject project )
    {
        return project.getFolder( "/" + MONKEY_DIR );
    }
    public void finished()
    {
        final IProject project = getWorkspace().getRoot().getFolder( new Path( getContainerName() ) ).getProject();
        final IFolder folder = getMonkeyFolder( project );
        try
        {
            if( !folder.exists() )
                folder.create( true, true, null );
        }
        catch( final CoreException ce )
        {
            throw new RuntimeException( "Could not create monkey folder for project: " + project.getName() + ". " + ce, ce );
        }
    }
    /**
     * Uses the standard container selection dialog to choose the new value for
     * the container field.
     */
    private void handleBrowse()
    {
        final ContainerSelectionDialog dialog = new ContainerSelectionDialog( getShell(), 
                                                                              getWorkspace().getRoot(), 
                                                                              false,
                                                                              "Select Project" );
        if( dialog.open() == OK )
        {
            final Object[] result = dialog.getResult();
            if( result.length == 1 )
            {
                final IFolder monkeyFolder = getMonkeyFolder( getWorkspace().getRoot().findMember( ( IPath )result[ 0 ] ).getProject() );
                containerText.setText( monkeyFolder.getFullPath().toString() );
            }
        }
    }
    /**
     * Ensures that both text fields are set.
     */
    private void dialogChanged()
    {
        if( isBlank( getContainerName() ) )
        {
            updateStatus( "File container must be specified" );
            return;
        }
        final IResource container = getWorkspace().getRoot().getFolder( new Path( getContainerName() ) );
        final String fileName = getFileName();
        if( !container.getProject().exists() )
        {
            updateStatus( "Project must exist: " + container.getProject().getName() );
            return;
        }
        if( ( container.getType() & ( PROJECT | FOLDER ) ) == 0 )
        {
            updateStatus( "File container must exist" );
            return;
        }
        if( !container.getProject().isAccessible() )
        {
            updateStatus( "Project must be writable" );
            return;
        }
        if( isBlank( fileName ) )
        {
            updateStatus( "File name must be specified" );
            return;
        }
        if( fileName.replace( '\\', '/' ).indexOf( '/', 1 ) > 0 )
        {
            updateStatus( "File name must be valid" );
            return;
        }
        final int dotLoc = fileName.lastIndexOf( '.' );
        if( dotLoc == -1 )
        {
            updateStatus( "File extension must be \"" + SCRIPT_SUFFIX + "\"" );
            return;
        }
        final String ext = fileName.substring( dotLoc + 1 );
        if( !ext.equalsIgnoreCase( SCRIPT_SUFFIX ) )
        {
            updateStatus( "File extension must be \"" + SCRIPT_SUFFIX + "\"" );
            return;
        }
        if( isBlank( getMenuName() ) )
        {
            updateStatus( "Menu name cannot be blank" );
            return;
        }
        if( !getScriptLanguages().containsKey( getLang() ) )
        {
            updateStatus( "No Language: " + getLang() + " in the supported set: " + getScriptLanguages().keySet() );
            return;
        }
        updateStatus( null );
    }
    private void updateStatus( final String message )
    {
        setErrorMessage( message );
        setPageComplete( message == null );
    }
    public String getContainerName()
    {
        return containerText.getText();
    }
    public String getFileName()
    {
        return fileText.getText();
    }
    public String getMenuName()
    {
        return menuText.getText();
    }
    public String getLang()
    {
        return langCombo.getText();
    }
    public String getKudos()
    {
        return defaultString( kudosText.getText() );
    }
    public String getJobMode()
    {
        return jobModeCombo.getText();
    }
    public String getExecMode()
    {
        return execModeCombo.getText();
    }
}

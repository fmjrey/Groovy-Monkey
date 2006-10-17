package net.sf.groovymonkey.dom.cvsdom;
import static org.eclipse.core.runtime.SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK;
import static org.eclipse.swt.widgets.Display.getCurrent;
import static org.eclipse.swt.widgets.Display.getDefault;
import static org.eclipse.team.internal.ccvs.core.CVSProviderPlugin.getPlugin;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.resources.RemoteFolder;
import org.eclipse.team.internal.ccvs.ui.operations.CheckoutIntoOperation;
import org.eclipse.team.internal.ccvs.ui.operations.DisconnectOperation;

public class CVSDOM
{
    public ICVSRepositoryLocation getKnownRepository( final String locationString )
    {
        for( ICVSRepositoryLocation location : getPlugin().getKnownRepositories() )
        {
            if( location.getLocation( true ).equals( locationString ) )
                return location;
        }
        return null;
    }
    public List< ICVSRemoteResource > getRepositoryResources( final IProgressMonitor progressMonitor,
                                                              final ICVSRepositoryLocation location,
                                                              final String subfolder )
    throws CVSException
    {
        final IProgressMonitor monitor = progressMonitor == null ? new NullProgressMonitor() : progressMonitor;
        final List< ICVSRemoteResource > folders = new ArrayList< ICVSRemoteResource >();
        final ICVSRemoteResource[] members = location.members( null, false, null );
        monitor.beginTask( "Getting Remote CVS Resources", members.length );
        for( final ICVSRemoteResource member : members )
        {
            if( monitor.isCanceled() )
                return null;
            monitor.subTask( member.getName() );
            final RemoteFolder folder = ( RemoteFolder )member;
            folder.fetchChildren( new SubProgressMonitor( monitor, PREPEND_MAIN_LABEL_TO_SUBTASK ) );
            if( StringUtils.isBlank( subfolder ) )
            {
                folders.add( member );
                monitor.worked( 1 );
                continue;
            }
            if( !folder.childExists( subfolder ) )
            {
                monitor.worked( 1 );
                continue;
            }
            folders.add( ( ICVSRemoteResource )folder.getFolder( subfolder ) );
            monitor.worked( 1 );
        }
        monitor.done();
        return folders;
    }
    public CVSDOM checkOut( final IProgressMonitor progressMonitor, 
                            final List< ICVSRemoteResource > remoteResources,
                            final IFolder target,
                            final boolean disconnect ) 
    throws CVSException, InterruptedException
    {
        final IProgressMonitor monitor = progressMonitor == null ? new NullProgressMonitor() : progressMonitor;
        monitor.beginTask( "Checking out into target: " + target.getFullPath(), remoteResources.size() );
        for( final ICVSRemoteResource folder : remoteResources )
        {
            if( monitor.isCanceled() )
                return this;
            if( !( folder instanceof ICVSRemoteFolder ) )
                continue;
            final IFolder targetFolder = target.getFolder( folder.getRemoteParent().getRepositoryRelativePath() );
            new CheckoutIntoOperation( null, ( ICVSRemoteFolder )folder, targetFolder, true ).execute( new SubProgressMonitor( monitor, 1 ) );
            monitor.worked( 1 );
        }
        if( !disconnect )
            return this;
        disconnect( target.getProject() );
        monitor.done();
        return this;
    }
    public CVSDOM disconnect( final IProject project ) 
    {
        if( getCurrent() == null )
        {
            final Runnable runnable = new Runnable()
            {
                public void run()
                {
                    disconnect( project );
                }
                
            };
            getDefault().syncExec( runnable );
            return this;
        }
        try
        {
            new DisconnectOperation( null, new IProject[] { project }, true ).run();
        }
        catch( final InvocationTargetException e )
        {
            throw new RuntimeException( e );
        }
        catch( final InterruptedException e )
        {
            throw new RuntimeException( e );
        }
        return this;
    }
}

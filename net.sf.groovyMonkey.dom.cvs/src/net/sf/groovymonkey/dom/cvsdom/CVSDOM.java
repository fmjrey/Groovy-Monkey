package net.sf.groovymonkey.dom.cvsdom;
import static org.eclipse.core.runtime.SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK;
import static org.eclipse.team.internal.ccvs.core.CVSProviderPlugin.getPlugin;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.resources.RemoteFolder;

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
}

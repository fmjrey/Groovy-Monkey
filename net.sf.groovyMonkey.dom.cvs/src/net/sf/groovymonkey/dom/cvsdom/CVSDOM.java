package net.sf.groovymonkey.dom.cvsdom;
import static org.eclipse.team.internal.ccvs.core.CVSProviderPlugin.getPlugin;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;

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
}

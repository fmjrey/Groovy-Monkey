package net.sf.groovyMonkey.editor;
import static net.sf.groovyMonkey.GroovyMonkeyPlugin.PLUGIN_ID;
import static net.sf.groovyMonkey.util.ListUtils.list;
import java.util.List;
import net.sf.groovyMonkey.RunMonkeyScript;
import net.sf.groovyMonkey.ScriptMetadata;
import net.sf.groovyMonkey.Tags;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator;

public class MarkerResolutionGenerator 
implements IMarkerResolutionGenerator
{
    public IMarkerResolution[] getResolutions( final IMarker marker )
    {
        final List< IMarkerResolution > list = list();
        if( marker == null )
            return list.toArray( new IMarkerResolution[ 0 ] );
        try
        {
            if( marker.getAttribute( PLUGIN_ID + ".tag" ) == null )
                return list.toArray( new IMarkerResolution[ 0 ] );
            final String tagString = ( String )marker.getAttribute( PLUGIN_ID + ".tag" );
            final Tags.Type tagType = Tags.Type.valueOf( tagString );
            switch( tagType )
            {
                case JOB:
                    addJobSuggestions( marker, list );
                break;
                case LANG:
                    addLangSuggestions( marker, list );
                break;
                case EXEC_MODE:
                    addExecModeSuggestions( marker, list );
                break;
            }
        }
        catch( final CoreException e )
        {
            throw new RuntimeException( e );
        }
        return list.toArray( new IMarkerResolution[ 0 ] );
    }
    private void addExecModeSuggestions( final IMarker marker, 
                                         final List< IMarkerResolution > list )
    {
        final ChangeToQuickFix.IRunnable runnable = new ChangeToQuickFix.IRunnable()
        {
            public void run( final ScriptMetadata metadata, 
                             final String value )
            {
                metadata.setExecMode( value );
            }   
        };
        for( final ScriptMetadata.ExecModes exec : ScriptMetadata.ExecModes.values() )
            list.add( new ChangeToQuickFix( exec.name(), runnable ) );
    }
    private void addJobSuggestions( final IMarker marker, 
                                    final List< IMarkerResolution > list )
    {
        final ChangeToQuickFix.IRunnable runnable = new ChangeToQuickFix.IRunnable()
        {
            public void run( final ScriptMetadata metadata, 
                             final String value )
            {
                metadata.setJobMode( value );
            }   
        };
        for( final ScriptMetadata.JobModes job : ScriptMetadata.JobModes.values() )
            list.add( new ChangeToQuickFix( job.name(), runnable ) );
    }
    private void addLangSuggestions( final IMarker marker, 
                                     final List< IMarkerResolution > list )
    {
        final ChangeToQuickFix.IRunnable runnable = new ChangeToQuickFix.IRunnable()
        {
            public void run( final ScriptMetadata metadata, 
                             final String value )
            {
                metadata.setLang( value );
            }   
        };
        for( final String lang : RunMonkeyScript.getScriptFactories().keySet() )
            list.add( new ChangeToQuickFix( lang, runnable ) );
    }
}

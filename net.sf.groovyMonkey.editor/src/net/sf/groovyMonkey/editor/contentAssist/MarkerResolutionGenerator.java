package net.sf.groovyMonkey.editor.contentAssist;
import static net.sf.groovyMonkey.GroovyMonkeyPlugin.PLUGIN_ID;
import static net.sf.groovyMonkey.RunMonkeyScript.getScriptFactories;
import static net.sf.groovyMonkey.ScriptMetadata.DEFAULT_JOB;
import static net.sf.groovyMonkey.ScriptMetadata.DEFAULT_LANG;
import static net.sf.groovyMonkey.ScriptMetadata.DEFAULT_MODE;
import static net.sf.groovyMonkey.util.ListUtils.list;
import java.util.List;
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
                    addJobSuggestions( list );
                break;
                case LANG:
                    addLangSuggestions( list );
                break;
                case EXEC_MODE:
                    addExecModeSuggestions( list );
                break;
            }
        }
        catch( final CoreException e )
        {
            throw new RuntimeException( e );
        }
        return list.toArray( new IMarkerResolution[ 0 ] );
    }
    public static void addExecModeSuggestions( final List< IMarkerResolution > list )
    {
        addExecModeSuggestions( list, true );
    }
    public static void addExecModeSuggestions( final List< IMarkerResolution > list,
                                               final boolean force )
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
        {
            if( exec.equals( DEFAULT_MODE ) )
                list.add( 0, new ChangeToQuickFix( exec.name(), runnable, true ).setForce( force ) );
            else
                list.add( new ChangeToQuickFix( exec.name(), runnable ).setForce( force ) );
        }
    }
    public static void addJobSuggestions( final List< IMarkerResolution > list )
    {
        addJobSuggestions( list, true );
    }
    public static void addJobSuggestions( final List< IMarkerResolution > list,
                                          final boolean force )
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
        {
            if( job.equals( DEFAULT_JOB ) )
                list.add( 0, new ChangeToQuickFix( job.name(), runnable, true ).setForce( force ) );
            else
                list.add( new ChangeToQuickFix( job.name(), runnable ).setForce( force ) );
        }
    }
    public static void addLangSuggestions( final List< IMarkerResolution > list )
    {
        addLangSuggestions( list, true );
    }
    public static void addLangSuggestions( final List< IMarkerResolution > list,
                                           final boolean force )
    {
        final ChangeToQuickFix.IRunnable runnable = new ChangeToQuickFix.IRunnable()
        {
            public void run( final ScriptMetadata metadata, 
                             final String value )
            {
                metadata.setLang( value );
            }   
        };
        for( final String lang : getScriptFactories().keySet() )
        {
            if( lang.equalsIgnoreCase( DEFAULT_LANG ) )
                list.add( 0, new ChangeToQuickFix( lang, runnable, true ).setForce( force ) );
            else
                list.add( new ChangeToQuickFix( lang, runnable ).setForce( force ) );
        }
    }
}

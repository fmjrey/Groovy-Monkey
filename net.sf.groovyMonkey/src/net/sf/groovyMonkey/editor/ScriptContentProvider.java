package net.sf.groovyMonkey.editor;
import static net.sf.groovyMonkey.ScriptMetadata.DEFAULT_LANG;
import static net.sf.groovyMonkey.ScriptMetadata.DEFAULT_MODE;
import static net.sf.groovyMonkey.ScriptMetadata.getScriptMetadata;
import static net.sf.groovyMonkey.dom.Utilities.getDOM;
import static net.sf.groovyMonkey.dom.Utilities.getFileContents;
import static net.sf.groovyMonkey.dom.Utilities.hasDOM;
import static org.apache.commons.lang.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang.builder.HashCodeBuilder.reflectionHashCode;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import net.sf.groovyMonkey.DOMDescriptor;
import net.sf.groovyMonkey.ScriptMetadata;
import net.sf.groovyMonkey.ScriptMetadata.JobModes;
import org.apache.commons.lang.ObjectUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class ScriptContentProvider 
implements ITreeContentProvider
{
    public static class Descriptor
    {
        @Override
        public boolean equals( final Object obj )
        {
            return reflectionEquals( this, obj );
        }
        @Override
        public int hashCode()
        {
            return reflectionHashCode( this );
        }
    }
    public static class LangDescriptor
    extends Descriptor
    {
        public String lang;
        public LangDescriptor( final String lang )
        {
            this.lang = lang;
        }
        @Override
        public String toString()
        {
            return "LANG: " + lang;
        }
    }
    public static class JobDescriptor
    extends Descriptor
    {
        public JobModes mode;
        public JobDescriptor( final JobModes mode )
        {
            this.mode = mode;
        }
        @Override
        public String toString()
        {
            return "Job: " + mode;
        }
    }
    public static class MenuDescriptor
    extends Descriptor
    {
        public String menu;
        public MenuDescriptor( final String menu )
        {
            this.menu = menu;
        }
        @Override
        public String toString()
        {
            return "Menu: " + menu;
        }
    }
    public static class VarDescriptor
    extends Descriptor
    {
        public final DOMDescriptor parent;
        public final String varName;
        public final Class type;
        
        public VarDescriptor( final DOMDescriptor parent, 
                              final String varName, 
                              final Class type )
        {
            this.parent = parent;
            this.varName = varName;
            this.type = type;
        }
        @Override
        public String toString()
        {
            return varName + ": " + type.getName();
        }
    };
    public static final class MethodDescriptor
    implements Comparable< MethodDescriptor >
    {
        public final Method method;
        public MethodDescriptor( final Method method )
        {
            this.method = method;   
        }
        @Override
        public String toString()
        {
            final Class[] parameters = method.getParameterTypes();
            final StringBuffer buffer = new StringBuffer();
            buffer.append( "(" );
            for( int i = 0; i < parameters.length; i++ )
            {
                final Class type = parameters[ i ];
                if( i == 0 )
                    buffer.append( " " ).append( type.getSimpleName() );
                else
                    buffer.append( ", " ).append( type.getSimpleName() );
            }
            if( parameters.length > 0 )
                buffer.append( " " );
            buffer.append( ")" );
            buffer.append( ": " + method.getReturnType().getSimpleName() );
            return method.getName() + buffer.toString();
        }
        public int compareTo( final MethodDescriptor descriptor )
        {
            if( method.getName().compareTo( descriptor.method.getName() ) != 0 )
                return method.getName().compareTo( descriptor.method.getName() );
            return method.toGenericString().compareTo( descriptor.method.toGenericString() );
        }
    }
    public static final class FieldDescriptor
    extends Descriptor
    implements Comparable< FieldDescriptor >
    {
        public final Field field;
        public FieldDescriptor( final Field field )
        {
            this.field = field;   
        }
        @Override
        public String toString()
        {
            return field.getName() + ": " + field.getType().getSimpleName();
        }
        public int compareTo( final FieldDescriptor descriptor )
        {
            return field.getName().compareTo( descriptor.field.getName() );
        }
    }
    public static final class ClassDescriptor
    extends Descriptor
    {
        public final Class clase;
        public final Object parent;
        public ClassDescriptor( final Class clase,
                                final Object parent )
        {
            this.clase = clase;
            this.parent = parent;
        }
        @Override
        public String toString()
        {
            return clase.getSimpleName();
        }
    }
    private final LangDescriptor lang = new LangDescriptor( DEFAULT_LANG );
    private final JobDescriptor job = new JobDescriptor( DEFAULT_MODE );
    private final MenuDescriptor menu = new MenuDescriptor( "" );
    private final List< DOMDescriptor > doms = new ArrayList< DOMDescriptor >();
    private final List< String > includes = new ArrayList< String >();
    private ScriptMetadata data = null;
    
    public Object[] getChildren( final Object parentElement )
    {
        if( parentElement instanceof DOMDescriptor )
        {
            final DOMDescriptor descriptor = ( DOMDescriptor )parentElement;
            final Map< String, Object > dom = getDOM( descriptor.plugin_name );
            final List< VarDescriptor > list = new ArrayList< VarDescriptor >();
            for( final String var : dom.keySet() )
                list.add( new VarDescriptor( descriptor, var, dom.get( var ).getClass() ) );
            return list.toArray( new VarDescriptor[ 0 ] );
        }
        if( parentElement instanceof VarDescriptor )
        {
            final VarDescriptor descriptor = ( VarDescriptor )parentElement;
            final Class type = descriptor.type;
            return new ClassDescriptor[] { new ClassDescriptor( type, descriptor ) };
        }
        if( parentElement instanceof ClassDescriptor )
        {
            final ClassDescriptor descriptor = ( ClassDescriptor )parentElement;
            final Set< FieldDescriptor > fields = new TreeSet< FieldDescriptor >();
            for( final Field field : descriptor.clase.getDeclaredFields() )
            {
                if( !Modifier.isPublic( field.getModifiers() ) )
                    continue;
                fields.add( new FieldDescriptor( field ) );
            }
            final Set< MethodDescriptor > methods = new TreeSet< MethodDescriptor >();
            for( final Method method : descriptor.clase.getDeclaredMethods() )
            {
                if( !Modifier.isPublic( method.getModifiers() ) )
                    continue;
                methods.add( new MethodDescriptor( method ) );
            }
            final List< Object > list = new ArrayList< Object >();
            list.addAll( fields );
            list.addAll( methods );
            if( descriptor.clase.getSuperclass() != null )
                list.add( 0, new ClassDescriptor( descriptor.clase.getSuperclass(), descriptor ) );
            return list.toArray();
        }
        return null;
    }
    public Object getParent( final Object element )
    {
        if( element instanceof VarDescriptor )
            return (( VarDescriptor )element).parent;
        if( element instanceof ClassDescriptor )
            return (( ClassDescriptor )element).parent;
        return null;
    }
    public boolean hasChildren( final Object element )
    {
        if( element instanceof DOMDescriptor )
        {
            if( !hasDOM( (( DOMDescriptor)element).plugin_name ) )
                return false;
            return true;
        }
        if( element instanceof VarDescriptor )
            return true;
        if( element instanceof ClassDescriptor )
            return true;
        return false;
    }
    public Object[] getElements( final Object inputElement )
    {
        final List< Object > elements = new ArrayList< Object >();
        elements.add( menu );
        elements.add( lang );
        elements.add( job );
        elements.addAll( doms );
        elements.addAll( includes );
        return elements.toArray();
    }
    public void dispose()
    {
    }
    public void inputChanged( final Viewer viewer, 
                              final Object oldInput, 
                              final Object newInput )
    {
        if( !( newInput instanceof IAdaptable ) )
            return;
        final IFile script = ( IFile )(( IAdaptable )newInput).getAdapter( IFile.class );
        if( !script.exists() )
            return;
        try
        {
            data = getScriptMetadata( getFileContents( script ) );
            menu.menu = data.getMenuName();
            lang.lang = data.getLang();
            job.mode = data.getJobMode();
            doms.clear();
            doms.addAll( data.getDOMs() );
            includes.clear();
            includes.addAll( data.getIncludes() );
        }
        catch( final IOException ioe )
        {
            ioe.printStackTrace();
            throw new RuntimeException( ioe );
        }
        catch( final CoreException e )
        {
            e.printStackTrace();
            throw new RuntimeException( e );
        }
    }
    public boolean diff( final ScriptMetadata data )
    {
        return !ObjectUtils.equals( this.data, data );
    }
}

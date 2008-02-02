package net.sf.groovyMonkey.editor;
import static java.lang.reflect.Modifier.isPublic;
import static net.sf.groovyMonkey.GroovyMonkeyPlugin.bundleDescription;
import static net.sf.groovyMonkey.GroovyMonkeyPlugin.getAllReexportedBundles;
import static net.sf.groovyMonkey.GroovyMonkeyPlugin.getAllRequiredBundles;
import static net.sf.groovyMonkey.ScriptMetadata.DEFAULT_JOB;
import static net.sf.groovyMonkey.ScriptMetadata.DEFAULT_LANG;
import static net.sf.groovyMonkey.ScriptMetadata.DEFAULT_MODE;
import static net.sf.groovyMonkey.ScriptMetadata.getScriptMetadata;
import static net.sf.groovyMonkey.dom.Utilities.getContents;
import static net.sf.groovyMonkey.dom.Utilities.getDOMInfo;
import static net.sf.groovyMonkey.dom.Utilities.hasDOM;
import static org.apache.commons.lang.StringUtils.defaultString;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang.builder.HashCodeBuilder.reflectionHashCode;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;
import net.sf.groovyMonkey.DOMDescriptor;
import net.sf.groovyMonkey.ScriptMetadata;
import net.sf.groovyMonkey.Subscription;
import net.sf.groovyMonkey.ScriptMetadata.ExecModes;
import net.sf.groovyMonkey.ScriptMetadata.JobModes;
import net.sf.groovyMonkey.util.TreeList;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.service.resolver.ExportPackageDescription;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.PluginModelManager;

@SuppressWarnings("restriction")
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
    public static class ListenerDescriptor
    extends Descriptor
    {
        public final String filter;
        public ListenerDescriptor( final String filter )
        {
            this.filter = filter;
        }
        @Override
        public String toString()
        {
            return "Listener: " + filter;
        }
    }
    public static class LangDescriptor
    extends Descriptor
    {
        private String lang;
        public LangDescriptor( final String lang )
        {
            this.setLang( lang );
        }
        @Override
        public String toString()
        {
            return "LANG: " + getLang();
        }
        public void setLang( String lang )
        {
            this.lang = lang;
        }
        public String getLang()
        {
            return lang;
        }
    }
    public static class JobDescriptor
    extends Descriptor
    {
        private JobModes mode;
        public JobDescriptor( final JobModes mode )
        {
            this.setMode( mode );
        }
        @Override
        public String toString()
        {
            return "Job: " + getMode();
        }
        public void setMode( JobModes mode )
        {
            this.mode = mode;
        }
        public JobModes getMode()
        {
            return mode;
        }
    }
    public static class ExecDescriptor
    extends Descriptor
    {
        private ExecModes mode;
        public ExecDescriptor( final ExecModes mode )
        {
            this.setMode( mode );
        }
        @Override
        public String toString()
        {
            return "Exec-Mode: " + getMode();
        }
        public void setMode( ExecModes mode )
        {
            this.mode = mode;
        }
        public ExecModes getMode()
        {
            return mode;
        }
    }
    public static class MenuDescriptor
    extends Descriptor
    {
        private String menu;
        public MenuDescriptor( final String menu )
        {
            this.setMenu( menu );
        }
        @Override
        public String toString()
        {
            return "Menu: " + getMenu();
        }
        public void setMenu( String menu )
        {
            this.menu = menu;
        }
        public String getMenu()
        {
            return menu;
        }
    }
    public static class ScriptPathDescriptor
    extends Descriptor
    {
        private String path;
        public ScriptPathDescriptor( final String path )
        {
            this.setPath( path );
        }
        @Override
        public String toString()
        {
            return "Script-Path: " + getPath();
        }
        public void setPath( String path )
        {
            this.path = path;
        }
        public String getPath()
        {
            return path;
        }
    }
    public static class BundleDescriptor
    extends Descriptor
    implements Comparable< BundleDescriptor >
    {
        public final String name;
        public final Object parent;
        public BundleDescriptor( final String name,
                                 final Object parent )
        {
            this.name = name;
            this.parent = parent;
        }
        @Override
        public String toString()
        {
            return name;
        }
        public int compareTo( final BundleDescriptor descriptor )
        {
            return name.compareTo( descriptor.name );
        }
    }
    public static class PackageDescriptor
    extends Descriptor
    implements Comparable< PackageDescriptor >
    {
        public final String name;
        public final Object parent;
        public PackageDescriptor( final String name,
                                  final Object parent )
        {
            this.name = name;
            this.parent = parent;
        }
        @Override
        public String toString()
        {
            return name;
        }
        public int compareTo( final PackageDescriptor descriptor )
        {
            return name.compareTo( descriptor.name );
        }
    }
    public static class VarDescriptor
    extends Descriptor
    implements Comparable< VarDescriptor >
    {
        public final DOMDescriptor parent;
        public final String varName;
        public final String localName;
        public final Class<?> type;
        private final String toString;

        public VarDescriptor( final DOMDescriptor parent,
                              final String varName,
                              final Class<?> type )
        {
            this.parent = parent;
            this.varName = varName;
            this.localName = defaultString( parent.map.get( varName ) );
            this.type = type;
            if( StringUtils.isBlank( localName ) )
                toString = varName + ": " + type.getName();
            else
                toString = localName + "[" + varName + "]" + ":" + type.getName();
        }
        @Override
        public String toString()
        {
            return toString;
        }
        public int compareTo( final VarDescriptor descriptor )
        {
            return toString().compareTo( descriptor.toString() );
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
            final Class<?>[] parameters = method.getParameterTypes();
            final StringBuffer buffer = new StringBuffer();
            buffer.append( "(" );
            for( int i = 0; i < parameters.length; i++ )
            {
                final Class<?> type = parameters[ i ];
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
            if( !method.getName().equals( descriptor.method.getName() ) )
                return method.getName().compareTo( descriptor.method.getName() );
            return method.toGenericString().compareTo( descriptor.method.toGenericString() );
        }
        @Override
        public int hashCode()
        {
            final int PRIME = 31;
            int result = 1;
            result = PRIME * result + ( ( method == null ) ? 0 : method.toGenericString().hashCode() );
            return result;
        }
        @Override
        public boolean equals( Object obj )
        {
            if( this == obj )
                return true;
            if( obj == null )
                return false;
            if( getClass() != obj.getClass() )
                return false;
            final MethodDescriptor other = ( MethodDescriptor )obj;
            if( method == null )
            {
                if( other.method != null )
                    return false;
            }
            else if( compareTo( other ) != 0 )
                return false;
            return true;
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
    implements Comparable< ClassDescriptor >
    {
        public final Class<?> clase;
        public final Object parent;
        public ClassDescriptor( final Class<?> clase,
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
        public int compareTo( final ClassDescriptor descriptor )
        {
            return toString().compareTo( descriptor.toString() );
        }
    }
    private final LangDescriptor lang = new LangDescriptor( DEFAULT_LANG );
    private final JobDescriptor job = new JobDescriptor( DEFAULT_JOB );
    private final ExecDescriptor exec = new ExecDescriptor( DEFAULT_MODE );
    private final MenuDescriptor menu = new MenuDescriptor( "" );
    private final ScriptPathDescriptor path = new ScriptPathDescriptor( "" );
    private final List< ListenerDescriptor > listeners = new ArrayList< ListenerDescriptor >();
    private final List< DOMDescriptor > doms = new ArrayList< DOMDescriptor >();
    private final List< String > includes = new ArrayList< String >();
    private final List< BundleDescriptor > bundles = new TreeList< BundleDescriptor >();
    private ScriptMetadata data = null;
    private boolean flat = false;

    public Object[] getChildren( final Object parentElement )
    {
        if( parentElement instanceof DOMDescriptor )
        {
            final DOMDescriptor descriptor = ( DOMDescriptor )parentElement;
            final Map< String, Class<?> > dom = getDOMInfo( descriptor.pluginName );
            final List< VarDescriptor > list = new TreeList< VarDescriptor >();
            for( final Entry< String, Class<?> > entry : dom.entrySet() )
            {
                if( entry.getValue() == null )
                    continue;
                list.add( new VarDescriptor( descriptor, entry.getKey(), entry.getValue() ) );
            }
            return list.toArray( new VarDescriptor[ 0 ] );
        }
        if( parentElement instanceof VarDescriptor )
        {
            final VarDescriptor descriptor = ( VarDescriptor )parentElement;
            final Class<?> type = descriptor.type;
            return new ClassDescriptor[] { new ClassDescriptor( type, descriptor ) };
        }
        if( parentElement instanceof ClassDescriptor )
        {
            final ClassDescriptor descriptor = ( ClassDescriptor )parentElement;
            final Set< FieldDescriptor > fields = new TreeSet< FieldDescriptor >();
            for( final Field field : descriptor.clase.getDeclaredFields() )
            {
                if( !isPublic( field.getModifiers() ) )
                    continue;
                fields.add( new FieldDescriptor( field ) );
            }
            final Set< MethodDescriptor > methods = new TreeSet< MethodDescriptor >();
            for( final Method method : descriptor.clase.getDeclaredMethods() )
            {
                if( !isPublic( method.getModifiers() ) )
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
        if( parentElement instanceof BundleDescriptor )
        {
            final BundleDescriptor descriptor = ( BundleDescriptor )parentElement;
            final Set< PackageDescriptor > packages = new TreeSet< PackageDescriptor >();
            if( StringUtils.isNotBlank( descriptor.name ) && bundleDescription( descriptor.name ) != null )
                for( final ExportPackageDescription description : bundleDescription( descriptor.name ).getExportPackages() )
                    packages.add( new PackageDescriptor( description.getName(), descriptor ) );
            return packages.toArray();
        }
        if( parentElement instanceof PackageDescriptor )
        {
            final PackageDescriptor descriptor = ( PackageDescriptor )parentElement;
            final Set< ClassDescriptor > clases = new TreeSet< ClassDescriptor >();
            final IJavaProject javaProject = getSearchableProject( "" + descriptor.parent );
            if( javaProject == null )
                return clases.toArray();
            try
            {
                for( final IPackageFragmentRoot root : javaProject.getAllPackageFragmentRoots() )
                {
                    final IPackageFragment fragment = root.getPackageFragment( descriptor.name );
                    if( !fragment.exists() )
                        continue;
                    for( final IClassFile classFile : fragment.getClassFiles() )
                        clases.add( new ClassDescriptor( Class.forName( classFile.getType().getFullyQualifiedName() ), descriptor ) );
                    for( final ICompilationUnit unit : fragment.getCompilationUnits() )
                        for( final IType type : unit.getTypes() )
                            clases.add( new ClassDescriptor( Class.forName( type.getFullyQualifiedName() ), descriptor ) );
                }
            }
            catch( final ClassNotFoundException e )
            {
                throw new RuntimeException( e );
            }
            catch( final JavaModelException e )
            {
                throw new RuntimeException( e );
            }
            return clases.toArray();
        }
        return null;
    }
    public static IJavaProject getSearchableProject( final String projectName )
    {
        final PluginModelManager manager = PDECore.getDefault().getModelManager();
//        final IJavaProject javaProject = manager.getSearchablePluginsManager().getProxyProject();
//        if( javaProject != null )
//            return javaProject;
        for( final IPluginModelBase base : manager.getWorkspaceModels() )
        {
            final IProject project = base.getUnderlyingResource().getProject();
            if( project.getName().equals( projectName ) )
                return JavaCore.create( project );
        }
        return null;
    }
    public Object getParent( final Object element )
    {
        if( element instanceof VarDescriptor )
            return (( VarDescriptor )element).parent;
        if( element instanceof ClassDescriptor )
            return (( ClassDescriptor )element).parent;
        if( element instanceof PackageDescriptor )
            return (( PackageDescriptor )element).parent;
        return null;
    }
    public boolean hasChildren( final Object element )
    {
        if( element instanceof DOMDescriptor )
        {
            if( !hasDOM( (( DOMDescriptor)element).pluginName ) )
                return false;
            return true;
        }
        if( element instanceof VarDescriptor )
            return true;
        if( element instanceof ClassDescriptor )
            return true;
        if( element instanceof BundleDescriptor )
            return true;
        if( element instanceof PackageDescriptor )
            return true;
        return false;
    }
    public Object[] getElements( final Object inputElement )
    {
        final List< Object > elements = new ArrayList< Object >();
        if( isNotBlank( path.getPath() ) )
            elements.add( path );
        if( isNotBlank( menu.getMenu() ) )
            elements.add( menu );
        elements.add( lang );
        elements.add( job );
        elements.add( exec );
        elements.addAll( listeners );
        if( !flat )
            hierarchicalView( elements );
        else
            flatView( elements );
        return elements.toArray();
    }
    private void hierarchicalView( final List< Object > elements )
    {
        elements.addAll( doms );
        elements.addAll( includes );
        elements.addAll( bundles );
    }
    private void flatView( final List< Object > elements )
    {
        final List< VarDescriptor > set = new TreeList< VarDescriptor >();
        for( final DOMDescriptor dom : doms )
            for( final VarDescriptor child : ( VarDescriptor[] )getChildren( dom ) )
                set.add( child );
        elements.addAll( set );
        elements.addAll( includes );
        for( final BundleDescriptor bundle : bundles )
            for( final Object child : getChildren( bundle ) )
                elements.add( child );
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
            data = getScriptMetadata( getContents( script ) );
            menu.setMenu( defaultString( data.getMenuName() ) );
            path.setPath( defaultString( data.scriptPath() ) );
            lang.setLang( data.getLang() );
            job.setMode( data.getJobMode() );
            exec.setMode( data.getExecMode() );
            listeners.clear();
            for( final Subscription subscription : data.getSubscriptions() )
                listeners.add( new ListenerDescriptor( subscription.getFilter() ) );
            doms.clear();
            doms.addAll( data.getDOMs() );
            includes.clear();
            includes.addAll( data.getIncludes() );
            addBundles();
        }
        catch( final IOException ioe )
        {
            throw new RuntimeException( ioe );
        }
        catch( final CoreException e )
        {
            throw new RuntimeException( e );
        }
    }
    private void addBundles()
    {
        bundles.clear();
        for( final String bundle : getBundles( data ) )
            bundles.add( new BundleDescriptor( bundle, null ) );
    }
    public static Set< String > getBundles( final ScriptMetadata data )
    {
        final Set< String > bundles = new TreeSet< String >();
        if( data == null )
            return bundles;
        final Set< String > visited = new HashSet< String >();
        for( final String bundle : data.getIncludedBundles() )
        {
            bundles.add( bundle );
            visited.add( bundle );
        }
        for( final String bundle : getAllRequiredBundles() )
        {
            bundles.add( bundle );
            visited.add( bundle );
        }
        bundles.addAll( getReexportedBundles( visited, new TreeSet< String >( bundles ) ) );
        return bundles;
    }
    public static Set< String > getReexportedBundles( final Set< String > visited,
                                                      final Set< String > bundlesToCheck )
    {
        final Set< String > bundles = new TreeSet< String >();
        if( bundlesToCheck == null || bundlesToCheck.size() == 0 )
            return bundles;
        final Set< String > newBundles = new HashSet< String >();
        for( final String bundle : bundlesToCheck )
        {
            final Set< String > reexported = getAllReexportedBundles( bundle );
            for( final String exported : reexported )
            {
                if( visited.contains( exported ) )
                    continue;
                visited.add( exported );
                bundles.add( exported );
                newBundles.add( exported );
            }
        }
        if( newBundles.size() > 0 )
            bundles.addAll( getReexportedBundles( visited, newBundles ) );
        return bundles;
    }
    public boolean diff( final ScriptMetadata data )
    {
        return !ObjectUtils.equals( this.data, data );
    }
    public void setViewLayout( final boolean flat )
    {
        this.flat = flat;
    }
}

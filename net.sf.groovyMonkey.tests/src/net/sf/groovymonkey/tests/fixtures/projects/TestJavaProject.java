package net.sf.groovymonkey.tests.fixtures.projects;
import static org.apache.commons.lang.ArrayUtils.add;
import static org.eclipse.jdt.core.JavaCore.NATURE_ID;
import static org.eclipse.jdt.core.JavaCore.create;
import static org.eclipse.jdt.core.JavaCore.newSourceEntry;
import static org.eclipse.jdt.core.search.IJavaSearchConstants.CLASS;
import static org.eclipse.jdt.core.search.IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH;
import static org.eclipse.jdt.core.search.SearchEngine.createJavaSearchScope;
import static org.eclipse.jdt.core.search.SearchPattern.R_CASE_SENSITIVE;
import static org.eclipse.jdt.core.search.SearchPattern.R_EXACT_MATCH;
import static org.eclipse.jdt.launching.JavaRuntime.getDefaultJREContainerEntry;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.TypeNameRequestor;

public class TestJavaProject
extends TestProject
{
    private final IJavaProject javaProject;
    private final IPackageFragmentRoot sourceFolder;

    public TestJavaProject( final String name ) 
    throws CoreException
    {
        super( name );
        javaProject = create( project );
        final IFolder binFolder = createBinFolder();
        setJavaNature();
        javaProject.setRawClasspath( new IClasspathEntry[ 0 ], null );
        createOutputFolder( binFolder );
        sourceFolder = createSourceFolder();
        addSystemLibraries();
    }
    public IJavaProject getJavaProject()
    {
        return javaProject;
    }
    public IJavaProject javaProject()
    {
        return getJavaProject();
    }
    public boolean hasJar( final String jar ) 
    throws JavaModelException
    {
        final IClasspathEntry[] entries = javaProject.getRawClasspath();
        for( final IClasspathEntry entry : entries )
            if( entry.getPath().lastSegment().equals( jar ) )
                return true;
        return false;
    }
    public IPackageFragment createPackage( final String name ) 
    throws CoreException
    {
        return sourceFolder.createPackageFragment( name, false, null );
    }
    public IType createJavaType( final IPackageFragment pack, 
                                 final String cuName, 
                                 final String source ) 
    throws JavaModelException
    {
        final ICompilationUnit cu = pack.createCompilationUnit( cuName, source, false, null );
        return cu.findPrimaryType();
    }
    public IType createJavaType( final String packageName, 
                                 final String cuName, 
                                 final String source ) 
    throws CoreException
    {
        return createJavaType( createPackage( packageName ), cuName, source );
    }
    @Override
    public void dispose() 
    throws CoreException
    {
        waitForIndexer();
        super.dispose();
    }
    private IFolder createBinFolder() 
    throws CoreException
    {
        final IFolder binFolder = project.getFolder( "bin" );
        binFolder.create( false, true, null );
        return binFolder;
    }
    private void setJavaNature() 
    throws CoreException
    {
        final IProjectDescription description = project.getDescription();
        description.setNatureIds( new String[]{ NATURE_ID } );
        project.setDescription( description, null );
    }
    private void createOutputFolder( final IFolder binFolder ) 
    throws JavaModelException
    {
        final IPath outputLocation = binFolder.getFullPath();
        javaProject.setOutputLocation( outputLocation, null );
    }
    private IPackageFragmentRoot createSourceFolder() 
    throws CoreException
    {
        final IFolder folder = createFolder( "src" );
        final IClasspathEntry[] entries = javaProject.getResolvedClasspath( false );
        final IPackageFragmentRoot root = javaProject.getPackageFragmentRoot( folder );
        for( final IClasspathEntry entry : entries )
            if( entry.getPath().equals( folder.getFullPath() ) )
                return root;
        addToRawClassPath( newSourceEntry( root.getPath() ) );
        return root;
    }
    private void addSystemLibraries() 
    throws JavaModelException
    {
        addToRawClassPath( getDefaultJREContainerEntry() );
    }
    private void addToRawClassPath( final IClasspathEntry entry ) 
    throws JavaModelException
    {
        if( entry == null )
            return;
        javaProject.setRawClasspath( ( IClasspathEntry[] )add( javaProject.getRawClasspath(), entry ), null );
    }
    public TestJavaProject addWorkspacePathToClasspath( final String workspacePath ) 
    throws JavaModelException
    {
        addToRawClassPath( JavaCore.newContainerEntry( new Path( workspacePath ) ) );
        return this;
    }
    public TestJavaProject addExternalPathToClasspath( final String fullPath ) 
    throws JavaModelException
    {
        addToRawClassPath( JavaCore.newLibraryEntry( new Path( fullPath ), null, null, true ) );
        return this;
    }
    private void waitForIndexer() 
    throws JavaModelException
    {
        final TypeNameRequestor requestor = new TypeNameRequestor() {};
        new SearchEngine().searchAllTypeNames( null, 
                                               null, 
                                               R_EXACT_MATCH | R_CASE_SENSITIVE, 
                                               CLASS, 
                                               createJavaSearchScope( new IJavaElement[ 0 ] ),
                                               requestor, 
                                               WAIT_UNTIL_READY_TO_SEARCH, 
                                               null );
    }
}

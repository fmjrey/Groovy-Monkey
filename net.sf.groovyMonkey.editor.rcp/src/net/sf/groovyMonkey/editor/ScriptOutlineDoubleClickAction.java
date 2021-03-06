package net.sf.groovyMonkey.editor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;

public class ScriptOutlineDoubleClickAction 
implements IDoubleClickListener
{
    public void doubleClick( final DoubleClickEvent event )
    {
//        final ISelection select = event.getSelection();
//        if( !( select instanceof IStructuredSelection ) )
//            return;
//        final IStructuredSelection selection = ( IStructuredSelection )select;
//        final Object selected = selection.getFirstElement();
//        if( selected instanceof VarDescriptor )
//        {
//            final VarDescriptor descriptor = ( VarDescriptor )selected;
//            openTypeInEditor( descriptor.type.getName() );
//        }
//        if( selected instanceof ClassDescriptor )
//        {
//            final ClassDescriptor descriptor = ( ClassDescriptor )selected;
//            openTypeInEditor( descriptor.clase.getName() );
//        }
//        if( selected instanceof MethodDescriptor )
//        {
//            final MethodDescriptor descriptor = ( MethodDescriptor )selected;
//            openMethodInEditor( descriptor.method );
//        }
//        if( selected instanceof FieldDescriptor )
//        {
//            final FieldDescriptor descriptor = ( FieldDescriptor )selected;
//            openFieldInEditor( descriptor.field );
//        }
    }
//    private void openTypeInEditor( final String typeName )
//    {
//        try
//        {
//            final IJavaModel model = create( getWorkspace().getRoot() );
//            for( final IJavaProject project : model.getJavaProjects() )
//            {
//                final IType type = project.findType( typeName );
//                if( type != null )
//                {
//                    openInEditor( type );
//                    return;
//                }
//            }
//            openInformation( null, "Not Found", typeName + " not found in workspace." );            
//        }
//        catch( final PartInitException e )
//        {
//            error( "Exception Caught:", e.getMessage(), e );
//        }
//        catch( final JavaModelException e )
//        {
//            error( "Exception Caught:", e.getMessage(), e );
//        }
//    }
//    private void openMethodInEditor( final Method method )
//    {
//        final String typeName = method.getDeclaringClass().getName();
//        try
//        {
//            final IJavaModel model = create( getWorkspace().getRoot() );
//            for( final IJavaProject project : model.getJavaProjects() )
//            {
//                final IType type = project.findType( typeName );
//                if( type != null )
//                {
//                    final IEditorPart editor = openInEditor( type );
//                    final List< IMethod > methods = new ArrayList< IMethod >();
//                    for( final IMethod typeMethod : type.getMethods() )
//                    {
//                        if( typeMethod.getElementName().equals( method.getName() ) )
//                             methods.add( typeMethod );
//                    }
//                    if( methods.size() == 0 )
//                        return;
//                    if( methods.size() == 1 )
//                    {
//                        revealInEditor( editor, ( IJavaElement )methods.get( 0 ) );
//                        return;
//                    }
//                    final IStructuredContentProvider content = new IStructuredContentProvider()
//                    {
//                        public Object[] getElements( final Object inputElement )
//                        {
//                            return methods.toArray( new IMethod[ 0 ] );
//                        }
//                        public void dispose()
//                        {
//                        }
//                        public void inputChanged( Viewer viewer, Object oldInput, Object newInput )
//                        {
//                        }
//                    };
//                    final LabelProvider labelProvider = new LabelProvider()
//                    {
//                        @Override
//                        public String getText( final Object element )
//                        {
//                            if( element instanceof IMethod )
//                            {
//                                final IMethod method = ( IMethod )element;
//                                try
//                                {
//                                    final List< String > types = new ArrayList< String >();
//                                    for( final String type : method.getParameterTypes() )
//                                        types.add( getSignatureSimpleName( type ) );
//                                    return method.getElementName() + "( " + join( types.toArray(), ", " ) + " ): " + getSignatureSimpleName( method.getReturnType() );
//                                }
//                                catch( final JavaModelException e )
//                                {
//                                    logExceptionWarning( e );
//                                }
//                            }
//                            return super.getText( element );
//                        }                        
//                    };
//                    final ListDialog dialog = new ListDialog( null );
//                    dialog.setInput( methods );
//                    dialog.setContentProvider( content );
//                    dialog.setLabelProvider( labelProvider );
//                    dialog.setTitle( "Select the method to open:" );
//                    if( dialog.open() == Window.CANCEL )
//                        return;
//                    final Object[] results = dialog.getResult();
//                    if( results == null || results.length == 0 )
//                        return;
//                    revealInEditor( editor, ( IJavaElement )results[ 0 ] );
//                    return;
//                }
//            }
//            openInformation( null, "Not Found", typeName + " not found in workspace." );            
//        }
//        catch( final PartInitException e )
//        {
//            error( "Exception Caught:", e.getMessage(), e );
//        }
//        catch( final JavaModelException e )
//        {
//            error( "Exception Caught:", e.getMessage(), e );
//        }
//    }
//    private void openFieldInEditor( final Field field )
//    {
//        final String typeName = field.getDeclaringClass().getName();
//        try
//        {
//            final IJavaModel model = create( getWorkspace().getRoot() );
//            for( final IJavaProject project : model.getJavaProjects() )
//            {
//                final IType type = project.findType( typeName );
//                if( type != null )
//                {
//                    final IEditorPart editor = openInEditor( type );
//                    final IField foundField = type.getField( field.getName() );
//                    if( foundField != null )
//                        revealInEditor( editor, ( IJavaElement )foundField );
//                    return;
//                }
//            }
//            openInformation( null, "Not Found", typeName + " not found in workspace." );            
//        }
//        catch( final PartInitException e )
//        {
//            error( "Exception Caught:", e.getMessage(), e );
//        }
//        catch( final JavaModelException e )
//        {
//            error( "Exception Caught:", e.getMessage(), e );
//        }
//    }
}

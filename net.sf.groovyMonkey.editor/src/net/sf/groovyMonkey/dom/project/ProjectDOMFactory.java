package net.sf.groovyMonkey.dom.project;
import static org.eclipse.ui.PlatformUI.getWorkbench;
import net.sf.groovyMonkey.dom.IMonkeyDOMFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.FileEditorInput;

public class ProjectDOMFactory 
implements IMonkeyDOMFactory
{
    public Object getDOMroot()
    {
        final ISelection selection = getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();
        if( selection == null || selection.isEmpty() )
            return "No project selected";
        else if( selection instanceof IStructuredSelection )
        {
            final Object[] array = ( ( IStructuredSelection )selection ).toArray();
            final Object item1 = array[ 0 ];
            if( item1 instanceof IJavaProject )
            {
                final IProject project = ( ( IJavaProject )item1 ).getProject();
                return new Project( project );
            }
            else if( item1 instanceof IResource )
            {
                final IProject project = ( ( IResource )item1 ).getProject();
                return new Project( project );
            }
            else
                return null;
        }
        else if( selection instanceof ITextSelection )
        {
            final ITextSelection textsel = ( ITextSelection )selection;
            final IWorkbenchPage page = getWorkbench().getActiveWorkbenchWindow().getActivePage();
            final IWorkbenchPart part = page.getActiveEditor();
            if( part instanceof IEditorPart )
            {
                final IEditorInput input = ( ( IEditorPart )part ).getEditorInput();
                if( input instanceof FileEditorInput )
                {
                    final FileEditorInput finput = ( FileEditorInput )input;
                    return new Project( finput.getFile().getProject() );
                }
                return "Unable to determine project from text \"" + textsel.getText() + "\"";
            }
            return "Unable to determine project from text \"" + textsel.getText() + "\"";
        }
        else
            return "Cannot determine project from selection " + selection.toString();
    }
}

package net.sf.groovyMonkey.editor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.editors.text.FileDocumentProvider;

public class ScriptDocumentProvider 
extends FileDocumentProvider
{
    @Override
    protected IDocument createDocument( final Object element ) 
    throws CoreException
    {
        return super.createDocument( element );
    }
}

package net.sf.groovyMonkey.editor;
import static net.sf.groovyMonkey.editor.Configuration.LEGAL_TYPES;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.ui.editors.text.FileDocumentProvider;

public class ScriptDocumentProvider 
extends FileDocumentProvider
{
    @Override
    protected IDocument createDocument( final Object element ) 
    throws CoreException
    {
        final IDocument document = super.createDocument( element );
        if( document == null )
            return document;
        final IDocumentPartitioner partitioner = new FastPartitioner( scanner(), LEGAL_TYPES );
        partitioner.connect( document );
        document.setDocumentPartitioner( partitioner );
        return document;
    }
    private IPartitionTokenScanner scanner()
    {
        final IToken metadata = new Token( Configuration.METADATA_PARTITION );
        final IPredicateRule[] rules = { new MultiLineRule( "/*", "*/", metadata ) };
        final RuleBasedPartitionScanner scanner = new RuleBasedPartitionScanner();
        scanner.setPredicateRules( rules );
        return scanner;
    }
}

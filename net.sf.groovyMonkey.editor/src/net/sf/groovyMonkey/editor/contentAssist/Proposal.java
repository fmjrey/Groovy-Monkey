package net.sf.groovyMonkey.editor.contentAssist;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension2;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension3;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension4;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

public class Proposal
implements ICompletionProposal, ICompletionProposalExtension, ICompletionProposalExtension2,
           ICompletionProposalExtension3, ICompletionProposalExtension4
{
    private final String fString;
    private final String fPrefix;
    private final int fOffset;

    public Proposal( final String string,
    				 final String prefix,
    				 final int offset )
    {
        fString = string;
        fPrefix = prefix;
        fOffset = offset;
    }
    public void apply( final IDocument document )
    {
        apply( null, '\0', 0, fOffset );
    }
    public Point getSelection( final IDocument document )
    {
        return new Point( fOffset + fString.length(), 0 );
    }
    public String getAdditionalProposalInfo()
    {
        return null;
    }
    public String getDisplayString()
    {
        return fPrefix + fString;
    }
    public Image getImage()
    {
        return null;
    }
    public IContextInformation getContextInformation()
    {
        return null;
    }
    public void apply( final IDocument document,
    				   final char trigger,
    				   final int offset )
    {
        try
        {
            final String replacement = fString.substring( offset - fOffset );
            document.replace( offset, 0, replacement );
        }
        catch( final BadLocationException x )
        {
            // TODO Auto-generated catch block
            x.printStackTrace();
        }
    }
    public boolean isValidFor( final IDocument document,
    						   final int offset )
    {
        return validate( document, offset, null );
    }
    public char[] getTriggerCharacters()
    {
        return null;
    }
    public int getContextInformationPosition()
    {
        return 0;
    }
    public void apply( final ITextViewer viewer,
    				   final char trigger,
    				   final int stateMask,
    				   final int offset )
    {
        apply( viewer.getDocument(), trigger, offset );
    }
    public void selected( ITextViewer viewer, boolean smartToggle )
    {
    }
    public void unselected( ITextViewer viewer )
    {
    }
    public boolean validate( final IDocument document,
    						 final int offset,
    						 final DocumentEvent event )
    {
        try
        {
            final int prefixStart = fOffset - fPrefix.length();
            return offset >= fOffset && offset < fOffset + fString.length()
                    && document.get( prefixStart, offset - ( prefixStart ) ).equals( ( fPrefix + fString ).substring( 0, offset - prefixStart ) );
        }
        catch( final BadLocationException x )
        {
            return false;
        }
    }
    public IInformationControlCreator getInformationControlCreator()
    {
        return null;
    }
    public CharSequence getPrefixCompletionText( final IDocument document,
    											 final int completionOffset )
    {
        return fPrefix + fString;
    }
    public int getPrefixCompletionStart( final IDocument document,
    									 final int completionOffset )
    {
        return fOffset - fPrefix.length();
    }
    public boolean isAutoInsertable()
    {
        return true;
    }
}

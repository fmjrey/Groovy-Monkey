package net.sf.groovyMonkey.editor;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

public class TagCompletionProposal 
implements ICompletionProposal
{
    private final String tagName;
    
    public TagCompletionProposal( final String tagName )
    {
        this.tagName = tagName;
    }
    public void apply( final IDocument document )
    {
        
    }
    public String getAdditionalProposalInfo()
    {
        return null;
    }
    public IContextInformation getContextInformation()
    {
        return null;
    }
    public String getDisplayString()
    {
        return tagName;
    }
    public Image getImage()
    {
        return null;
    }
    public Point getSelection( final IDocument document )
    {
        return null;
    }
}

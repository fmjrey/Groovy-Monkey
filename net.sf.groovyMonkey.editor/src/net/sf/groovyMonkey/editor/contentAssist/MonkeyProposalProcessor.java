package net.sf.groovyMonkey.editor.contentAssist;
import static java.lang.Character.isJavaIdentifierPart;
import static net.sf.groovyMonkey.ScriptMetadata.getScriptMetadata;
import static net.sf.groovyMonkey.dom.Utilities.getDOMInfo;
import static net.sf.groovyMonkey.util.SetUtil.linkedSet;
import static net.sf.groovyMonkey.util.SetUtil.setAdd;
import static net.sf.groovyMonkey.util.SetUtil.treeSet;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.remove;
import static org.apache.commons.lang.StringUtils.substringAfterLast;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import net.sf.groovyMonkey.DOMDescriptor;
import net.sf.groovyMonkey.ScriptMetadata;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

public class MonkeyProposalProcessor
implements IContentAssistProcessor
{
    private final HippieProposalProcessor hippieProcessor;

    public MonkeyProposalProcessor()
    {
        this.hippieProcessor = new HippieProposalProcessor();
    }
	public ICompletionProposal[] computeCompletionProposals( final ITextViewer viewer,
                                                             final int offset )
    {
    	final Set< ICompletionProposal > proposals = linkedSet();
        addProposals( proposals, viewer, offset );
        for( final ICompletionProposal proposal : hippieProcessor.computeCompletionProposals( viewer, offset ) )
        	addProposal( proposals, proposal );
        return proposals.toArray( new ICompletionProposal[ 0 ] );
    }
    public static Set< ICompletionProposal > addProposals( final Set< ICompletionProposal > set,
    													   final ITextViewer viewer,
    													   final int offset )
    {
        final ScriptMetadata metadata = getScriptMetadata( viewer.getDocument().get() );
        final Map< String, Class<?> > varBinding = new LinkedHashMap< String, Class<?> >();
        for( final DOMDescriptor descriptor : metadata.getDOMs() )
            varBinding.putAll( getDOMInfo( descriptor.pluginName ) );
        final IDocument document = viewer.getDocument();
        try
        {
            final int lineNumber = document.getLineOfOffset( offset );
            final int lineStart = document.getLineOffset( lineNumber );
            final String line = document.get( lineStart, document.getLineLength( lineNumber ) );
            if( isBlank( line ) )
            {
                for( final String varName : varBinding.keySet() )
                    if( metadata.getLang().trim().toLowerCase().equals( "ruby" ) )
                        addProposal( set, new Proposal( "$" + varName, "", offset ) );
                    else
                        addProposal( set, new Proposal( varName, "", offset ) );
                return set;
            }
            final String prefix = getPrefix( viewer, offset );
            final Set< ICompletionProposal > methodSet = treeSet();
            for( final String varName : varBinding.keySet() )
            {
                final String var = metadata.getLang().trim().toLowerCase().equals( "ruby" ) ? "$" + varName.trim() : varName.trim();
                if( var.equals( prefix.trim() ) || var.startsWith( prefix.trim() ) )
                	addProposal( set, new Proposal( remove( varName, prefix ), prefix, offset, " - " + varBinding.get( varName ).getSimpleName() ) );
                if( prefix.trim().equals( var + "." ) || prefix.trim().startsWith( var + "." ) )
                {
                	final String methodPrefix = substringAfterLast( prefix.trim(), "." ).trim();
                	final Class<?> clase = varBinding.get( varName );
                	for( final Method method : clase.getMethods() )
                	{
                		if( method.getName().equals( methodPrefix ) || method.getName().startsWith( methodPrefix ) )
                		{
                			final StringBuffer suggestion = new StringBuffer();
                			suggestion.append( method.getName() );
                			suggestion.append( '(' );
                			final Class<?>[] types = method.getParameterTypes();
                			for( int i = 0; i < types.length; i++ )
                			{
								final Class<?> type = types[ i ];
								suggestion.append( ' ' ).append( type.getSimpleName() );
								if( i < types.length - 1 )
									suggestion.append( ',' );
								else
									suggestion.append( ' ' );
							}
                			suggestion.append( ')' );
                			final StringBuffer additionalInfo = new StringBuffer();
                			additionalInfo.append( " " )
                						  .append( method.getReturnType().getSimpleName() )
                						  .append( " - " )
                						  .append( method.getDeclaringClass().getSimpleName() );
                			addProposal( methodSet, new Proposal( remove( suggestion.toString(), methodPrefix ), methodPrefix, offset, additionalInfo.toString() ) );
                		}
                	}
                }
            }
            setAdd( set, methodSet.toArray( new ICompletionProposal[ 0 ] ) );
        }
        catch( final BadLocationException e )
        {
            throw new RuntimeException( e );
        }
        return set;
    }
    private static void addProposal( final Set< ICompletionProposal> set,
    								 final ICompletionProposal proposal )
    {
    	if( proposalExists( set, proposal ) )
    		return;
    	set.add( proposal );
    }
    private static boolean proposalExists( final Set< ICompletionProposal > set,
    									   final ICompletionProposal proposal )
    {
    	for( final ICompletionProposal p : set )
    	{
    		if( p instanceof Proposal && proposal instanceof Proposal )
    			return p.equals( proposal );
    		if( p instanceof Proposal && !( proposal instanceof Proposal ) )
    			return (( Proposal )p).getComparisonString().trim().equals( proposal.getDisplayString().trim() );
    		if( !( p instanceof Proposal ) && proposal instanceof Proposal )
    			return p.getDisplayString().trim().equals( (( Proposal )proposal).getComparisonString().trim() );
			if( p.getDisplayString().trim().equals( proposal.getDisplayString().trim() ) )
				return true;
    	}
    	return false;
    }
    public static String getPrefix( final ITextViewer viewer,
                                    int offset )
    throws BadLocationException
    {
        final IDocument doc= viewer.getDocument();
        if( doc == null || offset > doc.getLength() )
            return "";

        int length= 0;
        while( --offset >= 0 && ( isJavaIdentifierPart( doc.getChar( offset ) ) || doc.getChar( offset ) == '.' ) )
            length++;

        return doc.get( offset + 1, length );
    }
    public IContextInformation[] computeContextInformation( final ITextViewer viewer,
                                                            final int offset )
    {
        return hippieProcessor.computeContextInformation( viewer, offset );
    }
    public char[] getCompletionProposalAutoActivationCharacters()
    {
        return hippieProcessor.getCompletionProposalAutoActivationCharacters();
    }
    public char[] getContextInformationAutoActivationCharacters()
    {
        return hippieProcessor.getContextInformationAutoActivationCharacters();
    }
    public IContextInformationValidator getContextInformationValidator()
    {
        return hippieProcessor.getContextInformationValidator();
    }
    public String getErrorMessage()
    {
        return hippieProcessor.getErrorMessage();
    }
}

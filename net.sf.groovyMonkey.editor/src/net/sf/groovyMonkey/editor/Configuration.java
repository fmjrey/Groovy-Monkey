package net.sf.groovyMonkey.editor;
import static org.eclipse.jface.text.IDocument.DEFAULT_CONTENT_TYPE;
import net.sf.groovyMonkey.editor.contentAssist.MonkeyProposalProcessor;
import net.sf.groovyMonkey.editor.contentAssist.QuickAssistProcessor;
import net.sf.groovyMonkey.editor.contentAssist.ScriptMetadataContentAssistProcessor;
import org.apache.commons.lang.Validate;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.quickassist.IQuickAssistAssistant;
import org.eclipse.jface.text.quickassist.QuickAssistAssistant;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

public class Configuration
extends SourceViewerConfiguration
{
    public static final String METADATA_PARTITION = "__groovymonkey_metadata";
    static final String[] LEGAL_TYPES = { METADATA_PARTITION };
    private final IAdaptable adapter;

    public Configuration( final IAdaptable adapter )
    {
        Validate.notNull( adapter );
        this.adapter = adapter;
    }
    @Override
    public IQuickAssistAssistant getQuickAssistAssistant( final ISourceViewer sourceViewer )
    {
        final IQuickAssistAssistant assistant = new QuickAssistAssistant();
        assistant.setQuickAssistProcessor( new QuickAssistProcessor( adapter ) );
        return assistant;
    }
    @Override
    public IContentAssistant getContentAssistant( final ISourceViewer sourceViewer )
    {
        final ContentAssistant assistant = new ContentAssistant();
        assistant.setContentAssistProcessor( new ScriptMetadataContentAssistProcessor(), METADATA_PARTITION );
        assistant.setContentAssistProcessor( new MonkeyProposalProcessor(), DEFAULT_CONTENT_TYPE );
        assistant.setInformationControlCreator( getInformationControlCreator( sourceViewer ) );
        return assistant;
    }
}

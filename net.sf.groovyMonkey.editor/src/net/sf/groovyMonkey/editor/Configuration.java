package net.sf.groovyMonkey.editor;
import org.apache.commons.lang.Validate;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.text.quickassist.IQuickAssistAssistant;
import org.eclipse.jface.text.quickassist.QuickAssistAssistant;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

public class Configuration 
extends SourceViewerConfiguration
{
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
}

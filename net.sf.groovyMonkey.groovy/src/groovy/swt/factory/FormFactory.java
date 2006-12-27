/*
 * Created on Feb 27, 2004
 */
package groovy.swt.factory;
import static groovy.swt.SwtUtils.getParentWidget;
import static org.eclipse.swt.widgets.Display.getCurrent;
import groovy.swt.InvalidParentException;
import groovy.swt.SwtUtils;
import java.util.Map;
import org.codehaus.groovy.GroovyException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.forms.HyperlinkSettings;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.ScrolledPageBook;
import org.eclipse.ui.forms.widgets.Section;

/**
 * @author <a href:ckl at dacelo.nl">Christiaan ten Klooster </a>
 * @version $Revision$
 */
public class FormFactory
extends AbstractSwtFactory
implements SwtFactory
{
    /** static is evil, too many toolkits is evil */
    static FormToolkit toolkit;
    /**
     * @return Returns the toolkit.
     */
    public static FormToolkit getToolkit()
    {
        if( toolkit == null )
        {
            final FormColors formColors = new FormColors( getCurrent() == null ? new Display() : getCurrent() );
            toolkit = new FormToolkit( formColors );
        }
        return toolkit;
    }
    /** type of */
    private String type;
    /**
     * @param string
     */
    public FormFactory( final String type )
    {
        this.type = type;
    }
    /*
     * @see groovy.swt.factory.AbstractSwtFactory#newInstance(java.util.Map,
     *      java.lang.Object)
     */
    @Override
    public Object newInstance( final Map properties,
                               final Object parent )
    throws GroovyException
    {
        // boolean shouldLayout = properties.containsKey("parent");
        final Composite parentComposite = ( Composite )getParentWidget( parent, properties );
        final String styleProperty = ( String )properties.remove( "style" );
        final String text = ( String )properties.remove( "text" );
        int style = SWT.NULL;
        if( styleProperty != null )
            if( type.equals( "formSection" ) )
                style = SwtUtils.parseStyle( Section.class, styleProperty );
            else
                style = SwtUtils.parseStyle( SWT.class, styleProperty );
        if( parentComposite != null )
        {
            final Object formWidget = getFormWidget( parentComposite, properties, style, text );
            setBeanProperties( formWidget, properties );
            // if (shouldLayout && parentComposite != null && parentComposite
            // instanceof Composite) {
            // ((Composite) parentComposite).layout();
            // }
            return formWidget;
        }
        throw new InvalidParentException( "composite instance" );
    }
    /**
     * @param parentComposite
     * @param style
     * @param text
     * @return
     */
    private Object getFormWidget( final Composite parentComposite,
                                  final Map properties,
                                  final int style,
                                  final String text )
    throws GroovyException
    {
        if( "form".equals( type ) )
        {
            final Form form = getToolkit().createForm( parentComposite );
            form.setText( text );
            return form;
        }
        if( "scrolledForm".equals( type ) )
        {
            final ScrolledForm scrolledForm = getToolkit().createScrolledForm( parentComposite );
            scrolledForm.setText( text );
            return scrolledForm;
        }
        if( "formButton".equals( type ) )
            return getToolkit().createButton( parentComposite, text, style );
        if( "formColors".equals( type ) )
            return getToolkit().getColors();
        if( "formComposite".equals( type ) )
            return getToolkit().createComposite( parentComposite, style );
        if( "formCompositeSeparator".equals( type ) )
            return getToolkit().createCompositeSeparator( parentComposite );
        if( "formExpandableComposite".equals( type ) )
            return getToolkit().createExpandableComposite( parentComposite, style );
        if( "formText".equals( type ) )
        {
            final Text text2 = getToolkit().createText( parentComposite, text, style );
            getToolkit().paintBordersFor( parentComposite );
            return text2;
        }
        if( "formHyperlink".equals( type ) )
            return getToolkit().createHyperlink( parentComposite, text, style );
        if( "formImageHyperlink".equals( type ) )
            return getToolkit().createImageHyperlink( parentComposite, style );
        if( "formLabel".equals( type ) )
            return getToolkit().createLabel( parentComposite, text, style );
        if( "formPageBook".equals( type ) )
            return getToolkit().createPageBook( parentComposite, style );
        if( "formPageBookPage".equals( type ) )
        {
            if( parentComposite instanceof ScrolledPageBook )
            {
                final ScrolledPageBook pageBook = ( ScrolledPageBook )parentComposite;
                final String key = ( String )properties.remove( "key" );
                if( key != null )
                {
                    final Composite page = pageBook.createPage( key );
                    pageBook.registerPage( key, page );
                    return page;
                }
                throw new GroovyException( "attribute \"key\" is null" );
            }
            throw new InvalidParentException( "formPageBook" );
        }
        if( "formSection".equals( type ) )
        {
            final Section section = getToolkit().createSection( parentComposite, style );
            if( text != null )
                section.setText( text );
            section.setSeparatorControl( getToolkit().createCompositeSeparator( section ) );
            final String description = ( String )properties.remove( "description" );
            if( description != null )
                section.setDescription( description );
            final Composite client = getToolkit().createComposite( section );
            client.setLayout( new GridLayout() );
            section.setClient( client );
            return section;
        }
        if( "formSeparator".equals( type ) )
            return getToolkit().createSeparator( parentComposite, style );
        if( "formTable".equals( type ) )
            return getToolkit().createTable( parentComposite, style );
        if( "formToolkit".equals( type ) )
            return getToolkit();
        if( "formFormattedText".equals( type ) )
        {
            boolean parseTags = false;
            boolean expandURLs = false;
            if( properties.get( "parseTags" ) != null )
                parseTags = ( ( Boolean )properties.remove( "parseTags" ) ).booleanValue();
            if( properties.get( "expandURLs" ) != null )
                expandURLs = ( ( Boolean )properties.remove( "expandURLs" ) ).booleanValue();
            final FormText formText = getToolkit().createFormText( parentComposite, true );
            final HyperlinkSettings hyperlinkSettings = new HyperlinkSettings( Display.getCurrent() );
            hyperlinkSettings.setBackground( getToolkit().getColors().getBackground() );
            hyperlinkSettings.setActiveBackground( getToolkit().getColors().getBackground() );
            hyperlinkSettings.setForeground( getToolkit().getColors().getForeground() );
            hyperlinkSettings.setActiveForeground( getToolkit().getColors().getBackground() );
            formText.setHyperlinkSettings( hyperlinkSettings );
            formText.setText( text, parseTags, expandURLs );
            return formText;
        }
        if( "formTree".equals( type ) )
            return getToolkit().createTree( parentComposite, style );
        return null;
    }
}

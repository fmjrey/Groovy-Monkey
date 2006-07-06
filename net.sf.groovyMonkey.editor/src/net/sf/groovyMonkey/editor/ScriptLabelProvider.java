package net.sf.groovyMonkey.editor;
import static net.sf.groovyMonkey.GroovyMonkeyPlugin.icon;
import static net.sf.groovyMonkey.dom.Utilities.hasDOM;
import net.sf.groovyMonkey.DOMDescriptor;
import net.sf.groovyMonkey.editor.ScriptContentProvider.BundleDescriptor;
import net.sf.groovyMonkey.editor.ScriptContentProvider.ClassDescriptor;
import net.sf.groovyMonkey.editor.ScriptContentProvider.ExecDescriptor;
import net.sf.groovyMonkey.editor.ScriptContentProvider.JobDescriptor;
import net.sf.groovyMonkey.editor.ScriptContentProvider.FieldDescriptor;
import net.sf.groovyMonkey.editor.ScriptContentProvider.LangDescriptor;
import net.sf.groovyMonkey.editor.ScriptContentProvider.MenuDescriptor;
import net.sf.groovyMonkey.editor.ScriptContentProvider.MethodDescriptor;
import net.sf.groovyMonkey.editor.ScriptContentProvider.PackageDescriptor;
import net.sf.groovyMonkey.editor.ScriptContentProvider.VarDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

public class ScriptLabelProvider 
extends LabelProvider
{
    @Override
    public Image getImage( final Object element )
    {
        if( element instanceof MenuDescriptor )
            return icon( "submenu.gif" );
        if( element instanceof LangDescriptor )
            return icon( "classpath.gif" );
        if( element instanceof JobDescriptor )
            return icon( "run_exc.gif" );
        if( element instanceof ExecDescriptor )
            return icon( "progress_none.gif" );
        if( element instanceof DOMDescriptor )
        {
            if( !hasDOM( (( DOMDescriptor )element).plugin_name ) )
                return icon( "notinstalled_feature_obj.gif" );
            return icon( "feature_obj.gif" );
        }
        if( element instanceof VarDescriptor )
            return icon( "variable_tab.gif" );
        if( element instanceof ClassDescriptor )
            return icon( "class_obj.gif" );
        if( element instanceof MethodDescriptor )
            return icon( "methpub_obj.gif" );
        if( element instanceof FieldDescriptor )
            return icon( "field_public_obj.gif" );
        if( element instanceof String )
        {
            if( element.toString().endsWith( ".jar" ) )
                return icon( "jar_obj.gif" );
            return icon( "fldr_obj.gif" );
        }
        if( element instanceof BundleDescriptor )
            return icon( "plugin_obj.gif" );
        if( element instanceof PackageDescriptor )
            return icon( "package_obj.gif" );
        return super.getImage( element );
    } 
}

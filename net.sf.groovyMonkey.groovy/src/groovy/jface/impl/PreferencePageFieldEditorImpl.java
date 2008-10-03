/*
 * Created on Feb 20, 2004
 *
 */
package groovy.jface.impl;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.widgets.Composite;

/**
 * @author <a href="mailto:ckl@dacelo.nl">Christiaan ten Klooster </a>
 * @version $Revision$
 */
public class PreferencePageFieldEditorImpl extends FieldEditorPreferencePage {

    public static class FieldEditorCreator {

        private final Class<?> beanClass;

        private final String propertyName;

        private final String title;

        public FieldEditorCreator(Class<?> beanClass, String propertyName,
                String title) {
            this.beanClass = beanClass;
            this.propertyName = propertyName;
            this.title = title;
        }

        public FieldEditor createField(Composite parent) {

            FieldEditor fieldEditor = null;

            try {
                Class<?>[] types = { String.class, String.class, Composite.class};
                Constructor<?> constructor = beanClass.getConstructor(types);
                if (constructor != null) {
                    Object[] arguments = { propertyName, title, parent};
                    fieldEditor = (FieldEditor) constructor
                            .newInstance(arguments);
                }
            } catch (Exception e) {
                throw new RuntimeException( e );
            }

            return fieldEditor;
        }
    }

    private final List< FieldEditorCreator > creatorFieldsfields = new ArrayList< FieldEditorCreator >();

    public PreferencePageFieldEditorImpl(String title) {
        super(title, FieldEditorPreferencePage.GRID);
    }

    public void addFieldCreator(Class<?> beanClass, String propertyName,
            String title) {
        creatorFieldsfields.add(new FieldEditorCreator(beanClass, propertyName,
                title));
    }

    @Override
    protected void createFieldEditors() {
        Iterator<?> i = creatorFieldsfields.iterator();
        while (i.hasNext()) {
            FieldEditorCreator creator = (FieldEditorCreator) i.next();
            FieldEditor fieldEditor = creator
                    .createField(getFieldEditorParent());
            addField(fieldEditor);
        }
    }

}

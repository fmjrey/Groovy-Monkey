/*
 * Created on Feb 16, 2004
 *
 */
package groovy.swt;

import org.codehaus.groovy.GroovyException;

/**
 * @author <a href="mailto:ckl@dacelo.nl">Christiaan ten Klooster </a>
 * @version $Revision$
 */
public class InvalidParentException extends GroovyException {

    private static final long serialVersionUID = -3028426155147185670L;

    /**
     * @param message
     */
    public InvalidParentException(String property) {
        super("parent should be: " + property);
    }
}

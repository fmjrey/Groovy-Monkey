/*
 * Menu: testIncludeInBundleBeanshell
 * Kudos: James E. Ervin
 * License: EPL 1.0
 * LANG: Beanshell
 * Include-Bundle: org.apache.ant
 */
import net.sf.groovymonkey.tests.fixtures.dom.TestDOM;
import org.apache.tools.ant.BuildLogger;

Class.forName( "net.sf.groovymonkey.tests.fixtures.dom.TestDOM" );
clase = Class.forName( "org.apache.tools.ant.BuildLogger" );
System.out.println( "testIncludeInBeanshell: clase: " + clase );
new TestDOM().callDOM( "testIncludeInBundleBeanshell" );

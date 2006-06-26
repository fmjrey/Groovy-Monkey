/*
 * Menu: testIncludeInBundleGroovy
 * Kudos: James E. Ervin
 * License: EPL 1.0
 * LANG: Groovy
 * Include-Bundle: net.sf.groovymonkey.tests
 */
import net.sf.groovymonkey.tests.fixtures.dom.TestDOM
println 'Hello'
new TestDOM().callDOM( 'testIncludeInBundleGroovy' )

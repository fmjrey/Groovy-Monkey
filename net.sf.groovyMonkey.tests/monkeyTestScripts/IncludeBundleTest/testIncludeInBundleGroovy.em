/*
 * Menu: testIncludeInBundleGroovy
 * Kudos: James E. Ervin
 * License: EPL 1.0
 * LANG: Groovy
 * Include-Bundle: org.apache.ant
 */
import net.sf.groovymonkey.tests.fixtures.dom.TestDOM
println 'Hello'
def ant = new AntBuilder()
{
	echo( "hello" )
}
new TestDOM().callDOM( 'testIncludeInBundleGroovy' )


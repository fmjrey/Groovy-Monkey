/*
 * Menu: testIncludeInBundleDefault
 * Kudos: James E. Ervin
 * License: EPL 1.0
 * Include-Bundle: net.sf.groovyMonkey.tests
 * Include-Bundle: org.apache.ant
 */
function main()
{
	importPackage( net.sf.groovymonkey.tests.fixtures.dom );
	importPackage( org.apache.tools.ant )
	testInclude = new TestDOM();
	testInclude.callDOM( "testIncludeInBundleDefault" );
}

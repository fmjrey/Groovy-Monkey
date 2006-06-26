/*
 * Menu: testIncludeInBundleJavascript
 * Kudos: James E. Ervin
 * License: EPL 1.0
 * LANG: Javascript
 * Include-Bundle: net.sf.groovyMonkey.tests
 */
function main()
{
	importPackage( net.sf.groovymonkey.tests.fixtures.dom );
	testInclude = new TestDOM();
	testInclude.callDOM( "testIncludeInBundleDefault" );
}

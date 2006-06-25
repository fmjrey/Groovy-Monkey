/*
 * Menu: testIncludeInJavascript
 * Kudos: James E. Ervin
 * License: EPL 1.0
 * LANG: Javascript
 * Include: TestJavaProject/bin
 * DOM: http://test.update.site/net.sf.groovyMonkey.tests
 */
function main()
{
	testInclude = new net.sf.groovymonkey.tests.TestInclude();
	testInclude.callDOM( "testIncludeInDefault" );
}

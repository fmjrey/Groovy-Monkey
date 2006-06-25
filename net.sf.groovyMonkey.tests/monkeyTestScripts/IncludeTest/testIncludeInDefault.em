/*
 * Menu: testIncludeInDefault
 * Kudos: James E. Ervin
 * License: EPL 1.0
 * Include: TestJavaProject/bin
 * DOM: http://test.update.site/net.sf.groovyMonkey.tests
 */
function main()
{
	importPackage( net.sf.groovymonkey.tests );
	testInclude = new TestInclude();
	testInclude.callDOM( "testIncludeInDefault" );
}

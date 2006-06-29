/*
 * Menu: testIncludeInTcl
 * Kudos: James E. Ervin
 * License: EPL 1.0
 * LANG: Tcl
 * Include: TestJavaProject/bin
 */
package require java
set testDOMVar [java::new net.sf.groovymonkey.tests.TestInclude]
$testDOMVar callDOM "testIncludeInTcl"

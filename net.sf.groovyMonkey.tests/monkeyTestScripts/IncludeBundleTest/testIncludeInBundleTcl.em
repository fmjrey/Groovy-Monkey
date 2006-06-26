/*
 * Menu: testIncludeInBundleTcl
 * Kudos: James E. Ervin
 * License: EPL 1.0
 * LANG: Tcl
 * Include-Bundle: net.sf.groovyMonkey.tests
 */
package require java
set testDOMVar [java::new net.sf.groovymonkey.tests.fixtures.dom.TestDOM]
$testDOMVar callDOM "testIncludeInBundleTcl"

/*
 * Menu: testIncludeInBundleRuby
 * Kudos: James E. Ervin
 * License: EPL 1.0
 * LANG: Ruby
 * Include-Bundle: net.sf.groovyMonkey.tests
 */
require 'java'
include_class 'net.sf.groovymonkey.tests.fixtures.dom.TestDOM'
testInclude = TestDOM.new
testInclude.callDOM( 'testIncludeInBundleRuby' )

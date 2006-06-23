/*
 * Menu: testIncludeInRuby
 * Kudos: James E. Ervin
 * License: EPL 1.0
 * LANG: Ruby
 * Include: TestJavaProject/bin
 * DOM: http://test.update.site/net.sf.groovyMonkey.tests
 */
require 'java'
include_class 'net.sf.groovymonkey.tests.TestInclude'
testInclude = TestInclude.new
testInclude.callDOM( 'testIncludeInRuby' )

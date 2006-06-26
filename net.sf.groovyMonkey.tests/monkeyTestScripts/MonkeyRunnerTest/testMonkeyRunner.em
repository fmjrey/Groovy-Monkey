/*
 * Menu: testMonkeyRunner
 * Kudos: James E. Ervin
 * License: EPL 1.0
 * LANG: Groovy
 * DOM: http://test.update.site/net.sf.groovyMonkey.tests
 */
def map = [ message : 'testMonkeyRunner', returnedValue : [ : ] ]
monkeyRunner.runScript( window, '/TestMonkeyProject/includedScripts/monkeyRunner.em', map )

if( map.returnedValue.returned != true )
	throw new RuntimeException( "Error no returned value: " + map.returnedValue )

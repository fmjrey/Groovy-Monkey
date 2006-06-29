/*
 * Menu: testMonkeyRunner
 * Kudos: James E. Ervin
 * License: EPL 1.0
 * LANG: Groovy
 */
map.message = 'testMonkeyRunner'
map.returnedValue = new java.util.HashMap()
monkeyRunner.runScript( window, '/TestMonkeyProject/includedScripts/monkeyRunner.em', map, monitor )
if( map.returnedValue.returned != true )
	throw new RuntimeException( "Error no returned value: " + map.returnedValue )

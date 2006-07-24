/*
 * Menu: Install Test DOM
 * Kudos: James E. Ervin
 * License: EPL 1.0
 * DOM: http://groovy-monkey.sourceforge.net/update/net.sf.groovyMonkey.dom
 */
import java.io.File
import org.apache.commons.io.FileUtils

def bundleVersion = map.bundleVersion
def deployDir = map.deployDir
def plugin = 'net.sf.test.dom'
map.plugin = plugin
runnerDOM.runScript( window, "TestMonkeyProject/lib/uninstall.em", map )

def context = bundleDOM.context()
def installedBundle = context.installBundle( "file:" + deployDir + "/" + plugin + "_" + bundleVersion + ".jar" )
installedBundle.start()

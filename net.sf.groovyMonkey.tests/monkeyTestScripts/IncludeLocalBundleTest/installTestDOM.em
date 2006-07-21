/*
 * Menu: Install Test DOM
 * Kudos: James E. Ervin
 * License: EPL 1.0
 * DOM: http://groovy-monkey.sourceforge.net/update/net.sf.groovyMonkey.dom
 */
import java.io.File
import org.apache.commons.io.FileUtils

def plugin = 'net.sf.test.dom'
map.plugin = plugin
runner.runScript( window, "Eclipse Monkey Examples/lib/uninstall.em", map )

runner.runScript( window, "Eclipse Monkey Examples/lib/getBundleVersion.em", map )
def bundleVersion = map.bundleVersion

bundlerDOM.createDeployDir()
jface.syncExec
{
	bundlerDOM.buildPluginJar( workspace.getRoot().getProject( plugin ) )
}

def target = new File( "C:/eclipse3.2/eclipse/plugins/" + plugin + "_" + bundleVersion + ".jar" )
if( target.exists() )
	FileUtils.forceDelete( target )
FileUtils.copyFile( new File( "C:/tmp/deployedBundles/plugins/" + plugin + "_" + bundleVersion + ".jar" ), 
					target )

def context = bundle.context()
def installedBundle = context.installBundle( "file:C:/eclipse3.2/eclipse/plugins/" + plugin + "_" + bundleVersion + ".jar" )
installedBundle.start()

/*
 * Menu: uninstall
 * Kudos: James E. Ervin
 * License: EPL 1.0
 * DOM: http://groovy-monkey.sourceforge.net/update/net.sf.groovyMonkey.dom
 */
import net.sf.groovyMonkey.dom.Utilities

def pluginToUninstall = map.plugin
for( plugin in bundle.context().getBundles() )
{
	if( plugin.getSymbolicName().equals( pluginToUninstall ) )
		plugin.uninstall()
}

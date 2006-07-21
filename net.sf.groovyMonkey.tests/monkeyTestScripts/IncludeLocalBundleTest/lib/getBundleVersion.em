/*
 * Kudos: James E. Ervin
 * License: EPL 1.0
 * DOM: http://groovy-monkey.sourceforge.net/update/net.sf.groovyMonkey.dom
 */
import java.util.jar.Manifest

def file = workspace.getRoot().getProject( map.plugin ).getFile( 'META-INF/MANIFEST.MF' )
def input = file.getContents()
try
{
	def manifest = new Manifest( input )
	def attributes = manifest.getMainAttributes()
	def bundleVersion = attributes.getValue( 'Bundle-Version' )
	map.bundleVersion = bundleVersion
}
finally
{
	input.close()
}

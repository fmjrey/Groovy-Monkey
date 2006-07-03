/*
 * Menu: Find System Prints - Groovy
 * Kudos: Bjorn Freeman-Benson & Ward Cunningham & James E. Ervin
 * License: EPL 1.0
 * LANG: Groovy
 * DOM: http://download.eclipse.org/technology/dash/update/org.eclipse.dash.doms
 * DOM: http://groovy-monkey.sourceforge.net/update/plugins/net.sf.groovyMonkey.dom
 */

def files = resources.filesMatching(".*\\.java")
for( file in files )
{
	file.removeMyTasks()
  	for( line in file.lines ) 
  	{
    	if( line.string.contains( 'System.out.print' ) && !line.string.trim().startsWith( '//' ) ) 
    	{
       		line.addMyTask( line.string.trim() )
    	}
  	}
}
window.getActivePage().showView( 'org.eclipse.ui.views.TaskList' )

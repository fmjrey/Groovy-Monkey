/*
 * Menu: Find System Prints - Python
 * Kudos: Bjorn Freeman-Benson & Ward Cunningham & James E. Ervin
 * License: EPL 1.0
 * LANG: Python
 * DOM: http://download.eclipse.org/technology/dash/update/org.eclipse.dash.doms
 */
files = resources.filesMatching('.*\\.java').tolist()
for file in files:
	file.removeMyTasks()
	lines = file.lines.tolist()
	for line in lines:
		if line.getString().find( 'System.out.print' ) != -1:
			line.addMyTask( line.getString().strip() )

window.getActivePage().showView( 'org.eclipse.ui.views.TaskList' )

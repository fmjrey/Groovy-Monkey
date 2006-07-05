/*
 * Menu: Find System Prints - Ruby
 * Kudos: Bjorn Freeman-Benson & Ward Cunningham & James E. Ervin
 * License: EPL 1.0
 * LANG: Ruby
 * Job: UIJob
 * DOM: http://download.eclipse.org/technology/dash/update/org.eclipse.dash.doms
 */
 files = $resources.filesMatching( ".*\\.java" )
 for file in files
	file.removeMyTasks()
	for line in file.getLines()
		line.addMyTask( line.getString().to_s().strip() ) if line.getString().to_s() =~ /System\.out\.print(ln)? *\(.*\)/
	end
 end
 
 $window.getActivePage().showView( 'org.eclipse.ui.views.TaskList' )
 
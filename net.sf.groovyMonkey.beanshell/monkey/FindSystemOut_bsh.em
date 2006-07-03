/*
 * Menu: Find System Prints - Beanshell
 * Kudos: Bjorn Freeman-Benson & Ward Cunningham & James E. Ervin
 * License: EPL 1.0
 * LANG: Beanshell
 * DOM: http://download.eclipse.org/technology/dash/update/org.eclipse.dash.doms
 */

files = resources.filesMatching( ".*\\.java" );
for( i = 0; i < files.length; i++ )
{
  files[i].removeMyTasks();
  lines = files[i].lines;
  for( j = 0; j < lines.length; j++ ) 
  {
    if( lines[j].string.contains( "System.out.print" ) )
       lines[j].addMyTask( lines[j].string.trim() );
  }
}
final Runnable runnable = new Runnable()
{
	public void run()
	{
		window.getActivePage().showView( "org.eclipse.ui.views.TaskList" );
	}
};
runner.asyncExec( runnable );

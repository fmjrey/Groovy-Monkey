/*
 * Menu: Launch Manager
 * Kudos: James E. Ervin
 * License: EPL 1.0
 * LANG: Groovy
 * DOM: http://groovy-monkey.sourceforge.net/update/net.sf.groovyMonkey.dom
 * Exec-Mode: UIJob
 */
import org.eclipse.swt.SWT
 
def configurations = launchManager.manager().getLaunchConfigurations()
def selected = []
shell = jface.shell( text: 'Select desired configurations:', location: [ 100, 100 ] )
{
    gridLayout()
	table = table( toolTipText: "Select configurations to execute", style:'check,border,v_scroll,h_scroll,single' ) 
	{
        gridLayout()
        gridData( horizontalAlignment: SWT.FILL, verticalAlignment: SWT.FILL, grabExcessHorizontalSpace: true, grabExcessVerticalSpace: true, heightHint: 400 )
		for( config in configurations )
		{
			tableItem().setText( "${config.name}" )
		}
	}
    composite()
    {
       	gridLayout( numColumns: 2 )
       	button( text: 'run', background: [ 0, 255, 255 ] )
       	{
       	    onEvent( type: 'Selection', closure: 
       	    {
       	        for( item in table.items )
       	        {
       	            if( !item.checked )
       	                continue
       	            selected.add( item.text )
       	        }
       	        shell.close()
       	    } )
       	 }
        button( text: "cancel", background: [ 0, 255, 255 ] )
        {
            onEvent( type: 'Selection', closure: 
       	    {
				selected.clear()
       	        shell.close()
       	    } )
        }
    }
}
shell.pack()
shell.open()
while( !shell.isDisposed() ) 
{ 
    if( !shell.display.readAndDispatch() )
		shell.display.sleep()
}
selected.each 
{
	out.println "${it}"
}
 
launchManager.launch( 'launch test', selected )

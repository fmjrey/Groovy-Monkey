/*
 * Menu: Test SWT - Groovy
 * Kudos: James E. Ervin
 * License: EPL 1.0
 * DOM: http://groovy-monkey.sourceforge.net/update/plugins/groovy.swt
 * Exec-Mode: UIJob
 */
 
 def subapp = jface.shell( window.getShell() ) 
 {	
 	gridLayout()
    group( text:"Groovy SWT", background:[255, 255, 255] ) 
    {
    	gridLayout()
        label( text:"groove fun !" ,background:[255, 255, 255] )
        label( text:"Email: ckl@dacelo.nl", background:[255, 255, 255] )
    }
}
subapp.pack()
subapp.open()

package net.groovy.example
import org.apache.commons.httpclient.*
import org.apache.commons.httpclient.auth.*
import org.apache.commons.httpclient.methods.*
import org.apache.commons.lang.builder.*
import org.eclipse.swt.SWT
import org.eclipse.swt.events.TreeEvent
import org.eclipse.swt.events.TreeListener
import org.eclipse.swt.layout.FormAttachment
import org.eclipse.swt.program.Program
import org.eclipse.swt.widgets.TreeItem
import groovy.jface.JFaceBuilder
import groovy.util.XmlSlurper
import java.util.logging.Level
import java.util.logging.Logger

class DeliciousAPI 
implements CredentialsProvider, Runnable, TreeListener
{
	static void main( args )
	{
	    Logger.getLogger( 'net' ).setLevel( Level.ALL )
	    Logger.getLogger( 'org' ).setLevel( Level.ALL )
	    new DeliciousAPI().run()
  	}
    def authMap = [ : ]
    def client = new HttpClient()
    def tags = []
    def jface = new JFaceBuilder()
    def shell
    def tree
    def deliciousUser = 'jervin'
    def deliciousPassword = 'atreus'
    def proxyUser = 'proxyUser'
    def proxyPassword = 'proxyPassword'
    def proxyDomain = 'proxyDomain'
    def proxyHost = 'proxyHost'
    
    public DeliciousAPI()
    {
    }
    public void treeCollapsed( TreeEvent e )
    {
        println "treeCollapsed(): event: ${e.dump()}"
    }
    public void treeExpanded( TreeEvent e )
    {
        println "treeExpanded(): event: ${e.dump()}"
    }
	public void run()
	{
    	client.params.setParameter( CredentialsProvider.PROVIDER, this )
    	client.hostConfiguration.setProxy( proxyHost, 80 )
    	def creds = new UsernamePasswordCredentials( deliciousUser, deliciousPassword )
    	client.state.setCredentials( new AuthScope( 'del.icio.us', 80 ), creds )
    	showExistingTags()
        tags.each
        { tag ->
        	println "tag: ${tag}"
        	//openTagInBrowser( tag )
        }
    	println 'finished....'
    }
	private void showExistingTags()
	{
	    def url = 'http://del.icio.us/api/tags/get'
	    def method = new GetMethod( url )
	    println "getting tags from: ${url}"
	    println "method: ${method}"
        client.executeMethod( method )
    	println 'status: ' + method.statusText
        def existingTags = new XmlSlurper().parseText( method.responseBodyAsString )
        def listener = this
        shell = jface.shell( text: 'Select desired tags:', location: [ 100, 100 ], size: [ 800, 700 ] )
        {
       	    gridLayout()
       		tree = tree( toolTipText: "Select tags to open in browser", style:'check,border,v_scroll,h_scroll', size: [ 600, 400 ] ) 
   			{
       	        gridLayout()
       	        gridData( horizontalAlignment: SWT.FILL, verticalAlignment: SWT.FILL, grabExcessHorizontalSpace: true, grabExcessVerticalSpace: true, heightHint: 400 )
				for( tag in existingTags.tag )
					treeItem( text: "${tag.@tag}" )
       	        onEvent( type: 'Selection', closure:
       	        { event ->
					println 'Selection: ' + event.dump()
					println 'Selection< Item >: ' + event.item.dump()
					def urls = getURLs( event.item.text )
					println 'Selection< Tag URLs >: ' + urls
					for( tagURL in urls )
					    new TreeItem( event.item, SWT.NONE ).setText( tagURL )
       	        } )
			}
       	    tree.addTreeListener( listener )
       	    composite()
       	    {
       	        gridLayout( numColumns: 2 )
       	 		button( text: 'open', background: [ 0, 255, 255 ] )
       	 		{
       	            onEvent( type: 'Selection', closure: 
       	            {
       	                for( item in tree.items )
       	                {
       	                    if( !item.checked )
       	                        continue
       	                    tags.add( item.text )
       	                }
       	                shell.close()
       	            } )
       	 		}
        		button( text: "cancel", background: [ 0, 255, 255 ] )
        		{
        		    onEvent( type: 'Selection', closure: 
       	            {
						tags.clear()
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
        tags.each 
        {
    		println "${it}"
    	}
	}
	def getURLs( tag )
	{
	    def method = new GetMethod( "http://del.icio.us/api/posts/recent?&tag=${tag}" )
	    client.executeMethod( method )
	    def responseBody = method.responseBodyAsString
	    println 'status: ' + method.statusText
        def tags = new XmlSlurper().parseText( responseBody )
        def urls = []
        tags.post.each 
        { post ->
        	urls.add( "${post.@href}" )
	    }
	    return urls
	}
	private void openTagInBrowser( tag )
	{
	    def method = new GetMethod( "http://del.icio.us/api/posts/all?&tag=${tag}" )
	    client.executeMethod( method )
	    def responseBody = method.responseBodyAsString
	    println 'status: ' + method.statusText
        def tags = new XmlSlurper().parseText( responseBody )
        tags.post.each 
        { post ->
	    	Program.launch( "${post.@href}" )
	    }
	}
    private String readConsole()
    {
        return System.in.readLine()
    }
    public Credentials getCredentials( AuthScheme authscheme, String host, int port, boolean proxy )
    throws CredentialsNotAvailableException 
    {
        if( authscheme == null )
            return null
        println "authScheme: ${authscheme}"
        def key = authscheme.schemeName + '@' + host + ':' + port
        println "key: ${key}"
        if( authMap[ key ] != null )
            return authMap[ key ]
        try
        {
            if( authscheme instanceof NTLMScheme ) 
            {
                println host + ":" + port + " requires Windows authentication"
                return authMap[ key ] = new NTCredentials( proxyUser, proxyPassword, host, proxyDomain )
            }
            if( authscheme instanceof RFC2617Scheme )
            {
                println( host + ":" + port + " requires authentication with the realm '" + authscheme.getRealm() + "'" )
                return authMap[ key ] = new UsernamePasswordCredentials( proxyUser, proxyPassword )
            }
            throw new CredentialsNotAvailableException( 'Unsupported authentication scheme: ' + authscheme.schemeName )
        } 
        catch( final IOException e )
        {
            throw new CredentialsNotAvailableException( e.getMessage(), e )
        }
    }
}

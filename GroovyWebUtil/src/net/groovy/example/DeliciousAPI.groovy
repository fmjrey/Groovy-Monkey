package net.groovy.example
import org.apache.commons.httpclient.*
import org.apache.commons.httpclient.auth.*
import org.apache.commons.httpclient.methods.*
import org.apache.commons.lang.builder.*
import org.eclipse.swt.SWT
import org.eclipse.swt.layout.FormAttachment
import org.eclipse.swt.program.Program
import groovy.jface.JFaceBuilder
import groovy.util.XmlSlurper

class DeliciousAPI 
implements CredentialsProvider, Runnable
{
	static void main( args )
  	{
	    new DeliciousAPI().run()
  	}
    @Property authMap = [ : ]
    @Property client = new HttpClient()
    @Property tags = []
    @Property jface = new JFaceBuilder()
    @Property shell
    @Property table
    
    public DeliciousAPI()
    {
    }
	public void run()
	{
    	client.params.setParameter( CredentialsProvider.PROVIDER, this )
    	client.hostConfiguration.setProxy( 'proxy-corporate', 80 )
    	def creds = new UsernamePasswordCredentials( 'user-delicious', 'password-delicious' )
    	client.state.setCredentials( new AuthScope( 'del.icio.us', 80 ), creds )
    	showExistingTags()
        tags.each
        { tag ->
        	openTagInBrowser( tag )
        }
    	println 'finished....'
    }
	private void showExistingTags()
	{
	    def url = 'http://del.icio.us/api/tags/get'
	    def method = new GetMethod( url )
        client.executeMethod( method )
    	println 'status: ' + method.statusText
        def existingTags = new XmlSlurper().parseText( method.responseBodyAsString )
        shell = jface.shell( text: 'Select desired tags:', location: [ 100, 100 ], size: [ 800, 700 ] )
        {
       	    gridLayout()
       		table = table( toolTipText: "Select tags to open in browser", style:'check,border,v_scroll,h_scroll', size: [ 600, 400 ] ) 
   			{
       	        gridLayout()
       	        gridData( horizontalAlignment: SWT.FILL, verticalAlignment: SWT.FILL, grabExcessHorizontalSpace: true, grabExcessVerticalSpace: true, heightHint: 400 )
				for( tag in existingTags.tag )
				{
					tableItem().setText( "${tag.@tag}" )
				}
			}
       	    composite()
       	    {
       	        gridLayout( numColumns: 2 )
       	 		button( text: 'open', background: [ 0, 255, 255 ] )
       	 		{
       	            onEvent( type: 'Selection', closure: 
       	            {
       	                for( item in table.items )
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
    public Credentials getCredentials( AuthScheme authscheme, String host, int port, boolean proxy ) throws CredentialsNotAvailableException 
    {
        if( authscheme == null )
            return null
        def key = authscheme.schemeName + '@' + host + ':' + port
        if( authMap[ key ] != null )
            return authMap[ key ]
        try
        {
            if( authscheme instanceof NTLMScheme ) 
            {
                println( host + ":" + port + " requires Windows authentication" )
                return authMap[ key ] = new NTCredentials( 'user-corporate', 'password-corporate', host, 'domain-corporate' )
            }
            if( authscheme instanceof RFC2617Scheme )
            {
                println( host + ":" + port + " requires authentication with the realm '" + authscheme.getRealm() + "'" )
                return authMap[ key ] = new UsernamePasswordCredentials( 'ervinja', 'atr3u5134804' )
            }
            throw new CredentialsNotAvailableException( 'Unsupported authentication scheme: ' + authscheme.schemeName )
        } 
        catch( final IOException e )
        {
            throw new CredentialsNotAvailableException( e.getMessage(), e )
        }
    }
}

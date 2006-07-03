new File( '.' ).eachFile
{ file ->
	def path = file.getPath().tokenize( File.separator )
	path.remove( 0 )
	path = path.join( '/' )
	println 'path: ' + path
	def scp
	if( !file.isDirectory() )
		scp = 'scp -p'
	else
	    scp = 'scp -r -p'
	def process = "${scp} ${path} jervin@shell.sourceforge.net:/home/groups/g/gr/groovy-monkey/htdocs/update/${path}".execute()
    println '  exitCode: ' + process.waitFor()
	println '  stdout: ' + process.getText()
	println '  stderr: ' + process.getErr().getText()
}

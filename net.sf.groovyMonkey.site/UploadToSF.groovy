def clearCmd = "ssh jervin@shell.sf.net /home/users/j/je/jervin/clearUpdate.sh"
println "clearCmd: ${clearCmd}"
def clearProcess = clearCmd.execute()
println '  exitCode: ' + clearProcess.waitFor()
println '  stdout: ' + clearProcess.getText()
println '  stderr: ' + clearProcess.getErr().getText()

new File( '.' ).eachFile
{ file ->
    println "file: ${file} -> ${file.dump()}"
	def path = file.getPath().tokenize( File.separator )
	path.remove( 0 )
	path = path.join( '/' )
	println 'path: ' + path
	def scp
	if( !file.isDirectory() )
		scp = "scp -p"
	else
	    scp = "scp -r -p"
	def execString = "${scp} ${path} jervin@shell.sourceforge.net:/home/groups/g/gr/groovy-monkey/htdocs/update/${path}"
	println "${execString}"
	def process = execString.execute()
    println '  exitCode: ' + process.waitFor()
	println '  stdout: ' + process.getText()
	println '  stderr: ' + process.getErr().getText()
}

def chmodCmd = "ssh jervin@shell.sourceforge.net /home/users/j/je/jervin/chmodUpdate.sh"
println "chmodCmd: ${chmodCmd}"
def chmodProcess = chmodCmd.execute()
println '  exitCode: ' + chmodProcess.waitFor()
println '  stdout: ' + chmodProcess.getText()
println '  stderr: ' + chmodProcess.getErr().getText()

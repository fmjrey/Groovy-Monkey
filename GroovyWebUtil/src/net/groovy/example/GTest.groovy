package net.groovy.example;

class GTest 
{
  	static void main( args ) 
  	{
      	def List list = ["Rod", "Phil", "James", "Chris"]
      	def shorts = list.findAll { it.size() < 5 }
      	shorts.each { println it }
  	}
	void foo()
	{
	    
	}
}
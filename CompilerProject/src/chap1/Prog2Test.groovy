package chap1;
class Prog2Test extends GroovyTestCase  
{
    
    def prog2 = new Prog2()

	void testInterpretNumExp()
	{
	    def value = prog2.interpretExpression( num( 13 ) )
	    assert value instanceof IntAndTable
	    assert value.table.isEmpty()
	    assert value.returnValue == 13
	}
    void testInterpretIdExp()
    {
        def value = prog2.interpretExpression( idExp( 'James' ) )
	    assert value instanceof IntAndTable
	    assert !value.table.isEmpty()
	    assert value.table.containsKey( 'James' )
	    assert value.table.James == 0
	    assert value.returnValue == 0
    }
    void testInterpretSimpleOpExp()
    {
        def value = prog2.interpretExpression( add( num( 5 ), num( 8 ) ) )
        assert value instanceof IntAndTable
	    assert value.table.isEmpty()
	    assert value.returnValue == 13
    }
    void testInterpretSimpleAssignStm()
    {
        def value = prog2.interpretStatement( assign( 'a', num( 13 ) ) )
        assert value instanceof Map
        assert !value.isEmpty()
        assert value.containsKey( 'a' )
        assert value.a == 13
    }
    void testInterpretSimpleCompoundStm()
    {
        def value = prog2.interpretStatement( compound( assign( 'a', num( 13 ) ), 
                							  			assign( 'b', add( idExp( 'a' ), num( 8 ) ) ) ) )
        assert value instanceof Map
        assert !value.isEmpty()
        assert value.containsKey( 'a' )
        assert value.a == 13
        assert value.containsKey( 'b' )
        assert value.b == 21
    }
    void testWholeShebang()
    {
        def value = prog2.interpretStatement( compound( assign( 'a', add( num( 5 ), num( 3 ) ) ), 
  			  								  		    compound( assign( 'b', eseqExp( printStm( pairExpList( idExp( 'a' ), lastExpList( sub( idExp( 'a' ), num( 1 ) ) ) ) ), 
				 		  																times( num( 10 ), idExp( 'a' ) ) ) ), 
																  printStm( lastExpList( idExp( 'b' ) ) ) ) ) )
		assert value instanceof Map
		assert !value.isEmpty()
		assert value.containsKey( 'a' )
		assert value.a == 8
		assert value.containsKey( 'b' )
		assert value.b == 80
		println "value: ${value}"
		assert value.__output__ == "8 7\n80\n"
    }
    Object invokeMethod( final String name, final Object args )
    {
		return ConstructorsCategory.invokeMethod( name, args )
    }
}
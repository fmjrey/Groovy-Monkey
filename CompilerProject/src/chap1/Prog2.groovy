package chap1;

class Prog2
{
	def interpretStatement( statement, table = [:] )
	{
	    if( statement instanceof AssignStm )
	    {
	        table[ statement.id ] = interpretExpression( statement.exp, table ).returnValue
	        return table
	    }
	    if( statement instanceof CompoundStm )
	        return interpretStatement( statement.stm2, interpretStatement( statement.stm1, table ) )
	    // This means this is a PrintStm
	    interpretPrintStmnt( statement.exps, table )	    
	}
	def interpretPrintStmnt( expList, table = [:] )
	{
	    if( !table.containsKey( '__output__' ) )
	        table.__output__ = ''
	    if( expList instanceof LastExpList )
	    {
	        def intAndTable = interpretExpression( expList.head, table )
	        table.__output__ += '' + intAndTable.returnValue + '\n'
	        return table
	    }
		// This means a PairExpList
		def intAndTable = interpretExpression( expList.head, table )
		table.__output__  += '' + intAndTable.returnValue + ' '
		return interpretPrintStmnt( expList.tail, table )
	}
	def interpretExpression( exp, table = [:] )
	{
	    if( exp instanceof NumExp )
	        return new IntAndTable( table:table, returnValue:exp.num )
	    if( exp instanceof IdExp )
	    {
	        if( !table.containsKey( exp.id ) )
	        {
	        	table.put( exp.id, 0 )
	        	return new IntAndTable( table:table )
	        }
	        return new IntAndTable( table:table, returnValue:table[ exp.id ] )
	    }
	    if( exp instanceof OpExp )
	    {
	        def retValue1 = interpretExpression( exp.left, table )
	        def retValue2 = interpretExpression( exp.right, table )
	        def retValue = retValue1.returnValue + retValue2.returnValue
	        if( exp.op == Ops.DIV )
	            retValue = retValue1.returnValue / retValue2.returnValue
	        if( exp.op == Ops.TIMES )
	            retValue = retValue1.returnValue * retValue2.returnValue
	        if( exp.op == Ops.MINUS )
	            retValue = retValue1.returnValue - retValue2.returnValue
	        return new IntAndTable( table:table, returnValue:retValue )
	    }
	    if( exp instanceof EseqExp )
	    {
	        def newTable = interpretStatement( exp.stm, table )
	        return interpretExpression( exp.exp, newTable )
	    }
	    return new IntAndTable( table:table, returnValue:0 )
	}
}
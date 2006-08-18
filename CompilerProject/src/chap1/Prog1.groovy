package chap1;

Object invokeMethod( final String name, final Object args )
{
	return ConstructorsCategory.invokeMethod( name, args )
}

def prog1 = compound( assign( 'a', opExp( num( 5 ), Ops.PLUS, num( 3 ) ) ), 
        			  compound( assign( 'b', eseqExp( printStm( pairExpList( idExp( 'a' ), lastExpList( opExp( idExp( 'a' ), Ops.MINUS, num( 1 ) ) ) ) ), 
        			          				 		  opExp( num( 10 ), Ops.TIMES, idExp( 'a' ) ) ) ), 
        			            printStm( lastExpList( idExp( 'b' ) ) ) ) )


def maxArgs( stm )
{
    if( !( stm instanceof Stm ) )
        return countExpArgs( stm )
    if( stm instanceof AssignStm )
        return countExpArgs( stm.exp )
    if( stm instanceof CompoundStm )
    {
        args1 = maxArgs( stm.stm1 )
        args2 = maxArgs( stm.stm2 )
        return Math.max( args1, args2 )
    }
    // stm is a PrintStm
    return Math.max( countPrintStmtArgs( stm.exps ), countExpArgsList( stm.exps ) )
}
def countPrintStmtArgs( exps )
{
    // This method is from within a PrintStmt
    if( exps instanceof LastExpList )
        return 1
    // exps is a PairExpList
    return 1 + countPrintStmtArgs( exps.tail )
}
def countExpArgsList( exps )
{
    if( !( exps instanceof ExpList ) )
        return 0
    if( exps instanceof LastExpList )
        return countExpArgs( exps.head )
    // exps is a PairExpList
    return Math.max( countExpArgs( exps.head ), countExpArgsList( exps.tail ) )
}
def countExpArgs( exp )
{
    if( !( exp instanceof Exp ) )
        return 0
    if( exp instanceof EseqExp )
        return Math.max( maxArgs( exp.stm ), countExpArgs( exp.exp ) )
    if( exp instanceof OpExp )
        return Math.max( countExpArgs( exp.left ), countExpArgs( exp.right ) )
	return 0
}
println "prog1 has a print statement with ${maxArgs(prog1)} args."

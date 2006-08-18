package chap1;

class ConstructorsCategory 
{
    static closures = 
    [ 
    	compound : { s1, s2 -> new CompoundStm( stm1:s1, stm2:s2 ) },
        assign : { id, exp -> new AssignStm( id:id, exp:exp ) },
        opExp : { left, op, right -> new OpExp( left:left, op:op, right:right ) },
        add : { left, right -> new OpExp( left:left, op:Ops.PLUS, right:right ) },
        sub : { left, right -> new OpExp( left:left, op:Ops.MINUS, right:right ) },
        times : { left, right -> new OpExp( left:left, op:Ops.TIMES, right:right ) },
        num : { num -> new NumExp( num:num ) },
        eseqExp : { stm, exp ->  new EseqExp( stm:stm, exp:exp ) },
        idExp: { id -> new IdExp( id:id ) },
        lastExpList: { head -> new LastExpList( head:head ) },
        pairExpList: { head, tail -> new PairExpList( head:head, tail: tail ) },
        printStm: { exps -> new PrintStm( exps:exps ) }
    ]
    static Object invokeMethod( final String name, final Object args )
    {
        if( !closures.containsKey( name ) )
            throw new MissingMethodException( name, this.getClass(), args )
        return closures.get( name ).call( args )
    }
}
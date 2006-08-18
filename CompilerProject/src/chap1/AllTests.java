package chap1;

import groovy.util.GroovyTestSuite;
import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests
{
    private static final String TEST_ROOT = "src/chap1/";
    
    public static Test suite()
    throws Exception
    {
        final TestSuite suite = new TestSuite( "Test for " + AllTests.class.getPackage().getName() );
        final GroovyTestSuite gsuite = new GroovyTestSuite();
        suite.addTestSuite( gsuite.compile( TEST_ROOT + "Prog2Test.groovy" ) );
        //$JUnit-BEGIN$
        //$JUnit-END$
        return suite;
    }
}

package net.sf.groovymonkey.tests;
import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests
{
    public static Test suite()
    {
        final TestSuite suite = new TestSuite( "Test for " + AllTests.class.getPackage().getName() );
        //$JUnit-BEGIN$
        suite.addTestSuite( IncludeTest.class );
        suite.addTestSuite( LanguageTest.class );
        //$JUnit-END$
        return suite;
    }
}

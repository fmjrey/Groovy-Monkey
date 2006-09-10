package net.sf.groovymonkey.tests;
import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests
{
    public static Test suite()
    {
        final TestSuite suite = new TestSuite( "Test for " + AllTests.class.getPackage().getName() );
        //$JUnit-BEGIN$
        suite.addTestSuite( ActionsTests.class );
        suite.addTestSuite( IncludeBundleTest.class );
        suite.addTestSuite( IncludeLocalBundleTest.class );
        suite.addTestSuite( MonkeyRunnerTest.class );
        suite.addTestSuite( LanguageTest.class );
        suite.addTestSuite( IncludeTest.class );
        suite.addTestSuite( ResourcesTest.class );
        suite.addTestSuite( ScriptMetadataTest.class );
        suite.addTestSuite( VarBindingTest.class );
        //$JUnit-END$
        return suite;
    }
}

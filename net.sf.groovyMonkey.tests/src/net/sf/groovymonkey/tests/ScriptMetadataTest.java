package net.sf.groovymonkey.tests;
import static net.sf.groovyMonkey.GroovyMonkeyPlugin.FILE_EXTENSION;
import junit.framework.TestCase;
import net.sf.groovyMonkey.ScriptMetadata;

public class ScriptMetadataTest 
extends TestCase
{
    public void testLegalFilenames()
    {
        final ScriptMetadata data = new ScriptMetadata();
        data.setMenuName( "This is a test" );
        assertEquals( "ThisIsATest" + FILE_EXTENSION, data.getReasonableFilename() );
        data.setMenuName( "ABCD@#$%@$#DEFG" );
        assertEquals( "ABCDDEFG" + FILE_EXTENSION, data.getReasonableFilename() );
        data.setMenuName( "!!!+++" );
        assertEquals( "script" + FILE_EXTENSION, data.getReasonableFilename() );
        data.setMenuName( null );
        assertEquals( "script" + FILE_EXTENSION, data.getReasonableFilename() );
        data.setMenuName( "Explore > JDT" );
        assertEquals( "ExploreJDT" + FILE_EXTENSION, data.getReasonableFilename() );
    }
}

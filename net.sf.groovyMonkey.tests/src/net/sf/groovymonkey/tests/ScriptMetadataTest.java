package net.sf.groovymonkey.tests;
import static net.sf.groovyMonkey.GroovyMonkeyPlugin.FILE_EXTENSION;
import static net.sf.groovyMonkey.GroovyMonkeyPlugin.MONKEY_DIR;
import static net.sf.groovyMonkey.GroovyMonkeyPlugin.SCRIPTS_PROJECT;
import junit.framework.TestCase;
import net.sf.groovyMonkey.ScriptMetadata;

public class ScriptMetadataTest 
extends TestCase
{
    public void testLegalFilenames()
    {
        final ScriptMetadata data = new ScriptMetadata();
        data.setMenuName( "This is a test" );
        final String defaultPath = "/" + SCRIPTS_PROJECT + "/" + MONKEY_DIR + "/";
        assertEquals( defaultPath + "ThisIsATest" + FILE_EXTENSION, data.scriptPath() );
        data.setMenuName( "ABCD@#$%@$#DEFG" );
        data.setFile( null );
        assertEquals( defaultPath + "ABCDDEFG" + FILE_EXTENSION, data.scriptPath() );
        data.setMenuName( "!!!+++" );
        data.setFile( null );
        assertEquals( defaultPath + "script" + FILE_EXTENSION, data.scriptPath() );
        data.setMenuName( null );
        data.setFile( null );
        assertEquals( defaultPath + "script" + FILE_EXTENSION, data.scriptPath() );
        data.setMenuName( "Explore > JDT" );
        data.setFile( null );
        assertEquals( defaultPath + "ExploreJDT" + FILE_EXTENSION, data.scriptPath() );
    }
}

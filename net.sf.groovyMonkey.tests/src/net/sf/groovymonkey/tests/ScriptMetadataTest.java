/*******************************************************************************
 * Copyright (c) 2005, 2006 Eclipse Foundation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bjorn Freeman-Benson - initial implementation
 *     Ward Cunningham - initial implementation
 *******************************************************************************/
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
        assertEquals( defaultPath + "This_Is_A_Test" + FILE_EXTENSION, data.scriptPath() );
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
        assertEquals( defaultPath + "Explore__JDT" + FILE_EXTENSION, data.scriptPath() );
    }
}

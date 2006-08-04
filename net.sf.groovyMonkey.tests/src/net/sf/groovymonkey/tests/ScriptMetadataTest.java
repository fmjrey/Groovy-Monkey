/*******************************************************************************
 * Copyright (c) 2005 Eclipse Foundation
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
import junit.framework.TestCase;
import net.sf.groovyMonkey.ScriptMetadata;

public class ScriptMetadataTest extends TestCase
{
    public void testLegalFilenames()
    {
        final ScriptMetadata data = new ScriptMetadata();
        data.setMenuName( "This is a test" );
        assertEquals( "This_is_a_test" + FILE_EXTENSION, data.getReasonableFilename() );
        data.setMenuName( "ABCD@#$%@$#DEFG" );
        assertEquals( "ABCDDEFG" + FILE_EXTENSION, data.getReasonableFilename() );
        data.setMenuName( "!!!+++" );
        assertEquals( "script" + FILE_EXTENSION, data.getReasonableFilename() );
        data.setMenuName( null );
        assertEquals( "script" + FILE_EXTENSION, data.getReasonableFilename() );
    }
}

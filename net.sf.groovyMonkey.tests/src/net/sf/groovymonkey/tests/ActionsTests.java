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
import static net.sf.groovyMonkey.actions.PasteScriptFromClipboardAction.collapseEscapedNewlines;
import static net.sf.groovyMonkey.actions.PublishScriptForEmail.breakIntoShorterLines;
import junit.framework.TestCase;

public class ActionsTests
extends TestCase
{
    public void testCollapseEscapedNewlines()
    {
        final String s1 = "this is a test\nand another test\nand a third line\n";
        final String r1 = collapseEscapedNewlines( s1 );
        assertEquals( s1, r1 );
        final String s2 = "this is a test\\\nand another test\nand a third line\n";
        final String e2 = "this is a test\nand another test\nand a third line\n";
        final String r2 = collapseEscapedNewlines( s2 );
        assertEquals( e2, r2 );
    }
    public void testBreakIntoShorterLines()
    {
        final String s1 = "0123456789";
        final String r1 = breakIntoShorterLines( s1 );
        assertEquals( s1, r1 );
        final String s2 = s1 + s1 + s1 + s1 + s1 + s1;
        final String e2 = s1 + s1 + s1 + s1 + s1 + "\\\n" + s1;
        final String r2 = breakIntoShorterLines( s2 );
        assertEquals( e2, r2 );
        final String s3 = s1 + s1 + s1 + s1 + s1 + "\n" + s1;
        final String e3 = s1 + s1 + s1 + s1 + s1 + "\n" + s1;
        final String r3 = breakIntoShorterLines( s3 );
        assertEquals( e3, r3 );
        final String s4 = s1 + s1 + s1 + s1 + s1 + s1 + s1 + s1 + s1 + s1 + s1;
        final String e4 = s1 + s1 + s1 + s1 + s1 + "\\\n" + s1 + s1 + s1 + s1 + s1 + "\\\n" + s1;
        final String r4 = breakIntoShorterLines( s4 );
        assertEquals( e4, r4 );
    }
}

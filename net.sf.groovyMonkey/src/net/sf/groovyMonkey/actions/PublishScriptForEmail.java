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
package net.sf.groovyMonkey.actions;
import static java.util.regex.Pattern.compile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PublishScriptForEmail 
extends PublishScript
{
    @Override
    protected String decorateText( final String contents )
    {
        final String munged = breakIntoShorterLines( contents );
        return super.decorateText( munged );
    }
    public String breakIntoShorterLines( final String contents )
    {
        final Pattern pattern = compile( "([^\n\r]{50})(?![\n\r])" );
        final Matcher matcher = pattern.matcher( contents );
        final String munged = matcher.replaceAll( "$1\\\\\n" );
        return munged;
    }
}

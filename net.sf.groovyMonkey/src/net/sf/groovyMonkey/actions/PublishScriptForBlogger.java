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

public class PublishScriptForBlogger 
extends PublishScript
{
    @Override
    protected String decorateText( final String contents )
    {
        return "<pre>\n" + super.decorateText( contents ) + "\n</pre>\n";
    }
}

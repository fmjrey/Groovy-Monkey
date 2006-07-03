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

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PublishScriptForEmail extends PublishScript {
	protected String decorateText(String contents) {
		String munged = breakIntoShorterLines(contents);
		return super.decorateText(munged);
	}

	String breakIntoShorterLines(String contents) {
		Pattern pattern = Pattern.compile("([^\n\r]{50})(?![\n\r])");
		Matcher matcher = pattern.matcher(contents);
		String munged = matcher.replaceAll("$1\\\\\n");
		return munged;
	}
}

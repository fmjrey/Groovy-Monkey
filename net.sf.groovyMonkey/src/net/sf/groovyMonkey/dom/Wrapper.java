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

package net.sf.groovyMonkey.dom;

import java.lang.reflect.Method;

public class Wrapper {
	Object object;

	public Wrapper(Object obj) {
		object = obj;
	}

	public Object call(String methodName) throws Exception {
		Class[] args = {};
		Method method = object.getClass().getDeclaredMethod(methodName, args);
		Object[] params = {};
		return method.invoke(object, params);
	}
}

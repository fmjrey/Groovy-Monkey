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

package net.sf.groovyMonkey.internal;

import junit.framework.TestCase;

public class DynamicStateTests extends TestCase {

	public void test1() {
		DynamicState ds = new DynamicState();
		assertNull( ds.get("foo") );
	} public void test2 () {
		DynamicState ds = new DynamicState();
		ds.set("foo", "three");
		assertEquals("three", ds.get("foo"));
	} public void test3 () {
		DynamicState ds = new DynamicState();
		ds.set("foo", "three");
		ds.begin("bar");
		assertEquals("three", ds.get("foo"));
	} public void test4 () {
		DynamicState ds = new DynamicState();
		ds.begin("bar");
		ds.set("foo", "three");
		assertEquals("three", ds.get("foo"));
		ds.end("bar");
		assertNull( ds.get("foo"));
	} public void test5 () {
		DynamicState ds = new DynamicState();
		ds.begin("bar");
		ds.set("foo", "three");
		assertEquals("three", ds.get("foo"));
		ds.end("noname");
		assertEquals("three", ds.get("foo"));
	} public void test6 () {
		DynamicState ds = new DynamicState();
		ds.begin("bar");
		ds.set("foo", "three");
		ds.begin("whiz");
		ds.set("foo", "four");
		assertEquals("four", ds.get("foo"));
		ds.end("bar");
		assertNull( ds.get("foo"));
	} 
}

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

package net.sf.groovyMonkey.dom.resources;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;

/**
 * Run this class with Run As... JUnit Plug-in Test
 */
public class ResourcesTests extends TestCase {

	public static void main(String[] args) {
		junit.textui.TestRunner.run(ResourcesTests.class);
	}

	public void setUp() throws Exception {
		IWorkspace w = ResourcesPlugin.getWorkspace();
		IProject project = w.getRoot().getProject(
				"Eclipse Monkey Resources DOM Test");

		if (!project.exists())
			project.create(null);
		project.open(null);

		IFolder folder = project.getFolder("if_i_ran_the_zoo");
		folder.create(IResource.NONE, true, null);

		IFile file = folder.getFile("lunk.java");
		byte[] buf = new byte[0];
		InputStream stream = new ByteArrayInputStream(buf);
		file.create(stream, false, null);
		stream.close();

		file = folder.getFile("joat.txt");
		String s = "I'll load up five boats with a family of Joats\n"
				+ "Whose feet are like cows', but wear squirrel-skin coats\n";
		stream = new ByteArrayInputStream(s.getBytes());
		file.create(stream, false, null);
		stream.close();
	}

	public void tearDown() throws Exception {
		IWorkspace w = ResourcesPlugin.getWorkspace();
		IProject project = w.getRoot().getProject(
				"Eclipse Monkey Resources DOM Test");
		if (project.exists())
			project.delete(true, true, null);
	}

	public void testFilesMatching() throws Exception {

		Resources resources = (Resources) new ResourcesDOMFactory()
				.getDOMroot();
		Object[] result = resources.filesMatching(".*\\.java");
		assertEquals(1, result.length);
		assertTrue(result[0] instanceof File);
		assertEquals("lunk.java", ((File) result[0]).getEclipseObject()
				.getName());
	}

	public void testGetLines() throws Exception {
		Resources resources = (Resources) new ResourcesDOMFactory()
				.getDOMroot();
		Object[] result = resources.filesMatching(".*\\.txt");
		File file = (File) result[0];
		List< Line > lines = file.getLines();
		assertEquals(2, lines.size());
		assertEquals(
				"Whose feet are like cows', but wear squirrel-skin coats",
				lines.get( 1 ).getString());
	}

}

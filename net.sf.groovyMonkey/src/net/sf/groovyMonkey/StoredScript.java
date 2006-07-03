package net.sf.groovyMonkey;

import org.eclipse.core.resources.IFile;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;

public class StoredScript {
	public IFile scriptFile;
	public ScriptMetadata metadata;
	public Script compiledScript;
	public Scriptable compiledScope;
}

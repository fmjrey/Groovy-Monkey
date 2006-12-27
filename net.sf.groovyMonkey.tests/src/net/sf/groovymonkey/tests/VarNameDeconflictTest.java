package net.sf.groovymonkey.tests;
import static net.sf.groovymonkey.tests.Activator.bundle;
import static net.sf.groovymonkey.tests.Activator.context;
import static net.sf.groovymonkey.tests.fixtures.projects.TestMonkeyProject.MONKEY_EXT;
import static net.sf.groovymonkey.tests.fixtures.projects.TestMonkeyProject.runMonkeyScript;
import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.io.IOUtils.copy;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.core.resources.IFile;
import org.osgi.framework.Bundle;

public class VarNameDeconflictTest
extends TestCaseAbstract
{
    private final String DEPLOY_DIR = "/tmp/PDEJUnit/" + getClass().getSimpleName() + "/" + getName();
    private static final String TESTING_DOM = "net.sf.testing.dom";
    private static final String ANOTHER_DOM = "net.sf.testing.anotherDom";
    private InputStream installTestingDOMScriptInput = null;
    private IFile installTestingDOMScript = null;
    private InputStream installAnotherDOMScriptInput = null;
    private IFile installAnotherDOMScript = null;
    private InputStream uninstallScriptInput = null;
    private InputStream scriptFileInput = null;
    private IFile script = null;

    public VarNameDeconflictTest( final String name )
    {
        super( name );
    }
    @Override
    protected void setUp()
    throws Exception
    {
        super.setUp();
        deleteDirectory( new File( DEPLOY_DIR ) );
        new File( DEPLOY_DIR ).mkdirs();
        scriptFileInput = bundle().getResource( MONKEY_TEST_SCRIPTS + VarNameDeconflictTest.class.getSimpleName() + "/" + getName() + MONKEY_EXT ).openStream();
        script = monkeyProject.makeMonkeyScript( getName(), scriptFileInput );
        installTestingDOMScriptInput = bundle().getResource( MONKEY_TEST_SCRIPTS + VarNameDeconflictTest.class.getSimpleName() + "/installTestingDOM" + MONKEY_EXT ).openStream();
        installTestingDOMScript = monkeyProject.makeMonkeyScript( "installTestingDOM", installTestingDOMScriptInput );
        installAnotherDOMScriptInput = bundle().getResource( MONKEY_TEST_SCRIPTS + VarNameDeconflictTest.class.getSimpleName() + "/installAnotherDOM" + MONKEY_EXT ).openStream();
        installAnotherDOMScript = monkeyProject.makeMonkeyScript( "installAnotherDOM", installAnotherDOMScriptInput );
        uninstallScriptInput = bundle().getResource( MONKEY_TEST_SCRIPTS + VarNameDeconflictTest.class.getSimpleName() + "/lib/uninstall" + MONKEY_EXT ).openStream();
        monkeyProject.makeFile( "/lib", "uninstall" + MONKEY_EXT, uninstallScriptInput );
    }
    @Override
    protected void tearDown()
    throws Exception
    {
        super.tearDown();
        closeQuietly( scriptFileInput );
        closeQuietly( installTestingDOMScriptInput );
        closeQuietly( installAnotherDOMScriptInput );
        closeQuietly( uninstallScriptInput );
        deleteDirectory( new File( DEPLOY_DIR ) );
    }
    private void deployTestingDOM()
    throws Exception
    {
        deployBundle( TESTING_DOM + "_1.0.0.jar" );
    }
    private void deployAnotherDOM()
    throws Exception
    {
        deployBundle( ANOTHER_DOM + "_1.0.0.jar" );
    }
    private void deployBundle( final String bundleName )
    throws Exception
    {
        final InputStream input = bundle().getResource( MONKEY_TEST_SCRIPTS + VarNameDeconflictTest.class.getSimpleName() + "/" + bundleName ).openStream();
        final OutputStream output = new FileOutputStream( new File( DEPLOY_DIR, bundleName ) );
        try
        {
            copy( input, output );
        }
        finally
        {
            closeQuietly( input );
            closeQuietly( output );
        }
    }
    public List< Bundle > getTestingDOMBundles()
    {
        final List< Bundle > bundles = new ArrayList< Bundle >();
        for( final Bundle bundle : context().getBundles() )
            if( bundle.getSymbolicName().trim().equals( TESTING_DOM )
                || bundle.getSymbolicName().trim().equals( ANOTHER_DOM ) )
                bundles.add( bundle );
        return bundles;
    }
    private void installTestingDOM( final String version )
    {
        final Map< String, Object > map = new HashMap< String, Object >();
        map.put( "bundleVersion", version );
        map.put( "deployDir", DEPLOY_DIR );
        runMonkeyScript( installTestingDOMScript, map );
    }
    private void installAnotherDOM( final String version )
    {
        final Map< String, Object > map = new HashMap< String, Object >();
        map.put( "bundleVersion", version );
        map.put( "deployDir", DEPLOY_DIR );
        runMonkeyScript( installAnotherDOMScript, map );
    }
    /**
     * This unit test deploys and installs two test doms called net.sf.testing.dom and
     * net.sf.testing.anotherDom, that have two distinct dom objects that are both mapped
     * to the binding variable name testingDOM, the script remaps it for the variable from
     * the AnotherDOM plugin and checks that both DOM objects are called appropriately.
     * @throws Exception
     */
    public void testDeconflict()
    throws Exception
    {
        final List< Bundle > installedBundles = getTestingDOMBundles();
        if( installedBundles.size() > 0 )
            fail( TESTING_DOM + " is already installed: " + installedBundles + ". Check that your workspace doesn't have it open or that it is not included in the set of runtime plugins." );
        deployTestingDOM();
        installTestingDOM( "1.0.0" );
        deployAnotherDOM();
        installAnotherDOM( "1.0.0" );
        runMonkeyScript( script );
    }
}

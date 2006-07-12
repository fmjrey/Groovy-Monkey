package net.sf.groovymonkey.tests;
import static net.sf.groovymonkey.tests.Activator.bundle;
import static net.sf.groovymonkey.tests.Activator.context;
import static net.sf.groovymonkey.tests.fixtures.projects.TestMonkeyProject.MONKEY_EXT;
import static net.sf.groovymonkey.tests.fixtures.projects.TestMonkeyProject.runMonkeyScript;
import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.io.IOUtils.copy;
import static org.osgi.framework.Bundle.ACTIVE;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import net.sf.groovymonkey.tests.fixtures.dom.TestDOM;
import org.eclipse.core.resources.IFile;
import org.osgi.framework.Bundle;

public class IncludeLocalBundleTest 
extends TestCaseAbstract
{
    private final String DEPLOY_DIR = "/tmp/PDEJUnit/" + getClass().getSimpleName() + "/" + getName();
    private final String TEST_DOM = "net.sf.test.dom";
    private InputStream scriptFileInput = null;
    private IFile script = null;
    
    public IncludeLocalBundleTest( final String name )
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
        scriptFileInput = bundle().getResource( MONKEY_TEST_SCRIPTS + IncludeLocalBundleTest.class.getSimpleName() + "/" + getName() + MONKEY_EXT ).openStream();
        script = monkeyProject.makeMonkeyScript( getName(), scriptFileInput );
        new TestDOM().callDOM( "" );
    }
    @Override
    protected void tearDown() 
    throws Exception
    {
        super.tearDown();
        closeQuietly( scriptFileInput );
        deleteDirectory( new File( DEPLOY_DIR ) );
    }
    private void deployFirstVersion()
    throws Exception
    {
        deployBundle( TEST_DOM + "_1.0.0.jar" );
    }
    private void deployBundle( final String bundleName ) 
    throws Exception
    {
        final InputStream input = bundle().getResource( MONKEY_TEST_SCRIPTS + IncludeLocalBundleTest.class.getSimpleName() + "/" + bundleName ).openStream();
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
    private void deployUpdatedVersion()
    throws Exception
    {
        deployBundle( TEST_DOM + "_1.0.1.jar" );
    }
    public List< Bundle > getTestDOMBundles()
    {
        final List< Bundle > bundles = new ArrayList< Bundle >();
        for( final Bundle bundle : context().getBundles() )
            if( bundle.getSymbolicName().trim().equals( TEST_DOM ) )
                bundles.add( bundle );
        return bundles;
    }
    public void testIncludeLocalBundle()
    throws Exception
    {
        deployFirstVersion();
        final List< Bundle > installedBundles = getTestDOMBundles();
        if( installedBundles.size() > 0 )
            fail( TEST_DOM + " is already installed: " + installedBundles );
        runMonkeyScript( script );
        final List< Bundle > updatedBundles = getTestDOMBundles();
        assertEquals( 1, updatedBundles.size() );
        final Bundle bundle = updatedBundles.get( 0 );
        assertEquals( ACTIVE, bundle.getState() );
    }
    public void testUpdateLocalBundle()
    throws Exception
    {
        deployFirstVersion();
        deployUpdatedVersion();
        final List< Bundle > installedBundles = getTestDOMBundles();
        if( installedBundles.size() > 0 )
            fail( TEST_DOM + " is already installed: " + installedBundles );
        runMonkeyScript( script );
        final List< Bundle > updatedBundles = getTestDOMBundles();
        assertEquals( 1, updatedBundles.size() );
        final Bundle bundle = updatedBundles.get( 0 );
        assertEquals( ACTIVE, bundle.getState() );
    }
}

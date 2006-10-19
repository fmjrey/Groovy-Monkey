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
import static net.sf.groovyMonkey.GroovyMonkeyPlugin.MENU_EDIT_PATH;
import static net.sf.groovyMonkey.GroovyMonkeyPlugin.MENU_PATH;
import static net.sf.groovyMonkey.GroovyMonkeyPlugin.MONKEY_DIR;
import static net.sf.groovyMonkey.GroovyMonkeyPlugin.scriptStore;
import static net.sf.groovyMonkey.RunMonkeyScript.LAST_RUN;
import static net.sf.groovyMonkey.dom.Utilities.openEditor;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sf.groovyMonkey.RunMonkeyScript;
import net.sf.groovyMonkey.ScriptMetadata;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.WorkbenchWindow;

public class RecreateMonkeyMenuAction 
implements IWorkbenchWindowActionDelegate
{
    class MonkeyMenuStruct
    {
        String key;
        IMenuManager menu;
        MonkeyMenuStruct submenu;
    }
    class Association 
    implements Comparable< Association >
    {
        final String key;
        final IFile file;
        final int uniqueId;

        Association( final String k, 
                     final IFile f )
        {
            key = k;
            file = f;
            uniqueId = id++;
        }
        public int compareTo( final Association association )
        {
            final int value = key.compareTo( association.key );
            if( value != 0 )
                return value;
            if( uniqueId < association.uniqueId )
                return -1;
            return 1;
        }
    }
    
    private final Pattern submenu_pattern = compile( "^(.+?)>(.*)$" );
    private static int id = 0;
    private IWorkbenchWindow window;
    
    public RecreateMonkeyMenuAction() {}

    public void run( final IAction action )
    {
        final List< ScriptMetadata > metaDatas = getAllMetadatas();
        final List< Association > menuData = createMenuFromMetadatas( metaDatas );
        createTheMenu( menuData, action );
    }
    private List< ScriptMetadata > getAllMetadatas()
    {
        final List< ScriptMetadata > result = new ArrayList< ScriptMetadata >();
        for( final String scriptPath : scriptStore().keySet() )
        {
            if( !scriptPath.contains( "/" + MONKEY_DIR + "/" ) )
                continue;
            final ScriptMetadata metadata = scriptStore().get( scriptPath );
            if( isBlank( metadata.getMenuName() ) )
                continue;
            result.add( scriptStore().get( scriptPath ) );
        }
        return result;
    }
    private void createTheMenu( final List< Association > menuData, 
                                final IAction action )
    {
        final MenuManager outerManager = ( ( WorkbenchWindow )window ).getMenuManager();
        if( outerManager == null )
            return;
        //final IMenuManager menuManager = ( IMenuManager )( ( ActionSetContributionItem )contribution ).getInnerItem();
        final IMenuManager menuManager = new MenuManager( "GMonkey", MENU_PATH );
        outerManager.replaceItem( MENU_PATH, menuManager );
        final MonkeyMenuStruct current = new MonkeyMenuStruct();
        current.key = "";
        current.menu = menuManager;
        current.submenu = new MonkeyMenuStruct();
        final SortedSet< Association > sorted = new TreeSet< Association >();
        sorted.addAll( menuData );
        for( final Association association : sorted )
            addNestedMenuAction( current, association.key, association.file );
        final IWorkbenchWindow _window = window;
        if( sorted.size() != 0 )
            menuManager.add( new Separator() );
        menuManager.add( new Action( "Paste New Script" )
        {
            @Override
            public void run()
            {
                final IWorkbenchWindowActionDelegate delegate = new PasteScriptFromClipboardAction();
                delegate.init( _window );
                delegate.run( action );
            }
        } );
        if( sorted.size() == 0 )
            menuManager.add( new Action( "Examples" )
            {
                @Override
                public void run()
                {
                    final IWorkbenchWindowActionDelegate delegate = new CreateGroovyMonkeyExamplesAction();
                    delegate.init( _window );
                    delegate.run( action );
                }
            } );
        final IMenuManager editMenu = menuManager.findMenuUsingPath( MENU_EDIT_PATH ) != null ? menuManager.findMenuUsingPath( MENU_EDIT_PATH ) : new MenuManager( "Edit Script", MENU_EDIT_PATH );
        menuManager.add( editMenu );
        current.key = "";
        current.menu = editMenu;
        current.submenu = new MonkeyMenuStruct();
        for( final Association association : sorted )
            addNestedMenuEditAction( current, association.key, association.file );
        outerManager.updateAll(  true );
    }
    private void addNestedMenuAction( final MonkeyMenuStruct current, 
                                      final String menuString, 
                                      final IFile scriptFile )
    {
        if( menuString == null )
            return;
        final Matcher match = submenu_pattern.matcher( menuString );
        if( match.find() )
        {
            final String primaryKey = match.group( 1 ).trim();
            final String secondaryKey = match.group( 2 ).trim();
            if( !primaryKey.equals( current.submenu.key ) )
            {
                final IMenuManager submenu = new MenuManager( primaryKey );
                current.menu.add( submenu );
                current.submenu.menu = submenu;
                current.submenu.key = primaryKey;
                current.submenu.submenu = new MonkeyMenuStruct();
            }
            addNestedMenuAction( current.submenu, secondaryKey, scriptFile );
        }
        else
            current.menu.add( menuAction( menuString, scriptFile ) );
    }
    private void addNestedMenuEditAction( final MonkeyMenuStruct current, 
                                          final String menuString, 
                                          final IFile scriptFile )
    {
        if( menuString == null )
            return;
        final Matcher match = submenu_pattern.matcher( menuString );
        if( match.find() )
        {
            final String primaryKey = match.group( 1 ).trim();
            final String secondaryKey = match.group( 2 ).trim();
            if( !primaryKey.equals( current.submenu.key ) )
            {
                final IMenuManager submenu = new MenuManager( primaryKey );
                current.menu.add( submenu );
                current.submenu.menu = submenu;
                current.submenu.key = primaryKey;
                current.submenu.submenu = new MonkeyMenuStruct();
            }
            addNestedMenuEditAction( current.submenu, secondaryKey, scriptFile );
        }
        else
            current.menu.add( editAction( menuString, scriptFile ) );
    }
    private Action editAction( final String key, 
                               final IFile script )
    {
        final Action action = new Action( key )
        {
            @Override
            public void run()
            {
                try
                {
                    openEditor( script );
                }
                catch( final PartInitException e )
                {
                    throw new RuntimeException( e );
                }
            }
        };
        if( LAST_RUN != null && script.equals( LAST_RUN.getFile() ) )
            action.setAccelerator( SWT.ALT | SWT.CONTROL | 'E' );
        return action;
    }
    private Action menuAction( final String key, 
                               final IFile script )
    {
        final RunMonkeyScript runner = new RunMonkeyScript( script, window );
        final Action action = new Action( key )
        {
            @Override
            public void run()
            {
                runner.run( false );
            }
        };
        if( LAST_RUN != null && script.equals( LAST_RUN.getFile() ) )
            action.setAccelerator( SWT.ALT | SWT.CONTROL | 'M' );
        return action;
    }
    private List< Association > createMenuFromMetadatas( final Collection< ScriptMetadata > metadata )
    {
        final List< Association > menuData = new ArrayList< Association >();
        for( final ScriptMetadata data : metadata )
            if( isNotBlank( data.getMenuName() ) )
                menuData.add( new Association( data.getMenuName(), data.getFile() ) );
        return menuData;
    }
    public void selectionChanged( final IAction action, final ISelection selection )
    {
    }
    public void dispose()
    {
    }
    public void init( final IWorkbenchWindow window )
    {
        this.window = window;
    }
}

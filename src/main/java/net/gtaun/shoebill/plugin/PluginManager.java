/**
 * Copyright (C) 2011 JoJLlmAn
 * Copyright (C) 2011 MK124
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.gtaun.shoebill.plugin;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.log4j.Logger;

import net.gtaun.shoebill.IShoebill;
import net.gtaun.shoebill.IShoebillLowLevel;
import net.gtaun.shoebill.Shoebill;
import net.gtaun.shoebill.event.plugin.PluginLoadEvent;
import net.gtaun.shoebill.event.plugin.PluginUnloadEvent;

/**
 * @author JoJLlmAn, MK124
 *
 */

public class PluginManager implements IPluginManager
{
	public static final Logger LOGGER = Logger.getLogger(PluginManager.class);
	
	
	final FilenameFilter jarFileFliter = new FilenameFilter()
	{
		public boolean accept( File dir, String name )
		{
			final String extname = ".jar";
			return name.length()>extname.length() && name.substring( name.length()-4, name.length() ).equals(extname);
		}
	};
	
	
	private Map<Class<? extends Plugin>, Plugin> plugins;

	private ClassLoader classLoader;
	private IShoebill shoebill;
	private File pluginDir, dataDir;
	
	
	public PluginManager( IShoebill shoebill, File pluginFolder, File gamemodeFolder, File dataFolder )
	{
		plugins = new HashMap<Class<? extends Plugin>, Plugin>();
		
		File[] files = pluginFolder.listFiles( jarFileFliter );
		File[] gamemodeFiles = gamemodeFolder.listFiles( jarFileFliter );
		URL[] urls = new URL[ files.length + gamemodeFiles.length ];
		for( int i=0; i<files.length+gamemodeFiles.length; i++ ) try
		{
			if( i<files.length )	urls[i] = files[i].toURI().toURL();
			else					urls[i] = gamemodeFiles[i-files.length].toURI().toURL();
		}
		catch( MalformedURLException e )
		{
			e.printStackTrace();
		}
		
		this.classLoader = URLClassLoader.newInstance(urls, getClass().getClassLoader());
		this.shoebill = shoebill;
		this.pluginDir = pluginFolder;
		this.dataDir = dataFolder;
	}
	
	public void loadAllPlugin()
	{
		File[] files = pluginDir.listFiles( jarFileFliter );
		for( File file : files ) loadPlugin( file );
	}
	
	public Gamemode constructGamemode( File file ) throws IOException
	{
		JarFile jarFile = new JarFile( file );
		JarEntry entry = jarFile.getJarEntry( "gamemode.yml" );
		InputStream in = jarFile.getInputStream( entry );
		
		GamemodeDescription desc;
		Gamemode gamemode = null;
		
		LOGGER.info("Load gamemode: " + file.getName() );
		
		try
		{
			desc = new GamemodeDescription(in, classLoader);
			gamemode = desc.getClazz().newInstance();
			gamemode.setContext( desc, shoebill, new File(dataDir, desc.getClazz().getName()) );
			gamemode.enable();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		
		return gamemode;
	}
	
	public void deconstructGamemode( Gamemode gamemode )
	{
		try
		{
			gamemode.disable();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}

	@Override
	public Plugin loadPlugin( String filename )
	{
		File file = new File(pluginDir, filename);
		return loadPlugin( file );
	}

	@Override
	public Plugin loadPlugin( File file )
	{
		if( file.canRead() == false ) return null;
		
		LOGGER.info("Load plugin: " + file.getName() );
		
		try
		{
			JarFile jarFile = new JarFile( file );
			JarEntry entry = jarFile.getJarEntry( "plugin.yml" );
			InputStream in = jarFile.getInputStream( entry );
			
			PluginDescription desc = new PluginDescription(in, classLoader);
			Class<? extends Plugin> clazz = desc.getClazz();
			if( plugins.containsKey(clazz) )
			{
				LOGGER.warn("There's a plugin which has the same class as \"" + desc.getClazz().getName() + "\".");
				LOGGER.warn("Abandon loading " + desc.getClazz().getName());
				return null;
			}
			
			Constructor<? extends Plugin> constructor = clazz.getConstructor();
			Plugin plugin = constructor.newInstance();
			
			File pluginDataFolder = new File(dataDir, desc.getClazz().getName());
			if( ! pluginDataFolder.exists() ) pluginDataFolder.mkdirs();
			
			plugin.setContext( desc, shoebill, pluginDataFolder );
			plugin.enable();
			
			plugins.put( clazz, plugin );
			
			PluginLoadEvent event = new PluginLoadEvent(plugin);
			IShoebillLowLevel shoebillLowLevel = (IShoebillLowLevel) Shoebill.getInstance();
			shoebillLowLevel.getEventManager().dispatchEvent( event, this );
			
			return plugin;
		}
		catch( Exception e )
		{
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void unloadPlugin( Plugin plugin )
	{
		for( Entry<Class<? extends Plugin>, Plugin> entry : plugins.entrySet() )
		{
			if( entry.getValue() != plugin ) continue;
			LOGGER.info("Unload plugin: " + plugin.getDescription().getClazz().getName() );

			PluginUnloadEvent event = new PluginUnloadEvent(plugin);
			IShoebillLowLevel shoebillLowLevel = (IShoebillLowLevel) Shoebill.getInstance();
			shoebillLowLevel.getEventManager().dispatchEvent( event, this );
			
			try
			{
				plugin.disable();
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
			
			plugins.remove( entry.getKey() );
			return;
		}
	}
	
	@Override
	public <T extends Plugin> T getPlugin( Class<T> clz )
	{
		T plugin = clz.cast( plugins.get(clz) );
		if( plugin != null ) return plugin;
		
		for( Plugin p : plugins.values() )
		{
			if( clz.isInstance(p) ) return clz.cast( p );
		}
		
		return null;
	}

	@Override
	public Collection<Plugin> getPlugins()
	{
		return plugins.values();
	}
}

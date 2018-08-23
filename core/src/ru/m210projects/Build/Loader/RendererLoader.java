//This file is part of BuildGDX.
//Copyright (C) 2017-2018  Alexander Makarov-[M210] (m210-2007@mail.ru)
//
//BuildGDX is free software: you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//BuildGDX is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with BuildGDX.  If not, see <http://www.gnu.org/licenses/>.

package ru.m210projects.Build.Loader;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import ru.m210projects.Build.Engine;
import ru.m210projects.Build.Render.Renderer;

public class RendererLoader {
	
	public static Renderer loadRenderer(File jarfile, Engine en)
	{
		try {
			JarFile jarFile = new JarFile(jarfile);
			Enumeration<JarEntry> e = jarFile.entries();

			URL[] urls = { jarfile.toURI().toURL() };
			URLClassLoader cl = URLClassLoader.newInstance(urls);

			Constructor<?> renderMain = null; 
			while (e.hasMoreElements()) {
			    JarEntry je = e.nextElement();
			    if(je.isDirectory() || !je.getName().endsWith(".class")) {
			        continue;
			    }
			    
			    // -6 because of .class
			    String className = je.getName().substring(0,je.getName().length()-6);
			    className = className.replace('/', '.');
			    Class<?> cls = cl.loadClass(className);  
			    
			    if(cls.getInterfaces()[0].equals(Renderer.class)) {
				    if(cls.getConstructors().length != 0) {
				    	for(int i = 0; i < cls.getConstructors().length; i++) {
					    	if(cls.getConstructors()[i].getParameters()[0].getType().equals(Engine.class)) {
					    		if(renderMain == null)
					    			renderMain = cls.getConstructors()[i];
					    		else {
					    			jarFile.close();
					    			throw new Exception("Error: more then one renderer in library?");
					    		}
					    	}
				    	}
				    } 
			    }
			}
			
			jarFile.close();
			if(renderMain != null)
				return (Renderer) renderMain.newInstance(en);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		
		return null;
	}
}

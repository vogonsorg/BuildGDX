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

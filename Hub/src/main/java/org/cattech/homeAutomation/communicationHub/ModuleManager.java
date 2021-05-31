package org.cattech.homeAutomation.communicationHub;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cattech.homeAutomation.configuration.HomeAutomationConfiguration;
import org.cattech.homeAutomation.moduleBase.HomeAutomationModule;

public class ModuleManager {
	private static final String PROP_SKIP_LOAD_MODULE = "skipModule";

	private static final long STARTUP_WAIT = 5000;

	private static Logger log = LogManager.getLogger(ModuleManager.class);

	private static URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
	private static DynamicURLClassLoader dynalLoader = new DynamicURLClassLoader(urlClassLoader);
	
//	static ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
	HomeAutomationConfiguration config;
	

	public ModuleManager(HomeAutomationConfiguration config) {
		this.config = config;
		try {
			addSoftwareLibrary(config.getLibFolder() + "/");
			addSoftwareLibrary(config.getModulesFolder() + "/");
		} catch (Exception e) {
			log.error("Error adding class to module loader",e);
		}
	}

	private static void addSoftwareLibrary(String file) throws Exception {
		dynalLoader.addFolderOfJars(file);
	}

	// ================================================================================
	public void startLoadableModules(ChannelController controller) {
		try {
			File folder = new File(config.getModulesFolder());
			File[] listOfFiles = folder.listFiles((dir, name) -> name.endsWith(".jar"));

			Properties props = config.getProps();

			if (null == listOfFiles) {
				log.info("--- Did not find any modules to load ---");
			} else {
				log.info("Enumerating " + listOfFiles.length + " modules from " + config.getModulesFolder());

				for (File jarName : listOfFiles) {
					log.info("===== Scanning jar for module capabilities : " + jarName.getName());
					
					String skipName = jarName.getName().replaceAll(".jar$","");
					String skip = props.getProperty(PROP_SKIP_LOAD_MODULE + "." + skipName);
					
					if (skip != null) {
						log.info("Skipping load of module : " + skipName);
					}else {
						JarFile jarFile;
						try {
							jarFile = new JarFile(jarName);
							Enumeration<JarEntry> e = jarFile.entries();
	
							HashMap<String,String> manifest = loadJarManifest(jarFile); 
							
							while (e.hasMoreElements()) {
								JarEntry je = e.nextElement();
								if (je.isDirectory() || !je.getName().endsWith(".class")) {
									continue;
								}
								// -6 because of .class
								String className = je.getName().substring(0, je.getName().length() - 6);
								className = className.replace('/', '.');
								Object mod = null;
								
								try {
									mod = dynalLoader.loadClass(className);
									if (mod != null && HomeAutomationModule.class.isAssignableFrom((Class<?>) mod)) {
										log.debug("Jar contains loadable module, loading : " + je.getName());
										
										mod = ((Class<?>) mod).getConstructor(ChannelController.class).newInstance(controller);
	
										HomeAutomationModule hMod = (HomeAutomationModule) mod;
										if (hMod.autoStartModule()) {
											hMod.setManifest(manifest);
											long timer = System.currentTimeMillis() + STARTUP_WAIT;
											new Thread(hMod, hMod.getModuleChannelName()).start();
											while(! hMod.isRunning() && timer > System.currentTimeMillis()) {
												hMod.sleepNoThrow(10);
											}
											if (! hMod.isRunning()) {
												throw new ModuleAutostartException(hMod.getModuleChannelName() +" did not start within " + STARTUP_WAIT + "miliseconds.");
											}
											
										} else {
											throw new ModuleAutostartException(hMod.getModuleChannelName() +" was configured to not autostart.");
										}
									}
								} catch (ModuleAutostartException e2) {
									log.error("Autostart : ",e2);
								} catch (Throwable e1) {
									log.error("Could not create instance of " + className, e1);
									log.debug("Classpath : \n\t" + System.getProperty("java.class.path").replaceAll(":", "\n\t"));
									log.debug("ClassURLs : \n\t" + dynalLoader.getURLsAsString().replaceAll(":", "\n\t"));
								}
							}
						} catch (IOException e2) {
							log.error("Could not load jar : " + jarName + "(" + e2.getMessage() + ")");
						}
					}
					log.info("===== Completed scanning : " + jarName.getName());

				}
			}
		} catch (Throwable e) {
			log.error("Could not initialize module loader, skipping module load.", e);
		}

	}

	private HashMap<String, String> loadJarManifest(JarFile jarFile) throws IOException {
		HashMap<String, String> manifest = new HashMap<String, String>();
		JarEntry  man = jarFile.getJarEntry("META-INF/MANIFEST.MF");
        InputStream input = jarFile.getInputStream(man);
        
        InputStreamReader isr = new InputStreamReader(input);
        BufferedReader reader = new BufferedReader(isr);
        String line;

        while ((line = reader.readLine()) != null) {
            String[] entry = line.split(":",2);
            if (entry.length > 0) {
            	if (entry.length > 1) {
            		manifest.put(entry[0], entry[1]);
            	}else {
            		manifest.put(entry[0], null);
            	}
            }
        }
        reader.close();
        
		return manifest;
	}
}

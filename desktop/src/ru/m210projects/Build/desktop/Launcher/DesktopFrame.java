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

package ru.m210projects.Build.desktop.Launcher;

import static ru.m210projects.Build.FileHandle.Compat.FilePath;
import static ru.m210projects.Build.FileHandle.Compat.FileUserdir;
import static ru.m210projects.Build.FileHandle.Compat.cache;
import static ru.m210projects.Build.FileHandle.Compat.toLowerCase;
import static ru.m210projects.Build.OnSceenDisplay.Console.OSDTEXT_GOLD;
import static ru.m210projects.Build.OnSceenDisplay.Console.OSDTEXT_RED;
import static ru.m210projects.Build.Render.VideoMode.getmode;
import static ru.m210projects.Build.Render.VideoMode.getmodeindex;
import static ru.m210projects.Build.Render.VideoMode.initVideoModes;
import static ru.m210projects.Build.Render.VideoMode.setFullscreen;
import static ru.m210projects.Build.Render.VideoMode.strvmodes;
import static ru.m210projects.Build.Render.VideoMode.validmodes;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import javax.sound.midi.MidiDevice;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.lwjgl.opengl.Display;

import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import ru.m210projects.Build.OnSceenDisplay.Console;
import ru.m210projects.Build.Types.BConfig;
import ru.m210projects.Build.desktop.audio.midi.MidiMusicModule;

public class DesktopFrame {
	
	private JFrame frmPropFrame;
	private JButton startButton;
	private JButton mySiteButton; 
	private JButton donateButton;
	private JButton pathButton;
	private JTextField textField;
	private JLabel label;
	private JComboBox<String> comboBox;
	private JCheckBox fullscr;
	private JCheckBox borderless;
	private JCheckBox chckbxUpdate;
	private JCheckBox chckbxAlwaysShowThis;
	private JCheckBox autoloadcheckBox;
	private JComboBox<String> midiDeviceList;
	private JComboBox<String> soundDeviceList;
	private int midiDevice;
	private JEditorPane textPane;
	private JScrollPane logText;
	private JTabbedPane tabbedPane;
	private HashMap <Integer, String> midiMap;
	private boolean settingsInited;
	private boolean settingsCreated;
	private boolean aboutInited;
	
	private String path;
	private String appname;
	private String appversion;
	
	private BConfig cfg;
	private boolean portableAvailable = false;
	private boolean portableMode;
	private BConfig portableCFG;
	private String userHomePath;
	private String portablePath;
	private LaunchCallback cLaunch;
	
	public DesktopFrame(String appname, String appversion, String[] resources, 
			LaunchCallback cLaunch, URL Title, URL FrameIcon, final URL AboutImg) throws Exception
	{
		final String apptitle = appname + " " + appversion;
		this.appname = appname;
		this.appversion = appversion;
		this.cLaunch = cLaunch;
	
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		
		path = getDirPath();
		int res = checkResources(path, resources);
		if(res == 1) {
			portableAvailable = true;
			portablePath = path; //port in game folder and can write
		}
			
		if(portableAvailable) {
			cfg = portableCFG = cLaunch.buildConfig(portablePath, appname + ".ini");
			FileUserdir = portablePath; 
			portableMode = true; 
		} 

		if(!portableAvailable || cfg.userfolder) {
			FileUserdir = createUserPath();
			portableMode = false; 
		}

		if(!portableMode) {
			cfg = cLaunch.buildConfig(FileUserdir, appname + ".ini");
			if(res == -1 && cfg.path != null && !cfg.path.isEmpty()) 
				FilePath = path = cfg.path;
			else FilePath = path;
			if(!portableAvailable) cfg.userfolder = true;
		}
		
		frmPropFrame = new JFrame();
		JPanel panel = new JPanel();
		panel.setLayout(null);
		panel.setPreferredSize(new Dimension(400, 337)); //window size for pack()

		final ImagePanel logo = new ImagePanel(Title);
		
		final Font font = new Font(Font.DIALOG, Font.PLAIN, 12);
		pathButton = new JButton("...");
		textField = new JTextField();

		if(FrameIcon != null)
			frmPropFrame.setIconImage(Toolkit.getDefaultToolkit().getImage(FrameIcon));
		frmPropFrame.setBounds(0, 0, 406, 358);
		frmPropFrame.setTitle(apptitle);
		frmPropFrame.setLocationRelativeTo(null);
		frmPropFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmPropFrame.getContentPane().setLayout(new BorderLayout(0, 0));
		frmPropFrame.getContentPane().add(panel, BorderLayout.CENTER);
		
		logo.setBounds(10, 11, 380, 100);
		panel.add(logo);
		startButton = new JButton();
		startButton.setFont(font);
		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				FilePath = path;
				launchPort(apptitle, cfg);
			}
		});
		startButton.setBounds(10, 304, 380, 23);
		panel.add(startButton);
		panel.getRootPane().setDefaultButton(startButton); 
		
		final JPanel settings = new JPanel();
		JPanel main = new JPanel();
		final JPanel about = new JPanel();
		settings.setLayout(null);
		main.setLayout(null);
		about.setLayout(null);
		initMainTab(main, font, FrameIcon, resources); //54ms
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBounds(10, 122, 380, 175);
		tabbedPane.setFont(font);
		panel.add(tabbedPane);
		tabbedPane.addTab("Main", main);
		
		tabbedPane.addTab("Settings", settings);
		tabbedPane.addTab("About", about);
		
		tabbedPane.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent event) {
				JTabbedPane sourceTabbedPane = (JTabbedPane) event.getSource();
		        int index = sourceTabbedPane.getSelectedIndex();
		        if(index == 1)
		        	initSettingsTab(settings, font);	//218ms
		        if(index == 2)
		        	initAboutTab(about, font, AboutImg);	//359ms
			}
		});

		startButtonStatus(checkResources(path, resources) != -1, appname, resources[0]);

		new Thread(new Runnable() 
		{
			public void run()
			{
				while(!logo.isLoaded());
				logo.repaint(); //draw logo after load
			}
		}).start();

		frmPropFrame.setResizable(false);
		frmPropFrame.pack();
		frmPropFrame.setVisible(true);
		frmPropFrame.requestFocus();
		
		if(!cfg.startup && startButton.isEnabled())
			launchPort(apptitle, cfg);
	}
	
	private void startButtonStatus(boolean resources, String appname, String resname)
	{
		startButton.setEnabled(resources);
		if(resources) {
			startButton.setText("Play " + appname);
			startButton.requestFocus();
		}
		else 
			startButton.setText(resname + " resources not found!");
	}
	
	private void initMainTab(JPanel main, Font font, final URL icon, final String[] resources)
	{
		final JButton btnOpenAppdata = new JButton("Open appdata");
		pathButton.setFont(font);
		pathButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File f = DirectoryBrowser.showDirectoryChooser(null, path, resources[0], icon);
				if(f != null)
					path = f.getAbsolutePath() + File.separator;
					textField.setText(path);  
					
					startButtonStatus(checkResources(path, resources) != -1, appname, resources[0]);
			}
		});
		pathButton.setBounds(325, 11, 30, 23);
		main.add(pathButton);

		textField.setFont(font);
		
		File fpath = new File(path);
		if(!fpath.exists()) //Game folder check (if deleted)
			path = getDirPath();
		
		textField.setText(path);
		textField.setEditable(false);
		textField.setBounds(18, 12, 297, 20);
		main.add(textField);
		
		chckbxUpdate = new JCheckBox("Check for updates");
		chckbxUpdate.setFont(font);
		chckbxUpdate.setBounds(18, 70, 280, 23);
		chckbxUpdate.setSelected(cfg.checkVersion);
		chckbxUpdate.addItemListener(new ItemListener() {
		    public void itemStateChanged(ItemEvent e) {
		    	cfg.checkVersion = e.getStateChange() == 1;
		    }
		});
		main.add(chckbxUpdate);
		
		chckbxAlwaysShowThis = new JCheckBox("Always show this window at startup");
		chckbxAlwaysShowThis.setFont(font);
		chckbxAlwaysShowThis.setBounds(18, 90, 280, 23);
		chckbxAlwaysShowThis.setSelected(cfg.startup);
		chckbxAlwaysShowThis.addItemListener(new ItemListener() {
		    public void itemStateChanged(ItemEvent e) {
		    	cfg.startup = e.getStateChange() == 1;
		    }
		});
		main.add(chckbxAlwaysShowThis);
		
		autoloadcheckBox = new JCheckBox("Enable \"autoload\" folder");
		autoloadcheckBox.setSelected(cfg.autoloadFolder);
		autoloadcheckBox.setFont(font);
		autoloadcheckBox.setBounds(18, 110, 280, 23);
		autoloadcheckBox.addItemListener(new ItemListener() {
		    public void itemStateChanged(ItemEvent e) {
		    	cfg.autoloadFolder = e.getStateChange() == 1;
		    }
		});
		main.add(autoloadcheckBox);

		btnOpenAppdata.setFont(font); //Button
		btnOpenAppdata.setBounds(18, 43, 141, 23);
		btnOpenAppdata.setEnabled(!portableMode);
		if(portableMode)
			btnOpenAppdata.setText("Portable mode");
		btnOpenAppdata.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					File file = new File (FileUserdir);
					Desktop desktop = Desktop.getDesktop();
					desktop.open(file);
					} catch (Exception e) { e.printStackTrace();
				}
			}
		});
		main.add(btnOpenAppdata);
		
		JCheckBox chckbxUseUserHome = new JCheckBox("Use \"user home\" folder");
		chckbxUseUserHome.setEnabled(portableAvailable);
		chckbxUseUserHome.setSelected(cfg.userfolder);
		chckbxUseUserHome.addItemListener(new ItemListener() {
		    public void itemStateChanged(ItemEvent e) {
		    	if(portableAvailable) {
		    		portableCFG.userfolder = e.getStateChange() == 1;
		    		FileUserdir = portablePath;
		    		portableCFG.saveConfig(portablePath);
		    		cache = null; //reinit usercache
		    	}
		    	
		    	if(e.getStateChange() == 1) {
		    		btnOpenAppdata.setEnabled(true);
		    		btnOpenAppdata.setText("Open appdata");
		    		FileUserdir = createUserPath();
		    	}
		    	else {
		    		btnOpenAppdata.setEnabled(false);
		    		btnOpenAppdata.setText("Portable mode");
		    		FileUserdir = portablePath;
		    	}

		    	cfg = cLaunch.buildConfig(FileUserdir, appname + ".ini");
		    	cfg.userfolder = e.getStateChange() == 1;
		    	autoloadcheckBox.setSelected(cfg.autoloadFolder);
		    	chckbxAlwaysShowThis.setSelected(cfg.startup);
		    	settingsInited = false;
		    }
		});
		chckbxUseUserHome.setBounds(165, 44, 190, 23);
		chckbxUseUserHome.setFont(font);
		main.add(chckbxUseUserHome);
	}
	
	private void initSettingsTab(JPanel settings, Font font)
	{
		if(!settingsCreated) {
			initVideoModes(LwjglApplicationConfiguration.getDisplayModes(), LwjglApplicationConfiguration.getDesktopDisplayMode());
			
			JLabel sounddrv = new JLabel("Sound: ");
			sounddrv.setFont(font);
			sounddrv.setBounds(10, 12, 77, 14);
			settings.add(sounddrv);

			soundDeviceList = new JComboBox<String>();
			soundDeviceList.setFont(font);
			soundDeviceList.setBounds(91, 9, 256, 20);
			initSoundDevices();
			soundDeviceList.addItemListener(new ItemListener(){
				@Override
				public void itemStateChanged(ItemEvent e) {
					if(e.getStateChange() == ItemEvent.SELECTED) {
						cfg.snddrv = soundDeviceList.getSelectedIndex();
					}
				}
			});
			settings.add(soundDeviceList);

			label = new JLabel("Video mode:");
			label.setFont(font);
			label.setBounds(10, 74, 77, 14);
			settings.add(label);
	
			fullscr = new JCheckBox("Fullscreen");
			fullscr.setFont(font);
			fullscr.setBounds(269, 71, 100, 23);
			fullscr.setEnabled(false);
			fullscr.addItemListener(new ItemListener() {
			    public void itemStateChanged(ItemEvent e) {
			    	if(e.getStateChange() == 1)
			    		cfg.fullscreen = 1;
			    	else cfg.fullscreen = 0;
			    }
			});
			settings.add(fullscr);
	
			comboBox = new JComboBox<String>();
			comboBox.setFont(font);
			comboBox.setBounds(91, 71, 172, 20);
			for(int i = 0; i < validmodes.size(); i++)
				comboBox.addItem(strvmodes[i]);
			
			comboBox.addItemListener(new ItemListener(){
				@Override
				public void itemStateChanged(ItemEvent e) {
					if(e.getStateChange() == ItemEvent.SELECTED)
					{
						DisplayMode mode = getmode(comboBox.getSelectedIndex());
						cfg.ScreenWidth = mode.width;
						cfg.ScreenHeight = mode.height;
						fullscr.setEnabled(true);
					}
				}
			});
			settings.add(comboBox);

			borderless = new JCheckBox("Borderless mode");
			borderless.setFont(font);
			borderless.setBounds(10, 102, 140, 23);
			borderless.addItemListener(new ItemListener() {
			    public void itemStateChanged(ItemEvent e) {
			    	cfg.borderless = e.getStateChange() == 1;
			    }
			});
			settings.add(borderless);

			midiDeviceList = new JComboBox<String>();
			midiDeviceList.setFont(font);
			midiDeviceList.setBounds(91, 39, 256, 20);
			midiDeviceList.addItemListener(new ItemListener(){
				@Override
				public void itemStateChanged(ItemEvent e) {
					if(e.getStateChange() == ItemEvent.SELECTED) {
						midiDevice = midiDeviceList.getSelectedIndex() - 1;
						if(midiDevice != -1) {
							cfg.middrv = 1;
							cfg.midiSynth = midiMap.get(midiDevice);
						} else cfg.middrv = 0;
					}
				}
			});
			settings.add(midiDeviceList);
			
			JLabel label_2 = new JLabel("Midi Synth:");
			label_2.setFont(font);
			label_2.setBounds(10, 42, 77, 14);
			settings.add(label_2);
			settingsCreated = true;
		}

		if(settingsInited)
			return;
		
		fullscr.setSelected(cfg.fullscreen == 1);
		borderless.setSelected(cfg.borderless);
		initMidiDevice(cfg.midiSynth);

		midiDeviceList.setSelectedIndex(midiDevice + 1);
		soundDeviceList.setSelectedIndex(cfg.snddrv);
		
		int xdim = cfg.ScreenWidth;
		int ydim = cfg.ScreenHeight;
		int index = getmodeindex(xdim, ydim);
		if(index == -1) {
			comboBox.setEditable(true);
			try { 
				fullscr.setEnabled(false);
				comboBox.setSelectedItem(xdim + " x " + ydim + " 32bpp"); 
			} catch(Exception e) {}
			comboBox.setEditable(false);
		} else {
			comboBox.setSelectedIndex(index);
			fullscr.setEnabled(true);
		}
		settingsInited = true;
	}
	
	private void initAboutTab(JPanel about, Font font, URL img)
	{
		if(aboutInited)
			return;
		
		mySiteButton = new JButton("http://m210.duke4.net");
		mySiteButton.setFont(font);
		mySiteButton.setBounds(111, 88, 254, 23);
		mySiteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					openWebpage(new URL("http://m210.duke4.net/").toURI());
					} catch (Exception e) { e.printStackTrace();
				}
			}
		});
		about.add(mySiteButton);

		donateButton = new JButton("Donate");
		donateButton.setFont(font);
		donateButton.setBounds(10, 88, 91, 23);
		donateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					openWebpage(new URL("https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=axeleratorm210%40gmail%2ecom&lc=GB&item_name=M210%20Projects%20%28" + appname + "%29&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHosted").toURI());
					} catch (Exception e) { e.printStackTrace();
				}
			}
		});
		about.add(donateButton);
		
		ImagePanel icon = new ImagePanel(img);
		icon.setBounds(10, 11, 64, 64);
		about.add(icon);
		
		JLabel lbAppTile = new JLabel(frmPropFrame.getTitle());
		lbAppTile.setBounds(98, 29, 250, 14);
		lbAppTile.setFont(font);
		about.add(lbAppTile);
		
		JLabel lblAlexanderM = new JLabel("[M210®]     email: m210-2007@mail.ru");
		lblAlexanderM.setBounds(98, 50, 250, 14);
		lblAlexanderM.setFont(font);
		about.add(lblAlexanderM);
		
		while(!icon.isLoaded());
		
		aboutInited = true;
	}
	
	private void launchPort(final String title, final BConfig cfg)
	{	
		startButton.setEnabled(false);
    	tabbedPane.setEnabledAt(0, false);
    	tabbedPane.setEnabledAt(1, false);
    	tabbedPane.setEnabledAt(2, false);
    	if(!settingsInited) initMidiDevice(cfg.midiSynth);
    	
    	//Message log
		textPane = new JEditorPane();
		textPane.setEditable(false);
		textPane.setText("Initializing system...");
		logText = new JScrollPane(textPane);
		logText.setBorder(null);
		logText.setBounds(10, 150, 380, 169);

		final Timer logUpdate = new Timer(50, new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
            	textPane.setText(Console.GetLog());
            }
        });
		logUpdate.start();
		
		tabbedPane.setComponentAt(0, logText);
		tabbedPane.setSelectedIndex(0);
    	
		new Thread(new Runnable() {
	        public void run()
	        {
	        	String apptitle = title;
	        	if(!settingsInited)
	        		initVideoModes(LwjglApplicationConfiguration.getDisplayModes(), LwjglApplicationConfiguration.getDesktopDisplayMode());
	        	
	        	LwjglApplicationConfiguration lwjglConfig = new LwjglApplicationConfiguration();
	    		lwjglConfig.fullscreen = setFullscreen(cfg.ScreenWidth, cfg.ScreenHeight, cfg.fullscreen == 1);
	    		lwjglConfig.width = (cfg.ScreenWidth);
	    		lwjglConfig.height = (cfg.ScreenHeight);
	    		lwjglConfig.resizable = false;
	    		lwjglConfig.depth = 32; //z-buffer

	    		lwjglConfig.backgroundFPS = cfg.fpslimit;
	    		lwjglConfig.foregroundFPS = cfg.fpslimit;
	    		lwjglConfig.vSyncEnabled = cfg.gVSync;
	    		
	    		if(cfg.borderless)
	    			System.setProperty("org.lwjgl.opengl.Window.undecorated", "true");
//	    		System.setProperty("org.lwjgl.opengl.Display.allowSoftwareOpenGL", "true");
	    		
	    		if(cfg.checkVersion)
	    		{
	    			String newver = checkVersion(cLaunch.verfile, appversion);
	    			if(newver != null)
	    				apptitle += " (new " + newver + ")";
	    		}
	    		
	    		lwjglConfig.title = apptitle;

	        	cLaunch.run(lwjglConfig, midiDevice, cfg);

				portableCFG = null; 

				logUpdate.stop();
				while(!Display.isCreated()); //Don't close frame while game display not created
				frmPropFrame.dispose();
	        }
	    }).start();
	}
	
	private String getVersion(String filename) throws Exception
	{
		URL version = new URL("http://m210.ucoz.ru/Files/" + filename);
        BufferedReader in = new BufferedReader(new InputStreamReader(version.openStream()));
        String inputLine = in.readLine();
        in.close();
		return inputLine;
	}
	
	public String checkVersion(String filename, String version)
	{
		if(!cLaunch.canCheck) return null;
		
		String newver = null;
		String verstd;
		try {
			verstd = getVersion(filename);
			int webver = Integer.parseInt(verstd);
			int appver = Integer.parseInt(version.replaceAll("[^0-9]", ""));
			
			if(webver > appver)
			{
				verstd = "v" + verstd.substring(0, 1) + "." + verstd.substring(1, verstd.length());
				newver = verstd;
			}
		} catch (Exception e) {}
	
		if(newver != null) {
			showMessage("Please update!", "New version available: " + newver);
		} else Console.Println("You are using the latest version");
		
		return newver;
	}
	
	private String createUserPath() {
		if(userHomePath != null)
			return userHomePath;
		
		userHomePath = System.getProperty("user.home") + File.separator + "M210Projects" + File.separator + appname + File.separator;
		File f = new File(userHomePath);
        if(!f.exists()) 
        	if (!f.mkdirs() && !f.isDirectory()) {
        		Console.Println("Can't create path \"" + userHomePath + '"', OSDTEXT_RED);
            }
        return userHomePath;
    }

	private void openWebpage(URI uri) throws Exception {
	    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
	    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE))
	         desktop.browse(uri);
	}
	
	private void showMessage(String header, String message)
	{
		JOptionPane frame = new JOptionPane();
		frame.setMessage(message);
		frame.setMessageType(JOptionPane.INFORMATION_MESSAGE);
		JDialog dlog = frame.createDialog(null, header);
		frame.setBackground(dlog.getBackground());
		dlog.setAlwaysOnTop(true);
		dlog.setVisible(true);
		
		Console.Println(message, OSDTEXT_GOLD);
	}

//	private int resnum;
	private int checkResources(String path, String[] resources)
	{
		if(resources.length < 2) {
			showMessage("Resource length error!", "Resource array should have a title and at least one resource file");
			return -1;
		}
		
		File directory = new File(path);
		
		File[] fList = directory.listFiles();
		if(fList == null)
			return -1;
		
		HashMap<String, File> filesMap = new HashMap<String, File>();
		for (File file : fList) 
			filesMap.put(toLowerCase(file.getName()), file);
		
		for(int i = 1; i < resources.length; i++) {
			if(filesMap.get(resources[i]) == null)
				return -1;
		}
			
//		resnum = -1;
//		int r = 0, i = 1;
//		while(i < resources[r].length) {
//			if(filesMap.get(resources[r][i]) == null) {
//				if(r < resources.length - 1) {
//					r++; i = 1;
//					continue;
//				}
//				return -1;
//			}
//			i++;
//		}
//		resnum = r;
		
//		if(!Files.isWritable(directory.toPath())) // JDK 1.7
		if(!isWritable(path))
			return 0;
		
		return 1;
	}
	
	private String getDirPath()
	{
		String path = System.getProperty("user.dir") + File.separator;
		String OS = System.getProperty("os.name");
		if( (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 ) ) {
			Console.LogPrint("Debug user.dir: " + path);
			String parent = System.getProperty("java.class.path");
			if(parent != null) 
				path = new File(parent).getAbsolutePath() + File.separator;

			Console.LogPrint("Debug parentpath: " + path); //XXX
		}
		
		return path;
	}
	
	private boolean isWritable(String path)
	{
		File f = new File(path + "tmp_check.write");
		if(f.exists()) return f.delete();
		try {
			if(f.createNewFile()) 
				return f.delete();
		} catch (Exception e) {}
		return false;
	}

	private void initSoundDevices()
	{
		if(soundDeviceList != null) {
			soundDeviceList.addItem("None");
			soundDeviceList.addItem("OpenAL Soft");
		}
	}
	
	private void initMidiDevice(String midiSynth)
	{
		midiDevice = -1;
		int defGervill = -1;
		byte[] namedata = new byte[64];
		if(midiMap == null) {
			List<MidiDevice> devices = MidiMusicModule.getDevices();
			midiMap = new HashMap<Integer, String>();

			if(midiDeviceList != null) midiDeviceList.addItem("None");
			for(int i = 0; i < devices.size(); i++) {
				String name = devices.get(i).getDeviceInfo().getName();
				//Cyrillic convert
				for(int c = 0; c < name.length(); c++) 
					namedata[c] = (byte) name.codePointAt(c);
				name = new String(namedata, 0, name.length()).trim();

				
				if(midiDeviceList != null) midiDeviceList.addItem(name);
				if(name.equalsIgnoreCase("Gervill"))
					defGervill = i;
				midiMap.put(i, name);
			} 
		}
		
		for(Integer key : midiMap.keySet()){
			if(midiMap.get(key).equals(midiSynth))
				midiDevice = key;
		}
		
		if(midiDevice == -1 && defGervill != -1)
		{
			midiDevice = defGervill;
			cfg.midiSynth = midiMap.get(midiDevice);
			cfg.middrv = 1;
		}
	}
}

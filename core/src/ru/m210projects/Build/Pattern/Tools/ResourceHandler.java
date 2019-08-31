package ru.m210projects.Build.Pattern.Tools;

import static ru.m210projects.Build.Engine.MAXTILES;
import static ru.m210projects.Build.Engine.picanm;
import static ru.m210projects.Build.Engine.tilesizx;
import static ru.m210projects.Build.Engine.tilesizy;
import static ru.m210projects.Build.Engine.waloff;
import static ru.m210projects.Build.FileHandle.Compat.*;
import java.io.File;
import java.util.Arrays;
import java.util.List;

import ru.m210projects.Build.Architecture.BuildGdx;
import ru.m210projects.Build.FileHandle.DirectoryEntry;
import ru.m210projects.Build.FileHandle.FileEntry;
import ru.m210projects.Build.FileHandle.Group;
import ru.m210projects.Build.FileHandle.GroupResource;
import ru.m210projects.Build.FileHandle.Resource;
import ru.m210projects.Build.Pattern.BuildGame;
import ru.m210projects.Build.Script.DefScript;

public abstract class ResourceHandler {
	
	public abstract class UserResource {
		public final String format;
		
		public abstract void execute(String filename);
		
		public UserResource(String format)
		{
			this.format = format;
		}
	}

	protected Group usergroup;
	protected boolean usecustomarts;
	protected BuildGame game;
	protected UserResource[] resources;
	
	public ResourceHandler(BuildGame game, UserResource... resources)
	{
		this.game = game;
		this.resources = resources;
	}
	
	public abstract void resetCustomArts();
	
	public abstract void searchEpisodeResources(DirectoryEntry cache);
	
	public abstract void prepareusergroup(Group group, boolean removable) throws Exception;
	
	public Group getUserGroup()
	{
		if(usergroup == null)
			usergroup = BuildGdx.cache.add("User", true);
		
		return usergroup;
	}
	
	public void resetEpisodeResources()
	{
		BuildGdx.cache.clearDynamicResources();

		usergroup = null;
		if(!usecustomarts)
			return; 

		System.err.println("Reset to default resources");
		Arrays.fill(tilesizx, 0, MAXTILES, (short)0);
		Arrays.fill(tilesizy, 0, MAXTILES, (short)0);
		Arrays.fill(picanm, 0, MAXTILES, 0);
		Arrays.fill(waloff, 0, MAXTILES, null);
		
		if(game.pEngine.loadpics("tiles000.art") == 0) {
			game.ThrowError("ART files not found " + new File(Path.Game.getPath() + "TILES###.ART").getAbsolutePath());
			System.exit(0);
		}

		game.setDefs(game.baseDef);  //return tilefromtiles textures
			
		resetCustomArts();

	    usecustomarts = false;
	}

	protected void initGroupResources(List<GroupResource> list)
	{
		for(Resource res : list) {
			GroupResource gres = (GroupResource) res;
			if(gres.getExtension().equals("art")) {
				game.pEngine.loadpic(gres.getFullName());
				usecustomarts = true;
			}
			for(int i = 0; i < resources.length; i++)
				if(gres.getExtension().equals(resources[i].format))
					resources[i].execute(gres.getFullName());
		}
	}
	
	public void checkEpisodeResources(DirectoryEntry parent, FileEntry pkg)
	{
		resetEpisodeResources();

		if(pkg != null) //packaged addon
		{
			try {
				Group gr = BuildGdx.cache.add(pkg.getPath());
				gr.setFlags(true, true);
				prepareusergroup(gr, true); //init other group-files and apply rfs scripts to the group
			} catch(Exception e) { 
				game.GameMessage("Error found in " + pkg.getName() + "\r\n" + e.getMessage()); 
				return;
			}
		} else if(!parent.getName().equals("<main>")) //addon in external folder
			searchEpisodeResources(parent); //init rfs scripts and add files to usergroup with rfs datas
		
		//Loading user package files
		DefScript addonScript = null;
		
		//TODO: search for buildgdx.def
		
		if(addonScript == null) //if addonDef not found
			addonScript = new DefScript(game.baseDef, null);

		//Loading user package files
		initGroupResources(BuildGdx.cache.getDinamicResources());
	}
}

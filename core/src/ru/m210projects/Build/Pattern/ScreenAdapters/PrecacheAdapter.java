package ru.m210projects.Build.Pattern.ScreenAdapters;

import static ru.m210projects.Build.Engine.MAXTILES;
import static ru.m210projects.Build.Engine.pow2char;
import static ru.m210projects.Build.Engine.waloff;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.badlogic.gdx.ScreenAdapter;

import ru.m210projects.Build.Engine;
import ru.m210projects.Build.Architecture.BuildGdx;
import ru.m210projects.Build.Pattern.BuildGame;
import ru.m210projects.Build.Pattern.BuildNet;
import ru.m210projects.Build.Pattern.MenuItems.MenuHandler;
import ru.m210projects.Build.Render.GLRenderer;

public abstract class PrecacheAdapter extends ScreenAdapter {
	
	private class PrecacheQueue {
		private String name;
		private Runnable runnable;
		
		public PrecacheQueue(String name, Runnable runnable)
		{
			this.name = name;
			this.runnable = runnable;
		}
	}

	private byte[] tiles;
	private int currentIndex = 0;
	private List<PrecacheQueue> queues = new ArrayList<PrecacheQueue>();
	protected Runnable toLoad;

	protected BuildNet net;
	protected Engine engine;
	protected BuildGame game;
	protected MenuHandler menu;
	
	public void addQueue(String name, Runnable runnable)
	{
		queues.add(new PrecacheQueue(name, runnable));
	}
	
	public void clearQueue()
	{
		queues.clear();	
	}
	
	public PrecacheAdapter(BuildGame game) {
		this.game = game;
		this.engine = game.pEngine;
		this.net = game.pNet;
		this.menu = game.pMenu;
		this.tiles = new byte[MAXTILES >> 3];
	}
	
	public ScreenAdapter init(Runnable toLoad)
	{
		this.toLoad = toLoad;
		return this;
	}
	
	@Override
	public void show()
	{
		net.ready2send = false;
		currentIndex = 0;
		Arrays.fill(tiles, (byte) 0);
	}
	
	public void addTile(int tile)
	{
		tiles[tile >> 3] |= pow2char[tile & 7];
	}
	
	protected abstract void draw(String title, int index);

	@Override
	public void render(float delta) {
		engine.clearview(0);
		if(currentIndex >= queues.size())
		{
			draw("Getting ready...", -1);
			if(toLoad != null) {
				BuildGdx.app.postRunnable(toLoad);
				toLoad = null;
			}
		} else {
			PrecacheQueue current = queues.get(currentIndex);
			draw(current.name, currentIndex);
			BuildGdx.app.postRunnable(current.runnable);
			currentIndex++;
		}
		
		engine.sampletimer();
		engine.nextpage();
	}
	
	protected void doprecache(int method)
	{
		GLRenderer gl = engine.glrender();
		for (int i = 0; i < MAXTILES; i++) {
			if (tiles[i >> 3] == 0) {
				i += 7;
				continue;
			}

			if ((tiles[i >> 3] & pow2char[i & 7]) != 0 && waloff[i] == null) {
				engine.loadtile(i);
				if(gl != null) 
					gl.precache(i, 0, method);
			}
		}
		Arrays.fill(tiles, (byte) 0);
	}
}

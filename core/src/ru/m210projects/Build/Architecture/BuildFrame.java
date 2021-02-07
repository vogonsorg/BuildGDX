// This file is part of BuildGDX.
// Copyright (C) 2017-2020  Alexander Makarov-[M210] (m210-2007@mail.ru)
//
// BuildGDX is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// BuildGDX is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with BuildGDX.  If not, see <http://www.gnu.org/licenses/>.

package ru.m210projects.Build.Architecture;

import java.net.URL;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.GdxRuntimeException;

public abstract class BuildFrame {

	public enum FrameType {
		Canvas, GL
	}

	public enum FrameStatus {
		Running, Pause, Resume, Changed, Closed
	}

	protected FrameType type;
	public URL icon;

	protected BuildInput input;
	protected BuildGraphics graphics;
	protected final BuildConfiguration config;
	protected final ApplicationListener listener;

	protected boolean wasActive = true;

	public BuildFrame(ApplicationListener listener, BuildConfiguration config) {
		this.config = config;
		this.listener = listener;
	}

	public abstract BuildGraphics getGraphics(FrameType type);

	public abstract BuildInput getInput(FrameType type);

	public void render() {
		listener.render();
	}

	public void create() {
		listener.create();
	}

	public void pause() {
		listener.pause();
	}

	public void resume() {
		listener.resume();
	}

	public void resize(int width, int height) {
		listener.resize(width, height);
	}

	public void setType(FrameType type)
	{
		if(getType() != type) {
			dispose();

			graphics = getGraphics(type);
			input = getInput(type);

			init();

			Gdx.gl = BuildGdx.gl = graphics.getGL10();
			Gdx.gl20 = BuildGdx.gl20 = graphics.getGL20();
			Gdx.gl30 = BuildGdx.gl30 = graphics.getGL30();

			Gdx.graphics = BuildGdx.graphics = graphics;
			Gdx.input = BuildGdx.input = input;
		}
	}

	public BuildInput getInput() {
		return input;
	}

	public BuildGraphics getGraphics() {
		return graphics;
	}

	public BuildFrame init() {
		try {
			graphics.init();
			input.init(this);
		} catch (Exception e) {
			throw new GdxRuntimeException(e);
		}

		graphics.setVSync(config.vsync);
		graphics.resize = true;
		graphics.lastTime = System.nanoTime();
		return this;
	}

	protected boolean isChanged() {
		config.x = graphics.getX();
		config.y = graphics.getY();
		if (graphics.resize || graphics.wasResized()
			|| graphics.getWidth() != config.width
			|| graphics.getHeight() != config.height) {
			graphics.resize = false;
			config.width = graphics.getWidth();
			config.height = graphics.getHeight();
			graphics.updateSize(config.width, config.height);
			graphics.requestRendering();
			return true;
		}
		return false;
	}

	public boolean process(boolean shouldRender) {
		boolean isActive = graphics.isActive();
		input.update();
		shouldRender |= graphics.shouldRender();
		input.processEvents();

		if (!isActive && config.backgroundFPS == -1) shouldRender = false;
		int frameRate = isActive ? config.foregroundFPS : config.backgroundFPS;

		if(graphics.vsync)
			frameRate = graphics.getRefreshRate();

		if (shouldRender) {
			graphics.updateTime();
			graphics.frameId++;
		} else {
			// Sleeps to avoid wasting CPU in an empty loop.
			if (frameRate == -1) frameRate = 10;
			if (frameRate == 0) frameRate = config.backgroundFPS;
			if (frameRate == 0) frameRate = 30;
		}
		if (frameRate > 0) graphics.sync(frameRate);

		return shouldRender;
	}

	public FrameStatus getStatus()
	{
		if(graphics.isCloseRequested() || !BuildGdx.app.running)
			return FrameStatus.Closed;

		boolean isActive = graphics.isActive();
		if (wasActive && !isActive) { // if it's just recently minimized from active state
			wasActive = false;
			return FrameStatus.Pause;
		}

		if (!wasActive && isActive) { // if it's just recently focused from minimized state
			wasActive = true;
			return FrameStatus.Resume;
		}

		if(isChanged())
			return FrameStatus.Changed;

		return FrameStatus.Running;
	}

	public boolean isActive()
	{
		return graphics.isActive();
	}

	public void dispose() {
		if(graphics != null) {
			graphics.dispose();

			Gdx.gl = BuildGdx.gl = null;
			Gdx.gl20 = BuildGdx.gl20 = null;
			Gdx.gl30 = BuildGdx.gl30 = null;
		}
	}

	public void update() {
		graphics.update();
	}

	public int getX() {
		return graphics.getX();
	}

	public int getY() {
		return  graphics.getY();
	}

	public FrameType getType() {
		return type;
	}

	public BuildConfiguration getConfig() {
		return config;
	}
}

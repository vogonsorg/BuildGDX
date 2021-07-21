package ru.m210projects.Build.Render;

import static ru.m210projects.Build.Engine.sector;
import static ru.m210projects.Build.Engine.show2dsector;
import static ru.m210projects.Build.Engine.sprite;
import static ru.m210projects.Build.Engine.wall;

import ru.m210projects.Build.Gameutils;
import ru.m210projects.Build.Types.SECTOR;
import ru.m210projects.Build.Types.SPRITE;
import ru.m210projects.Build.Types.WALL;

public class DefaultMapSettings implements IOverheadMapSettings {

	@Override
	public boolean isShowSprites(MapView view) {
		return false;
	}

	@Override
	public boolean isShowFloorSprites(MapView view) {
		return false;
	}

	@Override
	public boolean isShowRedWalls() {
		return true;
	}

	@Override
	public boolean isShowAllPlayers() {
		return false;
	}

	@Override
	public boolean isSpriteVisible(MapView view, int index) {
		switch (sprite[index].cstat & 48) {
		case 0:
			return true;
		case 16: // wall sprites
			return false;
		case 32: // floor sprites
			return isShowFloorSprites(view);
		}
		return true;
	}

	@Override
	public boolean isWallVisible(int w, int s) {
		WALL wal = wall[w];
		SECTOR sec = sector[s];
		if (wal.nextsector != 0) // red wall
			return (wal.nextwall <= w && ((sector[wal.nextsector].ceilingz != sec.ceilingz //
					|| sector[wal.nextsector].floorz != sec.floorz //
					|| ((wal.cstat | wall[wal.nextwall].cstat) & (16 + 32)) != 0) //
					|| (!isFullMap() && (show2dsector[wal.nextsector >> 3] & 1 << (wal.nextsector & 7)) == 0)));
		return true;
	}

	@Override
	public int getWallColor(int w) {
		WALL wal = wall[w];
		if (Gameutils.isValidSector(wal.nextsector)) // red wall
			return 31;
		return 31; // white wall
	}

	@Override
	public int getSpriteColor(int s) {
		SPRITE spr = sprite[s];
		switch (spr.cstat & 48) {
		case 0:
			return 31;
		case 16:
			return 31;
		case 32:
			return 31;
		}

		return 31;
	}

	@Override
	public int getPlayerSprite(int player) {
		return -1;
	}

	@Override
	public int getPlayerPicnum(int player) {
		int spr = getPlayerSprite(player);
		return spr != -1 ? sprite[spr].picnum : -1;
	}

	@Override
	public int getPlayerZoom(int player, int czoom) {
		return czoom;
	}

	@Override
	public boolean isFullMap() {
		return false;
	}

	@Override
	public boolean isScrollMode() {
		return false;
	}

	@Override
	public int getViewPlayer() {
		return 0;
	}
}

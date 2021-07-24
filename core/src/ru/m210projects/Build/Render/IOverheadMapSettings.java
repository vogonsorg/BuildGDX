package ru.m210projects.Build.Render;

public interface IOverheadMapSettings {

	public enum MapView {
		Polygons, Lines
	};

	public boolean isFullMap();

	public boolean isScrollMode();

	public int getViewPlayer();

	public boolean isShowSprites(MapView view);

	public boolean isShowFloorSprites();

	public boolean isShowRedWalls();

	public boolean isShowAllPlayers();

	public boolean isSpriteVisible(MapView view, int index);

	public boolean isWallVisible(int w, int ses);

	public int getWallColor(int w, int sec);

	public int getWallX(int w);

	public int getWallY(int w);

	public int getSpriteColor(int s);

	public int getSpriteX(int spr);

	public int getSpriteY(int spr);

	public int getPlayerSprite(int player);

	public int getPlayerPicnum(int player);

	public int getPlayerZoom(int player, int czoom);

}

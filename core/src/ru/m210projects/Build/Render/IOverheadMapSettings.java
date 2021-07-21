package ru.m210projects.Build.Render;

public interface IOverheadMapSettings {

	public enum MapView {
		Polygons, Lines
	};

	public boolean isFullMap();

	public boolean isScrollMode();

	public int getViewPlayer();

	public boolean isShowSprites(MapView view);

	public boolean isShowFloorSprites(MapView view);

	public boolean isShowRedWalls();

	public boolean isShowAllPlayers();

	public boolean isSpriteVisible(MapView view, int index);

	public boolean isWallVisible(int w, int s);

	public int getWallColor(int w);

	public int getSpriteColor(int s);

	public int getPlayerSprite(int player);

	public int getPlayerPicnum(int player);

	public int getPlayerZoom(int player, int czoom);

}

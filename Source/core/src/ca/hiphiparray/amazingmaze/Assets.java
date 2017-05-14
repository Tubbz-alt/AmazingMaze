/********************************************************************************
 * Amazing Maze is an educational game created in Java with the libGDX library.
 * Copyright (C) 2017 Hip Hip Array
 *
 * This file is part of Amazing Maze.
 *
 * Amazing Maze is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Amazing Maze is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Amazing Maze. If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package ca.hiphiparray.amazingmaze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader.FreeTypeFontLoaderParameter;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.utils.Disposable;

/**
 * Class to easily manage assets, furthering the capabilities of an {@link AssetManager};
 *
 * @author Vincent Macri
 */
public class Assets implements Disposable {

	/** Efficiently manages game assets. */
	protected AssetManager manager;

	/** The UI skin. */
	protected Skin skin;
	/** The location of the tile atlas. */
	protected static final String TILE_ATLAS_LOCATION = "tiles/tiles.atlas";

	/** The regular monospace font. */
	protected static final String MONO_REGULAR = "LiberationMono-Regular";
	/** The bold monospace font. */
	protected static final String MONO_BOLD = "LiberationMono-Bold";
	/** The italic monospace font. */
	protected static final String MONO_ITALIC = "LiberationMono-Italic";
	/** The bold italic monospace font. */
	protected static final String MONO_BOLD_ITALIC = "LiberationMono-BoldItalic";

	/** The regular sans font. */
	protected static final String SANS_REGULAR = "LiberationSans-Regular";
	/** The bold sans font. */
	protected static final String SANS_BOLD = "LiberationSans-Bold";
	/** The italic sans font. */
	protected static final String SANS_ITALIC = "LiberationSans-Italic";
	/** The bold italic sans font. */
	protected static final String SANS_BOLD_ITALIC = "LiberationSans-BoldItalic";

	/** The regular serif font. */
	protected static final String SERIF_REGULAR = "LiberationSerif-Regular";
	/** The bold serif font. */
	protected static final String SERIF_BOLD = "LiberationSerif-Bold";
	/** The italic serif font. */
	protected static final String SERIF_ITALIC = "LiberationSerif-Italic";
	/** The bold italic serif font. */
	protected static final String SERIF_BOLD_ITALIC = "LiberationSerif-BoldItalic";

	/** The menu image. */
	protected static final String MENU_IMAGE = "menu/title.png";

	/** The font size for small text. */
	protected static final int SMALL_FONT_SIZE = 32;
	/** The font size for regular text. */
	protected static final int REGULAR_FONT_SIZE = 64;
	/** The font size for large text. */
	protected static final int LARGE_FONT_SIZE = 128;

	/** The atlas name of the background tile. */
	private final String BACKGROUND = "background";
	/** The atlas name of the placeholder tile. */
	private final String PLACEHOLDER = "placeholder"; // TODO: Remove

	/** The atlas name of the on cross. */
	private final String CROSS_ON = "cross-on";
	/** The atlas name of the off cross. */
	private final String CROSS_OFF = "cross-off";
	/** The atlas name of the unknown cross. */
	private final String CROSS_UNKNOWN = "cross-unknown";

	/** The atlas name of the on turn. */
	private final String TURN_ON = "l-on";
	/** The atlas name of the off turn. */
	private final String TURN_OFF = "l-off";
	/** The atlas name of the unknown turn. */
	private final String TURN_UNKNOWN = "l-unknown";

	/** The atlas name of the unknown vertical wire. */
	private final String VERTICAL_UNKNOWN = "vertical-unknown";

	/** The atlas name of the on T. */
	private final String T_ON = "t-on";
	/** The atlas name of the off T. */
	private final String T_OFF = "t-off";
	/** The atlas name of the unknown T. */
	private final String T_UNKNOWN = "t-unknown";

	/** The tiles used in our map. */
	protected TiledMapTileSet tiles;

	/**
	 * {@link Assets} constructor.
	 * Calling this constructor loads in all of the game assets.
	 * As such, only one {@link Assets} instance should ever be created.
	 */
	public Assets() {
		manager = new AssetManager();

		// Allow loading FreeTypeFonts.
		FileHandleResolver resolver = new InternalFileHandleResolver();
		manager.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(resolver));
		manager.setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(resolver));

		loadSkin();
		loadMapResources();
		manager.load(MENU_IMAGE, Texture.class);

		manager.finishLoading();
	}

	/** Helper method for loading the map resources. */
	private void loadMapResources() {
		manager.load(TILE_ATLAS_LOCATION, TextureAtlas.class);
		manager.finishLoadingAsset(TILE_ATLAS_LOCATION);

		tiles = new TiledMapTileSet();

		StaticTiledMapTile backgroundTile = new StaticTiledMapTile(manager.get(Assets.TILE_ATLAS_LOCATION, TextureAtlas.class).findRegion(BACKGROUND));
		StaticTiledMapTile placeHolderTile = new StaticTiledMapTile(manager.get(Assets.TILE_ATLAS_LOCATION, TextureAtlas.class).findRegion(PLACEHOLDER));

		tiles.putTile(TileIDs.computeID(TileIDs.BACKGROUND), backgroundTile);
		tiles.putTile(TileIDs.computeID(TileIDs.PLACEHOLDER), placeHolderTile);

		StaticTiledMapTile verticalUnknown = new StaticTiledMapTile(manager.get(Assets.TILE_ATLAS_LOCATION, TextureAtlas.class).findRegion(VERTICAL_UNKNOWN));
		tiles.putTile(TileIDs.computeID(TileIDs.WIRE_RANGE, TileIDs.VERTICAL, TileIDs.UNKNOWN), verticalUnknown);
	}

	/** Load the UI skin. */
	private void loadSkin() {
		manager.load("menu/uiskin.json", Skin.class);
		manager.finishLoadingAsset("menu/uiskin.json"); // Make sure the skin is loaded before continuing.
		skin = manager.get("menu/uiskin.json", Skin.class); // Retrieve the skin.

		// Set the fonts. This is not done in the .json in order to allow easier adjusting of font sizes.
		skin.get(LabelStyle.class).font = getFont(SANS_REGULAR, REGULAR_FONT_SIZE);
		skin.get(TextButtonStyle.class).font = getFont(SANS_REGULAR, REGULAR_FONT_SIZE);
	}

	/**
	 * Return the given font, if it is loaded.
	 * The font will be lazily loaded if it is not loaded already.
	 *
	 * @param fontName the name of the font.
	 * @param fontSize the size of the font.
	 * @return the desired {@link BitmapFont}.
	 */
	protected BitmapFont getFont(String fontName, int fontSize) {
		if (manager.containsAsset(fontName + fontSize + ".ttf")) {
			return manager.get(fontName + fontSize + ".ttf");
		}
		FreeTypeFontLoaderParameter fontParams = new FreeTypeFontLoaderParameter();
		fontParams.fontFileName = "fonts/" + fontName + ".ttf";
		fontParams.fontParameters.size = (int) (fontSize * Gdx.graphics.getDensity()); // Make sure font size scales correctly on different monitors.
		manager.load(fontName + fontSize + ".ttf", BitmapFont.class, fontParams);
		manager.finishLoadingAsset(fontName + fontSize + ".ttf");
		return manager.get(fontName + fontSize + ".ttf");
	}

	@Override
	public void dispose() {
		manager.dispose();
	}
}

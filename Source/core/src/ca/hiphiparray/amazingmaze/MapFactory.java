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

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.utils.Array;

import ca.hiphiparray.amazingmaze.FishCell.FishColour;

/**
 * Class to procedurally generate maps.
 *
 * @since 0.1
 * @author Vincent Macri
 * @author Chloe Nguyen
 * <br>
 * Time (Chloe): 30 mins
 * <br>
 * Time (Vincent): 20 hours
 */
public class MapFactory {

	/** The width of the maps generated by this factory. */
	private final int width;
	/** The height of the maps generated by this factory. */
	private final int height;

	/** The random number generator used by this factory. */
	private final Random random;

	/**
	 * Reference to an {@link Assets} instance to get images from.
	 * Essentially, this is a reference to {@link AmazingMazeGame#assets}.
	 */
	private final Assets assets;

	/** The name of the background layer. */
	public static final String BACKGROUND_LAYER = "background";
	/** The name of the object layer. */
	public static final String OBJECT_LAYER = "objects";
	/** The name of the wire layer. */
	public static final String WIRE_LAYER = "wires";
	/** The name of the power-up layer. */
	public static final String ITEM_LAYER = "items";

	/** The distance between the wires. */
	final static int WIRE_DISTANCE = 5;
	/** The start distance. */
	final static int START_DISTANCE = 3;

	/** Arraylist of gates of wires that are on. */
	protected ArrayList<Circuit> gateOn;

	/** Array of locations of the gates. */
	private Array<Point> gateLocations;

	/**
	 * Constructor for creation of a map factory.
	 *
	 * @param game The {@link AmazingMazeGame} instance to get resources from.
	 * @param seed The seed to use for generation by this factory.
	 * @param width The width of the maps (in tiles) generated by this factory.
	 * @param height The height of the maps (in tiles) generated by this factory.
	 * @param tileSize The side length (in pixels) of the tiles.
	 */
	public MapFactory(final AmazingMazeGame game, long seed, int width, int height, int tileSize) {
		this.assets = game.assets;
		this.random = new Random(seed);
		this.width = width;
		this.height = height;
		this.gateLocations = new Array<Point>();
		this.gateOn = new ArrayList<Circuit>();
	}

	/**
	 * Return a map generated with the {@link MapFactory}'s parameters.
	 *
	 * @return a tiled map.
	 */
	public TiledMap generateMap() {
		TiledMap map = new TiledMap();
		map.getTileSets().addTileSet(assets.tiles);

		TiledMapTileLayer backgroundLayer = new TiledMapTileLayer(width, height, MazeScreen.TILE_SIZE, MazeScreen.TILE_SIZE);
		backgroundLayer.setName(BACKGROUND_LAYER);
		for (int c = 0; c < backgroundLayer.getWidth(); c++) {
			for (int r = 0; r < backgroundLayer.getHeight(); r++) {
				Cell cell = new Cell();
				cell.setTile(assets.tiles.getTile(TileIDs.computeID(TileIDs.BACKGROUND)));
				backgroundLayer.setCell(c, r, cell);
			}
		}
		map.getLayers().add(backgroundLayer);

		final int gateSpace = 2;
		final int extraRoom = 3;
		List<Integer> splits = generateWireLocations();

		TiledMapTileLayer objectLayer = new TiledMapTileLayer(width, height, MazeScreen.TILE_SIZE, MazeScreen.TILE_SIZE);
		objectLayer.setName(OBJECT_LAYER);
		TiledMapTileLayer wireLayer = new TiledMapTileLayer(width, height, MazeScreen.TILE_SIZE, MazeScreen.TILE_SIZE);
		wireLayer.setName(WIRE_LAYER);
		for (int col : splits) { // Place the middle barriers and the unknown wires.
			boolean upperOutput = random.nextBoolean();
			Circuit upperGate = new Circuit(upperOutput, random);
			Circuit lowerGate = new Circuit(!upperOutput, random);
			Point highLocation = new Point(col, height - gateSpace);
			Point lowLocation = new Point(col, gateSpace - 1);

			if (Circuit.evaluateGate(upperGate.getGate(), upperGate.isInputA(), upperGate.isInputB())) {
				gateOn.add(upperGate);
			} else {
				gateOn.add(lowerGate);
			}

			placeUpperCircuit(objectLayer, upperGate, highLocation);
			placeLowerCircuit(objectLayer, lowerGate, lowLocation);
			int barrierLoc = randomInt(gateSpace + extraRoom, height - (gateSpace + extraRoom));
			Cell cell = new Cell();
			cell.setTile(assets.tiles.getTile(TileIDs.computeID(TileIDs.BARRIER)));
			objectLayer.setCell(col, barrierLoc, cell);
			for (int r = barrierLoc - 1; r >= gateSpace; r--) { // Place the lower wires.
				WireCell wire = new WireCell(!upperOutput);
				wire.setTile(assets.tiles.getTile(TileIDs.computeID(TileIDs.WIRE_RANGE, TileIDs.VERTICAL, TileIDs.UNKNOWN)));
				wireLayer.setCell(col, r, wire);
			}
			for (int r = barrierLoc + 1; r < height - gateSpace; r++) { // Place the upper wires.
				WireCell wire = new WireCell(upperOutput);
				wire.setTile(assets.tiles.getTile(TileIDs.computeID(TileIDs.WIRE_RANGE, TileIDs.VERTICAL, TileIDs.UNKNOWN)));
				wireLayer.setCell(col, r, wire);
			}
		}
		for (int c = 0; c < width; c++) {
			if (!splits.contains(c)) {
				Cell cell = new Cell();
				cell.setTile(assets.tiles.getTile(TileIDs.computeID(TileIDs.BARRIER)));
				objectLayer.setCell(c, gateSpace, cell);
				cell = new Cell();
				cell.setTile(assets.tiles.getTile(TileIDs.computeID(TileIDs.BARRIER)));
				objectLayer.setCell(c, height - gateSpace - 1, cell);
			}
		}

		map.getLayers().add(objectLayer);
		map.getLayers().add(wireLayer);

		TiledMapTileLayer powerUpLayer = new TiledMapTileLayer(width, height, MazeScreen.TILE_SIZE, MazeScreen.TILE_SIZE);
		powerUpLayer.setName(ITEM_LAYER);
		for (int c = 1; c < width; c++) {
			if (!splits.contains(c)) {
				if (random.nextDouble() <= 0.25) {
					placeFish(powerUpLayer, c, gateSpace);
					c++;
				} else if (random.nextDouble() <= 0.1) {
					placeCheese(powerUpLayer, c, gateSpace);
					c++;
				}
			}
		}
		map.getLayers().add(powerUpLayer);

		return map;
	}

	/**
	 * Place a piece of cheese on the map.
	 *
	 * @param layer the layer to add the cheese to.
	 * @param col the column to place the cheese on.
	 * @param gateSpace how much space to leave for gates.
	 */
	private void placeCheese(TiledMapTileLayer layer, int col, int gateSpace) {
		int row = randomInt(gateSpace + 1, height - gateSpace - 1);
		Cell cheese = new Cell();
		cheese.setTile(assets.tiles.getTile(TileIDs.computeID(TileIDs.POWERUP_RANGE, TileIDs.CHEESE)));
		layer.setCell(col, row, cheese);
	}

	/**
	 * Place a fish on the map.
	 *
	 * @param layer the layer to add the fish to.
	 * @param col the column to place the fish on.
	 * @param gateSpace how much space to leave for gates.
	 */
	private void placeFish(TiledMapTileLayer layer, int col, int gateSpace) {
		FishCell fish;
		double r = random.nextDouble();
		if (r <= 0.2) {
			fish = new FishCell(assets.tiles.getTile(TileIDs.computeID(TileIDs.POWERUP_RANGE, TileIDs.FISH, TileIDs.BLUE)), FishColour.BLUE);
		} else if (r <= 0.4) {
			fish = new FishCell(assets.tiles.getTile(TileIDs.computeID(TileIDs.POWERUP_RANGE, TileIDs.FISH, TileIDs.PURPLE)), FishColour.PURPLE);
		} else if (r <= 0.6) {
			fish = new FishCell(assets.tiles.getTile(TileIDs.computeID(TileIDs.POWERUP_RANGE, TileIDs.FISH, TileIDs.GREEN)), FishColour.GREEN);
		} else if (r <= 0.8) {
			fish = new FishCell(assets.tiles.getTile(TileIDs.computeID(TileIDs.POWERUP_RANGE, TileIDs.FISH, TileIDs.RED)), FishColour.RED);
		} else {
			fish = new FishCell(assets.tiles.getTile(TileIDs.computeID(TileIDs.POWERUP_RANGE, TileIDs.FISH, TileIDs.ORANGE)), FishColour.ORANGE);
		}

		int row = randomInt(gateSpace + 1, height - gateSpace - 1);
		layer.setCell(col, row, fish);
	}

	/**
	 * Place the given circuit on the given layer.
	 * It is placed at the top, facing down.
	 *
	 * @param layer the layer to place the circuit on.
	 * @param circuit the circuit to use.
	 * @param location the location of the gate being placed.
	 */
	private void placeUpperCircuit(TiledMapTileLayer layer, Circuit circuit, Point location) {
		Cell gate = new Cell();
		gate.setTile(assets.tiles.getTile(TileIDs.computeID(TileIDs.GATE_RANGE, Circuit.getID(circuit.getGate()), TileIDs.UNKNOWN, TileIDs.DOWN_GATE)));
		layer.setCell(location.x, location.y, gate);
		gateLocations.add(new Point(location));

		Cell inputAStart = new Cell();
		int inputAPowerID = circuit.isInputA() ? TileIDs.ON : TileIDs.OFF;
		inputAStart.setTile(assets.tiles.getTile(TileIDs.computeID(TileIDs.WIRE_RANGE, TileIDs.VERTICAL, inputAPowerID)));
		layer.setCell(location.x - 1, location.y + 1, inputAStart);
		Cell inputATurn = new Cell();
		inputATurn.setTile(assets.tiles.getTile(TileIDs.computeID(TileIDs.WIRE_RANGE, TileIDs.TURN, inputAPowerID, TileIDs.UP_RIGHT)));
		layer.setCell(location.x - 1, location.y, inputATurn);

		Cell inputBStart = new Cell();
		int inputBPowerID = circuit.isInputB() ? TileIDs.ON : TileIDs.OFF;
		inputBStart.setTile(assets.tiles.getTile(TileIDs.computeID(TileIDs.WIRE_RANGE, TileIDs.VERTICAL, inputBPowerID)));
		layer.setCell(location.x + 1, location.y + 1, inputBStart);
		Cell inputBTurn = new Cell();
		inputBTurn.setTile(assets.tiles.getTile(TileIDs.computeID(TileIDs.WIRE_RANGE, TileIDs.TURN, inputBPowerID, TileIDs.UP_LEFT)));
		layer.setCell(location.x + 1, location.y, inputBTurn);
	}

	/**
	 * Place the given circuit on the given layer.
	 * It is placed at the bottom, facing up.
	 *
	 * @param layer the layer to place the circuit on.
	 * @param circuit the circuit to use.
	 * @param location the location of the gate being placed.
	 */
	private void placeLowerCircuit(TiledMapTileLayer layer, Circuit circuit, Point location) {
		Cell gate = new Cell();
		gate.setTile(assets.tiles.getTile(TileIDs.computeID(TileIDs.GATE_RANGE, Circuit.getID(circuit.getGate()), TileIDs.UNKNOWN, TileIDs.UP_GATE)));
		layer.setCell(location.x, location.y, gate);
		gateLocations.add(new Point(location));

		Cell inputAStart = new Cell();
		int inputAPowerID = circuit.isInputA() ? TileIDs.ON : TileIDs.OFF;
		inputAStart.setTile(assets.tiles.getTile(TileIDs.computeID(TileIDs.WIRE_RANGE, TileIDs.VERTICAL, inputAPowerID)));
		layer.setCell(location.x - 1, location.y - 1, inputAStart);
		Cell inputATurn = new Cell();
		inputATurn.setTile(assets.tiles.getTile(TileIDs.computeID(TileIDs.WIRE_RANGE, TileIDs.TURN, inputAPowerID, TileIDs.DOWN_RIGHT)));
		layer.setCell(location.x - 1, location.y, inputATurn);

		Cell inputBStart = new Cell();
		int inputBPowerID = circuit.isInputB() ? TileIDs.ON : TileIDs.OFF;
		inputBStart.setTile(assets.tiles.getTile(TileIDs.computeID(TileIDs.WIRE_RANGE, TileIDs.VERTICAL, inputBPowerID)));
		layer.setCell(location.x + 1, location.y - 1, inputBStart);
		Cell inputBTurn = new Cell();
		inputBTurn.setTile(assets.tiles.getTile(TileIDs.computeID(TileIDs.WIRE_RANGE, TileIDs.TURN, inputBPowerID, TileIDs.DOWN_LEFT)));
		layer.setCell(location.x + 1, location.y, inputBTurn);
	}

	/**
	 * Generate the placement of wires.
	 *
	 * @return a boolean array of where wires are to be placed.
	 */
	private List<Integer> generateWireLocations() {
		final int size = width / WIRE_DISTANCE;
		List<Integer> wires = new ArrayList<Integer>(size);

		for (int i = 0; i < size; i++) {
			wires.add(START_DISTANCE + (i * WIRE_DISTANCE));
		}

		return wires;
	}

	/**
	 * Get the {@link Array} of gate locations.
	 *
	 * @return the gate locations.
	 */
	public Array<Point> getGateLocations() {
		return gateLocations;
	}

	/**
	 * Get the {@link ArrayList} of gates of the wires that are on.
	 *
	 * @return the gates of the wires that are on.
	 */
	public ArrayList<Circuit> getGateOn() {
		return gateOn;
	}

	/**
	 * Use {@link #random} to generate a random integer in the given range.
	 *
	 * @param low The lowest number that can be generated (inclusive).
	 * @param high The highest number that can be generated (exclusive).
	 * @return A random number in the given range, or {@code high} if {@code high <= low}. If {@code high <= low} then {@code high} is no longer exclusive.
	 */
	private int randomInt(int low, int high) {
		if (high <= low) {
			return high;
		}
		return low + random.nextInt(high - low);
	}
}

/**
 * Z64Scene.java
 * Class representing a Zelda 64 scene.
 * Manages the scene's ROM file, room files, and various offsets related to headers, collisions, and pathways.
 */

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Z64Scene implements Iterable<RomFile> {
    private final RomFile _sceneRomFile;
    private final ArrayList<RomFile> _roomRomFiles;
    private final Set<Integer> _sceneHeaderOffsetList;
    private final Set<Integer> _collisionHeaderOffsetList;
    private final Set<Integer> _pathwayOffsetList;

    /**
     * Constructor for Z64Scene.
     * Initializes the scene by extracting offsets for headers, collisions, and pathways.
     *
     * @param scene The ROM file representing the scene.
     * @throws RuntimeException if the scene file is malformed or invalid.
     */
    public Z64Scene(RomFile scene) {
        _sceneRomFile = scene;
        _roomRomFiles = new ArrayList<>();
        _sceneHeaderOffsetList = new HashSet<>();
        _collisionHeaderOffsetList = new HashSet<>();
        _pathwayOffsetList = new HashSet<>();

        getAlternateSceneHeaderOffsets();
        getCollisionHeaderOffsets();
        getPathwayOffsets();
        fixSharpOcarinaWaterboxPointers();
    }

    /**
     * Converts four consecutive bytes in an array, representing a segment address,
     * into an offset within the scene data array.
     *
     * @param arr         The byte array containing the scene data.
     * @param offsetInArr The offset within the byte array to start reading.
     * @return The offset of the segment address in the scene data array.
     */
    private int segAddrToOffset(byte[] arr, int offsetInArr) {
        return (((int) arr[offsetInArr + 1] & 0xFF) << 16) |
                (((int) arr[offsetInArr + 2] & 0xFF) << 8) |
                (((int) arr[offsetInArr + 3] & 0xFF));
    }

    /**
     * Finds the addresses of all the alternate scene headers in the scene ROM file.
     *
     * @throws RuntimeException if scene data is malformed or missing expected headers.
     */
    private void getAlternateSceneHeaderOffsets() {
        byte[] sceneData = _sceneRomFile.getData();
        int altHeaderListOffset = getHeaderCmdOffset(0, DecompEnums.Z64SceneCommand.ALTERNATE_HEADER_LIST.ordinal());

        // Add default header at start of file
        _sceneHeaderOffsetList.add(0);

        if (altHeaderListOffset > 0) {
            // Skip the blank headers at the start
            altHeaderListOffset += 0xC;

            // Add each alternate header in the list
            while (sceneData[altHeaderListOffset] == Globals.SCENE_SEGMENT_NUM) {
                _sceneHeaderOffsetList.add(segAddrToOffset(sceneData, altHeaderListOffset));
                altHeaderListOffset += 4;
            }
        }
    }

    /**
     * Gets the offset of a command within a scene header.
     *
     * @param headerBase The base offset within the header to start searching from.
     * @param cmd        The command type to search for within the header.
     * @return The offset of the command in the scene data array, or -1 if not found.
     */
    private int getHeaderCmdOffset(int headerBase, int cmd) {
        byte[] sceneData = _sceneRomFile.getData();

        if (sceneData != null) {
            while (headerBase < sceneData.length && sceneData[headerBase] != DecompEnums.Z64SceneCommand.END.ordinal()) {
                if (sceneData[headerBase] == cmd) {
                    return headerBase;
                }
                headerBase += 8;
            }
        }
        return -1;
    }

    /**
     * If there are no waterboxes in a map, SharpOcarina sets the segment address
     * of the waterbox data to be the same address as CamData.
     * This method fixes this by setting the pointer to NULL instead.
     *
     * @throws RuntimeException if the scene data is malformed.
     */
    private void fixSharpOcarinaWaterboxPointers() {
        byte[] sceneData = _sceneRomFile.getData();

        for (Integer colHeaderOffset : _collisionHeaderOffsetList) {
            int numWaterboxes = Globals.readIntFromByteArray(sceneData, colHeaderOffset + 0x24);
            if (numWaterboxes == 0) {
                // Set null pointer
                for (int i = 0; i < 4; i++) {
                    sceneData[colHeaderOffset + 0x28 + i] = 0;
                }
            }
        }
    }

    /**
     * Sets the list of collision headers for the scene, to be patched later.
     *
     * @throws RuntimeException if scene data is malformed or missing collision headers.
     */
    private void getCollisionHeaderOffsets() {
        byte[] sceneData = _sceneRomFile.getData();

        for (Integer header : _sceneHeaderOffsetList) {
            int collisionHeaderCmdOffset = getHeaderCmdOffset(header, DecompEnums.Z64SceneCommand.COLLISION_HEADER.ordinal());
            int collisionHeaderOffset = segAddrToOffset(sceneData, collisionHeaderCmdOffset + 4);

            // Add collision header to list
            _collisionHeaderOffsetList.add(collisionHeaderOffset);
        }
    }

    /**
     * Sets the list of pathways to be added to XML.
     *
     * @throws RuntimeException if scene data is malformed or missing pathway data.
     */
    private void getPathwayOffsets() {
        byte[] sceneData = _sceneRomFile.getData();

        for (Integer header : _sceneHeaderOffsetList) {
            int pathwayHeaderCmdOffset = getHeaderCmdOffset(header, DecompEnums.Z64SceneCommand.PATH_LIST.ordinal());
            int pathwayOffset = segAddrToOffset(sceneData, pathwayHeaderCmdOffset + 4);

            // Add pathway to list
            _pathwayOffsetList.add(pathwayOffset);
        }
    }

    /**
     * Adds a new room to the scene.
     *
     * @param room The ROM file representing the room to add.
     */
    public void addRoom(RomFile room) {
        _roomRomFiles.add(room);
    }

    /**
     * Gets the ROM file of a room in the scene by index.
     *
     * @param index The index of the room.
     * @return The ROM file for the room at the given index.
     * @throws IndexOutOfBoundsException if the index is out of range.
     */
    public RomFile getRoom(int index) {
        return _roomRomFiles.get(index);
    }

    /**
     * Gets the ROM file of the scene.
     *
     * @return The ROM file representing the scene.
     */
    public RomFile getScene() {
        return _sceneRomFile;
    }

    /**
     * Returns the number of rooms currently added to the scene.
     *
     * @return The number of rooms in the scene.
     */
    public int getNumRooms() {
        return _roomRomFiles.size();
    }

    /**
     * Returns the name of the scene without the "_scene" suffix.
     *
     * @return The name of the scene file without the "_scene" suffix.
     */
    public String getName() {
        return _sceneRomFile.getName().replace("_scene", "");
    }

    /**
     * Generates the XML string for the scene file.
     *
     * @param scene The scene's ROM file.
     * @param nodes Additional XML nodes to include in the string.
     * @return The XML string for the scene file.
     */
    private String sceneXmlString(RomFile scene, String nodes) {
        return "\t<File Name=\"" + scene.getName() + "\" Segment=\"2\">\n" + nodes +
                "\t\t<Scene Name=\"" + scene.getName() + "\" Offset=\"0x0\"/>\n" +
                "\t</File>\n";
    }

    /**
     * Generates the XML string for a room file.
     *
     * @param room  The room's ROM file.
     * @param nodes Additional XML nodes to include in the string.
     * @return The XML string for the room file.
     */
    private String roomXmlString(RomFile room, String nodes) {
        return "\t<File Name=\"" + room.getName() + "\" Segment=\"3\">\n" + nodes +
                "\t\t<Room Name=\"" + room.getName() + "\" Offset=\"0x0\"/>\n" +
                "\t</File>\n";
    }

    /**
     * Saves the XML representation of the scene and its rooms to a file.
     *
     * @param outPath The path where the XML file should be saved.
     * @throws RuntimeException if an error occurs during file writing.
     */
    public void saveXml(String outPath) {
        String name = getName();
        File outXmlFile = new File(outPath + "/" + name + ".xml");

        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                Files.newOutputStream(outXmlFile.toPath()), StandardCharsets.UTF_8))) {

            // Write the scene XML string, including any extra nodes like CamData
            writer.write("<Root>\n");
            writer.write(sceneXmlString(getScene(), ""));

            // Write the room XML string
            for (int i = 0; i < getNumRooms(); i++) {
                writer.write(roomXmlString(getRoom(i), ""));
            }
            writer.write("</Root>\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Iterator for iterating through the scene and room ROM files.
     */
    private class SceneIterator implements Iterator<RomFile> {
        // Index is -1 for scene file, 0 for room 0, 1 for room 1, etc.
        private int _index = -1;

        /**
         * Checks if there are more files to iterate through.
         *
         * @return True if there are more files, false otherwise.
         */
        @Override
        public boolean hasNext() {
            return (_index < _roomRomFiles.size());
        }

        /**
         * Returns the next ROM file (either the scene or a room).
         *
         * @return The next ROM file (scene or room).
         */
        @Override
        public RomFile next() {
            RomFile out;

            if (_index < 0) {
                out = _sceneRomFile;
            } else {
                out = _roomRomFiles.get(_index);
            }
            _index++;

            return out;
        }
    }

    /**
     * Provides an iterator for the scene and room ROM files.
     *
     * @return An iterator for the scene and room ROM files.
     */
    @Override
    public Iterator<RomFile> iterator() {
        return new SceneIterator();
    }
}

/**
 * Z64Scene.java
 * Class representing a Zelda 64 scene
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
     * Constructor
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
     * into an offset within the scene data array
     */
    private int segAddrToOffset(byte[] arr, int offsetInArr) {
        return (((int) arr[offsetInArr + 1] & 0xFF) << 16) |
                (((int) arr[offsetInArr + 2] & 0xFF) << 8) |
                (((int) arr[offsetInArr + 3] & 0xFF));
    }

    /**
     * Finds the addresses of all the scene headers in the scene
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
            while (sceneData[altHeaderListOffset] == 0x02) {
                _sceneHeaderOffsetList.add(segAddrToOffset(sceneData, altHeaderListOffset));
                altHeaderListOffset += 0x04;
            }
        }
    }

    /**
     * Gets offset of a command within a scene header
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
     * To fix this, we can just set the pointer to NULL.
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
     * Sets list of collision headers to patch later
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
     * Sets list of pathways to add to XML
     */
    private void getPathwayOffsets() {
        byte[] sceneData = _sceneRomFile.getData();

        for (Integer header : _sceneHeaderOffsetList) {
            int pathwayHeaderCmdOffset = getHeaderCmdOffset(header, DecompEnums.Z64SceneCommand.PATH_LIST.ordinal());
            int pathwayOffset = segAddrToOffset(sceneData, pathwayHeaderCmdOffset + 4);

            // Add collision header to list
            _pathwayOffsetList.add(pathwayOffset);
        }
    }

    /**
     * Adds a new room to the scene
     */
    public void addRoom(RomFile room) {
        _roomRomFiles.add(room);
    }

    /**
     * Gets the room's RomFile
     */
    public RomFile getRoom(int index) {
        return _roomRomFiles.get(index);
    }

    /**
     * Gets the scene's RomFile
     */
    public RomFile getScene() {
        return _sceneRomFile;
    }

    /**
     * Returns the number of rooms currently added to the scene
     */
    public int getNumRooms() {
        return _roomRomFiles.size();
    }

    /**
     * Returns the name of the scene without the "_scene" suffix
     */
    public String getName() {
        return _sceneRomFile.getName().replace("_scene", "");
    }

    /**
     * XML generation
     */
    private String sceneXmlString(RomFile scene, String nodes) {
        return "\t<File Name=\"" + scene.getName() + "\" Segment=\"2\">\n" + nodes +
                "\t\t<Scene Name=\"" + scene.getName() + "\" Offset=\"0x0\"/>\n" +
                "\t</File>\n";
    }

    private String roomXmlString(RomFile room, String nodes) {
        return "\t<File Name=\"" + room.getName() + "\" Segment=\"3\">\n" + nodes +
                "\t\t<Room Name=\"" + room.getName() + "\" Offset=\"0x0\"/>\n" +
                "\t</File>\n";
    }

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
     * RomFile iteration
     */

    // Iterator that gives all the scene `RomFile`s
    private class SceneIterator implements Iterator<RomFile> {
        private int _index;

        /**
         * Constructor
         */
        public SceneIterator() {
            // -1 represents the scene file, 0 represents room 0, 1 represents room 1, etc.
            _index = -1;
        }

        /**
         * Tells the iterator when it is finished
         */
        public boolean hasNext() {
            return (_index < _roomRomFiles.size());
        }

        /**
         * Returns the next file
         */
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

    // Public iterator
    public Iterator<RomFile> iterator() {
        return new SceneIterator();
    }
}

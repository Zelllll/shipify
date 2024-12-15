/**
 * Z64Scene.java
 * Class representing a Zelda 64 scene
 */

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

public class Z64Scene implements Iterable<RomFile> {
    private RomFile _sceneRomFile;
    private ArrayList<RomFile> _roomRomFiles;
    private ArrayList<CamData> _camDataList;
    private ArrayList<Integer> _sceneHeaderOffsetList;

    // constructor
    public Z64Scene(RomFile scene) {
        _sceneRomFile = scene;
        _roomRomFiles = new ArrayList<>();
        _camDataList = new ArrayList<>();
        _sceneHeaderOffsetList = new ArrayList<>();

        findHeaderOffsets();
        setCamDataList();
    }

    // converts four consecutive bytes in an array, representing a segment address,
    // into an offset within the scene data array
    private int segAddrToOffset(byte[] arr, int offsetInArr) {
        return (((int) arr[offsetInArr + 1] & 0xFF) << 16) |
                (((int) arr[offsetInArr + 2] & 0xFF) << 8) |
                (((int) arr[offsetInArr + 3] & 0xFF));
    }

    // finds the addresses of all the scene headers in the scene
    private void findHeaderOffsets() {
        byte[] sceneData = _sceneRomFile.getData();
        int altHeaderListOffset = getHeaderCmdOffset(0, 0x18);

        // add default header at start of file
        _sceneHeaderOffsetList.add(0);

        if (altHeaderListOffset > 0) {
            // skip the blank headers at the start
            altHeaderListOffset += 0xC;

            // add each alternate header in the list
            while (sceneData[altHeaderListOffset] == 0x02) {
                _sceneHeaderOffsetList.add(segAddrToOffset(sceneData, altHeaderListOffset));
                altHeaderListOffset += 0x04;
            }
        }
    }

    // gets offset of a command within a scene header
    private int getHeaderCmdOffset(int headerBase, int cmd) {
        byte[] sceneData = _sceneRomFile.getData();

        if (sceneData != null) {
            while (headerBase < sceneData.length && sceneData[headerBase] != 0x14) {
                if (sceneData[headerBase] == cmd) {
                    return headerBase;
                }
                headerBase += 8;
            }
        }
        return -1;
    }

    // sets the list of CamData elements
    // currently designed only for SharpOcarina maps
    private void setCamDataList() {
        byte[] sceneData = _sceneRomFile.getData();

        for (Integer header : _sceneHeaderOffsetList) {
            int collisionHeaderCmdOffset = getHeaderCmdOffset(header, 0x03);
            int camDataOffset = segAddrToOffset(sceneData, segAddrToOffset(sceneData, collisionHeaderCmdOffset + 4) + 0x20);
            int tempOffset = camDataOffset;
            int nCams = 0;

            // now that we have the address of the cam data, guess the length of it
            while (sceneData[tempOffset] == 0x00 && sceneData[tempOffset + 0x4] == 0x02) {
                tempOffset += 0x08;
                nCams++;
            }

            _camDataList.add(new CamData(camDataOffset, nCams));
        }
    }

    // adds a new room to the scene
    public void addRoom(RomFile room) {
        _roomRomFiles.add(room);
    }

    // gets the room's RomFile
    public RomFile getRoom(int index) {
        return _roomRomFiles.get(index);
    }

    // gets the scene's RomFile
    public RomFile getScene() {
        return _sceneRomFile;
    }

    // returns the number of rooms currently added to the scene
    public int getNumRooms() {
        return _roomRomFiles.size();
    }

    // returns the name of the scene without the "_scene" suffix
    public String getName() {
        return _sceneRomFile.getName().replace("_scene", "");
    }

    /**
     * Class representing a CamData element
     */
    private class CamData {
        int _nCamData;
        int _offset;

        public CamData(int offset, int nCamData) {
            _nCamData = nCamData;
            _offset = offset;
        }

        public int getOffset() {
            return _offset;
        }

        public int getNumCamData() {
            return _nCamData;
        }
    }

    /**
     * XML generation
     */
    private String sceneXmlString(RomFile scene, String nodes) {
        return "\t<File Name=\"" + scene.getName() + "\" Segment=\"2\">\n" +
                "\t\t<Scene Name=\"" + scene.getName() + "\" Offset=\"0x0\"/>\n" +
                nodes +
                "\t</File>\n";
    }

    private String roomXmlString(RomFile room) {
        return "\t<File Name=\"" + room.getName() + "\" Segment=\"3\">\n" +
                "\t\t<Room Name=\"" + room.getName() + "\" Offset=\"0x0\"/>\n" +
                "\t</File>\n";
    }

    private String camDataXmlString(int offset, int count, String name) {
        return "\t\t<CamData Name=\"" + name +
                "\" Offset=\"0x" + Integer.toHexString(offset) +
                "\" Count=\"" + count +
                "\"/>\n";
    }

    public void saveXml(String outPath) {
        String name = getName();
        File outXmlFile = new File(outPath + "/" + name + ".xml");

        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(outXmlFile), "utf-8"))) {
            // create string containing all the XML nodes for CamData
            String sceneNodes = "";

            for (Z64Scene.CamData camData : _camDataList) {
                sceneNodes += camDataXmlString(camData.getOffset(), camData.getNumCamData(),
                        getName() + "_camData_" + Integer.toHexString(camData.getOffset()));
            }

            // write the scene XML string, including any extra nodes like CamData
            writer.write("<Root>\n");
            writer.write(sceneXmlString(getScene(), sceneNodes));

            // write the room XML string
            for (int i = 0; i < getNumRooms(); i++) {
                writer.write(roomXmlString(getRoom(i)));
            }
            writer.write("</Root>\n");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * RomFile iteration
     */

    // iterator that gives all the scene `RomFile`s
    private class SceneIterator implements Iterator<RomFile> {
        private int _index;

        // constructor
        public SceneIterator() {
            // -1 represents the scene file, 0 represents room 0, 1 represents room 1, etc.
            _index = -1;
        }

        // tells the iterator when it is finished
        public boolean hasNext() {
            return (_index < _roomRomFiles.size());
        }

        // returns the next file
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

    // public iterator
    public Iterator<RomFile> iterator() {
        return new SceneIterator();
    }
}

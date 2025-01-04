/**
 * Z64Text.java
 * Class representing Zelda 64 object files
 */

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

public class Z64Object implements Iterable<RomFile> {
    private final ArrayList<RomFile> _objectRomFiles = new ArrayList<>();

    // constructor
    public Z64Object(File f) {
        String name = f.getName();
        byte[] objectData = Globals.fileToByteArr(f);
        // objectData = patchBranchLists(objectData, name);
        _objectRomFiles.add(new RomFile(objectData, name));
    }

    /**
     * temporary gsSPBranchList() patch
     */
    // finds all instances of a gsSPBranchList() command within an object
    // assumes object is in segment 6
    private ArrayList<Integer> findBranchLists(byte[] objectData) {
        ArrayList<Integer> branchListCmdOffsets = new ArrayList<>();

        for (int i = Globals.GFX_CMD_SIZE; i < objectData.length; i += Globals.GFX_CMD_SIZE) {
            // looks for any gsSPBranchList() commands referencing data in segment 6
            // this is not a perfect check by any means, but it will work for the time being
            if (((int) objectData[i] & 0xFF) == 0xDE &&
                    ((int) objectData[i + 1] & 0xFF) == 0x01 &&
                    ((int) objectData[i + 2] & 0xFF) == 0x00 &&
                    ((int) objectData[i + 3] & 0xFF) == 0x00 &&
                    ((int) objectData[i + 4] & 0xFF) == 0x06) {
                branchListCmdOffsets.add(i);
            }
        }
        return branchListCmdOffsets;
    }

    // patches out gsSPBranchList() commands
    private byte[] patchBranchLists(byte[] objectData, String name) {
        ArrayList<Integer> branchListCmdOffsets = findBranchLists(objectData);
        ArrayList<Byte> appendedData = new ArrayList<>();

        if (branchListCmdOffsets.isEmpty()) {
            return objectData;
        }

        // modify displaylists to remove gsSPBranchList() command
        for (int offset : branchListCmdOffsets) {
            // avoid going out of range
            if (offset < Globals.GFX_CMD_SIZE || offset > objectData.length - Globals.GFX_CMD_SIZE) {
                continue;
            }

            // copying existing branchList command, and whatever command precedes it
            byte[] oldCmd = new byte[Globals.GFX_CMD_SIZE * 3];
            System.arraycopy(objectData, offset - Globals.GFX_CMD_SIZE, oldCmd, 0, Globals.GFX_CMD_SIZE * 2);

            // get offset in the appended section of the object that we will write the old commands to
            int appendOffset = appendedData.size();
            int realOffset = objectData.length + appendOffset;

            // write gsSPDisplayList() at ((Gfx)objectData)[offset - 1]
            objectData[offset - Globals.GFX_CMD_SIZE + 0] = (byte) ((int) 0xDE & 0xFF);
            objectData[offset - Globals.GFX_CMD_SIZE + 1] = (byte) ((int) 0x00 & 0xFF);
            objectData[offset - Globals.GFX_CMD_SIZE + 2] = (byte) ((int) 0x00 & 0xFF);
            objectData[offset - Globals.GFX_CMD_SIZE + 3] = (byte) ((int) 0x00 & 0xFF);

            // write segment address of fixed data
            objectData[offset - Globals.GFX_CMD_SIZE + 4] = (byte) ((int) 0x06 & 0xFF);
            objectData[offset - Globals.GFX_CMD_SIZE + 5] = (byte) ((int) ((realOffset >> 16) & 0xFF) & 0xFF);
            objectData[offset - Globals.GFX_CMD_SIZE + 6] = (byte) ((int) ((realOffset >> 8) & 0xFF) & 0xFF);
            objectData[offset - Globals.GFX_CMD_SIZE + 7] = (byte) ((int) (realOffset & 0xFF) & 0xFF);

            // write gsSPEndDisplayList() at ((Gfx)objectData)[offset]
            objectData[offset + 0] = (byte) ((int) 0xDF & 0xFF);
            objectData[offset + 1] = (byte) ((int) 0x00 & 0xFF);
            objectData[offset + 2] = (byte) ((int) 0x00 & 0xFF);
            objectData[offset + 3] = (byte) ((int) 0x00 & 0xFF);
            objectData[offset + 4] = (byte) ((int) 0x00 & 0xFF);
            objectData[offset + 5] = (byte) ((int) 0x00 & 0xFF);
            objectData[offset + 6] = (byte) ((int) 0x00 & 0xFF);
            objectData[offset + 7] = (byte) ((int) 0x00 & 0xFF);

            // modify gsSPBranchList() to be gsSPDisplayList() instead
            oldCmd[Globals.GFX_CMD_SIZE + 1] = 0x00;

            // write gsSPEndDiplayList() command
            oldCmd[(Globals.GFX_CMD_SIZE * 2) + 0] = (byte) ((int) 0xDF & 0xFF);
            oldCmd[(Globals.GFX_CMD_SIZE * 2) + 1] = (byte) ((int) 0x00 & 0xFF);
            oldCmd[(Globals.GFX_CMD_SIZE * 2) + 2] = (byte) ((int) 0x00 & 0xFF);
            oldCmd[(Globals.GFX_CMD_SIZE * 2) + 3] = (byte) ((int) 0x00 & 0xFF);
            oldCmd[(Globals.GFX_CMD_SIZE * 2) + 4] = (byte) ((int) 0x00 & 0xFF);
            oldCmd[(Globals.GFX_CMD_SIZE * 2) + 5] = (byte) ((int) 0x00 & 0xFF);
            oldCmd[(Globals.GFX_CMD_SIZE * 2) + 6] = (byte) ((int) 0x00 & 0xFF);
            oldCmd[(Globals.GFX_CMD_SIZE * 2) + 7] = (byte) ((int) 0x00 & 0xFF);

            // write old commands to appended section
            for (byte b : oldCmd) {
                appendedData.add(b);
            }

            // debug info
            System.out.println("    " + name + " : Patched gsSPBranchList at 0x" + Integer.toHexString(offset) +
                    ", new DL at 0x" + Integer.toHexString(realOffset));
        }

        // generate new array containing all the object data
        byte[] patchedObjectData = new byte[objectData.length + appendedData.size()];
        System.arraycopy(objectData, 0, patchedObjectData, 0, objectData.length);
        int offset = objectData.length;
        for (byte b : appendedData) {
            patchedObjectData[offset] = b;
            offset++;
        }

        // return newly generated object
        return patchedObjectData;
    }

    /**
     * RomFile iteration
     */
    public Iterator<RomFile> iterator() {
        return _objectRomFiles.iterator();
    }
}

/**
 * RomWriter.java
 * Class representing a full SoH patch ROM
 */

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;

public class RomWriter {
    private ArrayList<RomFile> _romFiles;

    // constructor
    public RomWriter() {
        _romFiles = new ArrayList<RomFile>();
    }

    public void add(RomFile romFile) {
        _romFiles.add(romFile);
    }

    private int getDmaTableLength() {
        return 16 * (_romFiles.size() + 1);
    }

    private int setOffsets() {
        int curOffset = Globals.ROM_BASE + getDmaTableLength();

        for (RomFile romFile : _romFiles) {
            romFile.setOffset(curOffset);
            curOffset += romFile.getSize();
        }
        return curOffset;
    }

    private void writeDmaTable(byte[] out) {
        int offset = Globals.ROM_BASE;

        for (RomFile file : _romFiles) {
            int fileStart = file.getOffset();
            int fileEnd = fileStart + file.getSize();

            // write start address of file
            out[offset + 0] = out[offset + 0 + 8] = (byte) ((fileStart >> 24) & 0xFF);
            out[offset + 1] = out[offset + 1 + 8] = (byte) ((fileStart >> 16) & 0xFF);
            out[offset + 2] = out[offset + 2 + 8] = (byte) ((fileStart >> 8) & 0xFF);
            out[offset + 3] = out[offset + 3 + 8] = (byte) (fileStart & 0xFF);

            // write end address of file
            out[offset + 4 + 0] = (byte) ((fileEnd >> 24) & 0xFF);
            out[offset + 4 + 1] = (byte) ((fileEnd >> 16) & 0xFF);
            out[offset + 4 + 2] = (byte) ((fileEnd >> 8) & 0xFF);
            out[offset + 4 + 3] = (byte) (fileEnd & 0xFF);

            // zero the last four bytes of the entry
            for (int j = 0; j < 4; j++) {
                out[offset + 12 + j] = 0;
            }

            offset += 16;
        }
    }

    private void writeFileToRomArray(byte[] romOut, RomFile romFile) {
        System.arraycopy(romFile.getData(), 0, romOut, romFile.getOffset(), romFile.getSize());
    }

    // saves patch ROM to disk
    public void saveRom(String outPath) {
        // list of files in order of their location in the output rom
        ArrayList<String> romFileNameList = new ArrayList<>();

        // set the offsets within the files
        int romSize = setOffsets();

        // allocate rom
        byte[] outRomData = new byte[romSize];

        // fill first 0x20 bytes of rom with zeros
        for (int i = 0; i < Globals.ROM_BASE; i++) {
            outRomData[i] = 0;
        }

        // make rom file
        File outRomFile = new File(outPath + "/" + Globals.ROM_OUT_NAME);

        // make rom file list file
        File outRomFileListFile = new File(outPath + "/" + Globals.ROM_FILE_LIST_OUT_NAME);

        // write dma table to the start of the rom
        writeDmaTable(outRomData);

        // add each file to the output rom
        for (RomFile romFile : _romFiles) {
            writeFileToRomArray(outRomData, romFile);
            romFileNameList.add(romFile.getName());
        }

        // output rom
        try (FileOutputStream outputStream = new FileOutputStream(outRomFile)) {
            outputStream.write(outRomData);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // output file list
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                Files.newOutputStream(outRomFileListFile.toPath()), StandardCharsets.UTF_8))) {
            for (String romFileName : romFileNameList) {
                writer.write(romFileName + "\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

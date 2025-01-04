/**
 * RomWriter.java
 * Class representing a full SoH patch ROM.
 * Handles adding RomFiles, managing offsets, and saving the patch ROM to disk.
 */

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;

public class RomWriter {
    private final ArrayList<RomFile> romFiles;

    /**
     * Constructor for RomWriter.
     * Initializes an empty list to store RomFiles.
     */
    public RomWriter() {
        romFiles = new ArrayList<RomFile>();
    }

    /**
     * Adds a RomFile to the writer's internal list.
     *
     * @param romFile The RomFile to add.
     */
    public void add(RomFile romFile) {
        romFiles.add(romFile);
    }

    /**
     * Calculates the total length of the DMA table.
     * Each entry in the DMA table is 16 bytes, including a final padding entry.
     *
     * @return The total length of the DMA table in bytes.
     */
    private int getDmaTableLength() {
        return 16 * (romFiles.size() + 1);
    }

    /**
     * Sets offsets for all RomFiles in the ROM and returns the total ROM size.
     * The offsets are calculated sequentially, starting after the DMA table.
     *
     * @return The size of the ROM in bytes after setting all file offsets.
     */
    private int setOffsets() {
        int curOffset = Globals.ROM_BASE + getDmaTableLength();

        for (RomFile romFile : romFiles) {
            romFile.setOffset(curOffset);
            curOffset += romFile.getSize();
        }
        return curOffset;
    }

    /**
     * Writes the DMA table to the start of the ROM data array.
     * The DMA table contains file start and end offsets for each RomFile.
     *
     * @param out The byte array representing the ROM data to be written.
     */
    private void writeDmaTable(byte[] out) {
        int offset = Globals.ROM_BASE;

        for (RomFile file : romFiles) {
            int fileStart = file.getOffset();
            int fileEnd = fileStart + file.getSize();

            // Write start address of the file
            out[offset + 0] = out[offset + 0 + 8] = (byte) ((fileStart >> 24) & 0xFF);
            out[offset + 1] = out[offset + 1 + 8] = (byte) ((fileStart >> 16) & 0xFF);
            out[offset + 2] = out[offset + 2 + 8] = (byte) ((fileStart >> 8) & 0xFF);
            out[offset + 3] = out[offset + 3 + 8] = (byte) (fileStart & 0xFF);

            // Write end address of the file
            out[offset + 4 + 0] = (byte) ((fileEnd >> 24) & 0xFF);
            out[offset + 4 + 1] = (byte) ((fileEnd >> 16) & 0xFF);
            out[offset + 4 + 2] = (byte) ((fileEnd >> 8) & 0xFF);
            out[offset + 4 + 3] = (byte) (fileEnd & 0xFF);

            // Zero the last four bytes of the entry
            for (int j = 0; j < 4; j++) {
                out[offset + 12 + j] = 0;
            }

            offset += 16;
        }
    }

    /**
     * Copies the data of a RomFile into the ROM's data array at its specified offset.
     *
     * @param romOut  The byte array representing the ROM data to be written to.
     * @param romFile The RomFile to be written into the ROM data.
     */
    private void writeFileToRomArray(byte[] romOut, RomFile romFile) {
        System.arraycopy(romFile.getData(), 0, romOut, romFile.getOffset(), romFile.getSize());
    }

    /**
     * Saves the constructed ROM and its file list to the specified output path.
     * Writes the ROM binary file and a corresponding file list for reference.
     *
     * @param outPath The directory path where the ROM and file list will be saved.
     */
    public void saveRom(String outPath) {
        // List of files in order of their location in the output ROM
        ArrayList<String> romFileNameList = new ArrayList<>();

        // Set the offsets within the files
        int romSize = setOffsets();

        // Allocate ROM data array
        byte[] outRomData = new byte[romSize];

        // Fill the first 0x20 bytes of the ROM with zeros
        for (int i = 0; i < Globals.ROM_BASE; i++) {
            outRomData[i] = 0;
        }

        // Create the ROM output file
        File outRomFile = new File(outPath + "/" + Globals.ROM_OUT_NAME);

        // Create the ROM file list output file
        File outRomFileListFile = new File(outPath + "/" + Globals.ROM_FILE_LIST_OUT_NAME);

        // Write the DMA table to the start of the ROM
        writeDmaTable(outRomData);

        // Add each file to the ROM output data array
        for (RomFile romFile : romFiles) {
            writeFileToRomArray(outRomData, romFile);
            romFileNameList.add(romFile.getName());
        }

        // Output the ROM binary file
        try (FileOutputStream outputStream = new FileOutputStream(outRomFile)) {
            outputStream.write(outRomData);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Output the ROM file list text file
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

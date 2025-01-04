/**
 * Z64Code.java
 * Class representing a Zelda 64 code file.
 * Manages the addition of code tables, their offsets, and generation of a ROM file.
 */

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;

public class Z64Code implements Iterable<RomFile> {
    private static class CodeVariable {
        private final byte[] tableData;
        private final String tableName;
        private int offset = 0;

        /**
         * Constructor for CodeVariable.
         * Represents a single table or data block within the code.
         *
         * @param data Byte array representing the table data.
         * @param name Name of the table or data block.
         */
        public CodeVariable(byte[] data, String name) {
            tableData = data;
            tableName = name;
        }

        /**
         * Gets the name of the table.
         *
         * @return The name of the table as a string.
         */
        public String getName() {
            return tableName;
        }

        /**
         * Gets the data of the table.
         *
         * @return The data of the table as a byte array.
         */
        public byte[] getData() {
            return tableData;
        }

        /**
         * Sets the offset of the table within the ROM file.
         *
         * @param offset The offset to set.
         */
        public void setOffset(int offset) {
            this.offset = offset;
        }

        /**
         * Gets the offset of the table within the ROM file.
         *
         * @return The offset as an integer.
         */
        public int getOffset() {
            return offset;
        }
    }

    private final ArrayList<CodeVariable> dataVariables = new ArrayList<>();
    private RomFile romFile = null;

    /**
     * Adds a new data table to the code.
     *
     * @param data Byte array representing the table data.
     * @param name Name of the table.
     */
    public void addArray(byte[] data, String name) {
        dataVariables.add(new CodeVariable(data, name));
    }

    /**
     * Checks if the code contains a table with the specified name.
     *
     * @param variableName The name of the table to search for.
     * @return True if the table exists, otherwise false.
     */
    public boolean contains(String variableName) {
        for (CodeVariable var : dataVariables) {
            if (var.getName().equals(variableName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Calculates the expected size of the ROM file based on the data tables.
     *
     * @return The total size of the ROM file in bytes.
     */
    private int getExpectedFileSize() {
        int size = 0;

        for (CodeVariable var : dataVariables) {
            size += var.getData().length;
        }

        return size;
    }

    /**
     * Generates the ROM file for code and sets the offsets for each data table.
     *
     * @return The generated ROM file as a `RomFile` object.
     */
    private RomFile genRomFile() {
        int fileSize = getExpectedFileSize();

        // Handle case where no tables are added
        if (fileSize == 0) {
            byte[] emptyArr = new byte[16];
            return new RomFile(emptyArr, Globals.CODE_NAME);
        }

        // Create raw data for the file
        byte[] rawData = new byte[fileSize];
        int offset = 0;

        // Add all tables and set their offsets
        for (CodeVariable var : dataVariables) {
            byte[] tableData = var.getData();
            int tableLength = tableData.length;

            var.setOffset(offset);
            System.arraycopy(tableData, 0, rawData, offset, tableLength);
            offset += tableLength;
        }

        // Generate the ROM file
        romFile = new RomFile(rawData, Globals.CODE_NAME);
        return romFile;
    }

    /**
     * Writes the offsets of the data tables to an output file.
     *
     * @param outputPath The path to the directory where the offset file will be saved.
     * @throws RuntimeException if an I/O error occurs during file writing.
     */
    public void writeDataOffsets(String outputPath) {
        if (genRomFile() == null) {
            return; // No data to write offsets for
        }

        File outFile = new File(outputPath + "/" + Globals.CODE_VARIABLE_OFFSET_LIST_OUT_NAME);

        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                Files.newOutputStream(outFile.toPath()), StandardCharsets.UTF_8))) {
            for (CodeVariable table : dataVariables) {
                writer.write(table.getName() + " : [code + 0x" +
                        Integer.toHexString(table.getOffset()) + "]\n");
            }
            if (romFile != null) {
                writer.write("\nend" + " : [code + 0x" +
                        Integer.toHexString(getExpectedFileSize()) + "]\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Iterator for ROM files.
     * Provides iteration over the single ROM file generated by this class.
     */
    private class CodeIterator implements Iterator<RomFile> {
        private boolean _done = false;

        /**
         * Checks if there are more elements to iterate.
         *
         * @return True if the iterator has more elements, otherwise false.
         */
        @Override
        public boolean hasNext() {
            return !_done;
        }

        /**
         * Retrieves the next element in the iteration.
         *
         * @return The next `RomFile` object.
         */
        @Override
        public RomFile next() {
            _done = true;
            return genRomFile();
        }
    }

    /**
     * Provides a public iterator for the ROM files.
     *
     * @return An iterator for the ROM files in this class.
     */
    @Override
    public Iterator<RomFile> iterator() {
        return new CodeIterator();
    }
}

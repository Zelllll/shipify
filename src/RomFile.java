/**
 * RomFile.java
 * Class representing a file within a ROM
 */

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class RomFile {
    private final String name;
    private final byte[] fileData;
    private int offset = 0;

    /**
     * Constructor for creating a RomFile from raw byte data.
     * Use this when you already have the file contents in memory.
     *
     * @param rawFile The byte array containing the file data.
     * @param name    The name to associate with this RomFile.
     */
    public RomFile(byte[] rawFile, String name) {
        // set the file name
        this.name = name;

        // set pointer to file byte data
        fileData = rawFile;
    }

    /**
     * Constructor for creating a RomFile from a physical file on disk.
     * Use this when you need to load the file's contents into memory.
     *
     * @param file The File object representing the file on disk.
     * @throws IllegalArgumentException If the file is null, not a valid file, or unreadable.
     * @throws RuntimeException         If an error occurs while reading the file data.
     */
    public RomFile(File file) {
        // Validate file input
        if (file == null || !file.isFile() || !file.canRead()) {
            throw new IllegalArgumentException("Invalid or unreadable file: " + file);
        }

        // Set file name
        name = file.getName();

        // Read file data
        try {
            fileData = Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: " + file.getName(), e);
        }
    }


    /**
     * Retrieves the data of the RomFile that is currently stored in memory.
     *
     * @return A byte array representing the contents of the RomFile.
     */
    public byte[] getData() {
        return fileData;
    }

    /**
     * Retrieves the name of the RomFile.
     *
     * @return A string representing the name of the RomFile, typically based on
     * the file name or the provided name during construction.
     */
    public String getName() {
        return name;
    }

    /**
     * Retrieves the current offset associated with this RomFile.
     *
     * @return An integer representing the offset, which may be used for addressing
     * or other context-specific purposes.
     */
    public int getOffset() {
        return offset;
    }

    /**
     * Updates the offset value associated with this RomFile.
     *
     * @param offset The new offset value to be set.
     */
    public void setOffset(int offset) {
        this.offset = offset;
    }

    /**
     * Retrieves the size of the RomFile in bytes.
     *
     * @return An integer representing the total size of the RomFile data.
     */
    public int getSize() {
        return fileData.length;
    }
}

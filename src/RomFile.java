/**
 * RomFile.java
 * Class representing a file within a ROM
 */

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class RomFile {
    private final String _name;
    private final byte[] _fileData;
    private int _offset;

    public RomFile(byte[] rawFile, String name) {
        // set default offset
        _offset = 0;

        // set the file name
        _name = name;

        // set pointer to file byte data
        _fileData = rawFile;
    }

    public RomFile(File file) {
        // set default offset
        _offset = 0;

        // set file name
        _name = file.getName();

        // allocate space for file data
        _fileData = new byte[(int) file.length()];

        // read the file
        try (DataInputStream stream = new DataInputStream(new FileInputStream(file))) {
            // read the file into ram
            stream.readFully(_fileData);
        } catch (IOException e) {
            // catch exception if the file isn't readable
            e.printStackTrace();
        }
    }

    // returns the data of the rom file in ram
    public byte[] getData() {
        return _fileData;
    }

    // returns the name of the rom file
    public String getName() {
        return _name;
    }

    public int getOffset() {
        return _offset;
    }

    public void setOffset(int offset) {
        _offset = offset;
    }

    public int getSize() {
        return _fileData.length;
    }
}

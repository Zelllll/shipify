/**
 * Z64Object.java
 * Class representing Zelda 64 object files.
 * Handles the creation and management of ROM files for Zelda 64 objects.
 */

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

public class Z64Object implements Iterable<RomFile> {
    private final ArrayList<RomFile> _objectRomFiles = new ArrayList<>();

    /**
     * Constructor for Z64Object.
     * Reads an object file and creates a corresponding ROM file.
     *
     * @param f The object file to be loaded.
     * @throws RuntimeException if the file cannot be read or converted to a byte array.
     */
    public Z64Object(File f) {
        String name = f.getName();
        byte[] objectData = Globals.fileToByteArr(f);
        _objectRomFiles.add(new RomFile(objectData, name));
    }

    /**
     * Provides an iterator for the ROM files.
     *
     * @return An iterator over the `RomFile` objects in this class.
     */
    @Override
    public Iterator<RomFile> iterator() {
        return _objectRomFiles.iterator();
    }
}

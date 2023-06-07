/**
 * Z64Audio.java
 * Class representing Zelda 64 audio files
 */

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

public class Z64Audio implements Iterable<RomFile>
{
    private static ArrayList<RomFile> _audioRomFiles;

    // constructor
    public Z64Audio(ArrayList<File> audioFiles, Z64Code code)
    {
        _audioRomFiles = new ArrayList<>();

        // load all audio code files
        for (File f : audioFiles)
        {
            String fileName = f.getName();

            // audio tables
            for (String tableName : Globals.AUDIO_TABLE_NAMES)
            {
                if (fileName.equals(tableName)) {
                    code.addArray(Globals.fileToByteArr(f), tableName);
                }
            }
            // audio binaries
            for (String binName : Globals.AUDIO_BIN_NAMES)
            {
                if (fileName.equals(binName)) {
                    _audioRomFiles.add(new RomFile(f));
                }
            }
        }

        // check if the user is missing an audio file
        if (!allFilesLoaded(code))
        {
            throw new RuntimeException("One or more audio files missing! Check documentation.");
        }
    }

    // verifies that all audio files were loaded successfully
    private boolean allFilesLoaded(Z64Code code)
    {
        // check if all audio binaries are loaded
        if (_audioRomFiles.size() != Globals.AUDIO_BIN_NAMES.length)
        {
            return false;
        }

        // check if all audio tables are loaded
        for (String name : Globals.AUDIO_TABLE_NAMES)
        {
            if (!code.contains(name))
            {
                return false;
            }
        }

        return true;
    }

    /**
     * RomFile iterator
     */

    // public iterator
    public Iterator<RomFile> iterator()
    {
        return _audioRomFiles.iterator();
    }
}

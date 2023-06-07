/**
 * Z64Text.java
 * Class representing Zelda 64 text files
 */

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

public class Z64Text implements Iterable<RomFile>
{
    private ArrayList<RomFile> _textRomFiles;

    public Z64Text(ArrayList<File> textFiles, Z64Code code)
    {
        _textRomFiles = new ArrayList<>();

        // load all text files
        for (File f : textFiles)
        {
            String fileName = f.getName();

            // text tables
            for (String tableName : Globals.TEXT_TABLE_NAMES)
            {
                if (fileName.equals(tableName))
                {
                    code.addArray(Globals.fileToByteArr(f), tableName);
                }
            }
            // text binaries
            for (String binName : Globals.TEXT_BIN_NAMES)
            {
                if (fileName.equals(binName))
                {
                    _textRomFiles.add(new RomFile(f));
                }
            }

            // check if the user is missing a text file
            if (!allFilesLoaded(code))
            {
                throw new RuntimeException("One or more audio files missing! Check documentation.");
            }
        }
    }

    // verifies that all text files were loaded successfully
    private boolean allFilesLoaded(Z64Code code)
    {
        // check if all text binaries are loaded
        if (_textRomFiles.size() != Globals.TEXT_BIN_NAMES.length)
        {
            return false;
        }

        // check if all text tables are loaded
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
        return _textRomFiles.iterator();
    }
}

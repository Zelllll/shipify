/**
 * Z64Text.java
 * Class representing Zelda 64 text files
 */

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * PAL Debug ROM text table locations
 *
 * base = [code + 0x12E4C0]
 *
 * nes at   [base + 0x0]
 * ger at   [base + 0x4228]
 * fra at   [base + 0x6338]
 * staff at [base + 0x8448]
 *
 * end = [base + 0x85D0]
 */

public class Z64Text implements Iterable<RomFile>
{
    private ArrayList<RomFile> _textRomFiles;

    public Z64Text(ArrayList<File> textFiles, Z64Code code)
    {
        _textRomFiles = new ArrayList<>();

        // text tables
        for (String tableName : Globals.TEXT_TABLE_NAMES)
        {
            for (File f : textFiles)
            {
                if (f.getName().equals(tableName))
                {
                    code.addArray(Globals.fileToByteArr(f), tableName);
                }
            }

        }

        // text binaries
        for (String binName : Globals.TEXT_BIN_NAMES)
        {
            for (File f : textFiles)
            {
                if (f.getName().equals(binName))
                {
                    _textRomFiles.add(new RomFile(f));
                }
            }
        }
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

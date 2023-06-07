/**
 * Z64Audio.java
 * Class representing Zelda 64 audio files
 */

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

public class Z64Audio implements Iterable<RomFile>
{
    private final String[] AUDIO_TABLE_NAMES = {
            Globals.CODE_TABLE_SAMPLE_BANK_NAME,
            Globals.CODE_TABLE_SEQUENCE_FONT_NAME,
            Globals.CODE_TABLE_SEQUENCE_NAME,
            Globals.CODE_TABLE_SOUND_FONT_NAME,
    };

    private static RomFile _audioBankRomFile, _audioTableRomFile, _audioSeqRomFile;

    // constructor
    public Z64Audio(ArrayList<File> audioFiles, Z64Code code)
    {
        // load all audio code tables into ram
        for (File f : audioFiles)
        {
            // audio code tables
            if (f.getName().equals(Globals.CODE_TABLE_SAMPLE_BANK_NAME))
            {
                code.addArray(Globals.fileToByteArr(f), Globals.CODE_TABLE_SAMPLE_BANK_NAME);
            }
            else if (f.getName().equals(Globals.CODE_TABLE_SEQUENCE_FONT_NAME))
            {
                code.addArray(Globals.fileToByteArr(f), Globals.CODE_TABLE_SEQUENCE_FONT_NAME);
            }
            else if (f.getName().equals(Globals.CODE_TABLE_SEQUENCE_NAME))
            {
                code.addArray(Globals.fileToByteArr(f), Globals.CODE_TABLE_SEQUENCE_NAME);
            }
            else if (f.getName().equals(Globals.CODE_TABLE_SOUND_FONT_NAME))
            {
                code.addArray(Globals.fileToByteArr(f), Globals.CODE_TABLE_SOUND_FONT_NAME);
            }
            // audio binaries
            else if (f.getName().equals(Globals.AUDIOBANK_NAME))
            {
                _audioBankRomFile = new RomFile(f);
            }
            else if (f.getName().equals(Globals.AUDIOTABLE_NAME))
            {
                _audioTableRomFile = new RomFile(f);
            }
            else if (f.getName().equals(Globals.AUDIOSEQ_NAME))
            {
                _audioSeqRomFile = new RomFile(f);
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
        RomFile[] audioBinReferences = {
                _audioBankRomFile,
                _audioTableRomFile,
                _audioSeqRomFile,
        };

        for (String name : AUDIO_TABLE_NAMES)
        {
            if (!code.contains(name))
            {
                return false;
            }
        }
        for (RomFile ar : audioBinReferences)
        {
            if (ar == null)
            {
                return false;
            }
        }
        return true;
    }

    /**
     * RomFile iterator
     */

    // iterator that gives all the audio `RomFile`s
    private class AudioIterator implements Iterator<RomFile>
    {
        private int _index;
        private RomFile[] _files = {
                _audioBankRomFile,
                _audioSeqRomFile,
                _audioTableRomFile,
        };

        // constructor
        public AudioIterator()
        {
            _index = 0;
        }

        // tells the iterator when it is finished
        public boolean hasNext()
        {
            return (_index < _files.length);
        }

        // returns the next file
        public RomFile next()
        {
            RomFile out = _files[_index];
            _index++;
            return out;
        }
    }

    // public iterator
    public Iterator<RomFile> iterator()
    {
        return new AudioIterator();
    }
}

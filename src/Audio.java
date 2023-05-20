import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

public class Audio implements Iterable<RomFile>
{
    private static byte[] _codeTableSampleBank, _codeTableSequenceFont, _codeTableSequence, _codeTableSoundFont;
    private static int _codeTableSampleBankOff, _codeTableSequenceFontOff, _codeTableSequenceOff, _codeTableSoundFontOff;
    private static RomFile _codeRomFile, _audioBankRomFile, _audioTableRomFile, _audioSeqRomFile;

    // constructor
    public Audio(ArrayList<File> audioFiles)
    {
        // load all audio code tables into ram
        for (File f : audioFiles)
        {
            if (f.getName().equals(Globals.CODE_TABLE_SAMPLE_BANK_NAME))
            {
                _codeTableSampleBank = Globals.loadFileByteArr(f);
            }
            else if (f.getName().equals(Globals.CODE_TABLE_SEQUENCE_FONT_NAME))
            {
                _codeTableSequenceFont = Globals.loadFileByteArr(f);
            }
            else if (f.getName().equals(Globals.CODE_TABLE_SEQUENCE_NAME))
            {
                _codeTableSequence = Globals.loadFileByteArr(f);
            }
            else if (f.getName().equals(Globals.CODE_TABLE_SOUND_FONT_NAME))
            {
                _codeTableSoundFont = Globals.loadFileByteArr(f);
            }
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
        if (!allFilesLoaded())
        {
            throw new RuntimeException("One or more audio files missing! Check documentation.");
        }

        // generate code file containing the audio tables
        _codeRomFile = genCodeFile();
    }

    // verifies that all audio files were loaded successfully
    private boolean allFilesLoaded()
    {
        byte[][] audioTableReferences = {
                _codeTableSampleBank,
                _codeTableSequenceFont,
                _codeTableSequence,
                _codeTableSoundFont,
        };
        RomFile[] audioBinReferences = {
                _audioBankRomFile,
                _audioTableRomFile,
                _audioSeqRomFile,
        };
        for (byte[] ar : audioTableReferences)
        {
            if (ar == null)
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

    // generates the `code` file containing the audio tables
    private RomFile genCodeFile()
    {
        int size = _codeTableSampleBank.length + _codeTableSequenceFont.length +
                _codeTableSequence.length + _codeTableSoundFont.length;
        byte[] codeFile = new byte[size];
        int offset = 0;

        // write sample bank table
        _codeTableSampleBankOff = offset;
        System.arraycopy(_codeTableSampleBank, 0, codeFile, offset, _codeTableSampleBank.length);
        offset += _codeTableSampleBank.length;

        // write sequence font table
        _codeTableSequenceFontOff = offset;
        System.arraycopy(_codeTableSequenceFont, 0, codeFile, offset, _codeTableSequenceFont.length);
        offset += _codeTableSequenceFont.length;

        // write sequence table
        _codeTableSequenceOff = offset;
        System.arraycopy(_codeTableSequence, 0, codeFile, offset, _codeTableSequence.length);
        offset += _codeTableSequence.length;

        // write sound font table
        _codeTableSoundFontOff = offset;
        System.arraycopy(_codeTableSoundFont, 0, codeFile, offset, _codeTableSoundFont.length);
        // offset += _codeTableSoundFont.length;

        return new RomFile(codeFile, "code");
    }

    // writes a text file with the offsets of the audio tables within `code`
    public void writeAudioOffsets(String outputPath)
    {
        File outFile = new File(outputPath + "\\" + Globals.AUDIO_OFFSET_OUT_NAME);

        // output file list
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(outFile), "utf-8")))
        {
            // write sample bank table offset
            writer.write(Globals.CODE_TABLE_SAMPLE_BANK_NAME + " : [code + 0x" +
                    Integer.toHexString(_codeTableSampleBankOff) + "\n");

            // write sequence font table offset
            writer.write(Globals.CODE_TABLE_SEQUENCE_FONT_NAME + " : [code + 0x" +
                    Integer.toHexString(_codeTableSequenceFontOff) + "\n");

            // write sample bank table offset
            writer.write(Globals.CODE_TABLE_SEQUENCE_NAME + " : [code + 0x" +
                    Integer.toHexString(_codeTableSequenceOff) + "\n");

            // write sample bank table offset
            writer.write(Globals.CODE_TABLE_SOUND_FONT_NAME + " : [code + 0x" +
                    Integer.toHexString(_codeTableSoundFontOff) + "\n");
        } catch (FileNotFoundException e)
        {
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    // iterator that gives all the audio `RomFile`s
    private class AudioIterator implements Iterator<RomFile>
    {
        private int _index;
        private RomFile[] _files = {
                _codeRomFile,
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
        return new Audio.AudioIterator();
    }
}

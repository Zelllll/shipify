import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Globals
{
    public static final String ROOM_FILE_EXTENSION = ".zroom";
    public static final String SCENE_FILE_EXTENSION = ".zscene";
    public static final String OBJECT_FILE_EXTENSION = ".zobj";

    public static final String AUDIOTABLE_NAME = "Audiotable";
    public static final String AUDIOSEQ_NAME = "Audioseq";
    public static final String AUDIOBANK_NAME = "Audiobank";
    public static final String CODE_TABLE_SAMPLE_BANK_NAME = "gSampleBankTable";
    public static final String CODE_TABLE_SEQUENCE_FONT_NAME = "gSequenceFontTable";
    public static final String CODE_TABLE_SEQUENCE_NAME = "gSequenceTable";
    public static final String CODE_TABLE_SOUND_FONT_NAME = "gSoundFontTable";

    public static final String[] AUDIO_FILE_NAMES = {
            AUDIOTABLE_NAME,
            AUDIOSEQ_NAME,
            AUDIOBANK_NAME,
            CODE_TABLE_SAMPLE_BANK_NAME,
            CODE_TABLE_SEQUENCE_FONT_NAME,
            CODE_TABLE_SEQUENCE_NAME,
            CODE_TABLE_SOUND_FONT_NAME,
    };
    public static final String[] SCENE_FILE_TYPES = {
            SCENE_FILE_EXTENSION,
            ROOM_FILE_EXTENSION,
    };
    public static final String[] OBJECT_FILE_TYPES = {
            OBJECT_FILE_EXTENSION,
    };
    public static final String[] ALL_FILE_TYPES = {
            ROOM_FILE_EXTENSION,
            SCENE_FILE_EXTENSION,
            OBJECT_FILE_EXTENSION,
    };

    public static final int ROM_BASE = 0x20;

    public static final String ROM_OUT_NAME = "patch_rom";
    public static final String ROM_FILE_LIST_OUT_NAME = "patch_files.txt";
    public static final String AUDIO_OFFSET_OUT_NAME = "audio_offsets.txt";

    public static final String[] MEME_STRINGS = {
            "Planting leeks...",
            "Ignoring pull requests...",
            "Filing cease and desist...",
            "Leaking Ford Quest...",
            "Banning Kenix...",
            "Arguing about uintptr_t...",
            "Withdrawing $1,000...",
    };

    public static byte[] loadFileByteArr(File file) {
        byte[] fileRaw = new byte[(int)file.length()];

        // read the file
        try (DataInputStream stream = new DataInputStream(new FileInputStream(file)))
        {
            // read the file into ram
            stream.readFully(fileRaw);
        } catch (IOException e)
        {
            // catch exception if the file isn't readable
            e.printStackTrace();
        }

        return fileRaw;
    }
}

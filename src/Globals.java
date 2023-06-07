/**
 * Globals.java
 * Collection of constants and globally accessible helper methods
 */

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Globals
{
    // audio
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
    public static final String[] AUDIO_TABLE_NAMES = {
            CODE_TABLE_SAMPLE_BANK_NAME,
            CODE_TABLE_SEQUENCE_FONT_NAME,
            CODE_TABLE_SEQUENCE_NAME,
            CODE_TABLE_SOUND_FONT_NAME,
    };
    public static final String[] AUDIO_BIN_NAMES = {
            AUDIOTABLE_NAME,
            AUDIOSEQ_NAME,
            AUDIOBANK_NAME,
    };

    // text
    public static final String TEXT_NES_DATA_NAME = "nes_message_data_static";
    public static final String TEXT_GER_DATA_NAME = "ger_message_data_static";
    public static final String TEXT_FRA_DATA_NAME = "fra_message_data_static";
    public static final String TEXT_STAFF_DATA_NAME = "staff_message_data_static";
    public static final String CODE_TABLE_TEXT_NES_NAME = "sNesMessageEntryTable";
    public static final String CODE_TABLE_TEXT_GER_NAME = "sGerMessageEntryTable";
    public static final String CODE_TABLE_TEXT_FRA_NAME = "sFraMessageEntryTable";
    public static final String CODE_TABLE_TEXT_STAFF_NAME = "sStaffMessageEntryTable";

    public static final String[] TEXT_FILE_NAMES = {
            TEXT_NES_DATA_NAME,
            TEXT_GER_DATA_NAME,
            TEXT_FRA_DATA_NAME,
            TEXT_STAFF_DATA_NAME,
            CODE_TABLE_TEXT_NES_NAME,
            CODE_TABLE_TEXT_GER_NAME,
            CODE_TABLE_TEXT_FRA_NAME,
            CODE_TABLE_TEXT_STAFF_NAME,
    };
    public static final String[] TEXT_TABLE_NAMES = {
            CODE_TABLE_TEXT_NES_NAME,
            CODE_TABLE_TEXT_GER_NAME,
            CODE_TABLE_TEXT_FRA_NAME,
            CODE_TABLE_TEXT_STAFF_NAME,
    };
    public static final String[] TEXT_BIN_NAMES = {
            TEXT_NES_DATA_NAME,
            TEXT_GER_DATA_NAME,
            TEXT_FRA_DATA_NAME,
            TEXT_STAFF_DATA_NAME,
    };

    // code
    public static final String CODE_NAME = "code_patch";

    public static final int ROM_BASE = 0x20;

    public static final String ROM_OUT_NAME = "patch_rom";
    public static final String ROM_FILE_LIST_OUT_NAME = "patch_files.txt";
    public static final String CODE_VARIABLE_OFFSET_LIST_OUT_NAME = "code_table_offsets.txt";

    public static final String[] MEME_STRINGS = {
            "Planting leeks...",
            "Ignoring pull requests...",
            "Filing cease and desist...",
            "Leaking 2rd Quest...",
            "Banning Kenix...",
            "Arguing about uintptr_t...",
            "Withdrawing $1,000...",
    };

    // loads a file into a byte array
    public static byte[] fileToByteArr(File file)
    {
        byte[] fileRaw = new byte[(int) file.length()];

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

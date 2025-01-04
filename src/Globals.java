/**
 * Globals.java
 * Collection of constants and globally accessible helper methods
 */

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Globals {
    // Audio
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

    // Text
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

    // Code
    public static final String CODE_NAME = "code_patch";

    // Scenes
    public static final int SCENE_SEGMENT_NUM = 2;
    public static final int ROOM_SEGMENT_NUM = 3;

    // Entrance table
    public static final int ENTRANCE_ENTRY_SIZE = 4;
    public static final String ENTRANCE_TABLE_HEADER_OUT_NAME = "entrance_table.h";
    public static final String CODE_TABLE_ENTRANCE_NAME = "gEntranceTable";

    // Entrance cutscene table
    public static final int ENTRANCE_CS_ENTRY_SIZE = 8;
    public static final String ENTRANCE_CS_TABLE_OUT_NAME = "sEntranceCutsceneTable.txt";
    public static final String CODE_TABLE_ENTRANCE_CS_NAME = "sEntranceCutsceneTable";

    // Rom writing
    public static final int ROM_BASE = 0x20;
    public static final String ROM_OUT_NAME = "patch_rom";
    public static final String ROM_FILE_LIST_OUT_NAME = "patch_files.txt";
    public static final String CODE_VARIABLE_OFFSET_LIST_OUT_NAME = "code_table_offsets.txt";

    // Meme strings
    public static final String[] MEME_STRINGS = {
            "Planting leeks...",
            "Ignoring pull requests...",
            "Filing cease and desist...",
            "Leaking 2rd Quest...",
            "Banning Kenix...",
            "Arguing about uintptr_t...",
            "Withdrawing $1,000...",
            "Decompiling Majora's Mask a year early...",
            "Giving Bruce credit advice...",
            "Adding useless cutscene command renames...",
            "Renaming ENEMY to NPC_UNFRIENDLY...",
            "Decompiling z_sram.c...",
    };

    /**
     * Reads the contents of a file into a byte array.
     *
     * @param file The file to be loaded.
     * @return A byte array containing the file's data.
     * @throws IllegalArgumentException If the provided file is null, does not exist, or is not a file.
     * @throws RuntimeException         If the file size exceeds the maximum array size or if an error occurs while reading the file.
     */
    public static byte[] fileToByteArr(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("Invalid file provided");
        }

        long fileSize = file.length();
        if (fileSize > Integer.MAX_VALUE) {
            throw new RuntimeException("File is too large to read into a byte array");
        }

        byte[] fileRaw = new byte[(int) fileSize];

        try (DataInputStream stream = new DataInputStream(Files.newInputStream(file.toPath()))) {
            stream.readFully(fileRaw);
        } catch (IOException e) {
            throw new RuntimeException("Error reading file: " + e.getMessage(), e);
        }

        return fileRaw;
    }

    /**
     * Reads a 4-byte integer from a byte array at a specified offset.
     *
     * @param arr        The byte array containing the data.
     * @param offsetInArr The offset within the array to start reading from.
     * @return The integer value read from the array.
     * @throws IndexOutOfBoundsException If the offset plus 4 bytes exceeds the array length.
     */
    public static int readIntFromByteArray(byte[] arr, int offsetInArr) {
        if (offsetInArr + 4 > arr.length) {
            throw new IndexOutOfBoundsException("Not enough bytes to read an int");
        }
        return ((arr[offsetInArr] & 0xFF) << 24) |
                ((arr[offsetInArr + 1] & 0xFF) << 16) |
                ((arr[offsetInArr + 2] & 0xFF) << 8) |
                (arr[offsetInArr + 3] & 0xFF);
    }

    /**
     * Reads a 2-byte short from a byte array at a specified offset.
     *
     * @param arr        The byte array containing the data.
     * @param offsetInArr The offset within the array to start reading from.
     * @return The short value read from the array.
     * @throws IndexOutOfBoundsException If the offset plus 2 bytes exceeds the array length.
     */
    public static int readShortFromByteArray(byte[] arr, int offsetInArr) {
        if (offsetInArr + 2 > arr.length) {
            throw new IndexOutOfBoundsException("Not enough bytes to read a short");
        }
        return ((arr[offsetInArr] & 0xFF) << 8) |
                (arr[offsetInArr + 1] & 0xFF);
    }
}

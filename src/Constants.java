public class Constants
{
    public static final String ROOM_FILE_EXTENSION = ".zroom";
    public static final String SCENE_FILE_EXTENSION = ".zscene";
    public static final String OBJECT_FILE_EXTENSION = ".zobj";

    public static final String[] AUDIO_FILE_NAMES = {
            "Audiotable",
            "Audioseq",
            "Audiobank",
            "gSampleBankTable",
            "gSequenceFontTable",
            "gSequenceTable",
            "gSoundFontTable",
    };
    public static final String[] SCENE_FILE_TYPES = {
            SCENE_FILE_EXTENSION,
            ROOM_FILE_EXTENSION,
    };
    public static final String[] OBJECT_FILE_TYPES = {
            OBJECT_FILE_EXTENSION,
    };

    public static final int ROM_BASE = 0x20;
}

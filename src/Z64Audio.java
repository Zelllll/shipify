/**
 * Z64Audio.java
 * Class representing Zelda 64 audio files.
 * Handles loading and verifying the required audio binaries and tables for the game.
 */

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

public class Z64Audio implements Iterable<RomFile> {
    private static ArrayList<RomFile> audioRomFiles;

    /**
     * Constructor for Z64Audio.
     * Loads audio binaries and tables from the provided list of files,
     * and integrates them into the game's code and ROM.
     *
     * @param audioFiles List of audio files to load.
     * @param code       Instance of Z64Code to manage audio tables.
     * @throws RuntimeException if any required audio files are missing.
     */
    public Z64Audio(ArrayList<File> audioFiles, Z64Code code) {
        audioRomFiles = new ArrayList<>();

        // Load all audio-related files
        for (File f : audioFiles) {
            String fileName = f.getName();

            // Load audio tables into the Z64Code instance
            for (String tableName : Globals.AUDIO_TABLE_NAMES) {
                if (fileName.equals(tableName)) {
                    code.addArray(Globals.fileToByteArr(f), tableName);
                }
            }

            // Load audio binaries into the ROM file list
            for (String binName : Globals.AUDIO_BIN_NAMES) {
                if (fileName.equals(binName)) {
                    audioRomFiles.add(new RomFile(f));
                }
            }
        }

        // Verify that all required audio files are loaded
        if (!allFilesLoaded(code)) {
            throw new RuntimeException("One or more audio files missing! Check documentation.");
        }
    }

    /**
     * Verifies that all required audio files (binaries and tables) were successfully loaded.
     *
     * @param code Instance of Z64Code used to check for loaded audio tables.
     * @return True if all required files are loaded, otherwise false.
     */
    private boolean allFilesLoaded(Z64Code code) {
        // Check if all audio binaries are loaded
        if (audioRomFiles.size() != Globals.AUDIO_BIN_NAMES.length) {
            return false;
        }

        // Check if all audio tables are loaded
        for (String name : Globals.AUDIO_TABLE_NAMES) {
            if (!code.contains(name)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Provides an iterator for the audio ROM files.
     * This allows iteration over all loaded audio binaries in the Z64Audio instance.
     *
     * @return Iterator for the audio ROM files.
     */
    @Override
    public Iterator<RomFile> iterator() {
        return audioRomFiles.iterator();
    }
}

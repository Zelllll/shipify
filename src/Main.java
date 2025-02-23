/**
 * Main.java
 * Main program code
 */

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    private static String outputPath;
    private static final ArrayList<File> sceneFiles = new ArrayList<>();
    private static final ArrayList<File> audioFiles = new ArrayList<>();
    private static final ArrayList<File> objectFiles = new ArrayList<>();
    private static final ArrayList<File> textFiles = new ArrayList<>();
    private static final ArrayList<File> miscFiles = new ArrayList<>();
    private static File entranceTableFile = null;
    private static File entranceCutsceneTableFile = null;

    /**
     * Entry point for the program.
     *
     * @param args Command-line arguments. Requires at least two arguments:
     *             input directory path and output directory path.
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println(OutputHelpers.RED + OutputHelpers.BOLD + "Error: Insufficient arguments provided!" +
                    OutputHelpers.RESET);
            System.out.println(OutputHelpers.YELLOW + "Usage: " + OutputHelpers.RESET + OutputHelpers.CYAN +
                    "java ProgramName input_dir output_dir" + OutputHelpers.RESET);
            System.out.println(OutputHelpers.YELLOW + "\tinput_dir: " + OutputHelpers.RESET +
                    "Input directory containing ROM hack files");
            System.out.println(OutputHelpers.YELLOW + "\toutput_dir: " + OutputHelpers.RESET +
                    "Output directory for generated files");
            return; // Exit if insufficient arguments
        }

        // Set paths from the user
        String inputPath = args[0];
        outputPath = args[1];

        // Validate input directory
        File inputDir = new File(inputPath);
        if (!inputDir.exists() || !inputDir.isDirectory()) {
            throw new IllegalArgumentException("Input directory does not exist or is not a directory: " + inputPath);
        }
        System.out.println(OutputHelpers.YELLOW + "Input directory verified: " + inputPath + OutputHelpers.RESET);

        // Validate or create output directory
        File outputDir = new File(outputPath);
        if (!outputDir.exists()) {
            if (outputDir.mkdirs()) {
                System.out.println(OutputHelpers.YELLOW + "Output directory created: " + outputPath + OutputHelpers.RESET);
            } else {
                throw new RuntimeException("Failed to create output directory: " + outputPath);
            }
        } else if (!outputDir.isDirectory()) {
            throw new IllegalArgumentException("Output path exists but is not a directory: " + outputPath);
        }
        System.out.println(OutputHelpers.YELLOW + "Output directory verified: " + outputPath + OutputHelpers.RESET);

        // Retrieve files from the input directory
        File[] files = inputDir.listFiles();
        if (files == null || files.length == 0) {
            throw new IllegalArgumentException("Input directory is empty or inaccessible: " + inputPath);
        }
        System.out.println(OutputHelpers.BLUE + "Found " + files.length + " files in input directory." + OutputHelpers.RESET);

        // Split the array of files into individual file type arrays
        splitFileTypes(files);

        // Generate output
        build();

        System.out.println(OutputHelpers.GREEN + OutputHelpers.BOLD + "Success!" + OutputHelpers.RESET +
                " Output generated in: " + outputPath);
    }


    /**
     * Categorizes files into various types based on their names.
     *
     * @param files Array of files to categorize.
     */
    public static void splitFileTypes(File[] files) {
        for (File f : files) {
            String fileName = f.getName();

            // If the file is a directory, continue
            if (!f.isFile()) {
                continue;
            }

            if (fileName.equals(Globals.CODE_TABLE_ENTRANCE_NAME)) {
                entranceTableFile = f;
            } else if (fileName.equals(Globals.CODE_TABLE_ENTRANCE_CS_NAME)) {
                entranceCutsceneTableFile = f;
            } else if (fileName.endsWith("_scene") || fileName.contains("_room_")) {
                // Check if it is a scene/room file
                sceneFiles.add(f);
            } else if (fileName.startsWith("object_")) {
                // Check if it is an object file
                objectFiles.add(f);
            } else {
                boolean added = false;
                // Check if it is an audio file
                for (String s : Globals.AUDIO_FILE_NAMES) {
                    if (fileName.equals(s)) {
                        audioFiles.add(f);
                        added = true;
                        break;
                    }
                }
                if (added) continue;

                // Check if it is a text file
                for (String s : Globals.TEXT_FILE_NAMES) {
                    if (fileName.equals(s)) {
                        textFiles.add(f);
                        added = true;
                        break;
                    }
                }
                if (added) continue;

                // Add it as a misc. file if none of the other cases are true
                miscFiles.add(f);
            }
        }
    }

    /**
     * Builds the output ROM.
     */
    private static void build() {
        RomWriter rom = new RomWriter();
        Z64Code code = new Z64Code();

        // Build each section of the rom
        buildScenes(rom);
        buildObjects(rom);
        buildMisc(rom);
        buildText(rom, code);
        buildAudio(rom, code);
        buildCode(rom, code);
        buildEntranceTable();
        buildEntranceCutsceneTable();

        // Print random meme string
        System.out.println(OutputHelpers.RED + Globals.MEME_STRINGS[(new Random()).nextInt(Globals.MEME_STRINGS.length)] +
                OutputHelpers.RESET);

        // Save rom to disk
        rom.saveRom(outputPath);
    }

    /**
     * Builds the miscellaneous files section of the ROM.
     *
     * @param rom The ROM writer object to which the files will be added.
     */
    private static void buildMisc(RomWriter rom) {
        OutputHelpers.printProgress("Building miscellaneous files");

        // If there are no miscellaneous files, skip
        if (miscFiles.isEmpty()) {
            return;
        }

        for (File f : miscFiles) {
            rom.add(new RomFile(f));
        }
    }

    /**
     * Builds the object section of the ROM.
     *
     * @param rom The ROM writer object to which the objects will be added.
     */
    private static void buildObjects(RomWriter rom) {
        OutputHelpers.printProgress("Building objects");

        // If there are no object files, do not attempt to instantiate a Z64Object object
        if (objectFiles.isEmpty()) {
            return;
        }

        for (File f : objectFiles) {
            Z64Object newObject = new Z64Object(f);

            for (RomFile rf : newObject) {
                rom.add(rf);
            }
        }
    }

    /**
     * Builds the audio section of the ROM.
     *
     * @param rom  The ROM writer object to which the audio files will be added.
     * @param code The Z64Code object containing additional ROM-related information.
     */
    private static void buildAudio(RomWriter rom, Z64Code code) {
        OutputHelpers.printProgress("Building audio");

        // If there are no audio files, do not attempt to instantiate a Z64Audio object
        if (audioFiles.isEmpty()) {
            return;
        }

        // Instantiate a Z64Audio object and build
        Z64Audio audio = new Z64Audio(audioFiles, code);

        for (RomFile rf : audio) {
            rom.add(rf);
        }
    }

    /**
     * Builds the text section of the ROM.
     *
     * @param rom  The ROM writer object to which the text files will be added.
     * @param code The Z64Code object containing additional ROM-related information.
     */
    private static void buildText(RomWriter rom, Z64Code code) {
        OutputHelpers.printProgress("Building text");

        // If there are no text files, do not attempt to instantiate a Z64Audio object
        if (textFiles.isEmpty()) {
            return;
        }

        // Instantiate a Z64Text object and build
        Z64Text text = new Z64Text(textFiles, code);

        for (RomFile rf : text) {
            rom.add(rf);
        }
    }

    /**
     * Builds the code section of the ROM.
     *
     * @param rom  The ROM writer object to which the code files will be added.
     * @param code The Z64Code object containing additional ROM-related information.
     */
    private static void buildCode(RomWriter rom, Z64Code code) {
        OutputHelpers.printProgress("Building code");

        for (RomFile romFile : code) {
            rom.add(romFile);
        }

        code.writeDataOffsets(outputPath);
    }

    /**
     * Builds the entire entrance table to a header file.
     */
    private static void buildEntranceTable() {
        OutputHelpers.printProgress("Building entrance table");

        if (entranceTableFile == null) {
            return;
        }

        // Create entrance table header file
        File outFile = new File(outputPath + "/" + Globals.ENTRANCE_TABLE_HEADER_OUT_NAME);

        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                Files.newOutputStream(outFile.toPath()), StandardCharsets.UTF_8))) {
            // Open entrance table as byte array
            byte[] entranceTableData = Globals.fileToByteArr(entranceTableFile);

            // Write each line of the entrance table header
            for (int i = 0; i < entranceTableData.length; i += Globals.ENTRANCE_ENTRY_SIZE) {
                writer.write(formatEntranceEntry(entranceTableData, i) + "\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Generates an individual entrance table line.
     *
     * @param entranceTableData Byte array of entrance table data.
     * @param offset            Offset within the data array to read from.
     * @return Formatted string representing the entrance table line.
     */
    private static String formatEntranceEntry(byte[] entranceTableData, int offset) {
        int entranceIndex = offset / Globals.ENTRANCE_ENTRY_SIZE;
        int sceneIndex = ((int) entranceTableData[offset + 0] & 0xFF);
        int spawnIndex = ((int) entranceTableData[offset + 1] & 0xFF);
        int flagsPacked = Globals.readShortFromByteArray(entranceTableData, offset + 2);

        // Unpack flags
        boolean bgmFlag = ((flagsPacked >> 15) & 1) == 1;
        boolean titleFlag = ((flagsPacked >> 14) & 1) == 1;
        int transitionEndIndex = ((flagsPacked >> 7) & 0x7F);
        int transitionStartIndex = (flagsPacked & 0x7F);
        String transitionEnd, transitionStart;
        if (transitionEndIndex >= DecompEnums.DECOMP_SCENE_TRANSITION_NAMES.length) {
            transitionEnd = "" + transitionEndIndex;
        } else {
            transitionEnd = DecompEnums.DECOMP_SCENE_TRANSITION_NAMES[transitionEndIndex];
        }
        if (transitionStartIndex >= DecompEnums.DECOMP_SCENE_TRANSITION_NAMES.length) {
            transitionStart = "" + transitionStartIndex;
        } else {
            transitionStart = DecompEnums.DECOMP_SCENE_TRANSITION_NAMES[transitionStartIndex];
        }

        String out = "DEFINE_ENTRANCE(";

        // Entrance enum name
        out += DecompEnums.DECOMP_ENTRANCE_INDEX_NAMES[entranceIndex] + ", ";

        // Scene enum name
        if (sceneIndex >= DecompEnums.DECOMP_SCENE_NAMES.length) {
            out += DecompEnums.DECOMP_SCENE_NAMES[0] + ", ";
        } else {
            out += DecompEnums.DECOMP_SCENE_NAMES[sceneIndex] + ", ";
        }

        // Spawn number
        out += spawnIndex + ", ";

        // Flags
        out += bgmFlag + ", ";
        out += titleFlag + ", ";
        out += transitionEnd + ", ";
        out += transitionStart + ")";

        return out;
    }


    /**
     * Builds the entrance cutscene table header.
     */
    private static void buildEntranceCutsceneTable() {
        OutputHelpers.printProgress("Building entrance cutscene table");

        if (entranceCutsceneTableFile == null) {
            return;
        }

        // Create entrance cutscene table output file
        File outFile = new File(outputPath + "/" + Globals.ENTRANCE_CS_TABLE_OUT_NAME);

        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                Files.newOutputStream(outFile.toPath()), StandardCharsets.UTF_8))) {
            // Open entrance cutscene table as byte array
            byte[] entranceCsTableData = Globals.fileToByteArr(entranceCutsceneTableFile);

            writer.write("EntranceCutscene " + Globals.CODE_TABLE_ENTRANCE_CS_NAME + "[] = {\n");

            // Write each line of the entrance cutscene table file
            for (int i = 0; i < entranceCsTableData.length; i += Globals.ENTRANCE_CS_ENTRY_SIZE) {
                writer.write(formatEntranceCutsceneEntry(entranceCsTableData, i) + "\n");
            }

            writer.write("};\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Formats an entrance cutscene table entry as a string.
     *
     * @param entranceCsTableData Byte array of entrance cutscene table data.
     * @param offset              Index to start reading from.
     * @return Formatted string representing the entrance cutscene table entry.
     */
    private static String formatEntranceCutsceneEntry(byte[] entranceCsTableData, int offset) {
        // Get arguments from table entry
        String entrance = DecompEnums.DECOMP_ENTRANCE_INDEX_NAMES[Globals.readShortFromByteArray(entranceCsTableData, offset)];
        int ageRestriction = ((int) entranceCsTableData[offset + 2] & 0xFF);
        int flag = ((int) entranceCsTableData[offset + 3] & 0xFF);
        int segmentAddress = Globals.readIntFromByteArray(entranceCsTableData, offset + 4);

        // Format output string
        String out = "    {";
        out += entrance + ", ";
        out += ageRestriction + ", ";
        out += "0x" + Integer.toHexString(flag) + ", ";
        out += "\"__OTR__scenes/shared/???/???" + Integer.toHexString(segmentAddress) + "???\"},";

        return out;
    }

    /**
     * Builds the scene section of the ROM.
     *
     * @param rom The ROM writer object to which the scene files will be added.
     */
    private static void buildScenes(RomWriter rom) {
        OutputHelpers.printProgress("Building scenes and rooms");

        ArrayList<Z64Scene> sceneList = genSceneList();

        // Add all the scenes and rooms to the rom
        for (Z64Scene scene : sceneList) {
            // Add file to the rom
            for (RomFile romFile : scene) {
                rom.add(romFile);
            }

            // Generate xml
            scene.saveXml(outputPath);
        }
    }

    /**
     * Generates the scene list by iterating over scene files and constructing Z64Scene objects.
     *
     * @return A list of all generated scenes.
     */
    private static ArrayList<Z64Scene> genSceneList() {
        ArrayList<Z64Scene> out = new ArrayList<>();

        for (File f : sceneFiles) {
            // Check if the file is a scene
            if (f.getName().endsWith("_scene")) {
                // Create a new scene object
                Z64Scene scene = new Z64Scene(new RomFile(f));

                // Add all rooms
                addRoomsToScene(scene);

                // Add scene object to the output list
                out.add(scene);
            }
        }

        return out;
    }

    /**
     * Extracts the index from a room file name.
     * For example, getIndexFromRoomName("jabu_jabu_room_2") returns 2,
     * getIndexFromRoomName("jabu_jabu_room_25") returns 25.
     *
     * @param roomName The name of the room file.
     * @return The extracted room index.
     * @throws RuntimeException If no index is found in the room name.
     */
    private static int getIndexFromRoomName(String roomName) {
        Pattern p = Pattern.compile("[0-9]+$");
        Matcher m = p.matcher(roomName);
        if (m.find()) {
            return Integer.parseInt(m.group());
        } else {
            throw new RuntimeException();
        }
    }

    /**
     * Adds all rooms associated with a scene to the given scene object.
     *
     * @param scene The scene to which rooms will be added.
     */
    private static void addRoomsToScene(Z64Scene scene) {
        String sceneName = scene.getName();
        ArrayList<File> roomInputFiles = new ArrayList<>();
        int lastRoomAdded = -1;

        // Add all the room file names to the list
        for (File f : sceneFiles) {
            String fileName = f.getName();

            if (fileName.startsWith(sceneName + "_room_")) {
                roomInputFiles.add(f);
            }
        }

        while (lastRoomAdded < roomInputFiles.size() - 1) {
            for (File f : roomInputFiles) {
                int roomIndex = getIndexFromRoomName(f.getName());
                if (roomIndex == lastRoomAdded + 1) {
                    scene.addRoom(new RomFile(f));
                    lastRoomAdded++;
                }
            }
        }
    }
}

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class EntryPoint
{
    // user configurable
    private static final String INPUT_PATH = "";
    private static final String OUTPUT_PATH = "";

    public static void main(String[] args)
    {
        File[] files = new File(INPUT_PATH).listFiles();

        build(files);
    }

    private static void addRoomsToScene(File[] inputFiles, Scene scene) {
        ArrayList<File> roomInputFiles = new ArrayList<>();
        int lastRoomAdded = -1;

        // add all the room file names to the list
        for (File f : inputFiles)
        {
            if (!f.isFile()) {
                continue;
            }
            String sceneName = scene.getName();
            if (f.getName().contains(sceneName) && f.getName().contains("_room_"))
            {
                roomInputFiles.add(f);
            }
        }

        while (lastRoomAdded < roomInputFiles.size() - 1) {
            for (File f : roomInputFiles)
            {
                if (!f.isFile()) {
                    continue;
                }
                int indexOfRoomFile = Integer.parseInt(f.getName().replace(scene.getName() + "_room_", ""));

                if (indexOfRoomFile == lastRoomAdded + 1) {
                    scene.addRoom(new RomFile(f));
                    lastRoomAdded++;
                }
            }
        }
    }

    private static ArrayList<Scene> genSceneList(File[] inputFiles) {
        ArrayList<Scene> out = new ArrayList<>();

        for (File f : inputFiles)
        {
            if (!f.isFile()) {
                continue;
            }
            // check if the file is a scene
            if (f.getName().endsWith("_scene"))
            {
                // create a new scene object
                Scene scene = new Scene(new RomFile(f));

                // add all rooms
                addRoomsToScene(inputFiles, scene);

                // add scene object to the output list
                out.add(scene);
            }
        }

        return out;
    }

    private static void build(File[] inputFiles) {
        RomWriter outputRom = new RomWriter();
        ArrayList<Scene> sceneList = genSceneList(inputFiles);

        // add all the scenes and rooms to the rom
        for (Scene scene : sceneList)
        {
            // add file to the rom
            for (RomFile romFile : scene)
            {
                outputRom.add(romFile);
            }

            // generate xml
            SceneXmlGenerator xmlGenerator = new SceneXmlGenerator(scene);
            xmlGenerator.saveXml(OUTPUT_PATH);
        }

        // save rom to disk
        outputRom.saveRom(OUTPUT_PATH);
    }
}
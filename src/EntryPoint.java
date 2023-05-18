import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EntryPoint
{
    private static String _inputPath, _outputPath;
    private static ArrayList<File> _sceneFiles, _audioFiles, _objectFiles;

    /**
     * Setup
     */
    public static void main(String[] args)
    {
        if (args.length < 2)
        {
            System.out.println("usage: input_dir output_dir\n" +
                    "\tinput_dir: input directory containing rom hack files\n" +
                    "\toutput_dir: output directory");
        }
        else
        {
            // set paths from the user
            _inputPath = args[0];
            _outputPath = args[1];

            // check if the input path is valid, if not throw an exception
            File f = new File(_inputPath);
            if (!(f.exists() && f.isDirectory()))
            {
                throw new IllegalArgumentException("Input directory does not exist!!!");
            }

            System.out.println("Input directory exists...");

            // check if the output path is valid, and if not attempt to create it
            f = new File(_outputPath);
            if (!(f.exists() && f.isDirectory()))
            {
                f.mkdir();
                System.out.println("Output directory created...");
            }

            System.out.println("Output directory exists...");

            // create an array of all the input files
            File[] files = new File(_inputPath).listFiles();

            _sceneFiles = new ArrayList<>();
            _audioFiles = new ArrayList<>();
            _objectFiles = new ArrayList<>();

            // split the array of all files into individual file type arrays
            splitFileTypes(files);

            // generate output
            build();

            System.out.println("Success!");
        }
    }

    public static void splitFileTypes(File[] files)
    {
        for (File f : files)
        {
            String fileName = f.getName();

            // if the file is a directory, continue
            if (!f.isFile())
            {
                continue;
            }

            // check if it is an audio file
            for (String s : Constants.AUDIO_FILE_NAMES)
            {
                if (fileName.equals(s))
                {
                    _audioFiles.add(f);
                }
            }

            // check if it is a scene/room file
            for (String s : Constants.SCENE_FILE_TYPES)
            {
                if (fileName.endsWith(s))
                {
                    _sceneFiles.add(f);
                }
            }

            // check if it is an object file
            for (String s : Constants.OBJECT_FILE_TYPES)
            {
                if (fileName.endsWith(s))
                {
                    _objectFiles.add(f);
                }
            }
        }
    }

    /**
     * Output ROM building
     */
    private static void build()
    {
        RomWriter rom = new RomWriter();

        buildScenes(rom);
        buildObjects(rom);
        buildAudio(rom);

        // save rom to disk
        rom.saveRom(_outputPath);
    }

    private static void buildScenes(RomWriter rom)
    {
        System.out.println("Building scenes...");

        ArrayList<Scene> sceneList = genSceneList();

        // add all the scenes and rooms to the rom
        for (Scene scene : sceneList)
        {
            // add file to the rom
            for (RomFile romFile : scene)
            {
                rom.add(romFile);
            }

            // generate xml
            SceneXmlGenerator xmlGenerator = new SceneXmlGenerator(scene);
            xmlGenerator.saveXml(_outputPath);
        }
    }

    private static void buildObjects(RomWriter rom)
    {
        System.out.println("Building objects...");

        for (File f : _objectFiles)
        {
            rom.add(new RomFile(f));
        }
    }

    private static void buildAudio(RomWriter rom)
    {
        System.out.println("Building audio...");
        // TODO: build audio!!!
    }

    /**
     * Scene generation
     */
    // sourced from StackOverflow
    // https://stackoverflow.com/questions/3083154/how-can-i-get-the-last-integer-56-from-string-like-ra12ke43sh56
    private static int getIndexFromRoomName(String roomName)
    {
        Pattern p = Pattern.compile("[0-9]+$");
        Matcher m = p.matcher(roomName);
        if (m.find())
        {
            return Integer.parseInt(m.group());
        }
        else
        {
            throw new RuntimeException();
        }
    }

    private static void addRoomsToScene(Scene scene)
    {
        String sceneName = scene.getName();
        ArrayList<File> roomInputFiles = new ArrayList<>();
        int lastRoomAdded = -1;

        // add all the room file names to the list
        for (File f : _sceneFiles)
        {
            String fileName = f.getName();

            if (fileName.startsWith(sceneName) && fileName.endsWith(Constants.ROOM_FILE_EXTENSION))
            {
                roomInputFiles.add(f);
            }
        }

        while (lastRoomAdded < roomInputFiles.size() - 1)
        {
            for (File f : roomInputFiles)
            {
                int roomIndex = getIndexFromRoomName(f.getName().replace(Constants.ROOM_FILE_EXTENSION, ""));
                if (roomIndex == lastRoomAdded + 1)
                {
                    scene.addRoom(new RomFile(f));
                    lastRoomAdded++;
                }
            }
        }
    }

    private static ArrayList<Scene> genSceneList()
    {
        ArrayList<Scene> out = new ArrayList<>();

        for (File f : _sceneFiles)
        {
            // check if the file is a scene
            if (f.getName().endsWith(Constants.SCENE_FILE_EXTENSION))
            {
                // create a new scene object
                Scene scene = new Scene(new RomFile(f));

                // add all rooms
                addRoomsToScene(scene);

                // add scene object to the output list
                out.add(scene);
            }
        }

        return out;
    }
}

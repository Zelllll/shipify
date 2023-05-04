import java.util.ArrayList;
import java.util.Iterator;

public class Scene implements Iterable<RomFile>
{
    private RomFile _sceneRomFile;
    private ArrayList<RomFile> _roomRomFiles;

    // constructor
    public Scene(RomFile scene) {
        _sceneRomFile = scene;
        _roomRomFiles = new ArrayList<RomFile>();
    }

    // adds a new room to the scene
    public void addRoom(RomFile room) {
        _roomRomFiles.add(room);
    }

    // gets the room's RomFile
    public RomFile getRoom(int index) {
        return _roomRomFiles.get(index);
    }

    // gets the scene's RomFile
    public RomFile getScene() {
        return _sceneRomFile;
    }

    // returns the number of rooms currently added to the scene
    public int getNumRooms() {
        return _roomRomFiles.size();
    }

    // returns the name of the scene without the "_scene" suffix
    public String getName() {
        return _sceneRomFile.getName().replace("_scene", "");
    }

    // iterator that gives all the RomFile's in the scene
    class SceneIterator implements Iterator<RomFile>
    {
        private int _index;

        // constructor
        public SceneIterator()
        {
            // -1 represents the scene file, 0 represents room 0, 1 represents room 1, etc.
            _index = -1;
        }

        // tells the iterator when it is finished
        public boolean hasNext()
        {
            return (_index < _roomRomFiles.size());
        }

        // returns the next file
        public RomFile next()
        {
            RomFile out;

            if (_index < 0) {
                out = _sceneRomFile;
            } else {
                out = _roomRomFiles.get(_index);
            }
            _index++;

            return out;
        }
    }

    // public iterator
    public Iterator<RomFile> iterator()
    {
        return new SceneIterator();
    }
}

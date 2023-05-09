import java.util.ArrayList;
import java.util.Iterator;

public class Scene implements Iterable<RomFile>
{
    private RomFile _sceneRomFile;
    private ArrayList<RomFile> _roomRomFiles;
    private ArrayList<CamData> _camDataList;

    // constructor
    public Scene(RomFile scene)
    {
        _sceneRomFile = scene;
        _roomRomFiles = new ArrayList<>();
        _camDataList = new ArrayList<>();

        setCamDataList();
    }

    public ArrayList<CamData> getCamDataList()
    {
        return _camDataList;
    }

    private int getHeaderCmdOffset(int headerBase, int cmd)
    {
        byte[] sceneData = _sceneRomFile.getData();

        if (sceneData != null)
        {
            while (headerBase < sceneData.length && sceneData[headerBase] != 0x14)
            {
                if (sceneData[headerBase] == cmd)
                {
                    return headerBase;
                }
                headerBase += 8;
            }
        }
        return -1;
    }

    private int segAddrToOffset(byte[] arr, int offsetInArr)
    {
        return (((int) arr[offsetInArr + 1] & 0xFF) << 16) |
                (((int) arr[offsetInArr + 2] & 0xFF) << 8) |
                (((int) arr[offsetInArr + 3] & 0xFF));
    }

    private void setCamDataList()
    {
        byte[] sceneData = _sceneRomFile.getData();
        int collisionHeaderCmdOffset = getHeaderCmdOffset(0, 0x03);
        int camDataOffset = segAddrToOffset(sceneData, segAddrToOffset(sceneData, collisionHeaderCmdOffset + 4) + 0x20);
        int tempOffset = camDataOffset;
        int nCams = 0;

        // now that we have the address of the cam data, guess the length of it
        while (sceneData[tempOffset] == 0x00 && sceneData[tempOffset + 0x4] == 0x02)
        {
            tempOffset += 0x08;
            nCams++;
        }

        _camDataList.add(new CamData(camDataOffset, nCams));
    }

    protected class CamData
    {
        int _nCamData;
        int _offset;

        public CamData(int offset, int nCamData)
        {
            _nCamData = nCamData;
            _offset = offset;
        }

        public int getOffset()
        {
            return _offset;
        }

        public int getNumCamData()
        {
            return _nCamData;
        }
    }

    // adds a new room to the scene
    public void addRoom(RomFile room)
    {
        _roomRomFiles.add(room);
    }

    // gets the room's RomFile
    public RomFile getRoom(int index)
    {
        return _roomRomFiles.get(index);
    }

    // gets the scene's RomFile
    public RomFile getScene()
    {
        return _sceneRomFile;
    }

    // returns the number of rooms currently added to the scene
    public int getNumRooms()
    {
        return _roomRomFiles.size();
    }

    // returns the name of the scene without the "_scene" suffix
    public String getName()
    {
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

            if (_index < 0)
            {
                out = _sceneRomFile;
            }
            else
            {
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

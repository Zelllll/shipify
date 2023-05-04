import java.io.*;

public class RomFile
{
    private String _name;
    private byte[] _fileData;
    private int _offset;

    public RomFile(File file)
    {
        // set default offset
        _offset = 0;

        // set the file name
        _name = file.getName();

        try (DataInputStream stream = new DataInputStream(new FileInputStream(file)))
        {
            // allocate space for the file in ram
            _fileData = new byte[(int) file.length()];
            // read the file into ram
            stream.readFully(_fileData);
        } catch (IOException e)
        {
            // catch exception if the file isn't readable
            e.printStackTrace();
        }
    }

    // returns the data of the rom file in ram
    public byte[] getData()
    {
        return _fileData;
    }

    // returns the name of the rom file
    public String getName()
    {
        return _name;
    }

    public int getOffset()
    {
        return _offset;
    }

    public void setOffset(int offset)
    {
        _offset = offset;
    }

    public int getSize()
    {
        return (int) _fileData.length;
    }
}

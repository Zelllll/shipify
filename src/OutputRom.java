import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class OutputRom
{
    private ArrayList<RomFile> _romFiles;
    private String _outPath;

    public OutputRom(String outPath)
    {
        _romFiles = new ArrayList<RomFile>();
        _outPath = outPath;
    }

    public void add(RomFile romFile)
    {
        _romFiles.add(romFile);
    }

    private int getDmaTableLength()
    {
        return 16 * (_romFiles.size() + 1);
    }

    private int setOffsets()
    {
        int curOffset = getDmaTableLength();

        for (RomFile romFile : _romFiles)
        {
            romFile.setOffset(curOffset);
            curOffset += romFile.getSize();
        }
        return curOffset;
    }

    private void writeDmaTable(byte[] out)
    {
        int offset = 0;

        for (RomFile file : _romFiles)
        {
            int fileStart = file.getOffset();
            int fileEnd = fileStart + file.getSize();

            // write start address of file
            out[offset + 0] = out[offset + 0 + 8] = (byte) ((fileStart >> 24) & 0xFF);
            out[offset + 1] = out[offset + 1 + 8] = (byte) ((fileStart >> 16) & 0xFF);
            out[offset + 2] = out[offset + 2 + 8] = (byte) ((fileStart >> 8) & 0xFF);
            out[offset + 3] = out[offset + 3 + 8] = (byte) (fileStart & 0xFF);

            // write end address of file
            out[offset + 4 + 0] = (byte) ((fileEnd >> 24) & 0xFF);
            out[offset + 4 + 1] = (byte) ((fileEnd >> 16) & 0xFF);
            out[offset + 4 + 2] = (byte) ((fileEnd >> 8) & 0xFF);
            out[offset + 4 + 3] = (byte) (fileEnd & 0xFF);

            // zero the last four bytes of the entry
            for (int j = 0; j < 4; j++)
            {
                out[offset + 12 + j] = 0;
            }

            offset += 16;
        }
    }

    private void writeFileToRomArray(byte[] romOut, RomFile romFile)
    {
        System.arraycopy(romFile.getData(), 0, romOut, romFile.getOffset(), romFile.getSize());
    }

    public void saveRom()
    {
        // set the offsets within the files
        int romSize = setOffsets();
        // allocate rom
        byte[] outRomData = new byte[romSize];
        // make rom file
        File outFile = new File(_outPath);

        // write dma table to the start of the rom
        writeDmaTable(outRomData);

        // add each file to the output rom
        for (RomFile romFile : _romFiles)
        {
            writeFileToRomArray(outRomData, romFile);
            System.out.println(romFile.getName());
        }

        // output rom
        try (FileOutputStream outputStream = new FileOutputStream(outFile))
        {
            outputStream.write(outRomData);
        } catch (FileNotFoundException e)
        {
            throw new RuntimeException(e);
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}

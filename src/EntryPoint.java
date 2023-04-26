import java.io.File;

public class EntryPoint
{
    public static void main(String[] args)
    {
        String path = "path/to/asset/binaries";
        OutputRom outputRom = new OutputRom("asset.bin");

        File[] files = new File(path).listFiles();

        for (File file : files)
        {
            if (file.isFile())
            {
                RomFile romFile = new RomFile(file.getPath());
                outputRom.add(romFile);
            }
        }

        outputRom.saveRom();
    }
}
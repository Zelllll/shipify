import java.io.*;

public class SceneXmlGenerator
{
    private static Scene _scene;

    public SceneXmlGenerator(Scene scene) {
        _scene = scene;
    }

    public static String sceneXmlString(String name) {
        return "\t<File Name=\"" + name + "_scene\" Segment=\"2\">\n" +
                "\t\t<Scene Name=\"" + name + "_scene\" Offset=\"0x0\"/>\n" +
                "\t</File>\n";
    }

    public static String roomXmlString(String name, int roomIndex) {
        return "\t<File Name=\"" + name + "_room_" + roomIndex + "\" Segment=\"3\">\n" +
                "\t\t<Room Name=\"" + name + "_room_" + roomIndex + "\" Offset=\"0x0\"/>\n" +
                "\t</File>\n";
    }

    public void saveXml(String outPath) {
        String name = _scene.getName();
        File outXmlFile = new File(outPath + "\\" + name + ".xml");

        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(outXmlFile), "utf-8"))) {
            writer.write("<Root>\n");
            writer.write(sceneXmlString(name));

            for (int i = 0; i < _scene.getNumRooms(); i++)
            {
                writer.write(roomXmlString(name, i));
            }
            writer.write("</Root>\n");

        } catch (FileNotFoundException e)
        {
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}

import java.io.*;

public class SceneXmlGenerator
{
    private static Scene _scene;

    public SceneXmlGenerator(Scene scene)
    {
        _scene = scene;
    }

    private String sceneXmlString(String name, String nodes)
    {
        return "\t<File Name=\"" + name + "_scene\" Segment=\"2\">\n" +
                "\t\t<Scene Name=\"" + name + "_scene\" Offset=\"0x0\"/>\n" +
                nodes +
                "\t</File>\n";
    }

    private String roomXmlString(String name, int roomIndex)
    {
        return "\t<File Name=\"" + name + "_room_" + roomIndex + "\" Segment=\"3\">\n" +
                "\t\t<Room Name=\"" + name + "_room_" + roomIndex + "\" Offset=\"0x0\"/>\n" +
                "\t</File>\n";
    }

    private String camDataXmlString(int offset, int count, String name)
    {
        return "\t\t<CamData Name=\"" + name +
                "\" Offset=\"0x" + Integer.toHexString(offset) +
                "\" Count=\"" + count +
                "\"/>\n";
    }

    public void saveXml(String outPath)
    {
        String name = _scene.getName();
        File outXmlFile = new File(outPath + "\\" + name + ".xml");

        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(outXmlFile), "utf-8")))
        {
            // create string containing all the XML nodes for CamData
            String sceneNodes = "";

            for (Scene.CamData camData : _scene.getCamDataList())
            {
                sceneNodes += camDataXmlString(camData.getOffset(), camData.getNumCamData(), "g_" + _scene.getName() + "_camData_" + Integer.toHexString(camData.getOffset()));
            }

            // write the scene XML string, including any extra nodes like CamData
            writer.write("<Root>\n");
            writer.write(sceneXmlString(name, sceneNodes));

            // write the room XML string
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

/**
 * Z64Code.java
 * Class representing a Zelda 64 code file
 */

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

public class Z64Code implements Iterable<RomFile>
{
    private class CodeVariable
    {
        private byte[] _tableData;
        private String _tableName;
        private int _offset;

        // constructor
        public CodeVariable(byte[] data, String name)
        {
            _tableData = data;
            _tableName = name;
            _offset = 0;
        }

        // returns the name of the byte array
        public String getName()
        {
            return _tableName;
        }


        // returns the data of the byte array

        public byte[] getData()
        {
            return _tableData;
        }


        // sets the offset of the array
        public void setOffset(int offset)
        {
            _offset = offset;
        }


        // gets the offset of the array
        public int getOffset()
        {
            return _offset;
        }
    }

    private ArrayList<CodeVariable> _dataVariables;

    // constructor
    public Z64Code()
    {
        _dataVariables = new ArrayList<>();
    }

    // adds a new byte array
    public void addArray(CodeVariable newArray)
    {
        _dataVariables.add(newArray);
    }

    // adds a new array to code
    public void addArray(byte[] data, String name)
    {
        _dataVariables.add(new CodeVariable(data, name));
    }

    // checks if code currently contains a variable with the specified name
    public boolean contains(String variableName)
    {
        for (CodeVariable var : _dataVariables)
        {
            if (var.getName().equals(variableName))
            {
                return true;
            }
        }
        return false;
    }

    // gets the expected size of code
    private int getExpectedFileSize()
    {
        int size = 0;

        for (CodeVariable var : _dataVariables)
        {
            size += var.getData().length;
        }

        return size;
    }

    // generates the RomFile for code, and returns a reference to it,
    // and sets the offsets of each data variable
    private RomFile genRomFile()
    {
        int fileSize = getExpectedFileSize();

        // do not build the file if no tables are being added
        if (fileSize == 0)
        {
            byte[] emptyArr = new byte[16];
            return new RomFile(emptyArr, Globals.CODE_NAME);
        }

        // generate raw data for the file
        byte[] rawData = new byte[getExpectedFileSize()];
        int offset = 0;

        // add all the tables to the data
        for (CodeVariable var : _dataVariables)
        {
            byte[] tableData = var.getData();
            int tableLength = tableData.length;

            // sanity check
            if (offset > fileSize)
            {
                throw new RuntimeException("Something went wrong when generating code");
            }

            // set the offset of the variable
            var.setOffset(offset);

            // copy table into the code file data
            System.arraycopy(tableData, 0, rawData, offset, tableLength);

            // set offset for next iteration
            offset += tableLength;
        }

        // generate the RomFile
        return new RomFile(rawData, Globals.CODE_NAME);
    }

    // writes the offsets relative to `code` of the data variables contained within it
    public void writeDataOffsets(String outputPath)
    {
        // code file is empty
        if (genRomFile() == null)
        {
            return;
        }

        // create a file to write the offsets of the code variables in
        File outFile = new File(outputPath + "/" + Globals.CODE_VARIABLE_OFFSET_LIST_OUT_NAME);

        // output file list
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(outFile), "utf-8")))
        {
            for (CodeVariable table : _dataVariables)
            {
                writer.write(table.getName() + " : [code + 0x" +
                        Integer.toHexString(table.getOffset()) + "]\n");
            }
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

    /**
     * RomFile iteration
     */

    // iterator that gives all the code `RomFile`s (only a single one)
    private class CodeIterator implements Iterator<RomFile>
    {
        private boolean _done;

        // constructor
        public CodeIterator()
        {
            _done = false;
        }

        // tells the iterator when it is finished
        public boolean hasNext()
        {
            return !_done;
        }

        // returns the next file
        public RomFile next()
        {
            _done = true;
            return genRomFile();
        }
    }

    // public iterator
    public Iterator<RomFile> iterator()
    {
        return new CodeIterator();
    }
}

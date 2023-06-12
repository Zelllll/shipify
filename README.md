# Shipify
Shipify is a new tool designed to assist in porting existing Zelda 64 ROM Hacks to the [Ship of Harkinian](https://github.com/HarbourMasters/Shipwright) platform.

Currently, Shipify supports porting custom music, samples, instruments, text, maps, and models from existing Zelda 64 hacks. Shipify is designed mainly for the purpose of porting existing hacks to Ship of Harkinian, though it is *technically* possible to create entirely new hacks with it.

Ultimately, Shipify is tool designed to assist in the process of porting a hack, although it is not capable of porting all hacks automatically.

## Usage
Shipify expects you to create an input directory containing all of your assets, and an empty output directory.

To invoke Shipify in the command line, simply include the two file paths as your arguments.

```
shipify input_dir output_dir
```
Once Shipify finishes, it will generate a few files:
```
patch_rom
patch_files.txt
code_table_offsets.txt
```
Every time you run Shipify, you will need to replace `patch_rom` and `patch_files.txt` within Ship of Harkinian, and then recreate your `.otr`.

## Audio
In order to port a hack's audio, Shipify will expect you to extract these files from your hack and place them in the input directory:
```
Audioseq
Audiobank
Audiotable
gSampleBankTable
gSequenceFontTable
gSequenceTable
gSoundFontTable
```
Once Shipify has finished packing your assets, it will generate a file called `code_table_offsets.txt`. This file contains some information that you need to change within Ship of Harkinian.

At the top of [`Audio.xml`](https://github.com/HarbourMasters/Shipwright/blob/develop/soh/assets/xml/GC_MQ_D/audio/Audio.xml) there are some offsets of various tables that define how your audio files are laid out. We will need to change these to the values that are included in `code_table_offsets.txt`. The names in these two files are not exactly the same, so here is a translation:

```
SoundFontTableOffset == gSoundFontTable
SequenceTableOffset == gSequenceTable
SampleBankTableOffset == gSampleBankTable
SequenceFontTableOffset == gSequenceFontTable
```
You will also want to change `RangeStart` to be `0x0`, and `RangeEnd` to the end address included in `code_table_offsets.txt`.

After you have modified your `Audio.xml`, upon regenerating your `.otr`, all of your audio modifications will be ported.

## Maps
In Zelda 64, maps consist of one "scene" file, and one or more "room" files. Shipify mainly expects map binaries created by [SharpOcarina](http://n64vault.com/zelda-oot-tools:sharp-ocarina), although it can accept maps made with other tools, so long as they are a compiled binary (**not** a `.c` file).

Any maps you would like to include simply need to have all of their assoicated binaries placed in the input directory. They also must follow this naming scheme to be detected, with `my_map` being a name chosen by the user:
```
my_map_scene
my_map_room_0
my_map_room_1
...
```
Once Shipify finishes it will generate a corresponding `my_map.xml`, which should be placed in the [scene xml directory](https://github.com/HarbourMasters/Shipwright/tree/develop/soh/assets/xml/GC_MQ_D/scenes) in Ship of Harkinian.

In order for the game to know the map exists, you also need to modify an entry in [`gSceneTable`](https://github.com/HarbourMasters/Shipwright/blob/develop/soh/src/code/z_scene_table.c#L833) to include the name of your new map. So to replace the Deku Tree for example, simply change:
`TITLED_SCENE(ydan_scene, g_pn_06, 1, 19, 2),`
to:
`TITLED_SCENE(my_map_scene, g_pn_06, 1, 19, 2),`.

Once you have completed these steps and regenerate your `.otr`, your new map should exist in game.
## Text
The process for porting a hack's text is similar to that of porting the audio files. Shipify will expect you to extract these files from your hack, and include them in the input directory:
```
nes_message_data_static
ger_message_data_static
fra_message_data_static
staff_message_data_static
sNesMessageEntryTable
sGerMessageEntryTable
sFraMessageEntryTable
sStaffMessageEntryTable
```
It should be noted that `ger_message_data_static`, and `fra_message_data_static` are optional, and should only be included if the French or German languages were modified in the hack you are porting. All other listed files and tables are required.

Like with audio, we will need to edit [`message_data_static`](https://github.com/HarbourMasters/Shipwright/blob/develop/soh/assets/xml/GC_MQ_D/text/message_data_static.xml) within Ship of Harkinian using the values included in the generated `code_table_offsets.txt`.

Essentially, all `CodeOffset`'s in the xml will be replaced with the value of `sNesMessageEntryTable` in `code_table_offsets.txt`, except for the `CodeOffset` after `staff_message_data_static`, which should be replaced with the value of `sStaffMessageEntryTable` within `code_table_offsets.txt`.
The `LangOffset` after `ger_message_data_static` corresponds to the value of `sGerMessageEntryTable`, and the `LangOffset` after `fra_message_data_static` corresponds to `sFraMessageEntryTable` in `code_table_offsets.txt`.



## Objects
Any custom objects that you would like to port must be included in the input directory, and have the `object_` prefix.

If an object is simpily a modification of an object that already exists in the game (such as a texture swap, display list port, etc.), then it may simply work without any changes, once you regenerate your `.otr`.

If the object is brand new, it will need a new xml to be added to the [object xml directory](https://github.com/HarbourMasters/Shipwright/tree/develop/soh/assets/xml/GC_MQ_D/objects). It is up to the user to create an xml defining the contents of the object, as Shipify can not autogenerate them like it can with maps.

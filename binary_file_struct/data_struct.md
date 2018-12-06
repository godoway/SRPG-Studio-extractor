# Struct of data.dts

## Header

| length | content|
| :---: | --- |
| 4 bytes | SDTS(signatures) |
| 4 bytes | isDecrypt |
| 4 bytes | SRPG Studio version |
| 8 bytes | I DON'T KNOWN |
| 4 bytes | position of project.srpgs (can not be decompiled to source struct) |
| 4 bytes | position of "Graphics/mapchip" (need add 168 and the end is next position - 1, same as below)|
| 4 bytes | position of "Graphics/charchip" |
| 4 bytes | position of "Graphics/face" |
| 4 bytes | position of "Graphics/icon" |
| 4 bytes | position of "Graphics/motion" |
| 4 bytes | position of "Graphics/effect" |
| 4 bytes | position of "Graphics/weapon" |
| 4 bytes | position of "Graphics/bow" |
| 4 bytes | position of "Graphics/thumbnail" |
| 4 bytes | position of "Graphics/battleback" |
| 4 bytes | position of "Graphics/eventback" |
| 4 bytes | position of "Graphics/screenback" |
| 4 bytes | position of "Graphics/worldmap" |
| 4 bytes | position of "Graphics/eventstill" |
| 4 bytes | position of "Graphics/charillust" |
| 4 bytes | position of "Graphics/picture" |
| 4 bytes | position of "UI/menuwindow" |
| 4 bytes | position of "UI/textwindow" |
| 4 bytes | position of "UI/title" |
| 4 bytes | position of "UI/number" |
| 4 bytes | position of "UI/bignumber" |
| 4 bytes | position of "UI/gauge" |
| 4 bytes | position of "UI/line" |
| 4 bytes | position of "UI/risecursor" |
| 4 bytes | position of "UI/mapcursor" |
| 4 bytes | position of "UI/pagecursor" |
| 4 bytes | position of "UI/selectcursor" |
| 4 bytes | position of "UI/scrollcursor" |
| 4 bytes | position of "UI/panel" |
| 4 bytes | position of "UI/faceframe" |
| 4 bytes | position of "UI/screenframe" |
| 4 bytes | position of "Audio/music" |
| 4 bytes | position of "Audio/sound" |
| 4 bytes | position of "Fonts" |
| 4 bytes | position of "Video" |
| 4 bytes | position of "Script" |

other struct see the ```gwsl.srpgstudio.extractor.struct```
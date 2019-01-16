package gwsl.srpgstudio.extractor.struct;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DataStruct {

    private static volatile DataStruct INSTANCE;
    private long projectBegin;
    private long projectSize;
    private boolean isEncrypt;
    private List<Fragment> fragments;

    public List<FileType> fileTypeList;


    private DataStruct() {
        initFragment();
        initFileType();
    }


    public static DataStruct getInstance() {
        if (INSTANCE == null) {
            synchronized (DataStruct.class) {
                if (INSTANCE == null) {
                    INSTANCE = new DataStruct();
                }
            }
        }
        return INSTANCE;
    }

    private void initFileType() {
        fileTypeList = new ArrayList<>();
        fileTypeList.add(new FileType("jpg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}));
        fileTypeList.add(new FileType("png", new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47}));
        fileTypeList.add(new FileType("bmp", new byte[]{0x42, 0x4D}));
        fileTypeList.add(new FileType("mp3", new byte[]{0x49, 0x44, 0x33, 0x03}));
        fileTypeList.add(new FileType("wav", new byte[]{0x52, 0x49, 0x46, 0x46, 0x72}));
        fileTypeList.add(new FileType("ogg", new byte[]{0x4F, 0x67, 0x67}));
        fileTypeList.add(new FileType("mid", new byte[]{0x4D, 0x54, 0x68, 0x64}));
    }


    private void initFragment() {
        fragments = new ArrayList<>();
        fragments.add(new Fragment("Graphics/mapchip"));
        fragments.add(new Fragment("Graphics/charchip"));
        fragments.add(new Fragment("Graphics/face"));
        fragments.add(new Fragment("Graphics/icon"));
        fragments.add(new Fragment("Graphics/motion"));
        fragments.add(new Fragment("Graphics/effect"));
        fragments.add(new Fragment("Graphics/weapon"));
        fragments.add(new Fragment("Graphics/bow"));
        fragments.add(new Fragment("Graphics/thumbnail"));
        fragments.add(new Fragment("Graphics/battleback"));
        fragments.add(new Fragment("Graphics/eventback"));
        fragments.add(new Fragment("Graphics/screenback"));
        fragments.add(new Fragment("Graphics/worldmap"));
        fragments.add(new Fragment("Graphics/eventstill"));
        fragments.add(new Fragment("Graphics/charillust"));
        fragments.add(new Fragment("Graphics/picture"));
        fragments.add(new Fragment("UI/menuwindow"));
        fragments.add(new Fragment("UI/textwindow"));
        fragments.add(new Fragment("UI/title"));
        fragments.add(new Fragment("UI/number"));
        fragments.add(new Fragment("UI/bignumber"));
        fragments.add(new Fragment("UI/gauge"));
        fragments.add(new Fragment("UI/line"));
        fragments.add(new Fragment("UI/risecursor"));
        fragments.add(new Fragment("UI/mapcursor"));
        fragments.add(new Fragment("UI/pagecursor"));
        fragments.add(new Fragment("UI/selectcursor"));
        fragments.add(new Fragment("UI/scrollcursor"));
        fragments.add(new Fragment("UI/panel"));
        fragments.add(new Fragment("UI/faceframe"));
        fragments.add(new Fragment("UI/screenframe"));
        fragments.add(new Fragment("Audio/music"));
        fragments.add(new Fragment("Audio/sound"));
        fragments.add(new Fragment("Fonts"));
        fragments.add(new Fragment("Video"));
    }

    public Resource getProjectResource() {
        Resource projectResource = new Resource();
        projectResource.setPath(".");
        projectResource.setName("project");
        projectResource.setSuffix("no-srpgs");
        projectResource.setBegin(this.getProjectBegin());
        projectResource.setSize(this.getProjectSize());
        projectResource.setEnd(this.getProjectBegin() + this.getProjectSize() - 1);
        return projectResource;
    }

}

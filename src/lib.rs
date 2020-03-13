pub mod command;
pub mod info;

pub const DIRECTORIES_LEN: usize = 35;
pub const DIRECTORIES: [&str; DIRECTORIES_LEN] = [
    "Graphics/mapchip",
    "Graphics/charchip",
    "Graphics/face",
    "Graphics/icon",
    "Graphics/motion",
    "Graphics/effect",
    "Graphics/weapon",
    "Graphics/bow",
    "Graphics/thumbnail",
    "Graphics/battleback",
    "Graphics/eventback",
    "Graphics/screenback",
    "Graphics/worldmap",
    "Graphics/eventstill",
    "Graphics/charillust",
    "Graphics/picture",
    "UI/menuwindow",
    "UI/textwindow",
    "UI/title",
    "UI/number",
    "UI/bignumber",
    "UI/gauge",
    "UI/line",
    "UI/risecursor",
    "UI/mapcursor",
    "UI/pagecursor",
    "UI/selectcursor",
    "UI/scrollcursor",
    "UI/panel",
    "UI/faceframe",
    "UI/screenframe",
    "Audio/music",
    "Audio/sound",
    "Fonts",
    "Video"
];

pub const HEADER_JPEG: [u8; 3] = [0xFF, 0xD8, 0xFF];
pub const HEADER_PNG: [u8; 4] = [0x89, 0x50, 0x4E, 0x47];
pub const HEADER_BMP: [u8; 2] = [0x42, 0x4D];
pub const HEADER_MP3: [u8; 4] = [0x49, 0x44, 0x33, 0x03];
pub const HEADER_WAV: [u8; 5] = [0x52, 0x49, 0x46, 0x46, 0x72];
pub const HEADER_OGG: [u8; 3] = [0x4F, 0x67, 0x67];
pub const HEADER_MID: [u8; 4] = [0x4D, 0x54, 0x68, 0x64];

pub const DIRECTORIES: &'static [&'static str] = &[
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

pub mod command {
    use clap::{App, Arg};

    pub const UNPACK: &str = "unpack";
    pub const KEYGEN: &str = "keygen";

    pub fn unpack_command<'a>() -> App<'a> {
        App::new(UNPACK)
            .version("0.1")
            .about("to unpack data.dts")
            .arg(
                Arg::with_name("target")
                    .value_name("FILE")
                    .help("the target file to decrypt")
                    .index(1)
                    .takes_value(true)
                    .required(true)
            )
            .arg(
                Arg::with_name("key")
                    .short('k')
                    .long("key")
                    .value_name("FILE")
                    .help("the key of decryption")
                    .default_value("key.bin")
                    .takes_value(true)
            )
            .arg(
                Arg::with_name("output")
                    .short('o')
                    .long("output")
                    .value_name("PATH")
                    .help("the path of unpack")
                    .default_value("./output")
                    .takes_value(true)
            )
    }

    pub fn keygen_command<'a>() -> App<'a> {
        App::new(KEYGEN)
            .version("0.1")
            .about("to generate the key (generate key need encrypted and decrypted from same project.)")
            .arg(
                Arg::with_name("decrypted")
                    .value_name("DECRYPTED")
                    .help("the decrypted")
                    .index(1)
                    .takes_value(true)
                    .required(true)
            )
            .arg(
                Arg::with_name("encrypted")
                    .value_name("ENCRYPTED")
                    .help("the encrypted")
                    .index(2)
                    .takes_value(true)
                    .required(true)
            )
            .arg(
                Arg::with_name("output")
                    .short('o')
                    .long("output")
                    .value_name("FILE")
                    .help("the path of unpack")
                    .default_value("key.bin")
                    .takes_value(true)
            )
    }
}


pub mod info {
    use serde::{Deserialize, Serialize};

    use crate::DIRECTORIES;

    #[derive(Serialize, Deserialize, Debug)]
    pub struct DataInfo {
        pub cryptic: bool,
        pub version: u32,
        pub project_begin: u32,
        pub project_size: u32,
        pub fragments: Vec<FragmentInfo>,
    }

    #[derive(Serialize, Deserialize, Debug)]
    pub struct FragmentInfo {
        pub path: String,
        pub begin: u32,
        pub end: u32,
        pub size: u32,
        pub resource_group: Vec<ResourceGroup>,
    }

    #[derive(Serialize, Deserialize, Debug)]
    pub struct ResourceGroup {
        pub begin: u32,
        pub end: u32,
        pub path: String,
        pub resources: Vec<Resource>,
    }

    #[derive(Serialize, Deserialize, Debug)]
    pub struct Resource {
        pub begin: u32,
        pub end: u32,
        pub size: u32,
        pub name: String,
        pub suffix: String,
    }

    impl DataInfo {
        pub fn new(cryptic: bool, version: u32, project_begin: u32, project_size: u32) -> DataInfo {
            let fragments: Vec<_> = DIRECTORIES.iter()
                .map(|&dir| FragmentInfo::new(dir.to_string(), 0, 0, 0))
                .collect();

            DataInfo {
                cryptic,
                version,
                project_begin,
                project_size,
                fragments,
            }
        }
    }

    impl FragmentInfo {
        pub fn new(path: String, begin: u32, end: u32, size: u32) -> FragmentInfo {
            FragmentInfo {
                path,
                begin,
                end,
                size,
                resource_group: Vec::new(),
            }
        }
    }
}


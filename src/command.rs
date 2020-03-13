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
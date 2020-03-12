extern crate clap;

use std::{fs, io};
use std::fs::File;
use std::io::{BufReader, Read, Seek, SeekFrom};
use std::path::Path;

use clap::{App, AppSettings};

use srpg_studio_extractor::{command, DIRECTORIES};
use srpg_studio_extractor::info::{DataInfo, ResourceGroup};
use std::convert::TryInto;

fn main() -> Result<(), io::Error> {
    let unpack = command::unpack_command();
    let keygen = command::keygen_command();
    let sub_commands = vec![unpack, keygen];
    let app = App::new("srpg studio extractor")
        .version("0.1")
        .about("extractor of SRPG-Studio archive file")
        .author("gwsl <godoway@gmail.com>")
        .subcommands(sub_commands)
        .setting(AppSettings::ArgRequiredElseHelp);

    let app_matches = app.get_matches();
    match app_matches.subcommand() {
        (command::UNPACK, Some(sub)) => {
            let target = sub.value_of("target").unwrap();
            let key = sub.value_of("key").unwrap();
            let output = sub.value_of("output").unwrap();
            println!("target: {} | key: {} | output: {}", target, key, output);

            let path = Path::new(target);
            let file = File::open(path).unwrap();
            let mut data = analyze_header(&file);
            analyze_fragments(&file, &mut data);

            println!("{}", serde_json::to_string(&data).unwrap());
            create_unpack_dir(output)?;
        }
        (command::KEYGEN, Some(sub)) => {
            let decrypted = sub.value_of("decrypted").unwrap();
            let encrypted = sub.value_of("encrypted").unwrap();
            let output = sub.value_of("output").unwrap();
            println!("target: {} | key: {} | output: {}", decrypted, encrypted, output)
        }
        _ => println!("Some other subcommand was used")
    }

    Ok(())
}

fn create_unpack_dir(output: &str) -> Result<(), io::Error> {
    let dir = Path::new(output);
    if !dir.exists() {
        fs::create_dir(dir)?;
    }

    let paths: Vec<_> = DIRECTORIES.iter()
        .map(|s| dir.join(s))
        .filter(|p| !p.exists())
        .collect();

    for path in paths.iter() {
        let path_name = path.to_str().unwrap();
        match fs::create_dir_all(path) {
            Ok(_) => println!("{} create success.", path_name),
            Err(_) => println!("{} failed to create", path_name)
        }
    }
    Ok(())
}

fn analyze_header(file: &File) -> DataInfo {
    let len = file.metadata().unwrap().len();

    let mut reader = BufReader::new(file);
    let bytes4 = &mut [0u8; 4];

    reader.read(bytes4).unwrap();
    let signatures = String::from_utf8_lossy(bytes4);
    if "SDTS" != signatures {
        panic!("it is not a srpg studio data file.")
    }
    reader.read(bytes4).unwrap();
    let cryptic = u32::from_le_bytes(*bytes4) == 1;

    reader.read(bytes4).unwrap();
    let version = u32::from_le_bytes(*bytes4);

    reader.seek(SeekFrom::Current(8)).unwrap();
    reader.read(bytes4).unwrap();
    let project_begin = u32::from_le_bytes(*bytes4) + 168;
    let project_size = len as u32 - project_begin + 1;

    let mut base = DataInfo::new(cryptic, version, project_begin, project_size);

    let index = &mut [0u32; 36];
    for i in 0..36 {
        reader.read(bytes4).unwrap();
        index[i] = u32::from_le_bytes(*bytes4) + 168
    }
    let fragments = &mut base.fragments;
    for i in 0..35 {
        let begin = index[i];
        let end = index[i + 1];
        fragments[i].begin = begin;
        fragments[i].end = if begin == end { end } else { end - 1 };
        fragments[i].size = end - begin;
        if fragments[i].size == 4 { fragments[i].size = 0 }
    }
    println!(r#"data info:
    length: {}
    cryptic: {}
    version: {}
    project_begin: {}
    project_size: {}
    "#, len, base.cryptic, base.version, base.project_begin, base.project_size);

    base
}

fn analyze_fragments<'a>(file: &'a File, data: &'a mut DataInfo) -> &'a DataInfo {
    let mut reader = BufReader::new(file);
    let bytes4 = &mut [0u8; 4];
    let buff = &mut [0u8; 1024];

    let fragments = &mut data.fragments;
    fragments.iter_mut()
        .filter(|f| f.size != 0)
        .for_each(|f| {
            let position = SeekFrom::Start(f.begin as u64);
            &reader.seek(position).unwrap();

            reader.read(bytes4).unwrap();
            let count = if f.size == 0 {
                0
            } else {
                u32::from_le_bytes(*bytes4)
            };
            println!("'{}' resource group count: {}", f.path, count);

            let mut positions: Vec<u32> = Vec::new();
            let info_len = count * 4 + 4;
            let mut read = reader.read(buff).unwrap();
            let mut has_read = 0u32;
            while has_read < info_len {
                has_read += read as u32;
                for i in (0..read).step_by(4) {
                    let slice = &buff[i..(i + 4)];
                    let tmp: [u8; 4] = slice.try_into().unwrap();
                    let position = u32::from_le_bytes(tmp);
                    positions.push(position);
                    if positions.len() == count as usize { break; }
                }
                read = reader.read(buff).unwrap();
            }
            positions.push(f.end + 1);

            let mut resource_groups: Vec<ResourceGroup> = Vec::new();
            for i in 0..(positions.len() - 1) {
                let resource_group = ResourceGroup {
                    begin: positions[i] + f.begin,
                    end: positions[i + 1],
                    name: "".to_string(),
                    path: String::from(&f.path),
                };
                resource_groups.push(resource_group);
            }
            f.resource_group = resource_groups;
        });
    data
}

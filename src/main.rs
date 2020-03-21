extern crate clap;

use std::{fs, panic};
use std::fs::{File, OpenOptions};
use std::io::{BufReader, Read, Seek, SeekFrom, Error, Write};
use std::path::Path;

use clap::{App, AppSettings};

use srpg_studio_extractor::{command, DIRECTORIES, DIRECTORIES_LEN};
use srpg_studio_extractor::info::{DataInfo, ResourceGroup, Resource};
use std::convert::TryInto;

fn main() -> Result<(), Error> {
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
            let mut file = OpenOptions::new().read(true).write(true).open(path).unwrap();
            let mut data = analyze_header(&file);
            analyze_fragments(&file, &mut data);
            println!("{}", serde_json::to_string(&data).unwrap());

            if data.cryptic {
                let key_path = Path::new(key);
                let key = OpenOptions::new().read(true).open(key_path).unwrap();
                decrypt(&mut file, &key, &mut data);
            }

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

fn create_unpack_dir(output: &str) -> Result<(), Error> {
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

    let mut reader = BufReader::with_capacity(256, file);
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

    let index = &mut [0u32; DIRECTORIES_LEN + 1];
    for i in 0..DIRECTORIES_LEN + 1 {
        reader.read(bytes4).unwrap();
        index[i] = u32::from_le_bytes(*bytes4) + 168
    }
    let fragments = &mut base.fragments;
    for i in 0..DIRECTORIES_LEN {
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

    let mut resource_groups: Vec<_> = data.fragments.iter_mut()
        .filter(|f| f.size != 0)
        .map(|f| {
            let position = SeekFrom::Start(f.begin as u64);
            reader.seek(position).unwrap();

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
                    path: f.path.to_owned(),
                    resources: vec![],
                };
                resource_groups.push(resource_group);
            }
            f.resource_group = resource_groups;
            f
        })
        .flat_map(|f| &mut f.resource_group)
        .collect();
    resource_groups.iter_mut()
        .for_each(|rg| {
            let mut position = SeekFrom::Start(rg.begin as u64);

            reader.seek(position).unwrap();
            reader.read(bytes4).unwrap();
            let name_len = u32::from_le_bytes(*bytes4) as usize;

            let mut name_bytes = vec![0; name_len as usize];
            reader.read_exact(&mut name_bytes).unwrap();
            let (_, u16_bytes, _) = unsafe {
                name_bytes[..(name_len - 2)].align_to::<u16>()
            };
            let name = String::from_utf16_lossy(u16_bytes);

            position = SeekFrom::Current(8);
            reader.seek(position).unwrap();
            reader.read(bytes4).unwrap();
            let resource_count = u32::from_le_bytes(*bytes4);

            let mut resources: Vec<Resource> = Vec::new();
            for i in 0..resource_count {
                reader.read(bytes4).unwrap();
                let size = u32::from_le_bytes(*bytes4);
                let name = if i > 0 {
                    let c = (96u8 + i as u8) as char;
                    format!("{}-{}", name, c)
                } else { name.to_owned() };

                let resource = Resource {
                    begin: 0,
                    end: 0,
                    size,
                    name,
                    suffix: "unknown".to_string(),
                };
                resources.push(resource);
            }
            let mut p = reader.seek(SeekFrom::Current(0)).unwrap() as u32;
            for i in 0..(resource_count as usize) {
                let mut r = &mut resources[i];
                r.begin = p;
                r.end = r.begin + r.size - 1;
                p = p + r.size;
            }
            rg.resources = resources;
        });
    data
}

fn decrypt(target: &mut File, key: &File, data: &mut DataInfo) {
    let mut key_reader = BufReader::new(key);

    let source_buf = &mut [0u8; 1024];
    let key_buf = &mut [0u8; 1024];

    let fragments = &mut data.fragments;
    let mut resources: Vec<_> = fragments.iter_mut()
        .filter(|f| f.size != 0)
        .flat_map(|f| &f.resource_group)
        .flat_map(|rg| &rg.resources)
        .collect();

    let resource = Resource {
        begin: data.project_begin,
        end: data.project_begin + data.project_size - 1,
        size: data.project_size,
        name: "project".to_owned(),
        suffix: "unknown".to_owned(),
    };
    resources.push(&resource);

    resources.iter_mut().for_each(|res| {
        let mut had_read = 0;
        let mut had_write = 0;
        target.seek(SeekFrom::Start(res.begin as u64)).unwrap();
        key_reader.seek(SeekFrom::Start(0)).unwrap();

        let mut src_read = target.read(source_buf).unwrap();
        key_reader.read(key_buf).unwrap();

        while src_read > 0 {
            had_read += src_read;
            for (b1, b2) in source_buf.iter_mut().zip(key_buf.iter()) {
                *b1 ^= b2;
            }
            let size = res.size as usize;
            let need_write = if had_read > size {
                size - (had_read - 1024)
            } else {
                src_read
            };
            let write_slice = &source_buf[0..need_write];

            target.seek(SeekFrom::Current(-(src_read as i64))).unwrap();
            target.write(write_slice).unwrap();

            had_write += need_write;
            if had_write == size { return; }

            src_read = target.read(source_buf).unwrap();
            key_reader.read(key_buf).unwrap();
        }
        println!("{} had decrypted", res.name)
    });

    target.seek(SeekFrom::Start(4)).unwrap();
    target.write(&[0, 0, 0, 1]).unwrap();
    println!("decrypted!!", )
}
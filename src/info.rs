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
            resource_group: vec![],
        }
    }
}
# SRPG Studio Extractor

A extractor to extract data.dts from SPRG Studio archive file


## Usage 


unpack:
```
java -jar srpgstudio-extractor-0.1.jar --target targetFile --unpack [--output outputPath]
```

decrypt or encrypt
```
java -jar srpgstudio-extractor-0.1.jar --target targetFile [--key keyFile]
```

generate key need two file(big enough) which one is encrypt and other one is decrypt:
```
java -jar srpgstudio-extractor-0.1.jar --generate encryptFile decryptFile [--output outputPath]
```

## Unimplemented 
* export text
* translation patch
* ~~decompile to .srpg~~


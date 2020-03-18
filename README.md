# Archiver
Compresses files and folder into multiple files

## Structure

The project consists of 2 modules `cli` and `core`. `core` module contains the archive API and its default implementation (zip). The `cli` module allows to a user to perform archiving operations through command line interface.

## Building

Invoke `gradle build` to build and generate jar files. Jar files can be found under `build/libs/` of each module. 

To run the test cases, do `gradle test`.

## Using the CLI

CLI jar can be found in `cli/build/libs` after building. To get started, invoke `java -jar cli-{version}.jar -h`. This will output a brief summary on how to use the CLI.

Usual commands:

Compress
`java -jar cli-1.0.0-SNAPSHOT.jar compress -i {inputDir} -o {outputDir}`

Decompress:
`java -jar cli-1.0.0-SNAPSHOT.jar compress -i {inputDir} -o {outputDir}`

Max file size can be configured by passing `-m {sizeInBytes}`.

## Extending

To implement your own archiver, create a class and implement `com.danielgomez.archiver.Archiver`. The CLI module allows you to use your own archiver via `ServiceLoader`. Just add the class name under `cli/src/main/resources/META-INF/services/com.danielgomez.archiver.Archiver` and rebuild the jar.
Pass the parameter `-a {archiverName}` to use your own where {archiverName} is the lowercase name of the class (excluding `Archiver` suffix). Example, if your class name is `RarArchiver` the archiver name is `rar`.
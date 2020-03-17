package com.danielgomez.archiver;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class ZipTestUtils {

    public static FileSystem openZipFileSystem( Path zip ) {
        try {
            URI uri = URI.create( "jar:" + zip.toUri() );
            return FileSystems.newFileSystem( uri, Collections.emptyMap() );
        } catch ( IOException e ) {
            throw new RuntimeException( "Unable to open zip filesystem for " + zip );
        }
    }

    public static List<Path> list( FileSystem zipFileSystem, String path ) {
        try {
            Path root = zipFileSystem.getPath( path );
            try ( Stream<Path> children = Files.list( root ) ) {
                return children.collect( Collectors.toList() );
            }
        } catch ( IOException e ) {
            throw new RuntimeException( "Unable to retrieve children of " + path );
        }
    }

    public static List<Path> list( FileSystem zipFileSystem ) {
        return list( zipFileSystem, "/" );
    }

}

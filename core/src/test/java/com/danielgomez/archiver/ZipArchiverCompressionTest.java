package com.danielgomez.archiver;

import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import com.google.common.jimfs.Jimfs;

public class ZipArchiverCompressionTest {

    @Test
    @DisplayName( "Compressing throws exception when input path does not exist" )
    public void compressNonExistingInputPath( TestInfo testInfo ) {
        Path input = Paths.get( testInfo.getDisplayName() );
        ZipArchiver archiver = new ZipArchiver();
        assertThrows( FileNotFoundException.class, () -> archiver.compress( CompressionOptionsBuilder.create()
                .input( input )
                .output( mock( Path.class ) )
                .build() ) );
    }

    @Test
    @DisplayName( "Compressing throws exception when input path is not a directory" )
    public void compressInputPathNotDirectory() throws IOException {
        try ( FileSystem fileSystem = Jimfs.newFileSystem() ) {
            Path inputFile = fileSystem.getPath( "inputFile" );
            Files.createFile( inputFile );

            ZipArchiver archiver = new ZipArchiver();
            IllegalArgumentException exception = assertThrows( IllegalArgumentException.class,
                    () -> archiver.compress( CompressionOptionsBuilder.create()
                            .input( inputFile )
                            .output( mock( Path.class ) )
                            .build() ) );
            assertTrue( exception.getMessage().contains( inputFile + "' is not a directory" ) );
        }
    }

    @Test
    @DisplayName( "Compressing throws exception when input path is empty" )
    public void compressEmptyInputPath() throws IOException {
        try ( FileSystem fileSystem = Jimfs.newFileSystem() ) {
            Path inputDir = fileSystem.getPath( "input" );
            Files.createDirectories( inputDir );

            ZipArchiver archiver = new ZipArchiver();
            NoSuchFileException exception = assertThrows( NoSuchFileException.class,
                    () -> archiver.compress( CompressionOptionsBuilder.create()
                            .input( inputDir )
                            .output( mock( Path.class ) )
                            .build() ) );
            assertTrue( exception.getMessage().contains( inputDir + "' is empty" ) );
        }
    }

    @Test
    @DisplayName( "Compressing throws exception when output path is not a directory" )
    public void compressOutputPathNotDirectory() throws IOException {
        try ( FileSystem fileSystem = Jimfs.newFileSystem() ) {
            Path inputDir = fileSystem.getPath( "inputDir" );
            Files.createDirectories( inputDir );
            Files.createFile( inputDir.resolve( "file" ) );
            Path outputFile = fileSystem.getPath( "outputFile" );
            Files.createFile( outputFile );

            ZipArchiver archiver = new ZipArchiver();
            IllegalArgumentException exception = assertThrows( IllegalArgumentException.class,
                    () -> archiver.compress( CompressionOptionsBuilder.create()
                            .input( inputDir )
                            .output( outputFile )
                            .build() ) );
            assertTrue( exception.getMessage().contains( outputFile + "' is not a directory" ) );
        }
    }

    @Test
    @DisplayName( "Compressing generates a zip file with files inside it" )
    public void compressWithFiles() throws IOException {
        try ( FileSystem fileSystem = Jimfs.newFileSystem() ) {
            Path input = fileSystem.getPath( "input" );
            Files.createDirectories( input );

            int numFilesToCreate = 2;
            for ( int i = 0; i < numFilesToCreate; i++ ) {
                Path file = input.resolve( "" + i );
                Files.write( file, ( "" + i ).getBytes(), CREATE_NEW );
            }

            Path output = fileSystem.getPath( "output" );

            ZipArchiver archiver = new ZipArchiver();
            archiver.compress( CompressionOptionsBuilder.create()
                    .input( input )
                    .output( output )
                    .build() );

            FileSystem zipFs = ZipTestUtils.openZipFileSystem( output.resolve( "input.zip" ) );
            List<Path> children = ZipTestUtils.list( zipFs );
            assertEquals( numFilesToCreate, children.size() );

            for ( int i = 0; i < numFilesToCreate; i++ ) {
                Path child = zipFs.getPath( "" + i );
                assertTrue( Files.isRegularFile( child ) );
                assertArrayEquals( ( "" + i ).getBytes(), Files.readAllBytes( child ) );
            }
        }
    }

    @Test
    @DisplayName( "Compressing multiple files that exceed max file size should split to multiple zip files" )
    public void compressWithMaxFileSize() throws IOException {
        try ( FileSystem fileSystem = Jimfs.newFileSystem() ) {
            Path input = fileSystem.getPath( "input" );
            Files.createDirectories( input );
            int maxFileSize = 1000;

            Path file = input.resolve( "file1" );
            Files.write( file, new byte[900] );

            Path file2 = input.resolve( "file2" );
            Files.write( file2, new byte[300] );

            Path output = fileSystem.getPath( "output" );

            ZipArchiver archiver = new ZipArchiver();
            archiver.compress( CompressionOptionsBuilder.create()
                    .input( input )
                    .output( output )
                    .maxFileSize( maxFileSize )
                    .build() );

            assertTrue( Files.exists( output.resolve( "input.part.0.zip" ) ) );
            assertTrue( Files.size( output.resolve( "input.part.0.zip" ) ) < maxFileSize );

            assertTrue( Files.exists( output.resolve( "input.part.1.zip" ) ) );
            assertTrue( Files.size( output.resolve( "input.part.1.zip" ) ) < maxFileSize );

            assertFalse( Files.exists( output.resolve( "input.part.2.zip" ) ) );
        }
    }

    @Test
    @DisplayName( "Compressing a single file that exceeds max file size should split to multiple zip files" )
    public void compressExceedingIndividualFile() throws IOException {
        try ( FileSystem fileSystem = Jimfs.newFileSystem() ) {
            Path input = fileSystem.getPath( "input" );
            Files.createDirectories( input );
            int maxFileSize = 1000;

            Path file = input.resolve( "file" );
            Files.write( file, new byte[1500] );

            Path output = fileSystem.getPath( "output" );

            ZipArchiver archiver = new ZipArchiver();
            archiver.compress( CompressionOptionsBuilder.create()
                    .input( input )
                    .output( output )
                    .maxFileSize( maxFileSize )
                    .build() );

            assertEquals( 2, Files.list( output ).count() );
            assertTrue( Files.exists( output.resolve( "input.part.0.zip" ) ) );
            assertTrue( Files.size( output.resolve( "input.part.0.zip" ) ) < maxFileSize );

            assertTrue( Files.exists( output.resolve( "input.part.1.zip" ) ) );
            assertTrue( Files.size( output.resolve( "input.part.1.zip" ) ) < maxFileSize );
        }
    }

}

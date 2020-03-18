package com.danielgomez.archiver;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import com.google.common.jimfs.Jimfs;

public class ZipArchiverDecompressionTest {

    @Test
    @DisplayName( "Decompressing throws exception when input path does not exist" )
    public void compressNonExistingInputPath( TestInfo testInfo ) {
        Path input = Paths.get( testInfo.getDisplayName() );
        ZipArchiver archiver = new ZipArchiver();
        assertThrows( FileNotFoundException.class, () -> archiver.decompress( DecompressionOptionsBuilder.create()
                .input( input )
                .output( mock( Path.class ) )
                .build() ) );
    }

    @Test
    @DisplayName( "Decompressing throws exception when input path is not a directory" )
    public void decompressInputPathNotDirectory() throws IOException {
        try ( FileSystem fileSystem = Jimfs.newFileSystem() ) {
            Path inputFile = fileSystem.getPath( "inputFile" );
            Files.createFile( inputFile );

            ZipArchiver archiver = new ZipArchiver();
            IllegalArgumentException exception = assertThrows( IllegalArgumentException.class,
                    () -> archiver.decompress( DecompressionOptionsBuilder.create()
                            .input( inputFile )
                            .output( mock( Path.class ) )
                            .build() ) );
            assertTrue( exception.getMessage().contains( inputFile + "' is not a directory" ) );
        }
    }

    @Test
    @DisplayName( "Decompressing throws exception when input path is empty" )
    public void decompressEmptyInputPath() throws IOException {
        try ( FileSystem fileSystem = Jimfs.newFileSystem() ) {
            Path inputDir = fileSystem.getPath( "input" );
            Files.createDirectories( inputDir );

            ZipArchiver archiver = new ZipArchiver();
            NoSuchFileException exception = assertThrows( NoSuchFileException.class,
                    () -> archiver.decompress( DecompressionOptionsBuilder.create()
                            .input( inputDir )
                            .output( mock( Path.class ) )
                            .build() ) );
            assertTrue( exception.getMessage().contains( inputDir + "' is empty" ) );
        }
    }

    @Test
    @DisplayName( "Decompressing throws exception when output path is not a directory" )
    public void decompressOutputPathNotDirectory() throws IOException {
        try ( FileSystem fileSystem = Jimfs.newFileSystem() ) {
            Path inputDir = fileSystem.getPath( "inputDir" );
            Files.createDirectories( inputDir );
            Files.createFile( inputDir.resolve( "file" ) );
            Path outputFile = fileSystem.getPath( "outputFile" );
            Files.createFile( outputFile );

            ZipArchiver archiver = new ZipArchiver();
            IllegalArgumentException exception = assertThrows( IllegalArgumentException.class,
                    () -> archiver.decompress( DecompressionOptionsBuilder.create()
                            .input( inputDir )
                            .output( outputFile )
                            .build() ) );
            assertTrue( exception.getMessage().contains( outputFile + "' is not a directory" ) );
        }
    }
    
    @Test
    @DisplayName( "Decompress unpacks zip file" )
    public void decompressUnpacksZip( TestInfo testInfo ) throws IOException {
        Path input = ZipTestUtils.getResource( Paths.get( testInfo.getTestMethod().get().getName() ) );
        ZipArchiver archiver = new ZipArchiver();
        try ( FileSystem fileSystem = Jimfs.newFileSystem() ) {
            Path output = fileSystem.getPath( "output" );
            archiver.decompress( DecompressionOptionsBuilder.create()
                    .input( input )
                    .output( output )
                    .build() );
            String content = new String( Files.readAllBytes( output.resolve( "hello/world.txt" ) ) );
            assertEquals( "Hello World", content );
        }
    }

    @Test
    @DisplayName( "Decompress consolidates zip parts" )
    public void decompressConsolidatesZipParts( TestInfo testInfo ) throws IOException {
        Path input = ZipTestUtils.getResource( Paths.get( testInfo.getTestMethod().get().getName() ) );
        ZipArchiver archiver = new ZipArchiver();
        try ( FileSystem fileSystem = Jimfs.newFileSystem() ) {
            Path output = fileSystem.getPath( "output" );
            archiver.decompress( DecompressionOptionsBuilder.create()
                    .input( input )
                    .output( output )
                    .build() );
            String content = new String( Files.readAllBytes( output.resolve( "hello/1.txt" ) ) );
            assertEquals( "1", content );

            content = new String( Files.readAllBytes( output.resolve( "hello/2.txt" ) ) );
            assertEquals( "2", content );
        }
    }

    @Test
    @DisplayName( "Decompress consolidates chunked file" )
    public void decompressConsolidatesChunkedFile( TestInfo testInfo ) throws IOException {
        Path input = ZipTestUtils.getResource( Paths.get( testInfo.getTestMethod().get().getName() ) );
        ZipArchiver archiver = new ZipArchiver();
        try ( FileSystem fileSystem = Jimfs.newFileSystem() ) {
            Path output = fileSystem.getPath( "output" );
            archiver.decompress( DecompressionOptionsBuilder.create()
                    .input( input )
                    .output( output )
                    .build() );
            assertEquals( 1500, Files.readAllBytes( output.resolve( "file" ) ).length );
        }
    }

}

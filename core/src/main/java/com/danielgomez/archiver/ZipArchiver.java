package com.danielgomez.archiver;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements archive operations with zip format
 */
public class ZipArchiver implements Archiver {

    private static Logger LOGGER = LoggerFactory.getLogger( ZipArchiver.class );

    /**
     * Ensures the following conditions:
     * <ul>
     *     <li>Input path exists</li>
     *     <li>Input path is a directory</li>
     *     <li>Input path is not empty</li>
     *     <li>Output path is a directory if it exists</li>
     * </ul>
     * Additionally, it creates the output path if it does not exist.
     *
     * @param options parameters
     * @throws IOException when unable to determine conditions due to IO errors
     */
    private static void checkArguments( IOOptions options ) throws IOException {
        Path inputDir = options.getInput();
        if ( Files.notExists( inputDir ) )
            throw new FileNotFoundException( "Input '" + inputDir + "' does not exist" );
        if ( !Files.isDirectory( inputDir ) )
            throw new IllegalArgumentException( "Input '" + inputDir + "' is not a directory" );
        try ( Stream<Path> children = Files.list( inputDir ) ) {
            if ( !children.findAny().isPresent() )
                throw new NoSuchFileException( "Input '" + inputDir + "' is empty" );
        }

        Path outputDir = options.getOutput();
        if ( Files.exists( outputDir ) ) {
            if ( !Files.isDirectory( outputDir ) )
                throw new IllegalArgumentException( "Output '" + outputDir + "' is not a directory" );
        } else
            Files.createDirectories( outputDir );
    }

    /**
     * Compresses a directory and the files inside it by doing the following processes:
     * <ul>
     *     <li>
     *         Input directory is split into chunks. A chunk is a list of files in which the total file size do not
     *         exceed the maximum file size configured in the compression options. A file may also be chunked if it
     *         exceeds the limit.
     *     </li>
     *     <li>
     *         A corresponding zip file is generated for each chunk. If there is only one chunk, a single zip file is
     *         generated using the name of the input directory. Otherwise, multiple zip files are
     *         generated with a suffix of '.part.{n}' where 'n' is the chunk number. Chunks are written to a zip in a
     *         parallel-manner.
     *     </li>
     * </ul>
     * <p>
     *
     * @param options compression configuration
     * @throws IOException when compression fails due to IO errors
     */
    @Override
    public void compress( CompressionOptions options ) throws IOException {
        checkArguments( options );
        Path inputDir = options.getInput();
        Path outputDir = options.getOutput();
        Path output = outputDir.resolve( inputDir.getFileName() + ".zip" );

        Path tempDir = Files.createTempDirectory( "compress-" ).resolve( options.getInput().getFileName().toString() );
        List<List<Path>> chunked = chunk( inputDir, options, tempDir );
        if ( chunked.size() == 1 ) {
            writeToZip( chunked.get( 0 ), output, options, tempDir );
        } else {
            IntStream.range( 0, chunked.size() )
                    .parallel()
                    .forEach( i -> {
                        try {
                            Path zipFile = partFile( output, "" + i );
                            writeToZip( chunked.get( i ), zipFile, options, tempDir );
                        } catch ( IOException e ) {
                            sneakyThrow( e );
                        }
                    } );
        }
        if ( Files.notExists( tempDir ) )
            return;

        try ( Stream<Path> walk = Files.list( tempDir ) ) {
            walk.forEach( p -> {
                try {
                    Files.delete( p );
                } catch ( IOException e ) {
                    sneakyThrow( e );
                }
            } );
        }
        Files.delete( tempDir );
    }

    @Override
    public void decompress( DecompressionOptions options ) throws IOException {
        checkArguments( options );
        Path inputDir = options.getInput();
        Path outputDir = options.getOutput();

        List<Path> inputFiles = Files.list( inputDir ).sorted()
                .filter( path -> path.toString().endsWith( ".zip" ) )
                .collect( Collectors.toList() );
        if ( inputFiles.size() <= 0 )
            throw new IllegalArgumentException( "Input directory '" + inputDir + " is empty" );

        for ( Path inputFile : inputFiles ) {
            ZipInputStream zis = new ZipInputStream( Files.newInputStream( inputFile ) );
            ZipEntry zipEntry = zis.getNextEntry();
            while ( zipEntry != null ) {
                Path outputFile = outputDir.resolve( zipEntry.getName() );
                if ( zipEntry.isDirectory() ) {
                    Files.createDirectories( outputFile );
                } else {
                    outputFile = unpartFile( outputFile );
                    OutputStream fos = Files.newOutputStream( outputFile, CREATE, APPEND );
                    byte[] buffer = new byte[options.getBufferSize()];
                    int len;
                    while ( ( len = zis.read( buffer ) ) > 0 ) {
                        fos.write( buffer, 0, len );
                    }
                    fos.close();
                }
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();
        }
    }

    /**
     * Writes a list of path to a zip file
     *
     * @param contents paths to write
     * @param zipFile  the zip file
     * @param options  IO options
     */
    private static void writeToZip( List<Path> contents, Path zipFile, IOOptions options, Path tempDir )
            throws IOException {
        try ( ZipOutputStream zos = new ZipOutputStream( Files.newOutputStream( zipFile ) ) ) {
            for ( Path path : contents ) {
                Path transformed;
                if ( path.getFileName().toString().contains( ".part." ) )
                    transformed = tempDir.relativize( path );
                else {
                    transformed = options.getInput().relativize( path );
                }
                String fileName = transformed.toString();
                if ( Files.isRegularFile( path ) ) {
                    try ( InputStream fis = Files.newInputStream( path ) ) {
                        ZipEntry zipEntry = new ZipEntry( fileName );
                        zos.putNextEntry( zipEntry );
                        byte[] buffer = new byte[options.getBufferSize()];
                        int bufferReadLength;
                        while ( ( bufferReadLength = fis.read( buffer ) ) >= 0 ) {
                            zos.write( buffer, 0, bufferReadLength );
                        }
                        zos.closeEntry();
                    }
                    LOGGER.debug( "Written file={}", fileName );
                } else {
                    fileName += "/";
                    zos.putNextEntry( new ZipEntry( fileName ) );
                    zos.closeEntry();
                    LOGGER.debug( "Written directory={}", fileName );
                }
            }
        }
    }

    private static <E extends Throwable> void sneakyThrow( Throwable e ) throws E {
        throw ( E ) e;
    }

    private static List<List<Path>> chunk( Path dir, CompressionOptions options, Path tempDir ) throws IOException {
        ChunkingFileVisitor visitor = new ChunkingFileVisitor( options, tempDir );
        Files.walkFileTree( dir, visitor );
        return visitor.getChunks();
    }

    private static Path partFile( Path path, String partNumber ) {
        String fileName = path.getFileName().toString();
        if ( fileName.contains( "." ) ) {
            int extensionIndex = fileName.lastIndexOf( "." );
            String baseName = fileName.substring( 0, extensionIndex );
            String extension = fileName.substring( extensionIndex );
            return path.getParent().resolve( baseName + ".part." + partNumber + extension );
        }
        return path.getParent().resolve( fileName + ".part." + partNumber );
    }

    private static Path unpartFile( Path path ) {
        String fileName = path.getFileName().toString();
        if ( fileName.matches( ".*part.[0-9]+.*" ) ) {
            String baseName = fileName.substring( 0, fileName.indexOf( ".part" ) );
            String extension = fileName.substring( fileName.lastIndexOf( "." ) );
            return path.getParent().resolve( baseName + extension );
        }
        return path;
    }

    /**
     * A file visitor that chunks file paths such that each chunk does not exceed max file size. This class asserts that
     * a single path does not exceed max file size, otherwise an {@link IllegalArgumentException} is thrown.
     */
    private static class ChunkingFileVisitor extends SimpleFileVisitor<Path> {

        private List<List<Path>> chunks = new ArrayList<>();

        private List<Path> currentChunk = new ArrayList<>();

        private long currentChunkSize = 0;

        private CompressionOptions options;

        private final Path tempDir;

        public ChunkingFileVisitor( CompressionOptions options, Path tempDir ) {
            this.options = options;
            this.tempDir = tempDir;
        }

        public List<List<Path>> getChunks() {
            if ( currentChunk.size() > 0 ) {
                chunks.add( currentChunk );
                currentChunk = Collections.emptyList();
            }
            return chunks;
        }

        private long getMaxFileSize() {
            return options.getMaxFileSize();
        }

        @Override
        public FileVisitResult visitFile( Path file, BasicFileAttributes attrs ) throws IOException {
            if ( file.getFileName().toString().equals( ".DS_Store" ) )
                return FileVisitResult.CONTINUE;
            super.visitFile( file, attrs );
            long size = Files.size( file );
            if ( getMaxFileSize() > 0 && size + currentChunkSize > getMaxFileSize() ) {
                if ( size > getMaxFileSize() ) {
                    for ( Path part : refine( file, size, tempDir ) ) {
                        visitFile( part, attrs );
                    }
                    return FileVisitResult.CONTINUE;
                }
                chunks.add( currentChunk );
                currentChunk = new ArrayList<>();
                currentChunkSize = 0;
                addToChunk( file, size );
            } else {
                addToChunk( file, size );
                LOGGER.trace( "File '{}' added on chunk '{}'", file, chunks.size() );
            }
            return FileVisitResult.CONTINUE;
        }

        private void addToChunk( Path file, long size ) {
            currentChunk.add( file );
            currentChunkSize += size;
            LOGGER.trace( "'{}' added on chunk '{}'", file, chunks.size() );
        }

        @Override
        public FileVisitResult preVisitDirectory( Path dir, BasicFileAttributes attrs ) throws IOException {
            if ( dir.equals( options.getInput() ) )
                return FileVisitResult.CONTINUE;

            super.preVisitDirectory( dir, attrs );
            addToChunk( dir, 0 );
            return FileVisitResult.CONTINUE;
        }

        static void readWrite( InputStream raf, OutputStream bw, long numBytes ) throws IOException {
            byte[] buf = new byte[( int ) numBytes];
            int val = raf.read( buf );
            if ( val != -1 ) {
                bw.write( buf );
            }
        }

        /**
         * Splits a file into multiple files such that each part of the file does not exceed max file size
         *
         * @param path the file to split
         * @param size the size of the file
         * @return list of parts that were generated
         * @throws IOException when splitting fails
         */
        private List<Path> refine( Path path, long size, Path tempDir ) throws IOException {
            List<Path> createdParts = new ArrayList<>();
            long parts = size / getMaxFileSize();
            long sizePerPart = getMaxFileSize();
            long remainingSize = size % getMaxFileSize();
            int bufferSize = 1024;

            try ( InputStream is = Files.newInputStream( path ) ) {
                for ( int i = 0; i < parts; i++ ) {
                    Path part = tempDir.resolve( options.getInput().relativize( partFile( path, "" + i ) ).toString() );
                    Files.createDirectories( part.getParent() );
                    OutputStream bw = Files.newOutputStream( part );
                    if ( sizePerPart > bufferSize ) {
                        long numReads = sizePerPart / bufferSize;
                        long numRemainingRead = sizePerPart % bufferSize;
                        for ( int j = 0; j < numReads; j++ ) {
                            readWrite( is, bw, bufferSize );
                        }
                        if ( numRemainingRead > 0 ) {
                            readWrite( is, bw, numRemainingRead );
                        }
                    } else {
                        readWrite( is, bw, sizePerPart );
                    }
                    createdParts.add( part );
                    bw.close();
                }
                if ( remainingSize > 0 ) {
                    Path part = tempDir.resolve( options.getInput().relativize( partFile( path, "" + parts ) ).toString() );
                    try ( OutputStream bw = Files.newOutputStream( part ) ) {
                        readWrite( is, bw, remainingSize );
                        createdParts.add( part );
                    }
                }
            }
            return createdParts;
        }

    }

}

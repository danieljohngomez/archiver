package com.danielgomez.archiver;

import java.io.IOException;
import java.util.Iterator;
import java.util.ServiceLoader;

import com.beust.jcommander.JCommander;

public final class Cli {

    public static void main( String[] args ) throws IOException {
        ArchiverArgs archiverArgs = new ArchiverArgs();
        CompressionArgs compressionArgs = new CompressionArgs();
        DecompressionArgs decompressionArgs = new DecompressionArgs();
        JCommander commander = JCommander.newBuilder()
                .addObject( archiverArgs )
                .addCommand( "compress", compressionArgs )
                .addCommand( "decompress", decompressionArgs )
                .build();

        commander.parse( args );
        if ( archiverArgs.isHelp() ) {
            commander.usage();
            return;
        }

        Archiver archiver = loadArchiver(archiverArgs.getArchiver());
        if ( "compress".equals( commander.getParsedCommand() ) )
            compress( archiver, compressionArgs );
        else if ( "decompress".equals( commander.getParsedCommand() ) )
            decompress( archiver, decompressionArgs );
    }

    private static Archiver loadArchiver( String archiver ) {
        ServiceLoader<Archiver> archivers = ServiceLoader.load( Archiver.class );
        Iterator<Archiver> iterator = archivers.iterator();
        while ( iterator.hasNext() ) {
            Archiver arc = iterator.next();
            String name = arc.getClass().getSimpleName();
            if ( name.endsWith( "Archiver" ) )
                name = name.substring( 0, name.indexOf( "Archiver" ) );
            String type = name.toLowerCase();
            if ( type.equals( archiver ) )
                return arc;
        }
        throw new IllegalArgumentException( "Unable to find '" + archiver + "' archiver" );
    }

    private static void compress( Archiver archiver, CompressionArgs args ) throws IOException {
        archiver.compress( CompressionOptionsBuilder.create()
                .input( args.getInput() )
                .output( args.getOutput() )
                .maxFileSize( args.getMaxFileSize() )
                .bufferSize( args.getBufferSize() )
                .build()
        );
    }

    private static void decompress( Archiver archiver, DecompressionArgs args ) throws IOException {
        archiver.decompress( DecompressionOptionsBuilder.create()
                .input( args.getInput() )
                .output( args.getOutput() )
                .bufferSize( args.getBufferSize() )
                .build()
        );
    }

}

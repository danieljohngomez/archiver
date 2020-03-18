package com.danielgomez.archiver;

import java.io.IOException;

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

        if ( "compress".equals( commander.getParsedCommand() ) )
            compress( compressionArgs );
        else if ( "decompress".equals( commander.getParsedCommand() ) )
            decompress( decompressionArgs );
    }

    private static void compress( CompressionArgs args ) throws IOException {
        ZipArchiver zipArchiver = new ZipArchiver();
        zipArchiver.compress( CompressionOptionsBuilder.create()
                .input( args.getInput() )
                .output( args.getOutput() )
                .maxFileSize( args.getMaxFileSize() )
                .bufferSize( args.getBufferSize() )
                .build()
        );
    }

    private static void decompress( DecompressionArgs args ) throws IOException {
        ZipArchiver zipArchiver = new ZipArchiver();
        zipArchiver.decompress( DecompressionOptionsBuilder.create()
                .input( args.getInput() )
                .output( args.getOutput() )
                .bufferSize( args.getBufferSize() )
                .build()
        );
    }

}

package com.danielgomez.archiver;

import java.nio.file.Path;

import com.beust.jcommander.Parameter;

public class DecompressionArgs {

    @Parameter( names = { "-i",
            "--input" }, description = "Input directory where files to decompress are found", required = true )
    private Path input;

    @Parameter( names = { "-o",
            "--output" }, description = "Output directory where decompression results are generated", required = true )
    private Path output;

    @Parameter( names = { "-b",
            "--buffer-size" }, description = "The buffer size when writing files during decompression." )
    private int bufferSize = 1024;

    public Path getInput() {
        return input;
    }

    public Path getOutput() {
        return output;
    }

    public int getBufferSize() {
        return bufferSize;
    }

}
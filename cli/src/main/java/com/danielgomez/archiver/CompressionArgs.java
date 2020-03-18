package com.danielgomez.archiver;

import java.nio.file.Path;

import com.beust.jcommander.Parameter;

public class CompressionArgs {

    @Parameter( names = { "-i",
            "--input" }, description = "Input directory where files to compress are found", required = true )
    private Path input;

    @Parameter( names = { "-o",
            "--output" }, description = "Output directory where compression results are generated", required = true )
    private Path output;

    @Parameter( names = { "-m",
            "--max-file-size" }, description = "The maximum file size of a compressed file expressed in bytes." )
    private long maxFileSize = -1;

    @Parameter( names = { "-b",
            "--buffer-size" }, description = "The buffer size when writing files during compression." )
    private int bufferSize = 1024;

    public Path getInput() {
        return input;
    }

    public Path getOutput() {
        return output;
    }

    public long getMaxFileSize() {
        return maxFileSize;
    }

    public int getBufferSize() {
        return bufferSize;
    }

}
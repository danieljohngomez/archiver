package com.danielgomez.archiver;

import java.nio.file.Path;

/**
 * Basic archiving options
 * <p>
 * See {@link IOOptionsBuilder} for fluent building
 */
class IOOptions implements Options {

    private Path input;

    private Path output;

    private int bufferSize;

    protected IOOptions( Path input, Path output, int bufferSize ) {
        this.input = input;
        this.output = output;
        this.bufferSize = bufferSize;
        if ( this.bufferSize <= 0 )
            throw new IllegalArgumentException( "Buffer size must not be <= 0" );
    }

    /**
     * @return The input file for an archive operation
     */
    public Path getInput() {
        return input;
    }

    /**
     * @return The output file for an archive operation
     */
    public Path getOutput() {
        return output;
    }

    /**
     * @return The buffer size when writing files during compression. Value is always > 0.
     */
    public int getBufferSize() {
        return bufferSize;
    }
}

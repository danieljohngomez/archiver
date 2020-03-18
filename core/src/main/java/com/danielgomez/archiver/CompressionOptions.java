package com.danielgomez.archiver;

import java.nio.file.Path;

/**
 * Contains configuration for compression
 * <p>
 * See {@link CompressionOptionsBuilder} for fluent building
 */
public class CompressionOptions extends IOOptions {

    private long maxFileSize;

    public CompressionOptions( Path input, Path output, int bufferSize, long maxFileSize ) {
        super( input, output, bufferSize );
        this.maxFileSize = maxFileSize;
    }

    /**
     * @return The maximum file size (in bytes) of each compression output. Value <= 0 means no limit.
     */
    public long getMaxFileSize() {
        return this.maxFileSize;
    }

}

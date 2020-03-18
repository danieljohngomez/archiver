package com.danielgomez.archiver;

import java.nio.file.Path;

/**
 * Contains configuration for decompression
 * <p>
 * See {@link DecompressionOptionsBuilder} for fluent building
 */
public class DecompressionOptions extends IOOptions {

    protected DecompressionOptions( Path input, Path output, int bufferSize ) {
        super( input, output, bufferSize );
    }
}

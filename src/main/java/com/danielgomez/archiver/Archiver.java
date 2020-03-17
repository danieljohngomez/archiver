package com.danielgomez.archiver;

import java.io.IOException;

/**
 * Responsible for compressing and decompressing a file
 */
interface Archiver {

    /**
     * Generates compressed file(s) from an input directory. An archiver can generate multiple files thus the output
     * path must also be a directory.
     *
     * @param options compression configuration
     * @throws java.nio.file.NoSuchFileException   when the input path does not exist
     * @throws java.nio.file.NotDirectoryException when the input or output path is not a directory
     * @throws IOException                         for other errors during compression
     */
    void compress( CompressionOptions options ) throws IOException;

    /**
     * Returns back the original files that was used for {@link #compress(CompressionOptions)}.
     *
     * @param options decompression configuration
     * @throws java.nio.file.NoSuchFileException   when the input path does not exist
     * @throws java.nio.file.NotDirectoryException when the input/output path is not a directory
     * @throws IOException                         for other errors during decompression
     */
    void decompress( DecompressionOptions options ) throws IOException;
}

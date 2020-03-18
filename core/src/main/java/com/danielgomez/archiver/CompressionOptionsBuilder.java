package com.danielgomez.archiver;

/**
 * Fluent interface for building {@link CompressionOptions}
 */
public class CompressionOptionsBuilder extends IOOptionsBuilder<CompressionOptionsBuilder> {

    private int bufferSize = 1024 * 1024;

    private long maxFileSize = -1;

    private CompressionOptionsBuilder() { super();}

    public static CompressionOptionsBuilder create() {
        return new CompressionOptionsBuilder();
    }

    public CompressionOptionsBuilder bufferSize( int bufferSize ) {
        this.bufferSize = bufferSize;
        return this;
    }

    public CompressionOptionsBuilder maxFileSize( int maxFileSize ) {
        this.maxFileSize = maxFileSize;
        return this;
    }

    @Override
    public CompressionOptions build() {
        return new CompressionOptions( input, output, bufferSize, maxFileSize );
    }

}

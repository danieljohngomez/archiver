package com.danielgomez.archiver;

/**
 * Fluent interface for building {@link DecompressionOptions}
 */
class DecompressionOptionsBuilder extends IOOptionsBuilder<DecompressionOptionsBuilder> {

    private DecompressionOptionsBuilder() { super();}

    public static DecompressionOptionsBuilder create() {
        return new DecompressionOptionsBuilder();
    }

    @Override
    public DecompressionOptions build() {
        return new DecompressionOptions( input, output, bufferSize );
    }

}

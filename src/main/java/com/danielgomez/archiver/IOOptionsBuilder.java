package com.danielgomez.archiver;

import java.nio.file.Path;

/**
 * Fluent interface for building {@link IOOptions}
 */
class IOOptionsBuilder<T extends IOOptionsBuilder<T>> {

    protected Path input;

    protected Path output;

    protected int bufferSize = 1024;

    protected IOOptionsBuilder() {}

    public static IOOptionsBuilder<?> create() { return new IOOptionsBuilder<>(); }

    public T input( Path input ) {
        this.input = input;
        return ( T ) this;
    }

    public T output( Path output ) {
        this.output = output;
        return ( T ) this;
    }

    public T bufferSize( int bufferSize ) {
        this.bufferSize = bufferSize;
        return ( T ) this;
    }

    public IOOptions build() { return new IOOptions( input, output, bufferSize ); }
}

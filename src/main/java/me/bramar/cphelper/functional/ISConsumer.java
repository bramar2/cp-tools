package me.bramar.cphelper.functional;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@FunctionalInterface
public interface ISConsumer {
    void accept(InputStream in) throws IOException;
}

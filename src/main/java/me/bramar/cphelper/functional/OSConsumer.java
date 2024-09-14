package me.bramar.cphelper.functional;

import java.io.IOException;
import java.io.OutputStream;

@FunctionalInterface
public interface OSConsumer {
    void accept(OutputStream out) throws IOException;
}

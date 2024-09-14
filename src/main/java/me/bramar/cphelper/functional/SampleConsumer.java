package me.bramar.cphelper.functional;

import java.io.IOException;

public interface SampleConsumer {
    void accept(String in, String out) throws IOException;
}

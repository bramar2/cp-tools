package me.bramar.cphelper;

import java.io.IOException;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface ContestSite {
    List<String> contestIndices(String contestId) throws IOException;
    void downloadSamples(String contestId, String idx,
                         SampleConsumer onSample,
                         Consumer<String> onError) throws IOException;
}

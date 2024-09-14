package me.bramar.cphelper.site;

import me.bramar.cphelper.CompetitiveProgrammingHelperProgram;
import me.bramar.cphelper.functional.SampleConsumer;
import me.bramar.cphelper.lang.Lang;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

public abstract class ContestSite {
    protected CompetitiveProgrammingHelperProgram main;
    ContestSite(CompetitiveProgrammingHelperProgram main) {
        this.main = main;
    }
    public abstract List<String> contestIndices(String contestId) throws IOException;
    public abstract void downloadSamples(String contestId, String idx,
                         SampleConsumer onSample,
                         Consumer<String> onError) throws IOException;
    public abstract String longName();
    public abstract String shortName();
    public void customLibrary(List<String> args, Lang language) {};
}

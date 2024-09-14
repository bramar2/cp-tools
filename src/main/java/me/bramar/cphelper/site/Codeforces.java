package me.bramar.cphelper.site;

import com.google.gson.JsonParser;
import me.bramar.cphelper.CompetitiveProgrammingHelperProgram;
import me.bramar.cphelper.functional.SampleConsumer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static me.bramar.cphelper.CompetitiveProgrammingHelperProgram.*;
public class Codeforces extends ContestSite {
    private static final String API_STANDINGS = "https://codeforces.com/api/contest.standings?contestId=%s&from=1&count=1";
    private static final String PAGE_PROBLEM = "https://codeforces.com/problemset/problem/%s/%s";

    public Codeforces(CompetitiveProgrammingHelperProgram main) {
        super(main);
    }

    @Override
    public List<String> contestIndices(String contestId) throws IOException {
        System.out.println(ANSI_YELLOW+"Getting contest problems..."+ANSI_RESET);
        System.out.println();
        List<String> indices = new ArrayList<>();
        HttpURLConnection con = (HttpURLConnection) URI.create(API_STANDINGS.formatted(contestId)).toURL().openConnection();
        int res = con.getResponseCode();
        if(200 <= res && res <= 299) {
            try(InputStream in = con.getInputStream();
                InputStreamReader isr = new InputStreamReader(in)) {
                JsonParser.parseReader(isr).getAsJsonObject().getAsJsonObject("result")
                        .getAsJsonArray("problems")
                        .forEach(x -> indices.add(x.getAsJsonObject().get("index").getAsString()));
            }
        }else {
            try(InputStream in = con.getErrorStream()) {
                throw new IllegalArgumentException(ANSI_RED+"Error [Contest ID " + contestId + "]: " + new String(in.readAllBytes())+ANSI_RESET);
            }
        }
        System.out.println(ANSI_YELLOW+"Obtained problem indices: " + ANSI_CYAN + String.join(" ", indices) + ANSI_RESET);
        return indices;
    }

    @Override
    public void downloadSamples(String contestId, String idx,
                               SampleConsumer onSample,
                               Consumer<String> onError) throws IOException {
        HttpURLConnection con = (HttpURLConnection) URI.create(PAGE_PROBLEM.formatted(contestId, idx)).toURL().openConnection();
        con.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.9,id;q=0.8");
        con.setRequestProperty("Connection", "keep-alive");
        con.setRequestProperty("Host", "codeforces.com");
        con.setRequestProperty("Priority", "u=0, i");
        con.setRequestProperty("Sec-Fetch-Dest", "Document");
        con.setRequestProperty("Sec-Fetch-Mode", "navigate");
        con.setRequestProperty("Sec-Fetch-Site", "none");
        con.setRequestProperty("Sec-Fetch-User", "?1");
        con.setRequestProperty("Sec-GPC", "1");
        con.setRequestProperty("Upgrade-Insecure-Requests", "1");
        con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:129.0) Gecko/20100101 Firefox/129.0");
        int res = con.getResponseCode();
        if(200 <= res && res <= 299) {
            try(InputStream in = con.getInputStream()) {
                Document doc = Jsoup.parse(new String(in.readAllBytes()));
                List<String> sampleIn = new ArrayList<>();
                List<String> sampleOut = new ArrayList<>();
                for(Element sample : doc.getElementsByClass("sample-test")) {
                    // input
                    Element inputPre = sample.getElementsByClass("input").getFirst()
                            .getElementsByTag("pre").getFirst();
                    Element outputPre = sample.getElementsByClass("output").getFirst()
                            .getElementsByTag("pre").getFirst();
                    StringBuilder input = new StringBuilder();
                    for(Element child : inputPre.children()) {
                        input.append(child.text()).append('\n');
                    }
                    while(!input.isEmpty() && (input.charAt(input.length()-1) == '\n') || (input.charAt(input.length()-1) == '\r')) {
                        input.setLength(input.length() - 1);
                    }
                    sampleIn.add(input.toString().trim());
                    sampleOut.add(outputPre.text().trim());
                }
                if(sampleIn.isEmpty()) {
                    onError.accept(ANSI_RED+ "Found no samples at problem " + idx + ANSI_RESET);
                }else {
                    for(int i = 0; i < sampleIn.size(); i++) {
                        onSample.accept(sampleIn.get(i), sampleOut.get(i));
                    }
                }
            }
        }else {
            try(InputStream in = con.getErrorStream()) {
                onError.accept("Res code " + res + ": " + new String(in.readAllBytes()));
            }
        }
    }

    @Override
    public String longName() {
        return "codeforces";
    }

    @Override
    public String shortName() {
        return "cf";
    }
}

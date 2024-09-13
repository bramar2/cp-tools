package me.bramar.cphelper;

import com.google.gson.JsonParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.NodeFilter;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;

import static me.bramar.cphelper.Main.*;

public class Atcoder implements ContestSite {

    private static final String API_UNOFFICIAL_CONTEST_PROBLEM = "https://kenkoooo.com/atcoder/resources/contest-problem.json";
    private static final String PAGE_PROBLEM = "https://atcoder.jp/contests/%s/tasks/%s_%s";
    @Override
    public List<String> contestIndices(String contestId) throws IOException {
        System.out.println(ANSI_YELLOW+"Getting contest problems..."+ANSI_RESET);
        System.out.println();
        List<String> indices = new ArrayList<>();
        HttpURLConnection con = (HttpURLConnection) URI.create(API_UNOFFICIAL_CONTEST_PROBLEM).toURL().openConnection();
        con.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
        con.setRequestProperty("Accept-Encoding", "gzip");
        con.setRequestProperty("Cache-Control", "no-cache, max-age=0");
        con.setRequestProperty("User-Agent", "Atcoder-Tool");
        int res = con.getResponseCode();
        if((200 <= res && res <= 299) || res == 304) { // 304 Not Modified
            try(InputStream in = new GZIPInputStream(con.getInputStream());
                InputStreamReader isr = new InputStreamReader(in)) {
                JsonParser.parseReader(isr).getAsJsonArray().forEach((element) -> {
                    if(element.isJsonObject() && element.getAsJsonObject().has("contest_id") &&
                    element.getAsJsonObject().get("contest_id").getAsString().equalsIgnoreCase(contestId)) {
                        indices.add(element.getAsJsonObject().get("problem_index").getAsString());
                    }
                });
            }
        }else {
            try(InputStream in = new GZIPInputStream(con.getErrorStream())) {
                throw new IllegalArgumentException(ANSI_RED+"Error [Contest ID " + contestId + "]: " + new String(in.readAllBytes())+ANSI_RESET);
            }
        }
        indices.sort(Comparator.naturalOrder());
        System.out.println(ANSI_YELLOW+"Obtained problem indices: " + ANSI_CYAN + String.join(" ", indices) + ANSI_RESET);
        return indices;
    }

    @Override
    public void downloadSamples(String contestId, String idx, SampleConsumer onSample, Consumer<String> onError) throws IOException {
        HttpURLConnection con = (HttpURLConnection) URI.create(PAGE_PROBLEM.formatted(contestId, contestId, idx.toLowerCase())).toURL().openConnection();
        con.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.9,id;q=0.8");
        con.setRequestProperty("Connection", "keep-alive");
        con.setRequestProperty("Host", "atcoder.jp");
        con.setRequestProperty("Priority", "u=0, i");
        con.setRequestProperty("Sec-Fetch-Dest", "Document");
        con.setRequestProperty("Sec-Fetch-Mode", "navigate");
        con.setRequestProperty("Sec-Fetch-Site", "none");
        con.setRequestProperty("Sec-Fetch-User", "?1");
        con.setRequestProperty("Sec-GPC", "1");
        con.setRequestProperty("Upgrade-Insecure-Requests", "1");
        con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:129.0) Gecko/20100101 Firefox/130.0");
        int res = con.getResponseCode();
        if(200 <= res && res <= 299) {
            try(InputStream in = con.getInputStream()) {
                Document doc = Jsoup.parse(new String(in.readAllBytes()));
                List<String> sampleIn = new ArrayList<>();
                List<String> sampleOut = new ArrayList<>();
                for(Element sampleSection : doc.getElementById("task-statement").getElementsByClass("lang").getFirst()
                        .child(0).getElementsByTag("section")) {
                    if(sampleSection.getElementsByTag("h3").isEmpty() ||
                            sampleSection.getElementsByTag("pre").isEmpty()) continue;
                    if(!sampleSection.getElementsByTag("pre").getFirst()
                            .getElementsByTag("var").isEmpty())
                        continue;
                    Element sample = sampleSection.getElementsByTag("pre").getFirst();
                    if(sampleIn.size() > sampleOut.size()) {
                        sampleOut.add(sample.text().trim());
                    }else {
                        sampleIn.add(sample.text().trim());
                    }
                }
                if(sampleIn.isEmpty()) {
                    onError.accept(ANSI_RED + "Found no samples at problem " + idx + ANSI_RESET);
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
}

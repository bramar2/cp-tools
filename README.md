## Competitive programming tool. Extensible.
### For consoles not supporting ANSI, replace the ANSI codes in Main.java into "" (empty strings), then build manually with mvn compile assembly:single

### Dependencies: Gson, Jsoup (for download)
### Accessed URLs: https://codeforces.com/problemset/problem/%s/%s, https://codeforces.com/api/contest.standings?contestId=%s&from=1&count=1, https://atcoder.jp/contests/%s/tasks/%s_%s, (Unofficial) https://kenkoooo.com/atcoder/resources/contest-problem.json

```
%s-Helper
==== Help ====
%s d/download <contest-id>
%s g/gen
%s p/problem <letters...>
%s s/sample <problem> [start-idx]
%s t/test [-s] <problem>
%s v/diff <problem> <sample>
%s template <file>
%s c/config
==== Help ====
```
`%s represents the contest site.`


##### \<required>, \[optional]
#### %s d/download \<contest-id>
`Downloads all problem indices (index) and samples if possible. Example <contest-id>: 2000, abc370`
#### %s gen
`Generates template code files for all problem indices (index)`
#### %s p/problem \<letters...>
`Marks problem indices (in case d/download doesn't work).` \
`Then you can do gen, test, sample and everything else` \
`Example <letters...>: A B C1 C2 D E1 E2`
#### %s s/sample \<problem> \[start-idx]
`Allows you to manually enter samples input and output for testing.` \
`Start-idx is used as the start number for samples, default: 1` \
`Example <problem>: A, B, C1, C2, D, ...` \
`Example [start-idx]: 1, 2, 4, 19, 33, ...` \
#### %s t/test \[-s] \<problem>
`Test code using samples` \
`Example <problem>: A, B, C, C2, D, ...` \
`-s indicates skipping build stage`
#### %s v/diff \<problem> \<sample>
`Check difference of code and expected output using diff tool (required for this to work)` \
`Example <problem>: A, B, C, C2, D, ...` \
`Example <sample>: 1, 2, 4, 19, 33, ...` \
#### %s template \<file>
`Sets the template for the file. File extension is required and has to be valid and supported!` \
`The template gets stored in the same directory of the JAR file` \
`Example <file>: a1.cpp, pp.java`
#### %s c/config
`Opens console dialog/menu for viewing/editing config`

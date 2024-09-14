## Competitive programming tool for C++ in Codeforces and Atcoder. Extensible to other sites. (Currently only C++ build is supported)
### For consoles not supporting ANSI, replace the ANSI codes in Main.java into "" (empty strings), then build manually with mvn compile assembly:single

```
%s-Helper
==== Help ====
%s d/download <contest-id>
%s g/gen
%s p/problem <max-letter>
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
#### %s p/problem \<max-letter>
`Marks problems from letter A -> max-letter (in case d/download doesn't work). Example <max-letter>: G, AB` \
`Then you can do gen, test, sample and everything else` \
`Does not work with subproblems! Like C2, D3, ... (unlike download)`
#### %s s/sample \<problem> \[start-idx]
`Allows you to manually enter samples input and output for testing.` \
`Start-idx is used as the start number for samples, default: 1` \
`Example <problem>: A, B, C, C2, D, ...` \
`Example [start-idx]: 1, 2, 4, 19, 33, ...` \
#### %s t/test \[-s] \<problem>
`Test code using samples` \
`Example <problem>: A, B, C, C2, D, ...` \
`-s indicates skipping build stage`
#### %s v/diff \<problem> \<sample>
`Check difference of code and expected output using diff tool (requirement for this to work` \
`Example <problem>: A, B, C, C2, D, ...` \
`Example <sample>: 1, 2, 4, 19, 33, ...` \
#### %s template \<file>
`Sets the template for the file. File extension is required and has to be valid and supported!` \
`The template gets stored in the same directory of the JAR file` \
`Example <file>: a1.cpp, pp.java`
#### %s c/config
`Opens console dialog/menu for viewing/editing config`

package com.lz.devflow.util;

import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.Patch;
import org.apache.commons.lang3.RegExUtils;

import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {
    public static final String JAVA_FILE_EXTENSION = ".java";
    public static final String JS_FILE_EXTENSION = ".js";
    private static final List<String> CODE_REVIEW_SKIP_FILE_PATHS = List.of("**/assets/**", "**.json", "**/**.json",
            "**/**.jar", "**/**.png", "**/**.jpg", "**/**.jpeg", "**/**.gif", "**/**.svg", "**/**.ico", "**/**.woff", "**/**.woff2",
            "**/**.ttf", "**/**.eot", "**/**.otf", "**/**.pdf", "**/**.zip", "**/**.tar", "**/**.gz", "**/**.7z", "**/**.rar",
            "**/**.mp4", "**/**.avi", "**/**.mp3", "**/**.wav", "**/**.flac", "**/**.ogg", "**/**.webm", "**/**.mov", "**/**.wmv",
            "**/**.mkv", "**/**.flv", "**/**.m4v", "**/**.m4a", "**/**.aac", "**/**.flv", "**/**.swf", "**/**.webp", "**/**.bmp",
            "**/**.tiff", "**/**.tif", "**/**.psd", "**/**.ai", "**/**.eps", "**/**.indd", "**/**.raw", "**/**.cr2", "**/**.nef",
            "**/**.orf", "**/**.sr2", "**/**.pef", "**/**.dng", "**/**.xmp", "**/**.arw", "**/**.rw2", "**/**.nrw", "**/**.rwl",
            "**/**.mrw", "**/**.srf", "**/**.3fr", "**/**.dcr", "**/**.kdc", "**/**.mef", "**/**.mos", "**/**.raf", "**/**.srw",
            "**/**.erf", "**/**.fff", "**/**.iiq", "**/**.qtk", "**/**.qtk", "**/**.tif", "**/**.tiff", "**/**.psd", "**/**.ai",
            "**/**.eps", "**/**.indd", "**/**.raw", "**/**.cr2", "**/**.nef", "**/**.orf", "**/**.sr2", "**/**.pef", "**/**.dng",
            "**/**.xmp", "**/**.arw", "**/**.rw2", "**/**.nrw", "**/**.rwl", "**/**.mrw", "**/**.srf", "**/**.3fr", "**/**.html", "**/**.sql");

    private FileUtils() {
    }

    public static boolean requiresCodeReview(String path) {
        return CODE_REVIEW_SKIP_FILE_PATHS.stream().noneMatch(pattern -> {
            PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
            return matcher.matches(FileSystems.getDefault().getPath(path));
        });
    }

    public static List<Integer> getDiffLineNumbers(List<String> original, List<String> revised) {
        Patch<String> patch = DiffUtils.diff(original, revised);
        return getFirstLineNumberOfDiffs(patch);
    }

    public static List<Integer> getDiffLineNumbers(String unifiedDiffContent) {
        String formatedUnifiedDiff = RegExUtils.replaceAll(unifiedDiffContent, " @@ ", " @@\n");
        List<String> diffLines = List.of(formatedUnifiedDiff.split("\\r?\\n"));
        Patch<String> unifiedDiffs = UnifiedDiffUtils.parseUnifiedDiff(diffLines);
        return getFirstLineNumberOfDiffs(unifiedDiffs);
    }

    private static List<Integer> getFirstLineNumberOfDiffs(Patch<String> unifiedDiffs) {
        List<Integer> diffLineNumbers = new ArrayList<>();
        for (var delta : unifiedDiffs.getDeltas()) {
            diffLineNumbers.add(delta.getTarget().getPosition() + 1);
        }
        return diffLineNumbers;
    }

    public static boolean isCodeDependentSupportFile(String filePath) {
        return filePath.endsWith(JAVA_FILE_EXTENSION) || filePath.endsWith(JS_FILE_EXTENSION);
    }

}

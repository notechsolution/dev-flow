package com.lz.devflow.util;

import org.apache.commons.lang3.RegExUtils;
import org.apache.tika.Tika;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ContentUtils {
    public static String detectContentType(String content) {
        Tika tika = new Tika();
        try {
            // String to InputStream
            String mimeType = tika.detect(content.getBytes());
            if (mimeType.contains("html")) {
                return "html";
            } else if (mimeType.contains("markdown") || mimeType.contains("text")) {
                return "markdown";
            }
            return "unknown";
        } catch (Exception e) {
            return "unknown";
        }
    }

    public static boolean isHtml(String content) {
        return "html".equals(detectContentType(content));
    }

    public static boolean isMarkdown(String content) {
        return "markdown".equals(detectContentType(content));
    }

    public static String extractFileIdentifier(String url) {
        String fileIdentifier = "";
        Pattern pattern = Pattern.compile("fileIdentifier=([^&]+)");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            fileIdentifier = matcher.group(1);
        }
        return fileIdentifier;
    }

    public static List<String> extractImageUrls(String content) {
        // the content had "[Image](abc \"Image\")" => \[Image\]\(([^" ]+)
        // [1.00](https://devops.aliyun.com/xxx/url?fileIdentifier=1d636a1e7e0c2849640939e404) => \[1\.00\]\(([^"\)]+)\)
        // <img src="abc" alt="xxxx" />  => <img[^>]+src=\"([^\">]+)\"
        Pattern pattern = Pattern.compile("\\[Image\\]\\(([^\" ]+)|\\[1\\.00\\]\\(([^\"\\)]+)\\)|<img[^>]+src=\\\"([^\\\">]+)\\\"");
        Matcher matcher = pattern.matcher(content);
        List<String> imageUrls = new ArrayList<>();
        // Find all matches and add to result list
        while (matcher.find()) {
            if (matcher.group(1) != null) {
                imageUrls.add(matcher.group(1));
            } else if (matcher.group(2) != null) {
                imageUrls.add(matcher.group(2));
            } else if (matcher.group(3) != null) {
                imageUrls.add(matcher.group(3));
            }
        }

        return imageUrls;
    }

    public static String replaceUrlFromRemoteUrlToLocalUrl(String content, String fileIdentifier) {
        String url = "/api/images/search?fileIdentifier=" + fileIdentifier;

        // Replace "[Image](abc \"Image\")" with "[Image](fileIdentifier \"Image\")" but skip if already contains "/api/images/search?fileIdentifier="
        content = RegExUtils.replaceFirst(content, "\\[Image\\]\\((?!.*?/api/images/search\\?fileIdentifier=)[^\" ]+\\)", "[Image](" + url + ")");

        // Replace "[1.00](https://devops.aliyun.com/xxx/url?fileIdentifier=...)" with "[1.00](fileIdentifier)"
        content = RegExUtils.replaceFirst(content, "\\[1\\.00\\]\\((?!.*?/api/images/search\\?fileIdentifier=)[^\"\\)]+\\)", "[1.00](" + url + ")");

        // Replace "<img src=\"abc\" alt=\"xxxx\" />" with "<img src=\"fileIdentifier\" alt=\"xxxx\" />" but skip if already contains "/api/images/search?fileIdentifier="
        content = RegExUtils.replaceFirst(content, "<img[^>]+src=\\\"(?!.*?/api/images/search\\?fileIdentifier=)[^\\\">]+\\\"", "<img src=\"" + url + "\"");

        return content;
    }

    public static List<String> getLocalImageFileIdentifier(String content) {
        List<String> fileIdentifiers = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\(/api/images/search\\?fileIdentifier=([^\" )]+)");
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            if (matcher.group(1) != null) {
                fileIdentifiers.add(matcher.group(1));
            }
        }
        return fileIdentifiers;
    }
}
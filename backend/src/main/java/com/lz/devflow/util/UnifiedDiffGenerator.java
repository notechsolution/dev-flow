package com.lz.devflow.util;

import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.Patch;

import java.util.List;

public class UnifiedDiffGenerator {
    public static String generateUnifiedDiff(List<String> original, List<String> revised, String originalFilePath, String revisedFilePath) {
        // Compute the diff
        Patch<String> patch = DiffUtils.diff(original, revised);

        // Generate unified diff
        List<String> unifiedDiff = UnifiedDiffUtils.generateUnifiedDiff(
                originalFilePath, revisedFilePath, original, patch, 0);

        // Return only the diff sections as a single string
        return String.join("\n", unifiedDiff);
    }
}

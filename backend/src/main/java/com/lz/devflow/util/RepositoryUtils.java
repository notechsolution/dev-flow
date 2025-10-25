package com.lz.devflow.util;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;

public class RepositoryUtils {

    private RepositoryUtils() {
        // private constructor to prevent instantiation
    }

    // create public static methods to return default example for findBy example API
    public static <T> Example<T> toExample(T exampleEntity) {
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreNullValues() // Ignore null fields
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING) // Use "contains" for string matching
                .withIgnoreCase(); // Case-insensitive matching
        return Example.of(exampleEntity, matcher);
    }
}

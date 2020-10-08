/*
 * This software and associated documentation files are
 *
 * Copyright © 2020-2020 Koninklijke Philips N.V.
 *
 * and is made available for use within Philips and/or within Philips products.
 *
 * All Rights Reserved
 */

package com.philips.research.collector.core.spdx;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.function.BiConsumer;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class TagValueParserTest {
    private static final String TAG = "Tag";
    private static final String VALUE = "Value";
    private static final String TAG_VALUE = TAG + ": " + VALUE;
    private final BiConsumer<String, String> callback = mock(Callback.class);
    private final TagValueParser parser = new TagValueParser(callback);

    private InputStream lineStream(String... lines) {
        final var string = String.join("\n", lines);
        return new ByteArrayInputStream(string.getBytes());
    }

    @Test
    void throws_streamIssue() {
        assertThatThrownBy(() -> parser.parse(null))
                .isInstanceOf(SpdxException.class)
                .hasMessageContaining("reading");
    }

    @Test
    void parsesTagValuePairs() {
        final var stream = lineStream(TAG_VALUE);

        parser.parse(stream);

        verify(callback).accept(TAG, VALUE);
    }

    @Test
    void throws_nonTagValueLine() {
        final var stream = lineStream(TAG_VALUE, TAG_VALUE, "Clearly not a proper line");

        assertThatThrownBy(() -> parser.parse(stream))
                .isInstanceOf(SpdxException.class)
                .hasMessageContaining("format")
                .hasMessageContaining("Line 3");
    }

    @Test
    void skipsEmptyLines() {
        final var stream = lineStream("", " ", "\t  \t ");

        parser.parse(stream);

        verify(callback, never()).accept(anyString(), anyString());
    }

    @Test
    void skipsCommentLines() {
        final var stream = lineStream(TAG_VALUE, "## Comment line", TAG_VALUE);

        parser.parse(stream);

        verify(callback, times(2)).accept(TAG, VALUE);
    }

    @Test
    void unwrapsDelimitedValues() {
        final var stream = lineStream("Wrapped: <text>Text</text>", TAG_VALUE);

        parser.parse(stream);

        verify(callback).accept("Wrapped", "Text");
        verify(callback).accept(TAG, VALUE);
    }

    @Test
    void mergesDelimitedLines() {
        final var stream = lineStream("Multi: <text>First ", "Second", " ", " Third</text>", TAG_VALUE);

        parser.parse(stream);

        verify(callback).accept("Multi", "First \nSecond\n \n Third");
        verify(callback).accept(TAG, VALUE);
    }

    interface Callback extends BiConsumer<String, String> {
    }
}

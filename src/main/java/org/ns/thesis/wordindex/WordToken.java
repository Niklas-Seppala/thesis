package org.ns.thesis.wordindex;

import org.jetbrains.annotations.NotNull;

public record WordToken(@NotNull String word, int position) {
}

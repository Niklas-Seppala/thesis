package org.ns.thesis.wordindex;

import java.util.Iterator;
import java.util.stream.Stream;

/**
 * @author Niklas Seppälä
 */
public interface WordContextIterator extends Iterator<String>, AutoCloseable {

    /**
     * Converts this iterator to NONPARALLEL stream.
     * @return Nonparallel stream.
     */
    Stream<String> stream();

}

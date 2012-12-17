package org.reichhold.robus.lucene;

import java.util.Comparator;
import java.util.Map;

/**
 * User: matthias
 * Date: 14.12.12
 */
public class ScoreComparator implements Comparator<Integer> {
    Map<Integer, Float> base;

    public ScoreComparator(Map<Integer, Float> base) {
        this.base = base;
    }

    // Note: this comparator imposes orderings that are inconsistent with equals.
    public int compare(Integer a, Integer b) {
        if (base.get(a) < base.get(b)) {
            return -1;
        } else {
            return 1;
        } // returning 0 would merge keys
    }
}

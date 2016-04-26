package ngordnet;

import java.util.TreeMap;
import java.util.NavigableSet;
import java.util.Collection;
import java.util.TreeSet;
import java.util.HashSet;
import java.util.Set;
import java.util.LinkedHashSet;

public class TimeSeries<T extends Number> extends TreeMap<Integer, T> {

    /** Constructs a new empty TimeSeries. */
    public TimeSeries() {
        super();
    }

    /**
     * Returns the years in which this time series is valid. Doesn't really need
     * to be a NavigableSet. This is a private method and you don't have to
     * implement it if you don't want to.
     */
    private NavigableSet<Integer> validYears(int startYear, int endYear) {
        return null;
    }

    /**
     * Creates a copy of TS, but only between STARTYEAR and ENDYEAR. inclusive
     * of both end points.
     */
    public TimeSeries(TimeSeries<T> ts, int startYear, int endYear) {
        super();
        for (int year : ts.keySet()) {
            if (year >= startYear && year <= endYear) {
                this.put(year, ts.get(year));
            }
        }
    }

    /** Creates a copy of TS. */
    public TimeSeries(TimeSeries<T> ts) {
        super(ts);
    }

    /**
     * Returns the quotient of this time series divided by the relevant value in
     * ts. If ts is missing a key in this time series, return an
     * IllegalArgumentException.
     */
    public TimeSeries<Double> dividedBy(TimeSeries<? extends Number> ts) {
        TimeSeries<Double> bang = new TimeSeries<Double>();
        for (int year : this.keySet()) {
            if (!ts.containsKey(year)) {
                throw new IllegalArgumentException();
            }
            ///0
            Double val = this.get(year).doubleValue()
                    / ts.get(year).doubleValue();
            bang.put(year, val);
        }
        return bang;
    }

    /**
     * Returns the sum of this time series with the given ts. The result is a a
     * Double time series (for simplicity).
     */
    public TimeSeries<Double> plus(TimeSeries<? extends Number> ts) {
        TimeSeries<Double> boom = new TimeSeries<Double>();

        if (ts == null || ts.keySet() == null) {
            for (int year : this.keySet()) {
                boom.put(year, this.get(year).doubleValue());
            }
            return boom;
        }

        Set<Integer> tsKeys = new HashSet<Integer>(ts.keySet());
        for (int year : this.keySet()) {
            if (ts.containsKey(year)) {
                Double val = this.get(year).doubleValue()
                        + ts.get(year).doubleValue();
                boom.put(year, val);
                tsKeys.remove(year);
            } else {
                Double val = this.get(year).doubleValue();
                boom.put(year, val);
            }
        }
        for (int nian : tsKeys) {
            Double val = ts.get(nian).doubleValue();
            boom.put(nian, val);
        }
        return boom;
    }

    /** Returns all years for this time series (in any order). */
    public Collection<Number> years() {
        Collection<Number> years = new TreeSet<Number>();
        for (Number curr : this.keySet()) {
            years.add(curr);
        }
        return years;
    }

    /** Returns all data for this time series (the same order as years). */
    public Collection<Number> data() {
        Collection<Number> data = new LinkedHashSet<Number>();
        for (Number curr : this.keySet()) {
            data.add(this.get(curr));
        }
        return data;
    }
}

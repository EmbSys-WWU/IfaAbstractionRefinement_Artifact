package de.tub.pes.syscir.analysis.statespace_exploration;

import de.tub.pes.syscir.sc_model.variables.SCTIMEUNIT;
import java.math.BigInteger;

/**
 * Class indicating that a {@link AnalyzedProcess} or {@link Event} is waiting for some real
 * (positive, non-delta) amount of time.
 * 
 * @author Jonas Becker-Kupczok
 *
 */
public final class RealTimedBlocker extends TimedBlocker {

    public static final RealTimedBlocker ZERO = new RealTimedBlocker(0, SCTIMEUNIT.SC_SEC);

    private static final long FS_IN_SEC = 1_000_000_000_000_000L;
    private static final BigInteger FS_IN_SEC_BIG = BigInteger.valueOf(FS_IN_SEC);
    private static final int MAX_SEC_IN_LONG = (int) (Long.MAX_VALUE / FS_IN_SEC);

    private final int sec;
    private final short ms;
    private final short us;
    private final short ns;
    private final short ps;
    private final short fs;

    /**
     * Creates a new RealTimedBlocker with the given duration.
     * 
     * Every parameter must be non-negative. The duration is normalized before storage, i.e. no value
     * returned by {@link #getMs()}, {@link #getUs()}, {@link #getNs()}, {@link #getPs()} or
     * {@link #getFs()} will be larger than 999.
     * 
     * @param sec the number of seconds
     * @param ms the number of milliseconds
     * @param us the number of microseconds
     * @param ns the number of nanoseconds
     * @param ps the number of picoseconds
     * @param fs the number of femtoseconds
     */
    public RealTimedBlocker(int sec, int ms, int us, int ns, int ps, int fs) {
        if (fs > 1000) {
            ps += (fs / 1000);
            fs %= 1000;
        }
        if (ps > 1000) {
            ns += (ps / 1000);
            ps %= 1000;
        }
        if (ns > 1000) {
            us += (ns / 1000);
            ns %= 1000;
        }
        if (us > 1000) {
            ms += (us / 1000);
            us %= 1000;
        }
        if (ms > 1000) {
            sec += (ms / 1000);
            ms %= 1000;
        }

        this.sec = sec;
        this.ms = (short) ms;
        this.us = (short) us;
        this.ns = (short) ns;
        this.ps = (short) ps;
        this.fs = (short) fs;

        assert sec >= 0 && ms >= 0 && us >= 0 && ns >= 0 && ps >= 0 && fs >= 0;
    }


    /**
     * Creates a new RealTimedBlocker with the given duration.
     * 
     * The given duration must be non-negative and the unit may not be {@link SCTIMEUNIT#SC_ZERO_TIME}.
     * 
     * @param amount the amount of time to be waited in the given unit
     * @param unit the unit in which the time is measured
     */
    public RealTimedBlocker(int amount, SCTIMEUNIT unit) {
        this(unit == SCTIMEUNIT.SC_SEC ? amount : 0, unit == SCTIMEUNIT.SC_MS ? amount : 0,
                unit == SCTIMEUNIT.SC_US ? amount : 0, unit == SCTIMEUNIT.SC_NS ? amount : 0,
                        unit == SCTIMEUNIT.SC_PS ? amount : 0, unit == SCTIMEUNIT.SC_FS ? amount : 0);
    }

    /**
     * Returns the number of full seconds in the waited duration.
     *
     * @return number of full seconds
     */
    public int getSec() {
        return this.sec;
    }

    /**
     * Returns the number of full milliseconds (modulo full seconds) in the waited duration.
     *
     * @return number of full milliseconds
     */
    public int getMs() {
        return this.ms;
    }

    /**
     * Returns the number of full microseconds (modulo full milliseconds) in the waited duration.
     *
     * @return number of full microseconds
     */
    public int getUs() {
        return this.us;
    }

    /**
     * Returns the number of full nanoseconds (modulo full microseconds) in the waited duration.
     *
     * @return number of full nanoseconds
     */
    public int getNs() {
        return this.ns;
    }

    /**
     * Returns the number of full picoseconds (modulo full nanoseconds) in the waited duration.
     *
     * @return number of full picoseconds
     */
    public int getPs() {
        return this.ps;
    }

    /**
     * Returns the number of full femtoseconds (modulo full picoseconds) in the waited duration.
     *
     * @return number of full femtoseconds
     */
    public int getFs() {
        return this.fs;
    }

    /**
     * Returns the length of the waited duration in the given unit.
     *
     * @return length of the waited duration in the given unit
     */
    public int getValue(SCTIMEUNIT unit) {
        return SCTIMEUNIT.convert(this.sec, SCTIMEUNIT.SC_SEC, unit)
                + SCTIMEUNIT.convert(this.ms, SCTIMEUNIT.SC_MS, unit)
                + SCTIMEUNIT.convert(this.us, SCTIMEUNIT.SC_US, unit)
                + SCTIMEUNIT.convert(this.ns, SCTIMEUNIT.SC_NS, unit)
                + SCTIMEUNIT.convert(this.ps, SCTIMEUNIT.SC_PS, unit)
                + SCTIMEUNIT.convert(this.fs, SCTIMEUNIT.SC_FS, unit);
    }

    /**
     * Returns a new RealTimedBlocker with the difference in duration between this RealTimedBlocker and
     * the given parameter.
     * 
     * The given parameter must not have a longer duration than this.
     *
     * @param other the subtrahend
     * @return RealTimedBlocker with the duration of this minus the subtrahend
     * @throws IllegalArgumentException if the subtrahend has a larger duration than this
     */
    public RealTimedBlocker subtract(RealTimedBlocker other) throws IllegalArgumentException {
        int sec = this.sec - other.sec;
        int ms = this.ms - other.ms;
        int us = this.us - other.us;
        int ns = this.ns - other.ns;
        int ps = this.ps - other.ps;
        int fs = this.fs - other.fs;

        if (fs < 0) {
            int extra = Math.floorDiv(fs, 1000);
            ps += extra;
            fs += (-1000 * extra);
        }
        if (ps < 0) {
            int extra = Math.floorDiv(ps, 1000);
            ns += extra;
            ps += (-1000 * extra);
        }
        if (ns < 0) {
            int extra = Math.floorDiv(ns, 1000);
            us += extra;
            ns += (-1000 * extra);
        }
        if (us < 0) {
            int extra = Math.floorDiv(us, 1000);
            ms += extra;
            us += (-1000 * extra);
        }
        if (ms < 0) {
            int extra = Math.floorDiv(ms, 1000);
            sec += extra;
            ms += (-1000 * extra);
        }
        if (sec < 0) {
            throw new IllegalArgumentException("other is bigger than this");
        }

        return new RealTimedBlocker(sec, ms, us, ns, ps, fs);
    }

    /**
     * Returns a new RealTimedBlocker with the remainder of this duration if the other duration is
     * subtracted as often as possible (whole-numbered) without this becoming negative.
     * 
     * @param other the divisor
     * @return RealTimedBlocker the remainder
     * @throws IllegalArgumentException if the subtrahend has a larger duration than this
     */
    public RealTimedBlocker remainder(RealTimedBlocker other) throws IllegalArgumentException {
        int sec;
        long remaining;
        if (this.sec <= MAX_SEC_IN_LONG && other.sec <= MAX_SEC_IN_LONG) {
            long resultFs = toFemto() % other.toFemto();
            sec = (int) (resultFs / FS_IN_SEC);
            remaining = resultFs % FS_IN_SEC;
        } else {
            BigInteger resultFemtoBigInt = toFemtoBigInt().mod(other.toFemtoBigInt());
            sec = resultFemtoBigInt.divide(FS_IN_SEC_BIG).intValue();
            remaining = resultFemtoBigInt.mod(FS_IN_SEC_BIG).longValue();
        }

        int fs = (int) (remaining % 1000);
        remaining /= 1000L;
        int ps = (int) (remaining % 1000);
        remaining /= 1000L;
        int ns = (int) (remaining % 1000);
        remaining /= 1000L;
        int us = (int) (remaining % 1000);
        remaining /= 1000L;
        int ms = (int) remaining;

        return new RealTimedBlocker(sec, ms, us, ns, ps, fs);
    }

    private long toFemto() {
        return ((((this.sec * 1000L + this.ms) * 1000L + this.us) * 1000L + this.ns) * 1000L + this.ps) * 1000L
                + this.fs;
    }

    private BigInteger toFemtoBigInt() {
        BigInteger lessThenSeconds = BigInteger
                .valueOf((((this.ms * 1000L + this.us) * 1000L + this.ns) * 1000L + this.ps) * 1000L * this.fs);
        return lessThenSeconds.add(BigInteger.valueOf(this.sec).multiply(FS_IN_SEC_BIG));
    }

    @Override
    public int compareTo(TimedBlocker other) {
        if (other instanceof DeltaTimeBlocker) {
            return -1;
        }
        RealTimedBlocker rtb = (RealTimedBlocker) other;

        int result = Integer.compare(this.sec, rtb.sec);
        if (result != 0) {
            return result;
        }
        result = Integer.compare(this.ms, rtb.ms);
        if (result != 0) {
            return result;
        }
        result = Integer.compare(this.us, rtb.us);
        if (result != 0) {
            return result;
        }
        result = Integer.compare(this.ns, rtb.ns);
        if (result != 0) {
            return result;
        }
        result = Integer.compare(this.ps, rtb.ps);
        if (result != 0) {
            return result;
        }
        return Integer.compare(this.fs, rtb.fs);
    }

    @Override
    public int hashCode() {
        return this.sec + 31 * (this.ms + 31 * (this.us + 31 * (this.ns + 31 * (this.ps + 31 * (this.fs)))));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof RealTimedBlocker t)) {
            return false;
        }
        return this.sec == t.sec && this.ms == t.ms && this.us == t.us && this.ns == t.ns && this.ps == t.ps
                && this.fs == t.fs;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        if (this.sec != 0) {
            builder.append(this.sec).append("s");
        }
        if (this.ms != 0) {
            builder.append(this.ms).append("ms");
        }
        if (this.us != 0) {
            builder.append(this.us).append("us");
        }
        if (this.ns != 0) {
            builder.append(this.ns).append("ns");
        }
        if (this.ps != 0) {
            builder.append(this.ps).append("ps");
        }
        if (this.fs != 0) {
            builder.append(this.fs).append("fs");
        }

        if (builder.isEmpty()) {
            return "0s";
        }
        return builder.toString();
    }

}

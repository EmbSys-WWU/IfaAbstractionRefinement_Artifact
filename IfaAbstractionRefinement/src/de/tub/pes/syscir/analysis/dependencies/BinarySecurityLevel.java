package de.tub.pes.syscir.analysis.dependencies;

import de.tub.pes.syscir.analysis.syscdg.SecurityLattice;
import de.tub.pes.syscir.analysis.syscdg.SecurityLevel;

/**
 * This Enum provides two security levels, "public" and "secure".
 *
 * @author twierbru (but copied from Jan Kirchener's implementation in
 *         rescuetests.tests.syscdg.ExampleSecurityEnum)
 */
public enum BinarySecurityLevel implements SecurityLevel<BinarySecurityLevel> {

    LOW, HIGH;

    public static final SecurityLattice<BinarySecurityLevel> correspondingLattice = new SecurityLattice<>(HIGH, LOW);

    @Override
    public BinarySecurityLevel leastUpperBound(BinarySecurityLevel other) {
        if (this == HIGH)
            return this;
        else
            return other;
    }

    @Override
    public BinarySecurityLevel greatestLowerBound(BinarySecurityLevel other) {
        if (this == LOW)
            return this;
        else
            return other;
    }

    @Override
    public boolean equals(BinarySecurityLevel other) {
        return this == other;
    }
}

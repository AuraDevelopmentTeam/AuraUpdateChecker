package dev.aura.updatechecker.version;

import java.math.BigInteger;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(includeFieldNames = false)
public class NumberComponent implements VersionComponent {
    protected final BigInteger number;

    @Override
    public final VersionComponentType getVersionComponentType() {
        return VersionComponentType.NUMBER;
    }

    @Override
    public int compareTo(VersionComponent that) {
        VersionComponentType thatType = that.getVersionComponentType();

        if (thatType == VersionComponentType.LIST)
            return -that.compareTo(this);
        else if (thatType == VersionComponentType.STRING)
            return 1;

        NumberComponent thatNumber = (NumberComponent) that;

        return number.compareTo(thatNumber.number);
    }
}

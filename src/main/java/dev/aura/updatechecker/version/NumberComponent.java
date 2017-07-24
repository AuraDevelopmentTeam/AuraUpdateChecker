package dev.aura.updatechecker.version;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
class NumberComponent implements VersionComponent {
    protected final int number;

    @Override
    public final VersionComponentType getVersionComponentType() {
        return VersionComponentType.NUMBER;
    }

    @Override
    public int compareTo(VersionComponent that) {
        VersionComponentType thatType = that.getVersionComponentType();

        if (thatType == VersionComponentType.LIST)
            return that.compareTo(this);

        else if (thatType == VersionComponentType.STRING)
            return 1;

        NumberComponent thatNumber = (NumberComponent) that;

        return Integer.compare(number, thatNumber.number);
    }
}

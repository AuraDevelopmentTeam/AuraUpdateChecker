package dev.aura.updatechecker.version;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(includeFieldNames = false)
public class StringComponent implements VersionComponent {
    protected final String string;

    @Override
    public final VersionComponentType getVersionComponentType() {
        return VersionComponentType.STRING;
    }

    @Override
    public int compareTo(VersionComponent that) {
        // TODO: Implement comparison logic! With special cases etc!

        VersionComponentType thatType = that.getVersionComponentType();

        if (thatType != VersionComponentType.STRING)
            return -that.compareTo(this);

        StringComponent thatString = (StringComponent) that;

        return string.compareTo(thatString.string);
    }
}

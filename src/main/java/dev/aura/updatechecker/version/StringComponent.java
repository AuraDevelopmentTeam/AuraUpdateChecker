package dev.aura.updatechecker.version;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
class StringComponent implements VersionComponent {
    protected final String string;

    @Override
    public final VersionComponentType getVersionComponentType() {
        return VersionComponentType.STRING;
    }

    @Override
    public int compareTo(VersionComponent that) {
        return 0;
    }
}

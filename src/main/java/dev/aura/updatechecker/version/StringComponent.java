package dev.aura.updatechecker.version;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class StringComponent implements VersionComponent {
    protected final String string;
    
    @Override
    public final VersionComponentType getVersionComponentType() {
        return VersionComponentType.STRING;
    }

    @Override
    public int compareTo(VersionComponent o) {
        // TODO Auto-generated method stub
        return 0;
    }
}

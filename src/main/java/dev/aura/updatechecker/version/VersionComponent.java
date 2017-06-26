package dev.aura.updatechecker.version;

public interface VersionComponent extends Comparable<VersionComponent> {
    public VersionComponentType getVersionComponentType();
}

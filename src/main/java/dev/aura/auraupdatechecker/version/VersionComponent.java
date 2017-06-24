package dev.aura.auraupdatechecker.version;

public interface VersionComponent extends Comparable<VersionComponent> {
    public VersionComponentType getVersionComponentType();
}

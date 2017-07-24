package dev.aura.updatechecker.version;

interface VersionComponent extends Comparable<VersionComponent> {
    public VersionComponentType getVersionComponentType();
}

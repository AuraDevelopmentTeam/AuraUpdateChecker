package dev.aura.updatechecker.version;

public class NumberComponent implements VersionComponent {
    protected final int number;
    
    protected NumberComponent(String number) {
        this.number = Integer.parseInt(number);
    }
    
    @Override
    public final VersionComponentType getVersionComponentType() {
        return VersionComponentType.NUMBER;
    }
    
    @Override
    public int compareTo(VersionComponent arg0) {
        // TODO Auto-generated method stub
        return 0;
    }
}

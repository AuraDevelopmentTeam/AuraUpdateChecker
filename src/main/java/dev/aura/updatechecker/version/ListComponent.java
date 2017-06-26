package dev.aura.updatechecker.version;

import java.util.Arrays;
import java.util.List;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ListComponent implements VersionComponent {
    protected final List<VersionComponent> components;

    @Override
    public final VersionComponentType getVersionComponentType() {
        return VersionComponentType.LIST;
    }

    @Override
    public int compareTo(VersionComponent that) {
        if (that.getVersionComponentType() != VersionComponentType.LIST)
            return compareTo(new ListComponent(Arrays.asList(that)));

        ListComponent thatList = (ListComponent) that;
        boolean empty = components.isEmpty();
        boolean thatEmpty = thatList.components.isEmpty();

        if (empty)
            return thatEmpty ? 0 : -1;
        else if (thatEmpty)
            return 1;

        int thisSize = components.size();
        int thatSize = thatList.components.size();
        int size = Math.max(thisSize, thatSize);
        VersionComponent thisComponent;
        VersionComponent thatComponent;
        int result;

        for (int i = 0; i < size; ++i) {
            if (i < thisSize) {
                thisComponent = components.get(i);
            } else {
                thisComponent = Version.ZERO;
            }

            if (i < thatSize) {
                thatComponent = thatList.components.get(i);
            } else {
                thatComponent = Version.ZERO;
            }

            result = thisComponent.compareTo(thatComponent);

            if (result != 0)
                return result;
        }

        return 0;
    }
}

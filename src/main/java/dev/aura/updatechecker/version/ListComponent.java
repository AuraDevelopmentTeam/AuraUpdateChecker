package dev.aura.updatechecker.version;

import java.util.Iterator;
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
        boolean empty = components.isEmpty();

        if (that.getVersionComponentType() != VersionComponentType.LIST) {
            if (empty)
                return -1;

            // TODO fix logic!

            int result = 0;
            Iterator<VersionComponent> it = components.iterator();

            while (it.hasNext() && (result == 0)) {
                result = it.next().compareTo(that);
            }

            return result;
        }

        ListComponent thatList = (ListComponent) that;
        boolean thatEmpty = thatList.components.isEmpty();

        if (empty)
            return thatEmpty ? 0 : -1;
        else if (thatEmpty)
            return 1;

        // TODO List vs List logic!

        return 0;
    }
}

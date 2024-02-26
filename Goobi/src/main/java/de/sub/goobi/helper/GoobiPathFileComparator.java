package de.sub.goobi.helper;

import java.nio.file.Path;
import java.util.Comparator;

public class GoobiPathFileComparator implements Comparator<Path> {
    private GoobiStringFileComparator comparator = new GoobiStringFileComparator();

    @Override
    public int compare(Path o1, Path o2) {
        return comparator.compare(o1.toString(), o2.toString());
    }

}

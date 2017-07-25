package dev.aura.updatechecker.version;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import lombok.ToString;

@ToString
public class Version implements Comparable<Version> {
    protected static final NumberComponent ZERO = new NumberComponent(0);

    private static final Pattern[] listSeparators = new Pattern[] { Pattern.compile("_"), Pattern.compile("-"),
            Pattern.compile("\\."), Pattern.compile("(?=\\d)(?<=[a-z])|(?=[a-z])(?<=\\d)", Pattern.CASE_INSENSITIVE) };
    private static final Pattern number = Pattern.compile("^\\d+$");

    private final String input;
    private final VersionComponent component;

    public Version(String version) {
        input = version;
        component = parse(version);
    }

    @Override
    public int compareTo(Version other) {
        return component.compareTo(other.component);
    }

    @Override
    public boolean equals(Object other) {
        if ((other == null) || !(other instanceof Version))
            return false;

        return compareTo((Version) other) == 0;
    }

    private static VersionComponent parse(String version) {
        Matcher matcher;

        for (Pattern separator : listSeparators) {
            matcher = separator.matcher(version);

            if (matcher.find())
                return new ListComponent(
                        Arrays.stream(separator.split(version)).map(Version::parse).collect(Collectors.toList()));
        }

        if (number.matcher(version).find())
            return new NumberComponent(Integer.parseInt(version));
        else
            return new StringComponent(version);
    }
}

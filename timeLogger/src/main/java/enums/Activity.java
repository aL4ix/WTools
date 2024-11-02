package enums;

import java.util.stream.Stream;

public interface Activity {
    static <ActivityType extends Enum<ActivityType> & Activity> ActivityType
    fromDisplayedName(String displayedName, ActivityType[] enumValues) {
        for (ActivityType activity : enumValues) {
            if (activity.getDisplayedName().equals(displayedName)) {
                return activity;
            }
        }
        throw new IllegalArgumentException(String.format("'%s' is not an Activity", displayedName));
    }

    String getDisplayedName();

    default String nameToTitleCase(String name) {
        return Stream.of(name.split("_"))
                .map(w -> w.toUpperCase().charAt(0) + w.toLowerCase().substring(1))
                .reduce((s, s2) -> s + " " + s2).orElse("");
    }
}

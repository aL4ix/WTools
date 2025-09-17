package models;

import java.util.ArrayList;
import java.util.List;

public class TimeGroup {
    private final String issue;

    private final List<TimeEntry> entries;

    TimeGroup(String issue) {
        this.issue = issue;
        entries = new ArrayList<>();
    }

    void addEntry(TimeEntry entry) {
        entries.add(entry);
    }

    public String getIssue() {
        return issue;
    }

    public List<TimeEntry> getEntries() {
        return entries;
    }

    @Override
    public String toString() {
        return "TimeGroup{" +
                "issue='" + issue + '\'' +
                ", dateHourEntries=" + entries +
                '}';
    }
}

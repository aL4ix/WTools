package pages;

import models.BulkEntryRow;
import models.TimeGroup;
import utils.Browser;

import java.util.List;

public abstract class TimeLogger extends Page {
    protected String username;
    protected String password;

    public TimeLogger(String browserName, String username, String password) {
        super(new Browser(browserName));
        this.username = username;
        this.password = password;
    }

    public abstract boolean logTimeGroup(TimeGroup group);
    public abstract boolean logBulk(String issue, List<BulkEntryRow> bulkEntryRows);
}

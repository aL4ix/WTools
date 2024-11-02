package pages;

import models.TimeGroup;
import utils.Browser;

public abstract class TimeLogger extends Page {
    protected String username;
    protected String password;

    public TimeLogger(String browser, String username, String password) {
        super(new Browser(browser));
        this.username = username;
        this.password = password;
    }

    public abstract boolean logTimeGroup(TimeGroup group);
}

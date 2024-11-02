package pages;

import utils.Browser;

public abstract class Page {
    protected Browser browser;

    public Page(Browser browser) {
        this.browser = browser;
    }
}

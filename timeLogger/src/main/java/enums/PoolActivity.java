package enums;

public enum PoolActivity implements Activity {
    DESIGN, DEVELOPMENT, TEST_CASE_DESIGN, TESTING, MEETING, REPORTING, RESEARCH, IT_SUPPORT,
    VACATION_PTO_HOLIDAY("Vacation/PTO/Holiday"), TRAINING, COMP_TIME, MAINTENANCE;

    private final String displayedName;

    PoolActivity() {
        displayedName = nameToTitleCase(name());
    }

    PoolActivity(String displayedName) {
        this.displayedName = displayedName;
    }

    public String getDisplayedName() {
        return displayedName;
    }
}

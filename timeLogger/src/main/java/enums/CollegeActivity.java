package enums;

public enum CollegeActivity implements Activity {
    DESIGN, DEVELOPMENT, TEST_CASE_DESIGN, TESTING, MEETING, REPORTING, RESEARCH, IT_SUPPORT,
    VACATION_PTO_HOLIDAY("Vacation/PTO/Holiday"), COMP_TIME, MAINTENANCE;

    private final String displayedName;

    CollegeActivity() {
        displayedName = nameToTitleCase(name());
    }

    CollegeActivity(String displayedName) {
        this.displayedName = displayedName;
    }

    public String getDisplayedName() {
        return displayedName;
    }
}

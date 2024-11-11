package xl2testrail;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestCasesParser {
    public static List<Case> parse(String testCasesFile) throws IOException {
        FileInputStream file = new FileInputStream(testCasesFile);
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);
        StateMachine state = StateMachine.NEW_CASE;
        List<Case> cases = new ArrayList<>();
        for (Row row : sheet) {
            System.out.printf("Processing row %d\n", row.getRowNum());
            switch (state) {
                case PRECONDITIONS:
                    System.out.println("Maybe precondition");
                    Cell maybePrecond = row.getCell(0);
                    state = StateMachine.STEP;
                    if (maybePrecond == null) {
                        System.out.println("null");
                        extractPrecondition(row, cases);
                        break;
                    } else {
                        if (maybePrecond.getCellType() == CellType.BLANK ||
                                maybePrecond.getCellType() == CellType.STRING &&
                                        maybePrecond.getStringCellValue().isEmpty()) {
                            System.out.println("empty");
                            extractPrecondition(row, cases);
                            break;
                        }
                        System.out.printf("Jumping to step because cell is: %s", maybePrecond.getCellType());
                    }
                case STEP:
                    System.out.println("Maybe step");
                    Cell maybeStep = row.getCell(0);
                    if (maybeStep != null && maybeStep.getCellType().equals(CellType.NUMERIC)) {
                        double num = row.getCell(0).getNumericCellValue();
                        String desc = row.getCell(1).getStringCellValue();
                        String exp = cellToString(row.getCell(2));
                        System.out.println(desc);
                        cases.get(cases.size() - 1).getSteps().put((int) num, new Step(desc, exp));
                        break;
                    } else {
                        state = StateMachine.NEW_CASE;
                    }
                case NEW_CASE:
                    System.out.println("Maybe new case");
                    Cell maybeNewCase = row.getCell(0);
                    if (maybeNewCase == null) {
                        continue;
                    }
                    String title = maybeNewCase.getStringCellValue();
                    if (title.isBlank()) {
                        continue;
                    }
                    System.out.println(title);
                    cases.add(new Case(title));
                    state = StateMachine.PRECONDITIONS;
                    break;
            }
            System.out.println();
        }
        return cases;
    }

    private static void extractPrecondition(Row row, List<Case> cases) {
        String preconditions = row.getCell(2).getStringCellValue();
        System.out.println(preconditions);
        cases.get(cases.size() - 1).setPreconditions(preconditions);
    }

    private static String cellToString(Cell cell) {
        if (cell == null) {
            return "";
        }
        return cell.getStringCellValue();
    }
}

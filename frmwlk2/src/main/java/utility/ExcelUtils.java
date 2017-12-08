package utility;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import config.Constants;
import executionEngine.DriverScript;

public class ExcelUtils {

    private static XSSFSheet ExcelWSheet;
    private static XSSFWorkbook ExcelWBook;
    private static org.apache.poi.ss.usermodel.Cell Cell;
    private static XSSFRow Row;

    public static void setExcelFile(String Path) throws Exception {
        try {
            FileInputStream ExcelFile = new FileInputStream(Path);
            ExcelWBook = new XSSFWorkbook(ExcelFile);
        } catch (Exception e) {
            Log.error("Class Utils | Method setExcelFile | Exception desc : " + e);
            DriverScript.bResult = false;
        }
    }

    public static String getCellData(int RowNum, int ColNum, String SheetName) throws Exception {
        try {
            ExcelWSheet = ExcelWBook.getSheet(SheetName);
            Row = ExcelWSheet.getRow(RowNum);
            String cellData = "";
            if (Row != null) {
                if (Row.getCell(ColNum).getCellType() != 0) {
                    cellData = Row.getCell(ColNum).getRichStringCellValue().toString();
                } else {
                    XSSFCell xssfCell = Row.getCell(ColNum);
                    xssfCell.setCellType(1);
                    cellData = xssfCell.getRichStringCellValue().toString();
                }
            }
            return cellData;
        } catch (Exception e) {
            Log.error("Class Utils | Method getCellData | Exception desc : " + e);
            DriverScript.bResult = false;
            return "";
        }
    }

    public static int getRowCount(String SheetName) {
        int iNumber = 0;
        try {
            ExcelWSheet = ExcelWBook.getSheet(SheetName);
            iNumber = ExcelWSheet.getLastRowNum() + 1;
        } catch (Exception e) {
            Log.error("Class Utils | Method getRowCount | Exception desc : " + e);
            DriverScript.bResult = false;
        }
        return iNumber;
    }

    public static int getRowContains(String sTestCaseName, int colNum, String SheetName) throws Exception {
        int iRowNum = 1;
        try {
            ExcelWSheet = ExcelWBook.getSheet(SheetName);
            int rowCount = ExcelUtils.getRowCount(SheetName);
            for (; iRowNum < rowCount; iRowNum++) {
                if (ExcelUtils.getCellData(iRowNum, colNum, SheetName).equalsIgnoreCase(sTestCaseName)) {
                    break;
                }
            }
        } catch (Exception e) {
            Log.error("Class Utils | Method getRowContains | Exception desc : " + e);
            DriverScript.bResult = false;
        }
        return iRowNum;
    }

    public static int getTestStepsCount(String SheetName, String sTestCaseID, int iTestCaseStart) throws Exception {
        try {
            for (int i = iTestCaseStart; i <= ExcelUtils.getRowCount(SheetName); i++) {
                if (!(sTestCaseID.equals(ExcelUtils.getCellData(i, Constants.Col_TestCaseID, SheetName)))) {
                    int number = i;
                    return number;
                }
            }
            ExcelWSheet = ExcelWBook.getSheet(SheetName);
            int number = ExcelWSheet.getLastRowNum() + 1;
            return number;
        } catch (Exception e) {
            Log.error("Class Utils | Method getRowContains | Exception desc : " + e);
            DriverScript.bResult = false;
            return 0;
        }
    }

    public static void setCellData(String Result, int RowNum, int ColNum, String SheetName) throws Exception {
        try {
            ExcelWSheet = ExcelWBook.getSheet(SheetName);
            Row = ExcelWSheet.getRow(RowNum);
            Cell = Row.getCell(ColNum, org.apache.poi.ss.usermodel.Row.RETURN_BLANK_AS_NULL);
            if (Cell == null) {
                CellStyle cellStyle = null;
                if (Row.getCell(ColNum) != null) {
                    cellStyle = Row.getCell(ColNum).getCellStyle();
                }
                Cell = Row.createCell(ColNum);
                Cell.setCellValue(Result);
                if (Row.getCell(ColNum) != null) {
                    Cell.setCellStyle(cellStyle);
                }
            } else {
                Cell.setCellValue(Result);
            }
            FileOutputStream fileOut = new FileOutputStream(DriverScript.sPathTestData);
            ExcelWBook.write(fileOut);
            fileOut.flush();
            fileOut.close();
        } catch (Exception e) {
            DriverScript.bResult = false;
        }
    }

    public static void clearCellResults(int ColNum, String SheetName) throws Exception {
        try {
            ExcelWSheet = ExcelWBook.getSheet(SheetName);
            int rownum = ExcelWSheet.getLastRowNum();
            for (int i = 1; i < rownum + 1; i++) {
                XSSFRow row = ExcelWSheet.getRow(i);
                if (row != null) {
                    XSSFCell cell = row.getCell(ColNum);
                    if (cell != null) {
                        if (cell.getCTCell().isSetT())
                            cell.getCTCell().unsetT();
                        if (cell.getCTCell().isSetV())
                            cell.getCTCell().unsetV();
                    }
                }
            }
            FileOutputStream fileOut = new FileOutputStream(DriverScript.sPathTestData);
            ExcelWBook.write(fileOut);
            fileOut.flush();
            fileOut.close();
        } catch (Exception e) {
            DriverScript.bResult = false;
        }
    }

    public static void refreshFormulaCells() throws Exception {
        try {
            XSSFFormulaEvaluator.evaluateAllFormulaCells(ExcelWBook);
        } catch (Exception e) {
            DriverScript.bResult = false;
        }
    }

}

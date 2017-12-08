package config;

public class Constants {

    public static final String File_TestData = "DataEngine.xlsx";
    
    public static final String PathApplication = System.getProperty("user.dir");
    public static final String PathScreenshots = PathApplication;
    
    public static final String NamePath = "Evidencias";
    public static int TypeBrowser = 0;
    
    public static final int Col_TestCaseID = 0;
    public static final int Col_TestScenarioID = 1;
    public static final int Col_PageObject = 4;
    public static final int Col_ActionKeyword = 5;
    public static final int Col_Print = 6;
    public static final int Col_DataSet = 7;
    public static final int Col_TestStepResult = 8;

    public static final int Col_RunMode = 2;
    public static final int Col_Result = 3;

    public static final String Sheet_TestSteps = "Test Steps";
    public static final String Sheet_TestCases = "Test Cases";

    public static final String KEYWORD_FAIL = "FAIL";
    public static final String KEYWORD_PASS = "PASS";
}

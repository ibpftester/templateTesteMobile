package executionEngine;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.xml.DOMConfigurator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import config.ActionKeywords;
import config.Constants;
import project.ActionKeywordsSpecific;
import utility.Common;
import utility.ExcelUtils;
import utility.Log;

public class DriverScript {

    public static Properties OR_Init;
    public static Properties OR;
    public static ActionKeywordsSpecific actionKeywords;
    public static String sActionKeyword;
    public static String sPageObject;
    public static Method method[];
    public static int iTestStep;
    public static int iTestLastStep;
    public static String sTestCaseID;
    public static String sRunMode;
    public static String sData;
    public static String sPrint;
    public static boolean bResult;

    public static String sUrl;
    public static String sTimeLoadUrl;
    public static String sRepeatError;
    public static String sCloseBrowserError;
    public static String sStopTestsError;
    public static String sPathTestData;
    public static String sPathOR;
    public static String sPathDrivers;
    public static String sPathLog;
    public static String sOptionDFTMdSelect;
    public static String sUserB;
    public static String sPasswordB;

    public DriverScript() throws NoSuchMethodException, SecurityException {
        actionKeywords = new ActionKeywordsSpecific();
        method = actionKeywords.getClass().getMethods();
    }

    public static void main(String[] args) throws Exception {
        read_Config();

        String Path_OR = sPathOR;
        FileInputStream fs2 = new FileInputStream(Path_OR);
        OR = new Properties(System.getProperties());
        OR.load(new InputStreamReader(fs2, Charset.forName("UTF-8")));

        ExcelUtils.setExcelFile(sPathTestData);
        System.setProperty("logFilePath", sPathLog);
        DOMConfigurator.configure("log4j.xml");
        DriverScript startEngine = new DriverScript();
        startEngine.execute_TestCase();

        if (Constants.TypeBrowser == 1) {
            Runtime.getRuntime().exec("taskkill /F /IM geckodriver.exe");
        } else if (Constants.TypeBrowser == 2) {
            Runtime.getRuntime().exec("taskkill /F /IM IEDriverServer.exe");
        } else if (Constants.TypeBrowser == 3) {
            Runtime.getRuntime().exec("taskkill /F /IM chromedriver.exe");
        }
    }

    private void execute_TestCase() throws Exception {
        ExcelUtils.clearCellResults(Constants.Col_TestStepResult, Constants.Sheet_TestSteps);
        ExcelUtils.clearCellResults(Constants.Col_Result, Constants.Sheet_TestCases);
        int iTotalTestCases = ExcelUtils.getRowCount(Constants.Sheet_TestCases);
        int controllerFail = 0;
        for (int iTestcase = 1; iTestcase < iTotalTestCases; iTestcase++) {
            bResult = true;
            sTestCaseID = ExcelUtils.getCellData(iTestcase, Constants.Col_TestCaseID, Constants.Sheet_TestCases);
            sRunMode = ExcelUtils.getCellData(iTestcase, Constants.Col_RunMode, Constants.Sheet_TestCases);
            if (sRunMode.equals("Yes")) {
                iTestStep = ExcelUtils.getRowContains(sTestCaseID, Constants.Col_TestCaseID, Constants.Sheet_TestSteps);
                iTestLastStep = ExcelUtils.getTestStepsCount(Constants.Sheet_TestSteps, sTestCaseID, iTestStep);
                Log.startTestCase(sTestCaseID);
                bResult = true;
                for (; iTestStep < iTestLastStep; iTestStep++) {
                    sActionKeyword = ExcelUtils.getCellData(iTestStep, Constants.Col_ActionKeyword,
                        Constants.Sheet_TestSteps);
                    sPageObject = ExcelUtils.getCellData(iTestStep, Constants.Col_PageObject,
                        Constants.Sheet_TestSteps);
                    sData = ExcelUtils.getCellData(iTestStep, Constants.Col_DataSet, Constants.Sheet_TestSteps);
                    sPrint = ExcelUtils.getCellData(iTestStep, Constants.Col_Print, Constants.Sheet_TestSteps);
                    execute_Actions();
                    if (bResult == false) {
                        if (sStopTestsError.equals("S")) {
                            ExcelUtils.setCellData(Constants.KEYWORD_FAIL, iTestcase, Constants.Col_Result,
                                Constants.Sheet_TestCases);
                            iTestcase = iTotalTestCases;
                        }
                        else{
                            if (!sRepeatError.equals("S")) {
                                ExcelUtils.setCellData(Constants.KEYWORD_FAIL, iTestcase, Constants.Col_Result,
                                    Constants.Sheet_TestCases);
                            } else {
                                if (controllerFail < 3) {
                                    if (controllerFail == 2) {
                                        ExcelUtils.setCellData(Constants.KEYWORD_FAIL, iTestcase, Constants.Col_Result,
                                            Constants.Sheet_TestCases);
                                        controllerFail = 0;
                                    } else {
                                        controllerFail++;
                                        iTestcase--;
                                    }
                                }
                            }
                        }
                        Log.endTestCase(sTestCaseID);
                        break;
                    }
                }
                if (bResult == true) {
                    ExcelUtils.setCellData(Constants.KEYWORD_PASS, iTestcase, Constants.Col_Result,
                        Constants.Sheet_TestCases);
                    controllerFail = 0;
                    Log.endTestCase(sTestCaseID);
                }
            }
        }
    }

    private static void execute_Actions() throws Exception {
        for (int i = 0; i < method.length; i++) {
            if (method[i].getName().equals(sActionKeyword)) {
                method[i].invoke(actionKeywords, sPageObject, sData, sPrint);
                if (bResult == true) {
                    ExcelUtils.setCellData(Constants.KEYWORD_PASS, iTestStep, Constants.Col_TestStepResult,
                        Constants.Sheet_TestSteps);
                    break;
                } else {
                    ExcelUtils.setCellData(Constants.KEYWORD_FAIL, iTestStep, Constants.Col_TestStepResult,
                        Constants.Sheet_TestSteps);
                    if(sCloseBrowserError.equals("S")){
                        ActionKeywords.closeBrowser("", "", "");    
                    }
                    break;
                }
            }
        }
    }

    private static void read_Config() throws Exception {
        File fXmlFile = new File(Constants.PathApplication + "/config.xml");
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(fXmlFile);
        doc.getDocumentElement().normalize();
        NodeList nList = doc.getElementsByTagName("parameters");

        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                sUrl = eElement.getElementsByTagName("url").item(0).getTextContent();
                sTimeLoadUrl = eElement.getElementsByTagName("timeloadurl").item(0).getTextContent();
                sRepeatError = eElement.getElementsByTagName("repeaterror").item(0).getTextContent();
                sCloseBrowserError = eElement.getElementsByTagName("closebrowsererror").item(0).getTextContent();
                sStopTestsError = eElement.getElementsByTagName("stoptestserror").item(0).getTextContent();
                sPathTestData = Common.convertPathWindowsFormat(
                    eElement.getElementsByTagName("pathtestdata").item(0).getTextContent()) + "/DataEngine.xlsx";
                sPathOR = Common.convertPathWindowsFormat(
                    eElement.getElementsByTagName("pathor").item(0).getTextContent()) + "/OR.txt";
                sPathDrivers = Common
                    .convertPathWindowsFormat(eElement.getElementsByTagName("pathdrivers").item(0).getTextContent());
                sPathLog = Common
                    .convertPathWindowsFormat(eElement.getElementsByTagName("pathlog").item(0).getTextContent());
                sOptionDFTMdSelect = eElement.getElementsByTagName("optiondftmdselect").item(0).getTextContent();
                sUserB = eElement.getElementsByTagName("userB").item(0).getTextContent();
                sPasswordB = eElement.getElementsByTagName("passwordB").item(0).getTextContent();
            }
        }
    }
}

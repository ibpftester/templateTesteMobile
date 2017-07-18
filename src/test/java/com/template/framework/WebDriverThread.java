package com.template.framework;

import static com.template.framework.config.DriverType.CHROME;
import static com.template.framework.config.DriverType.valueOf;

import java.net.MalformedURLException;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.template.framework.config.DriverType;

public class WebDriverThread {

	private WebDriver webdriver;

	private DriverType selectedDriverType;

	private final DriverType defaultDriverType = CHROME;

	private final String browser = System.getProperty("browser").toUpperCase();

	private final String operatingSystem = System.getProperty("os.name").toUpperCase();

	private final String systemArchitecture = System.getProperty("os.arch");

	public WebDriver getDriver() throws Exception {
		if (null == webdriver) {
			selectedDriverType = determineEffectiveDriverType();
			DesiredCapabilities desiredCapabilities = selectedDriverType.getDesiredCapabilities();
			instantiateWebDriver(desiredCapabilities);
		}
		return webdriver;
	}

	public void quitDriver() {
		if (null != webdriver) {
			webdriver.quit();
		}
	}

	private DriverType determineEffectiveDriverType() {
		DriverType driverType = defaultDriverType;
		try {
			driverType = valueOf(browser);
		} catch (IllegalArgumentException ignored) {
			System.err.println("Unknown driver specified, defaulting to '" + driverType + "'...");
		} catch (NullPointerException ignored) {
			System.err.println("No driver specified, defaulting to '" + driverType + "'...");
		}
		return driverType;
	}

	private void instantiateWebDriver(DesiredCapabilities desiredCapabilities) throws MalformedURLException {
		System.out.println(" ");
		System.out.println("Current Operating System: " + operatingSystem);
		System.out.println("Current Architecture: " + systemArchitecture);
		System.out.println("Current Browser Selection: " + selectedDriverType);
		System.out.println(" ");
		webdriver = selectedDriverType.getWebDriverObject(desiredCapabilities);
	}
}
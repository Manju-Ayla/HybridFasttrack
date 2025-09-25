package org.example;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.remote.MobileCapabilityType;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.time.Duration;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.nio.file.Paths;

public class sampleTest {
    private AndroidDriver driver;
    private static ExtentReports extent;
    private ExtentTest test;

    @BeforeSuite
    public void setupReport() {
        // ✅ Put report in target folder for easy access
        ExtentSparkReporter reporter = new ExtentSparkReporter("target/extent-reports/test-report.html");
        extent = new ExtentReports();
        extent.attachReporter(reporter);
    }


    @BeforeClass
    public void setUp() throws MalformedURLException {
        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability(MobileCapabilityType.PLATFORM_NAME, "Android");
        caps.setCapability(MobileCapabilityType.DEVICE_NAME, "Pixel 6a");
        caps.setCapability(MobileCapabilityType.AUTOMATION_NAME, "UiAutomator2");
        caps.setCapability("appPackage", "com.aylanetworks.fasttrack");
        caps.setCapability("appActivity", "com.aylanetworks.hybridft.MainActivity");

        driver = new AndroidDriver(new URL("http://127.0.0.1:4723"), caps);
    }

    @Test
    public void enterCredentialsAndSignIn() throws InterruptedException, IOException {
        test = extent.createTest("Login Test", "Verify user can log in successfully");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Wait for login screen
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                AppiumBy.id("com.aylanetworks.fasttrack:id/userNameEditText")));

        // Enter credentials
        driver.findElement(AppiumBy.id("com.aylanetworks.fasttrack:id/userNameEditText"))
                .sendKeys("mobiletestcore@gmail.com");
        driver.findElement(AppiumBy.id("com.aylanetworks.fasttrack:id/passwordEditText"))
                .sendKeys("Ayla12345");

        // Hide keyboard if open
        try {
            driver.hideKeyboard();
        } catch (Exception e) {
            System.out.println("Keyboard not open, continuing...");
        }

        // Wait for Sign In button to be clickable, then click
        WebDriverWait buttonWait = new WebDriverWait(driver, Duration.ofSeconds(10));
        buttonWait.until(ExpectedConditions.elementToBeClickable(
                AppiumBy.id("com.aylanetworks.fasttrack:id/buttonSignIn"))).click();

        Thread.sleep(1000); // allow screen transition

        // Wait for home screen (drawer layout = stable element)
        WebDriverWait homeWait = new WebDriverWait(driver, Duration.ofSeconds(30));
        homeWait.until(ExpectedConditions.visibilityOfElementLocated(
                AppiumBy.id("com.aylanetworks.fasttrack:id/drawer_layout")));

        test.pass("✅ Login successful - Device list (home screen) is visible.");
        takeScreenshot("DeviceListPage");

        // Keep app open for 2 minutes to inspect manually
        System.out.println("⏳ App is running. Inspect manually for 30 seconds...");
        Thread.sleep(30000); // 2 minutes wait
    }

    @AfterMethod
    public void captureScreenshotOnFailure(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE) {
            try {
                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

                File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                String fileName = "screenshots/" + result.getName() + "_" + timestamp + ".png";
                File destFile = new File(fileName);
                destFile.getParentFile().mkdirs();
                Files.copy(screenshot.toPath(), destFile.toPath());

                String pageSourceFile = "screenshots/" + result.getName() + "_" + timestamp + ".xml";
                Files.writeString(Paths.get(pageSourceFile), driver.getPageSource());

                if (test != null) {
                    test.fail("Test Failed: " + result.getThrowable());
                    test.addScreenCaptureFromPath(destFile.getAbsolutePath());
                }
                System.out.println("📸 Screenshot saved at: " + destFile.getAbsolutePath());
                System.out.println("📄 Page source saved at: " + pageSourceFile);
            } catch (Exception e) {
                System.out.println("⚠️ Could not capture screenshot: " + e.getMessage());
            }
        }
    }

    private void takeScreenshot(String name) throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        String fileName = "screenshots/" + name + "_" + timestamp + ".png";
        File destFile = new File(fileName);
        destFile.getParentFile().mkdirs();
        Files.copy(screenshot.toPath(), destFile.toPath());
        if (test != null) {
            test.addScreenCaptureFromPath(destFile.getAbsolutePath());
        }
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @AfterSuite
    public void flushReport() {
        if (extent != null) {
            extent.flush();
        }
    }
}

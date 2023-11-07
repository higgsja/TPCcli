package com.hpi.ofxFileHandling;

import com.hpi.appcontrollers.CmdLineController;
import com.hpi.TPCCMcontrollers.BrokersController;
import com.hpi.ofxAggregates.OfxAggregateBase;
import com.hpi.ofxAggregates.OfxOfx;
import com.hpi.TPCCMcontrollers.CMLanguageController;
import com.hpi.TPCCMcontrollers.CMDBController;
import com.hpi.hpiUtils.CMProgressBarCLI;
import com.hpi.TPCCMprefs.*;
import com.hpi.hpiUtils.CMHPIUtils;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import javax.swing.JOptionPane;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.firefox.*;

/**
 *
 */
public class OfxFileController
    extends OfxAggregateBase
{

    private OfxOfx ofx;
    private Document doc;
    private final String errorPrefix;
    private String fErrorPrefix;
    CMProgressBarCLI bar;
    private ArrayList<String> fileList;
    //*** Singleton
    private static OfxFileController instance;

    protected OfxFileController()
    {
        // protected prevents instantiation outside of package
        this.doc = null;

        this.errorPrefix
            = this.getClass().getName();
        this.fErrorPrefix = null;
    }

    public synchronized static OfxFileController getInstance()
    {
        if (OfxFileController.instance == null)
        {
            OfxFileController.instance = new OfxFileController();
        }
        return OfxFileController.instance;
    }

    /**
     *
     * @param sDirectory
     */
    public void processETradeDownloadCrm(String sDirectory)
    {
        WebDriver driver;
        ChromeOptions options;
//        JavascriptExecutor js;
//        Map<String, Object> vars;

        System.setProperty("webdriver.chrome.driver", "/home/white/OneDrive/Documents/Dev/Apps/lib/chromedriver");
        options = new ChromeOptions();
        options
            .addArguments("--user-agent=" + "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/97.0.4692.71 Safari/537.36");
        options.addArguments("--disable-blink-features=AutomationControlled");
        driver = new ChromeDriver(options);
//        js = (JavascriptExecutor) driver;
//        vars = new HashMap<String, Object>();

        driver.get("https://us.etrade.com/e/t/user/login?TARGET=/e/t/invest/downloadofxtransactions?fp%3DTH");
        driver.manage().window().setSize(new Dimension(1589, 885));

        if (!driver.getTitle().contentEquals("Log On to E*TRADE | E*TRADE Financial"))
        {
            //failed
            return;
        }

        {
            WebElement user = driver.findElement(By.name("USER"));
            WebElement password = driver.findElement(By.name("PASSWORD"));
            WebElement login = driver.findElement(By.id("logon_button"));

            user.sendKeys("higgsja3082");
            password.sendKeys("J!gger3082");
            login.click();
        }

        //test on right page
        if (!driver.getTitle()
            .contentEquals("E*TRADE FINANCIAL - Account records - Bank - Account Details - Download"))
        {
            //failed
            return;
        }
        {
            WebElement dropdown = driver.findElement(By.id("dataFormat"));
            dropdown.sendKeys("Quicken");
//            dropdown.findElement(By
//                .xpath("//option[. = 'Quicken Web Connect For Quicken 2004 and Quicken for Macintosh']")).click();
        }
//        {
//            WebElement element = driver.findElement(By.id("dataFormat"));
//            Actions builder = new Actions(driver);
//            builder.moveToElement(element).clickAndHold().perform();
//        }
//        {
//            WebElement element = driver.findElement(By.id("dataFormat"));
//            Actions builder = new Actions(driver);
//            builder.moveToElement(element).perform();
//        }
//        {
//            WebElement element = driver.findElement(By.id("dataFormat"));
//            Actions builder = new Actions(driver);
//            builder.moveToElement(element).release().perform();
//        }

//        driver.findElement(By.cssSelector(".row:nth-child(2) .col-centered-8")).click();
//        driver.findElement(By.id("FromDate")).click();
//        driver.findElement(By.id("FromDate")).click();
        {
            WebElement element = driver.findElement(By.id("FromDate"));
            //Actions builder = new Actions(driver);
            //builder.doubleClick(element).perform();
            element.sendKeys("01/01/22");
        }

        {
            WebElement element = driver.findElement(By.id("ToDate"));
            //Actions builder = new Actions(driver);
            //builder.doubleClick(element).perform();
            element.sendKeys("01/22/22");
        }

        driver.quit();

    }

    /**
     *
     * @param sDirectory
     */
    public void processETradeDownloadFF(String sDirectory)
    {
        WebDriver driver;
        WebElement webElement;
        FirefoxOptions options;

        // delete file if exists
        try
        {
            Files.deleteIfExists(Paths.get("/home/white/Documents/Quicken/17780196.qfx"));
        } catch (IOException e)
        {

        }

        System.setProperty("webdriver.gecko.driver", "/home/white/OneDrive/Documents/Dev/Apps/lib/geckodriver");

        options = new FirefoxOptions();
        options.addPreference("browser.download.folderList", 2);
        options.addPreference("browser.download.manager.showWhenStarting", false);

        //Set downloadPath
        options.addPreference(
            "browser.download.dir", "/home/white/OneDrive/Documents/Quicken");

        //Set File Open &amp; Save preferences
        options.addPreference("browser.helperApps.neverAsk.openFile",
            "text/csv,"
            + "application/x-msexcel,"
            + "application/excel,"
            + "application/x-excel,application/vnd.ms-excel,"
            + "image/png,"
            + "image/jpeg,"
            + "text/html,"
            + "text/plain,"
            + "application/msword,"
            + "application/xml");

        options.addPreference("browser.helperApps.neverAsk.saveToDisk",
            "text/csv,"
            + "application/octet-stream"
            + "application/x-msexcel,"
            + "application/excel,"
            + "application/x-excel,"
            + "application/vnd.ms-excel,"
            + "image/png,"
            + "image/jpeg,text/html,"
            + "text/plain,"
            + "application/msword,"
            + "application/xml");

        options.addPreference("browser.helperApps.alwaysAsk.force", false);
        options.addPreference("browser.download.manager.alertOnEXEOpen", false);
        options.addPreference("browser.download.manager.focusWhenStarting", false);
        options.addPreference("browser.download.manager.useWindow", false);
        options.addPreference("browser.download.manager.showAlertOnComplete", false);
        options.addPreference("browser.download.manager.closeWhenDone",
            false);

        driver = new FirefoxDriver(options);

        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

        //Open URL
        //driver.get("https://clientcenter.tradestation.com/support/myaccount/Equities/TaxCenter.aspx");
        driver.get("https://us.etrade.com/e/t/invest/downloadofxtransactions?fp=TH");

        if (!driver.getTitle().contentEquals("Log On to E*TRADE | E*TRADE Financial"))
        {
            //failed
            return;
        }

        driver.findElement(By.id("user_orig")).sendKeys("higgsja3082");
        driver.findElement(By.name("PASSWORD")).sendKeys("J!gger3082");
        driver.findElement(By.id("logon_button")).click();

        // finally on the page
//        WebElement element = driver.findElement(By.id("dataFormat"));
//        element.sendKeys("Quicken");
//        element.sendKeys(Keys.ENTER);
        //from date
        driver.findElement(By.id("FromDate")).clear();
        driver.findElement(By.id("FromDate")).sendKeys("01/01/22");
//        element.sendKeys(Keys.ENTER);
        //end date
        driver.findElement(By.id("ToDate")).clear();
        driver.findElement(By.id("ToDate")).sendKeys("01/20/22");

        webElement = driver.findElement(By.id("dataFormat"));
        webElement.sendKeys("Quicken");
//        element.sendKeys(Keys.ENTER);

//        try
//        {
//            Thread.sleep(5000);
//        } catch (InterruptedException ex)
//        {
//            java.util.logging.Logger.getLogger(OfxFileController.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        element.sendKeys(Keys.ESCAPE);
        // do not like it but fails to click the button otherwise
//        try
//        {
//            Thread.sleep(500);
//        } catch (InterruptedException ex)
//        {
//            java.util.logging.Logger.getLogger(OfxFileController.class.getName()).log(Level.SEVERE, null, ex);
//        }
        webElement = driver.findElement(By.id("downloadBtn"));
        webElement.sendKeys(Keys.ENTER);
//        driver.findElement(By.id("downloadBtn")).click();

        // todo: check that file exists instead
//        try
//        {
//            Thread.sleep(5000);
//        }
//        catch (InterruptedException ex)
//        {
//            java.util.logging.Logger.getLogger(OfxFileController.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        driver.close();
//        driver.quit();
//        // delete file if exists
//        try
//        {
//            Files.deleteIfExists(Paths.get(
//                  "/home/white/Documents/Quicken/17531730.qfx"));
//        }
//        catch (IOException e)
//        {
//
//        }
//        driver.findElement(By.id("ddlAccounts")).sendKeys("1");
        // finally on the page; select the right file format
//        driver.findElement(By.id("ddlFileTypes")).sendKeys("Q");
//        driver.findElement(By.id("ddlFileTypes")).sendKeys(Keys.ENTER);
        // do not like it but fails to click the button otherwise
//        try
//        {
//            Thread.sleep(500);
//        }
//        catch (InterruptedException ex)
//        {
//            java.util.logging.Logger.getLogger(OfxFileController.class.getName()).log(Level.SEVERE, null, ex);
//        }
//
////        driver.findElement(By.id("btnSearch")).click();
//
//        // todo: check that file exists instead
//        try
//        {
//            Thread.sleep(5000);
//        }
//        catch (InterruptedException ex)
//        {
//            java.util.logging.Logger.getLogger(OfxFileController.class.getName()).log(Level.SEVERE, null, ex);
//        }
////        driver.close();
        //likely need to pause ...
        driver.quit();

    }

    /**
     *
     * @param sDirectory
     */
    public void processTradeStationDownload(String sDirectory)
    {
        String s;
        WebDriver driver;
        FirefoxOptions options;

        // delete file if exists
        try
        {
            Files.deleteIfExists(Paths.get(
                "/home/white/Documents/Quicken/17780196.qfx"));
        } catch (IOException e)
        {

        }

        System.setProperty("webdriver.gecko.driver",
            "/home/white/Documents/Dev/Apps/lib/geckodriver");

        options = new FirefoxOptions();
        options.addPreference("browser.download.folderList", 2);
        options.addPreference(
            "browser.download.manager.showWhenStarting", false);

        //Set downloadPath
        options.addPreference(
            "browser.download.dir", "/home/white/Documents/Quicken");

        //Set File Open &amp; Save preferences
        options.addPreference("browser.helperApps.neverAsk.openFile",
            "text/csv,"
            + "application/x-msexcel,"
            + "application/excel,"
            + "application/x-excel,application/vnd.ms-excel,"
            + "image/png,"
            + "image/jpeg,"
            + "text/html,"
            + "text/plain,"
            + "application/msword,"
            + "application/xml");

        options.addPreference("browser.helperApps.neverAsk.saveToDisk",
            "text/csv,"
            + "application/octet-stream"
            + "application/x-msexcel,"
            + "application/excel,"
            + "application/x-excel,"
            + "application/vnd.ms-excel,"
            + "image/png,"
            + "image/jpeg,text/html,"
            + "text/plain,"
            + "application/msword,"
            + "application/xml");
        options.addPreference("browser.helperApps.alwaysAsk.force", false);
        options.addPreference("browser.download.manager.alertOnEXEOpen",
            false);
        options.addPreference("browser.download.manager.focusWhenStarting",
            false);
        options.addPreference("browser.download.manager.useWindow", false);
        options.addPreference("browser.download.manager.showAlertOnComplete",
            false);
        options.addPreference("browser.download.manager.closeWhenDone",
            false);

        driver = new FirefoxDriver(options);

        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

        //Open URL
        driver.get("https://clientcenter.tradestation.com/support/myaccount/Equities/TaxCenter.aspx");

        if (!driver.getTitle().contentEquals("TradeStation Secure Client Login"))
        {
            //failed
            return;
        }

        driver.findElement(By.id("UserName")).sendKeys("BlueSoccer01");
        driver.findElement(By.id("Password")).sendKeys("ArmyMule76");
        driver.findElement(By
            .xpath("/html/body/div/div/div/div[2]/div/div[2]/div[2]/div[1]/section/form/div[3]/div/input")).click();
        // get security question
        s = driver.findElement(By
            .xpath("/html/body/div/div/div/div[2]/div/div[2]/div[2]/div[1]/section/form/div[1]/div[2]/label"))
            .getText();

        switch (s)
        {
            case "What is the name of your first pet?":
                driver.findElement(By.id("SecurityAnswer")).sendKeys("gustavus");
                driver.findElement(By.id("btnSubmit")).click();

                break;
            case "What is your mother's maiden name?":
                driver.findElement(By.id("SecurityAnswer")).sendKeys("garland");
                driver.findElement(By.id("btnSubmit")).click();
                break;
            case "What is your favorite hobby?":
                driver.findElement(By.id("SecurityAnswer")).sendKeys("golf");
                driver.findElement(By.id("btnSubmit")).click();
                break;
            default:
            // error
        }

        // finally on the page; select the right file format
        driver.findElement(By.id("ddlFileTypes")).sendKeys("Q");
//        driver.findElement(By.id("ddlFileTypes")).sendKeys(Keys.ENTER);

        // do not like it but fails to click the button otherwise
        try
        {
            Thread.sleep(500);
        } catch (InterruptedException ex)
        {
            java.util.logging.Logger.getLogger(OfxFileController.class.getName()).log(Level.SEVERE, null, ex);
        }

        driver.findElement(By.id("btnSearch")).click();

        // todo: check that file exists instead
        try
        {
            Thread.sleep(5000);
        } catch (InterruptedException ex)
        {
            java.util.logging.Logger.getLogger(OfxFileController.class.getName()).log(Level.SEVERE, null, ex);
        }
//        driver.close();
//        driver.quit();

        // delete file if exists
        try
        {
            Files.deleteIfExists(Paths.get(
                "/home/white/Documents/Quicken/17531730.qfx"));
        } catch (IOException e)
        {

        }

        driver.findElement(By.id("ddlAccounts")).sendKeys("1");

        // finally on the page; select the right file format
        driver.findElement(By.id("ddlFileTypes")).sendKeys("Q");
//        driver.findElement(By.id("ddlFileTypes")).sendKeys(Keys.ENTER);

        // do not like it but fails to click the button otherwise
        try
        {
            Thread.sleep(500);
        } catch (InterruptedException ex)
        {
            java.util.logging.Logger.getLogger(OfxFileController.class.getName()).log(Level.SEVERE, null, ex);
        }

        driver.findElement(By.id("btnSearch")).click();

        // todo: check that file exists instead
        try
        {
            Thread.sleep(5000);
        } catch (InterruptedException ex)
        {
            java.util.logging.Logger.getLogger(OfxFileController.class.getName()).log(Level.SEVERE, null, ex);
        }
//        driver.close();
        driver.quit();

    }

    /**
     *
     * @param sDirectory
     */
    public void processTradeStationDownload2(String sDirectory)
    {
        String s;
        WebDriver driver;
        // WebElement element;
        FirefoxOptions options;

        // delete file if exists
        try
        {
            Files.deleteIfExists(Paths.get(
                "/home/white/Documents/Quicken/17780196.qfx"));

            Files.deleteIfExists(Paths.get(
                "/home/white/Documents/Quicken/17531730.qfx"));
        } catch (IOException e)
        {

        }

        System.setProperty("webdriver.gecko.driver",
            "/home/white/Documents/Dev/Apps/lib/geckodriver");

        FirefoxBinary firefoxBinary = new FirefoxBinary();
        firefoxBinary.addCommandLineOptions("--headless");

        options = new FirefoxOptions();
        options.setBinary(firefoxBinary);

        options.addPreference("browser.download.folderList", 2);
        options.addPreference(
            "browser.download.manager.showWhenStarting", false);

        //Set downloadPath
        options.addPreference(
            "browser.download.dir", "/home/white/Documents/Quicken");

        //Set File Open &amp; Save preferences
        options.addPreference("browser.helperApps.neverAsk.openFile",
            "text/csv,"
            + "application/x-msexcel,"
            + "application/excel,"
            + "application/x-excel,application/vnd.ms-excel,"
            + "image/png,"
            + "image/jpeg,"
            + "text/html,"
            + "text/plain,"
            + "application/msword,"
            + "application/xml");

        options.addPreference("browser.helperApps.neverAsk.saveToDisk",
            "text/csv,"
            + "application/octet-stream"
            + "application/x-msexcel,"
            + "application/excel,"
            + "application/x-excel,"
            + "application/vnd.ms-excel,"
            + "image/png,"
            + "image/jpeg,text/html,"
            + "text/plain,"
            + "application/msword,"
            + "application/xml");
        options.addPreference("browser.helperApps.alwaysAsk.force", false);
        options.addPreference("browser.download.manager.alertOnEXEOpen", false);
        options.addPreference("browser.download.manager.focusWhenStarting",
            false);
        options.addPreference("browser.download.manager.useWindow", false);
        options.addPreference("browser.download.manager.showAlertOnComplete",
            false);
        options.addPreference("browser.download.manager.closeWhenDone", false);

        driver = new FirefoxDriver(options);

        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

        //Open URL
        driver.get("https://clientcenter.tradestation.com/support/myaccount/Equities/TaxCenter.aspx");

        if (!driver.getTitle().contentEquals("TradeStation Secure Client Login"))
        {
            //failed
            return;
        }

        driver.findElement(By.id("UserName")).sendKeys("BlueSoccer01");
        driver.findElement(By.id("Password")).sendKeys("ArmyMule76");
        driver.findElement(By
            .xpath("/html/body/div/div/div/div[2]/div/div[2]/div[2]/div[1]/section/form/div[3]/div/input")).click();
        // get security question
        s = driver.findElement(By
            .xpath("/html/body/div/div/div/div[2]/div/div[2]/div[2]/div[1]/section/form/div[1]/div[2]/label"))
            .getText();

        switch (s)
        {
            case "What is the name of your first pet?":
                driver.findElement(By.id("SecurityAnswer")).sendKeys("gustavus");
                driver.findElement(By.id("btnSubmit")).click();

                break;
            case "What is your mother's maiden name?":
                driver.findElement(By.id("SecurityAnswer")).sendKeys("garland");
                driver.findElement(By.id("btnSubmit")).click();
                break;
            case "What is your favorite hobby?":
                driver.findElement(By.id("SecurityAnswer")).sendKeys("golf");
                driver.findElement(By.id("btnSubmit")).click();
                break;
            default:
            // error
        }

        // finally on the page; select the right file format
        driver.findElement(By.id("ddlFileTypes")).sendKeys("Q");

        // do not like it but fails to click the button otherwise
        try
        {
            Thread.sleep(500);
        } catch (InterruptedException ex)
        {
            java.util.logging.Logger.getLogger(OfxFileController.class.getName()).log(Level.SEVERE, null, ex);
        }

        driver.findElement(By.id("btnSearch")).click();

        // todo: check that file exists instead
        try
        {
            Thread.sleep(5000);
        } catch (InterruptedException ex)
        {
            java.util.logging.Logger.getLogger(OfxFileController.class.getName()).log(Level.SEVERE, null, ex);
        }

        driver.findElement(By.id("ddlAccounts")).sendKeys("1");

        // do not like it but fails to click the button otherwise
        try
        {
            Thread.sleep(500);
        } catch (InterruptedException ex)
        {
            java.util.logging.Logger.getLogger(OfxFileController.class.getName()).log(Level.SEVERE, null, ex);
        }

        driver.findElement(By.id("btnSearch")).click();

        // todo: check that file exists instead
        try
        {
            Thread.sleep(5000);
        } catch (InterruptedException ex)
        {
            java.util.logging.Logger.getLogger(OfxFileController.class.getName()).log(Level.SEVERE, null, ex);
        }
//        driver.close();
        driver.quit();
    }

    /**
     * Retrieves list of files in the directory databases.config
     * specifies as &lt;QuickenData&gt; and processes them
     *
     * @return
     */
    public Boolean processOfxFiles2SQLSetup()
    {
        String s;
        File folder;
        File[] files;

        // initialize BrokersController
        BrokersController.getInstance();

        // get list of files to process
        this.fileList = new ArrayList<>();
        folder = new File(CMDirectoriesModel.getInstance().
            getProps().getProperty("OfxFiles"));

        files = folder.listFiles();

        if (files == null || files.length == 0)
        {
            return true;
        }

        for (File file : files)
        {
            if (file.isFile())
            {
                this.fileList.add(file.getAbsolutePath());
            }
        }

        if (this.fileList.isEmpty())
        {
            return true;
        }

        for (String file : this.fileList)
        {
            s = String.format(CMLanguageController.getAppProp("FCFormat1"),
                file,
                CMHPIUtils.getLongDate());

            System.out.println("Processing file: " + s + "\n");

            if (!processOfxFile(file))
            {
//                s = String.format(CMLanguageController.getAppProp("FCFormat2"),
//                      file, CMHPIUtils.getInstance().getLongDate());
                //return false;
                continue;
            }

            // ignore the signon message other than status
            if (this.ofx.getSignOnMsgsRSv1().getSonRS().getStatus().getCode() != 0)
            {
                switch (this.ofx.getSignOnMsgsRSv1().getSonRS().
                    getStatus().getCode())
                {
                    case 2000:
                    case 3000:
                    case 3001:
                    case 13504:
                    case 15000:
                    case 15500:
                    case 15501:
                    case 15502:
                    case 15506:
                    case 15507:
                    case 15510:
                    case 15511:
                    case 15512:
                    case 15513:
//                        s = CMLanguageController.getErrorProps().
//                              getProperty("OfxSignonStatus"
//                                    + this.ofx.getSignOnMsgsRSv1().
//                                          getSonRS().getStatus().getCode());
                        break;
                    default:
//                        s = String.format(CMLanguageController.
//                              getErrorProps().getProperty("Formatted3"),
//                              this.ofx.getSignOnMsgsRSv1().getSonRS().
//                                    getStatus().getCode());

                }
                return false;
            }

            if (processOfx2SQL())
            {
//                s = String.format(CMLanguageController.getAppProp("FCFormat3"),
//                      file, CMHPIUtils.getInstance().getLongDate());

                try
                {
                    Files.deleteIfExists(Paths.get(file));
                } catch (IOException ex)
                {
                    // not important
                }
            } else
            {
                return false;
            }
        }
        return true;
    }

    public Boolean processOfxFile2SQLSetup(String sInputFile)
    {
        // String s;

//         s = String.format(CMLanguageController.getAppProp("FCFormat1"),
//               sInputFile,
//               CMHPIUtils.getLongDate());
//         // initialize BrokersController
//         BrokersController.getInstance();
//         // ensure in right database
// //        this.connectDB("OfxBroker");
//         if (!processOfxFile(sInputFile))
//         {
//             s = String.format(CMLanguageController.getAppProp("FCFormat2"),
//                   sInputFile, CMHPIUtils.getLongDate());
//             return false;
//         }
        // ignore the signon message other than status
        if (this.ofx.getSignOnMsgsRSv1().getSonRS().getStatus().getCode() != 0)
        {
            //todo: provide an error message
//            s = "OfxSignonStatus"
//                    + this.ofx.getSignOnMsgsRSv1().getSonRS().
//                            getStatus().getCode().toString();
            // switch (this.ofx.getSignOnMsgsRSv1().getSonRS().
            //       getStatus().getCode())
            // {
            //     case 2000:
            //     case 3000:
            //     case 3001:
            //     case 13504:
            //     case 15000:
            //     case 15500:
            //     case 15501:
            //     case 15502:
            //     case 15506:
            //     case 15507:
            //     case 15510:
            //     case 15511:
            //     case 15512:
            //     case 15513:
            //         s = CMLanguageController.getErrorProps().
            //               getProperty("OfxSignonStatus"
            //                     + this.ofx.getSignOnMsgsRSv1().
            //                           getSonRS().getStatus().getCode());
            //         break;
            //     default:
            //         s = String.format(CMLanguageController.
            //               getErrorProps().getProperty("Formatted3"),
            //               this.ofx.getSignOnMsgsRSv1().getSonRS().
            //                     getStatus().getCode());

            // }
            return false;
        }

        if (processOfx2SQL())
        {
            // s = String.format(CMLanguageController.getAppProp("FCFormat3"),
            //       sInputFile, CMHPIUtils.getLongDate());

            return true;
        }

        return false;
    }

    public void processOfxDoc2SQLSetup(Document doc, CMProgressBarCLI bar)
    {
        String s;

        // initialize BrokersController
        BrokersController.getInstance();

        this.doc = doc;
        this.bar = bar;
        this.ofx = new OfxOfx();

        this.processOfxDoc();

        // ignore the signon message other than status
        if (this.ofx.getSignOnMsgsRSv1().getSonRS().getStatus().getCode() != 0)
        {
            //todo: provide an error message
//            s = "OfxSignonStatus"
//                    + this.ofx.getSignOnMsgsRSv1().getSonRS().
//                            getStatus().getCode().toString();
            switch (this.ofx.getSignOnMsgsRSv1().getSonRS().
                getStatus().getCode())
            {
                case 2000:
                case 3000:
                case 3001:
                case 13504:
                case 15000:
                case 15500:
                case 15501:
                case 15502:
                case 15506:
                case 15507:
                case 15510:
                case 15511:
                case 15512:
                case 15513:
                    s = CMLanguageController.getErrorProps().
                        getProperty("OfxSignonStatus"
                            + this.ofx.getSignOnMsgsRSv1().
                                getSonRS().getStatus().getCode());
                    break;
                default:
                    s = String.format(CMLanguageController.
                        getErrorProps().getProperty("Formatted3"),
                        this.ofx.getSignOnMsgsRSv1().getSonRS().getStatus().getCode());
            }
            s = String.format(CMLanguageController.
                getErrorProp("GeneralError"), s);

            CMHPIUtils.showDefaultMsg(CMLanguageController.getAppProp("Title")
                + CMLanguageController.getErrorProp("Title"),
                Thread.currentThread().getStackTrace()[1].getClassName() + "\n\t",
                Thread.currentThread().getStackTrace()[1].getMethodName(),
                s,
                JOptionPane.ERROR_MESSAGE);
            return;
//            throw new UnsupportedOperationException(s);
        }

        this.barLabel("        Updating database ...");

        processOfx2SQL();
    }

    private Boolean processOfx2SQL()
    {
        // from this ofx object, use <invacctfrom> to establish
        // the broker and account
        // returns the AcctId
        // ensure in the right database
//        CMDBController.getInstance().close();
//        this.connectDB("OfxBroker");
        this.ofx.getInvStmtMsgsRSv1().getInvStmtTrnRS().
            getInvStmtRS().getInvAcctFrom().
            doSQL(this.ofx.getSignOnMsgsRSv1().getSonRS().getOfxFI());

        this.ofx.getInvStmtMsgsRSv1().getInvStmtTrnRS().
            getInvStmtRS().doSQL(this.ofx.getInvStmtMsgsRSv1().
                getInvStmtTrnRS().getInvStmtRS().
                getInvAcctFrom());

        this.ofx.getSecListMsgsRSv1().getSecList().doSQL(this.ofx.
            getInvStmtMsgsRSv1().
            getInvStmtTrnRS().getInvStmtRS().
            getInvAcctFrom());

        // run any required stored procedures after all Ofx work is complete
        ofxBrokerAdjustments();

        return true;
    }

    public Boolean processOfxFile(String sInputFile)
    {
        if (!processFile2XMLDoc(sInputFile))
        {
            return false;
        }

        return processOfxDoc();
    }

    public Boolean processOfxDoc()
    {
        Element e1;
        String s;

        this.ofx = new OfxOfx();

        if ((e1 = doc.select("ofx").first()) == null)
        {
            s = String.format(CMLanguageController.
                getErrorProp("OfxResponseEmpty"));

            CMHPIUtils.showDefaultMsg(CMLanguageController.getAppProp("Title")
                + CMLanguageController.getErrorProp("Title"),
                Thread.currentThread().getStackTrace()[1].getClassName(),
                Thread.currentThread().getStackTrace()[1].
                    getMethodName(),
                s,
                JOptionPane.ERROR_MESSAGE);

            throw new UnsupportedOperationException(s);
        }

        return this.ofx.doData(e1);
    }

    /*
     * Given an input file, run through conversion to XML
     *
     * @param sInputFile
     * @return
     */
    public Boolean processFile2XMLDoc(String sInputFile)
    {
        // conversion of file to XML, returned in 'doc'
        return (doc = FixSGML2XML.getInstance().
            doSGMLFile2XML(sInputFile)) != null;
    }

    /*
     * Given an input file, run through conversion to XML
     * and place results in an output file
     *
     * @param sInputFile
     * @param sOutputFile
     * @return
     */
    public Boolean processFile2XMLFile(String sInputFile, String sOutputFile)
    {
        fErrorPrefix = Thread.currentThread().getStackTrace()[2].getMethodName();

        Path path;
        String s;

        // test validity of output file
        path = Paths.get(sOutputFile);
        try ( OutputStream output = Files.newOutputStream(path,
            StandardOpenOption.WRITE, StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING))
        {
            output.close();

            // conversion of file to XML, returned in 'doc'
            if (!this.processFile2XMLDoc(sInputFile))
            {
                Files.delete(path);
                return false;
            }
        } catch (IOException ex)
        {
            // unable to create the output file
            s = String.format(CMLanguageController.
                getErrorProps().getProperty("Formatted10"),
                sOutputFile);
            CMHPIUtils.showDefaultMsg(CMLanguageController.getErrorProps().
                getProperty("Title"),
                errorPrefix,
                fErrorPrefix,
                s,
                JOptionPane.ERROR_MESSAGE);
            return false;
        }

        try ( BufferedWriter output = Files.newBufferedWriter(path,
            Charset.forName("US-ASCII"),
            StandardOpenOption.WRITE, StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING))
        {
            s = doc.toString();
            output.write(s, 0, s.length());
            output.close();
        } catch (IOException ex)
        {
            // unable to create the output file
            s = String.format(CMLanguageController.
                getErrorProps().getProperty("Formatted10"),
                sOutputFile);
            CMHPIUtils.showDefaultMsg(CMLanguageController.getErrorProps().
                getProperty("Title"),
                errorPrefix,
                fErrorPrefix,
                s,
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    void barUpdate(Integer numerator, Integer denominator)
    {
        // CmdLineController cmdLineController;

        // cmdLineController = CmdLineController.getInstance();
        if (CmdLineController.getsCLIProgressBar().equalsIgnoreCase("false"))
        {
            return;
        }

        this.bar.barUpdate(numerator, denominator);
    }

    void barLabel(String sLabel)
    {
        // CmdLineController cmdLineController;

        // cmdLineController = CmdLineController.getInstance();
        if (CmdLineController.getsCLIProgressBar().equalsIgnoreCase("true"))
        {
            this.bar.barLabel(sLabel);
        }
    }

    private void ofxBrokerAdjustments()
    {
        //none at this time
//        int i = 0;
        //we have created a unique EquityId field in SecInfo and must populate it
        //stock: set to SecInfo.Ticker
        //options: set to OCC id
        //mutual funds: set to SecInfo.SecName
        //had a stored procedure to handle this
        //moved to ininital creation of the transaction
//        CMDBController.callStored("hlhtxc5_dbOfx.setEquityId");

//        String sql, db;

//        db = "hlhtxc5_dbOfx.";
//
//        // Need to guarantee common EquityId across all providers
//        // so create our own
//        // Set EquityId for mutual funds
//        sql = "update " + db + "SecInfo "
//              + "inner join hlhtxc5_dbOfx.MFInfo on "
//              + "hlhtxc5_dbOfx.SecInfo.BrokerId = hlhtxc5_dbOfx.MFInfo.BrokerId "
//              + "and hlhtxc5_dbOfx.SecInfo.SecId = hlhtxc5_dbOfx.MFInfo.SecId "
//              + "set EquityId = hlhtxc5_dbOfx.SecInfo.SecName "
//              + ";";
//        CMDBController.executeSQLSingleIntegerList(sql);
//
        // Set EquityId for Options
//        sql = "update " + db + "SecInfo "
//              + "inner join hlhtxc5_dbOfx.OptInfo on (hlhtxc5_dbOfx.SecInfo.BrokerId = hlhtxc5_dbOfx.OptInfo.BrokerId "
//            + "and hlhtxc5_dbOfx.SecInfo.SecId = hlhtxc5_dbOfx.OptInfo.SecId) "
//              + "set EquityId = hlhtxc5_dbOfx.optionToOCC(hlhtxc5_dbOfx.SecInfo.Ticker, "
//              + "left(hlhtxc5_dbOfx.OptInfo.DtExpire, 8), "
//              + "hlhtxc5_dbOfx.OptInfo.OptType, hlhtxc5_dbOfx.OptInfo.StrikePrice) "
//            + "where left(hlhtxc5_dbOfx.SecInfo.Ticker, 1) <> '#\';";
//        CMDBController.executeSQL(sql);

//        // Set EquityId for Stock
//        sql = "update " + db + "SecInfo "
//              + "inner join hlhtxc5_dbOfx.StockInfo on hlhtxc5_dbOfx.SecInfo.BrokerId = hlhtxc5_dbOfx.StockInfo.BrokerId "
//              + "and hlhtxc5_dbOfx.SecInfo.SecId = hlhtxc5_dbOfx.StockInfo.SecId "
//              + "set EquityId = hlhtxc5_dbOfx.SecInfo.Ticker "
//              + ";";
//        CMDBController.executeSQL(sql);
    }

//    private void connectDB(String sActiveDBType)
//    {
//        TPCCMDatabaseModel.DB db;
//
//        db = TPCCMDatabaseModel.getInstance().getActiveDB(sActiveDBType);
//
//        if (!CMDBController.getDbName().equalsIgnoreCase(db.getsDbName()))
//        {
//            // current connection is the wrong one
//            CMDBController.getInstance(
//                  db.getsDbName(),
//                  db.getsFullURL(),
//                  db.getsUId(),
//                  db.getsPW(),
//                  db.getsDriver());
//        }
//    }
}

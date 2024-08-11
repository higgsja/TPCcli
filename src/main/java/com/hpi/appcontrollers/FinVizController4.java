package com.hpi.appcontrollers;

import com.hpi.entities.FinVizEquityInfoModel;
import com.hpi.hpiUtils.CMProgressBarCLI;
import com.hpi.hpiUtils.CMHPIUtils;
import com.hpi.TPCCMcontrollers.*;
import java.io.*;
import java.net.UnknownHostException;
import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.swing.JOptionPane;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class FinVizController4
{

    private static int iRow;
    private static int iTotalRows;
    private static int iTotalRows2;
    private static Element dataTable;
    private static final ArrayList<FinVizEquityInfoModel> EQUITY_INFO_MODELS;
    private static final CMProgressBarCLI BAR_CLI;

    static
    {
        BAR_CLI = new CMProgressBarCLI(CmdLineController.getsCLIProgressBar());
        EQUITY_INFO_MODELS = new ArrayList<>();
    }

    public static final void doEquityInfo()
    {
        // first time through will get us the total row count
        iRow = 0;
        iTotalRows = 0;
        iTotalRows2 = 0;

        getDataTable();

//        BAR_CLI.barUpdate(0, iTotalRows * 2);
        iRow++;
        while (iRow <= iTotalRows)
        {
            //finViz tables have 20 rows; do 4 tables before wait
            //point is to not trigger the 'too many requests' fail
            if (iRow > 1
                && ((iRow - 1) / 80.0) == ((iRow - 1) / 80))
            {
                try
                {
                    Thread.sleep(3000);
                    BAR_CLI.barUpdate(iRow, iTotalRows * 2, "--sleep--");
                } catch (InterruptedException ex)
                {
                    //Logger.getLogger(FinVizController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            getDataTable();
//            BAR_CLI.barUpdate(iRow, iTotalRows * 2);
            if (FinVizController4.dataTable == null)
            {
                String s = "FinViz table appears empty, likely the table class name changed again";

                CMHPIUtils.showDefaultMsg(
                    CMLanguageController.getErrorProps().
                        getProperty("Title"),
                    Thread.currentThread().getStackTrace()[1].
                        getClassName(),
                    Thread.currentThread().getStackTrace()[1].
                        getMethodName(),
                    s,
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            doEquities();
        }

        doSQL();
    }

    private static void getDataTable()
    {
        // &r= establishes first row to display; iterate through those at 20 each
        // append the row count

        Iterator<Element> iteratorT;
        Boolean finVizError = true;
        Document doc = null;

        String sCustom = "v=152&c=1,2,3,4,5,6,7,8,9,14,15,16,17,18,19,20,49,52,53,54,55,56,57,58,59,62,65,67,68,69,48";
        sCustom += "&r=";

        String sUrlQuery = "https://finviz.com/screener.ashx";
        sUrlQuery += "?" + sCustom;

        try
        {
            while (finVizError)
            {
                //loop through all pages in FinViz, tracked by finVizError2
                sUrlQuery = sUrlQuery + Integer.toString(iRow);
                doc = Jsoup
                    .connect(sUrlQuery)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                        + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36")
                    .get();

//  put the html to a file for evaluation
//                final File f = new File("/home/white/OneDrive/Documents/Dev/TPCcli/html/finVizScrape.html");
//                FileUtils.writeStringToFile(f, doc.outerHtml(), StandardCharsets.UTF_8);
//
                if (doc.body().text().equalsIgnoreCase("Too many requests."))
                {
                    try
                    {
                        Thread.sleep(15000);
                        System.out.println("Too many requests triggered; rows: " + iRow);
                    } catch (InterruptedException ex)
                    {
                        //Logger.getLogger(FinVizController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else
                {
                    finVizError = false;
                }
            }

            if (iRow == 0)
            {
                //retrieve number of stocks finViz returned
                //first call get total rows for future comparison
                //subsequent pages must match this or it fails
                //this can happen if finViz adds/subtracts any equity
                //while we are iterating
                iTotalRows = getStocksNumber(doc);
                return;
            } else
            {
                iTotalRows2 = getStocksNumber(doc);
                if (iTotalRows2 != iTotalRows)
                {
                    String s = String.format(CMLanguageController.
                        getErrorProps().getProperty("Formatted17"),
                        sUrlQuery);

                    CMHPIUtils.showDefaultMsg(
                        CMLanguageController.getErrorProps().
                            getProperty("Title"),
                        Thread.currentThread().getStackTrace()[1].
                            getClassName(),
                        Thread.currentThread().getStackTrace()[1].
                            getMethodName(),
                        s,
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            //retrieve the element with the page data
            FinVizController4.dataTable = getPageData(doc);
        } catch (UnknownHostException e)
        {
            String s = String.format(CMLanguageController.
                getErrorProps().getProperty("Formatted18"),
                sUrlQuery);

            CMHPIUtils.showDefaultMsg(
                CMLanguageController.getErrorProps().
                    getProperty("Title"),
                Thread.currentThread().getStackTrace()[1].getClassName(),
                Thread.currentThread().getStackTrace()[1].getMethodName(),
                s,
                JOptionPane.ERROR_MESSAGE);
        } catch (IOException e)
        {
            String s = String.format(CMLanguageController.
                getErrorProps().getProperty("General"),
                e.getMessage());

            CMHPIUtils.showDefaultMsg(
                CMLanguageController.getErrorProps().
                    getProperty("Title"),
                Thread.currentThread().getStackTrace()[1].getClassName(),
                Thread.currentThread().getStackTrace()[1].getMethodName(),
                s,
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private static Integer getStocksNumber(Document doc)
    {
        // get the total number of stocks finViz returned
        //  This is in <div id="screener-total" class="count-text whitespace-nowrap"> #1 / 8687 Total </div>
        Element div;
        Integer divInt;

        if (doc == null)
        {
            return 0;
        }

        div = doc.getElementById("screener-total");
        if (div == null)
        {
            return 0;
        }

        if (div.text() == null)
        {
            return 0;
        }

        divInt = Integer.valueOf(
            div.text()
                .substring(div.text().indexOf("/") + 2,
                    div.text().indexOf("Total") - 1));

        return divInt;
    }

    private static Element getPageData(Document doc)
    {
        Element pageData;

        pageData = null;

        if (doc == null)
        {
            return null;
        }

        //find <table class="styled-table-new"> (table); there is only one
        //pageData = doc.getElementsByClass("table-light is-new").first();
        pageData = doc.getElementsByClass("styled-table-new").first();

        return pageData;
    }

    private static void doEquities()
    {
        String sCell;
        Iterator<Element> iteratorR;
        Element aElement;
        int intC;
        FinVizEquityInfoModel equityInfoModel;

        iteratorR = FinVizController4.dataTable.select("tr").iterator();
        int intR = 0;

        while (iteratorR.hasNext())
        {
            // iterate through rows, keep No. to use on next request
            intR++;
            if (intR == 1)
            {
                // first row is headers
                iteratorR.next();
                continue;
            }

            iRow++;
//            BAR_CLI.barUpdate(iRow, iTotalRows * 2);

            // have a row
            equityInfoModel = new FinVizEquityInfoModel();

            aElement = iteratorR.next();

            //create the cell iterator on the row
            Iterator<Element> iteratorC
                = aElement.select("td").iterator();

            intC = 0;

            while (iteratorC.hasNext())
            {
                intC++;
                // iterate through cells of row
                sCell = iteratorC.next().text();

                if (sCell.contains("<"))
                {
                    sCell = sCell.substring(0, sCell.indexOf('<'));
                }

                switch (intC)
                {
                    case 1: // Ticker
                        sCell = sCell.replaceAll(" ", "");
                        equityInfoModel.setTkr(sCell);
                        BAR_CLI.barUpdate(iRow, iTotalRows * 2, equityInfoModel.getTkr());

                        break;
                    case 2: // Company
                        equityInfoModel.setCompany(sCell);
                        break;
                    case 3: // Sector
                        equityInfoModel.setSector(sCell);
                        break;
                    case 4: // Industry
                        equityInfoModel.setIndustry(sCell);
                        break;
                    case 5: // Country
                        equityInfoModel.setCountry(sCell);
                        break;
                    case 6: // Mkt Cap
                        sCell = sCell.replaceAll(" ", "");
                        sCell = toBillion(sCell);
                        equityInfoModel.setMktCap(sCell);
                        break;
                    case 7: // PE
                        sCell = sCell.replaceAll(" ", "");
                        if (sCell.equalsIgnoreCase("-"))
                        {
                            sCell = "-999.999";
                        }
                        equityInfoModel.setPE(sCell);
                        break;
                    case 8: // FwdPE
                        sCell = sCell.replaceAll(" ", "");
                        if (sCell.equalsIgnoreCase("-"))
                        {
                            sCell = "-999.999";
                        }
                        equityInfoModel.setFwdPE(sCell);
                        break;
                    case 9: // PEG
                        sCell = sCell.replaceAll(" ", "");
                        if (sCell.equalsIgnoreCase("-"))
                        {
                            sCell = "-999.999";
                        }
                        equityInfoModel.setPEG(sCell);
                        break;
                    case 10: // Div
                        sCell = sCell.replaceAll(" ", "");
                        sCell = sCell.replaceAll("%", "");
                        if (sCell.equalsIgnoreCase("-"))
                        {
                            sCell = "-999.999";
                        }
                        equityInfoModel.setDiv(sCell);
                        break;
                    case 11: // Payout Ratio
                        sCell = sCell.replaceAll(" ", "");
                        sCell = sCell.replaceAll("%", "");
                        if (sCell.equalsIgnoreCase("-"))
                        {
                            sCell = "-999.999";
                        }
                        equityInfoModel.setPayoutRatio(sCell);
                        break;
                    case 12: // EPS
                        sCell = sCell.replaceAll(" ", "");
                        if (sCell.equalsIgnoreCase("-"))
                        {
                            sCell = "-999.999";
                        }
                        equityInfoModel.setEPS(sCell);
                        break;
                    case 13: // EPS this year
                        sCell = sCell.replaceAll(" ", "");
                        sCell = sCell.replaceAll("%", "");
                        if (sCell.equalsIgnoreCase("-"))
                        {
                            sCell = "-999.999";
                        }
                        equityInfoModel.setEPSCY(sCell);
                        break;
                    case 14: // EPS next year
                        sCell = sCell.replaceAll(" ", "");
                        sCell = sCell.replaceAll("%", "");
                        if (sCell.equalsIgnoreCase("-"))
                        {
                            sCell = "-999.999";
                        }
                        equityInfoModel.setEPSNY(sCell);
                        break;
                    case 15: // EPS past 5
                        sCell = sCell.replaceAll(" ", "");
                        sCell = sCell.replaceAll("%", "");
                        if (sCell.equalsIgnoreCase("-"))
                        {
                            sCell = "-999.999";
                        }
                        equityInfoModel.setEPSP5Y(sCell);
                        break;
                    case 16: // EPS next 5
                        sCell = sCell.replaceAll(" ", "");
                        sCell = sCell.replaceAll("%", "");
                        if (sCell.equalsIgnoreCase("-"))
                        {
                            sCell = "-999.999";
                        }
                        equityInfoModel.setEPSN5Y(sCell);
                        break;
                    case 17: // ATR
                        sCell = sCell.replaceAll(" ", "");
                        if (sCell.equalsIgnoreCase("-"))
                        {
                            sCell = "-999.999";
                        }
                        equityInfoModel.setATR(sCell);
                        break;
                    case 18: // sma20
                        sCell = sCell.replaceAll(" ", "");
                        sCell = sCell.replaceAll("%", "");
                        if (sCell.equalsIgnoreCase("-"))
                        {
                            sCell = "-999.999";
                        }
                        equityInfoModel.setSMA20(sCell);
                        break;
                    case 19: // sma50
                        sCell = sCell.replaceAll(" ", "");
                        sCell = sCell.replaceAll("%", "");
                        if (sCell.equalsIgnoreCase("-"))
                        {
                            sCell = "-999.999";
                        }
                        equityInfoModel.setSMA50(sCell);
                        break;
                    case 20: // sma200
                        sCell = sCell.replaceAll(" ", "");
                        sCell = sCell.replaceAll("%", "");
                        if (sCell.equalsIgnoreCase("-"))
                        {
                            sCell = "-999.999";
                        }
                        equityInfoModel.setSMA200(sCell);
                        break;
                    case 21: // 50d hi
                        sCell = sCell.replaceAll(" ", "");
                        sCell = sCell.replaceAll("%", "");
                        if (sCell.equalsIgnoreCase("-"))
                        {
                            sCell = "-999.999";
                        }
                        equityInfoModel.setHi50d(sCell);
                        break;
                    case 22: // 50d lo
                        sCell = sCell.replaceAll(" ", "");
                        sCell = sCell.replaceAll("%", "");
                        if (sCell.equalsIgnoreCase("-"))
                        {
                            sCell = "-999.999";
                        }
                        equityInfoModel.setLo50d(sCell);
                        break;
                    case 23: // 52w hi
                        sCell = sCell.replaceAll(" ", "");
                        sCell = sCell.replaceAll("%", "");
                        if (sCell.equalsIgnoreCase("-"))
                        {
                            sCell = "-999.999";
                        }
                        equityInfoModel.setHi52w(sCell);
                        break;
                    case 24: // 52w lo
                        sCell = sCell.replaceAll(" ", "");
                        sCell = sCell.replaceAll("%", "");
                        if (sCell.equalsIgnoreCase("-"))
                        {
                            sCell = "-999.999";
                        }
                        equityInfoModel.setLo52w(sCell);
                        break;
                    case 25: // RSI
                        sCell = sCell.replaceAll(" ", "");
                        if (sCell.equalsIgnoreCase("-"))
                        {
                            sCell = "-999.999";
                        }
                        equityInfoModel.setRSI(sCell);
                        break;
                    case 26: // Analyst recommendation
                        sCell = sCell.replaceAll(" ", "");
                        if (sCell.equalsIgnoreCase("-"))
                        {
                            sCell = "-999.999";
                        }
                        equityInfoModel.setAnRec(sCell);
                        break;
                    case 27: // Price
                        sCell = sCell.replaceAll(",", "");
                        if (sCell.equalsIgnoreCase("-"))
                        {
                            sCell = "-999.999";
                        }
                        equityInfoModel.setPrice(sCell);
                        break;
                    case 28: // Volume
                        sCell = sCell.replaceAll(" ", "");
                        sCell = sCell.replaceAll(",", "");
                        if (sCell.equalsIgnoreCase("-"))
                        {
                            sCell = "-999.999";
                        }
                        equityInfoModel.setVolume(sCell);
                        break;
                    case 29: // earnings: Aug 17, Aug 17/a, Aug 17/b
                        if (sCell.endsWith("a"))
                        {
                            sCell = sCell.replace("/a", "");
                            sCell = earningsDate(sCell);
                            sCell += " ";
                            sCell += "16:16:16";
                            equityInfoModel.setEarnDt(sCell);
                        } else if (sCell.endsWith("b"))
                        {
                            sCell = sCell.replace("/b", "");
                            sCell = earningsDate(sCell);
                            sCell += " ";
                            sCell += "05:05:05";
                            equityInfoModel.setEarnDt(sCell);
                        } else
                        {
                            sCell = earningsDate(sCell);
                            sCell += " ";
                            sCell += "12:12:12";
                            equityInfoModel.setEarnDt(sCell);
                        }
                        break;
                    case 30: // Target Price
                        sCell = sCell.replaceAll(" ", "");
                        sCell = sCell.replaceAll(",", "");
                        if (sCell.equalsIgnoreCase("-"))
                        {
                            sCell = "-999.999";
                        }
                        equityInfoModel.setTgtPrice(sCell);
                        break;
                    case 31: // Beta
                        sCell = sCell.replaceAll(" ", "");
                        sCell = sCell.replaceAll(",", "");
                        if (sCell.equalsIgnoreCase("-"))
                        {
                            sCell = "-999.999";
                        }
                        equityInfoModel.setBeta(sCell);
                        break;
                    default:
                }
            }

            // put it into the database
            EQUITY_INFO_MODELS.add(equityInfoModel);
        }
    }

    private static void doSQL()
    {
        String sql1
            = "insert ignore into hlhtxc5_dmOfx.EquityInfo (Ticker, Company, Sector, Industry, Country, `MktCap(B)`, PE, FwdPE, PEG, `Div`, PayoutRatio, EPS, `EPS/CY`, `EPS/NY`, `EPS/P5Y`, `EPS/N5Y`, ATR, SMA20, SMA50, SMA200, `50dHi`, `50dLo`, `52wHi`, `52wLo`, RSI, AnRec, Price, Volume, EarnDate, TgtPrice, `Date`, Beta) VALUES ";
        sql1 += "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        int rowCount = 0;
        try ( Connection con = CMDBController.getConnection(); //Statement stmt = con.createStatement();
            
             PreparedStatement ps = con.prepareStatement(sql1);)
        {
            con.setAutoCommit(false);

            for (FinVizEquityInfoModel eim : EQUITY_INFO_MODELS)
            {
                iRow++;
                rowCount++;
                BAR_CLI.barUpdate(iRow, iTotalRows * 2, eim.getTkr());

                ps.setString(1, eim.getTkr());
                ps.setString(2, eim.getCompany());
                ps.setString(3, eim.getSector());
                ps.setString(4, eim.getIndustry());
                ps.setString(5, eim.getCountry());

                ps.setString(6, eim.getMktCap());
                ps.setString(7, eim.getPE());
                ps.setString(8, eim.getFwdPE());
                ps.setString(9, eim.getPEG());
                ps.setString(10, eim.getDiv());

                ps.setString(11, eim.getPayoutRatio());
                ps.setString(12, eim.getEPS());
                ps.setString(13, eim.getEPSCY());
                ps.setString(14, eim.getEPSNY());
                ps.setString(15, eim.getEPSP5Y());

                ps.setString(16, eim.getEPSN5Y());
                ps.setString(17, eim.getATR());
                ps.setString(18, eim.getSMA20());
                ps.setString(19, eim.getSMA50());
                ps.setString(20, eim.getSMA200());

                ps.setString(21, eim.getHi50d());
                ps.setString(22, eim.getLo50d());
                ps.setString(23, eim.getHi52w());
                ps.setString(24, eim.getLo52w());
                ps.setString(25, eim.getRSI());

                ps.setString(26, eim.getAnRec());
                ps.setString(27, eim.getPrice());
                ps.setString(28, eim.getVolume());
                ps.setString(29, eim.getEarnDt());
                ps.setString(30, eim.getTgtPrice());

                String st = CMHPIUtils.getShortDateISO();
                LocalDate ld = LocalDate.parse(st);
                ps.setDate(31, java.sql.Date.valueOf(ld));

                ps.setString(32, eim.getBeta());

                ps.addBatch();

                rowCount = 0;
            }

            ps.executeBatch();
        } catch (SQLException e)
        {
            String s = String.format(CMLanguageController.
                getErrorProps().getProperty("GeneralError"),
                e.toString());

            CMHPIUtils.showDefaultMsg(
                CMLanguageController.getErrorProps().
                    getProperty("Title"),
                Thread.currentThread().getStackTrace()[1].
                    getClassName(),
                Thread.currentThread().getStackTrace()[1].
                    getMethodName(),
                s,
                JOptionPane.ERROR_MESSAGE);
        }

        // report number of rows added
        String s = String.format(CMLanguageController.
            getAppProp("EquityInfoReturn"),
            Integer.toString(iTotalRows));

        CMHPIUtils.showDefaultMsg(
            "\r\n" + CMLanguageController.getAppProp("Title"),
            Thread.currentThread().getStackTrace()[1].getClassName(),
            Thread.currentThread().getStackTrace()[1].getMethodName(),
            s,
            JOptionPane.INFORMATION_MESSAGE);
    }

    private static String toBillion(String sString)
    {
        Double dDouble;
        if (sString.equalsIgnoreCase("-"))
        {
            return "-999.999";
        }
        // can have M, B. Make all be in billions
        if (sString.endsWith("B"))
        {
            sString = sString.replace("B", "");
            return sString;
        }

        if (sString.endsWith("M"))
        {
            sString = sString.replace("M", "");
            dDouble = Double.parseDouble(sString) / 1000.00;
            sString = dDouble.toString();
            return sString;
        }

        return sString;
    }

    private String toMillion(String sString)
    {
        Double dDouble;
        if (sString.equalsIgnoreCase("-"))
        {
            return "-999.999";
        }
        // can have M, K. Make all M
        if (sString.endsWith("M"))
        {
            sString = sString.replace("M", "");
            return sString;
        }

        if (sString.endsWith("K"))
        {
            sString = sString.replace("K", "");
            dDouble = Double.parseDouble(sString) / 1000.00;
            sString = dDouble.toString();
            return sString;
        }

        return sString;
    }

    private static String earningsDate(String sString)
    {
        Calendar calNow;
        Calendar calEarnings;
        Date dateNow;
        Date dateConvert;
        DateFormat dateFormat;
        // Given Aug 16
        // get current year

        if (sString.equalsIgnoreCase("-"))
        {
            dateConvert = new Date();
            dateFormat = new SimpleDateFormat("MM/dd/yyyy");

            // no earnings date given, put it well into the future
            calEarnings = Calendar.getInstance();
            calEarnings.setTime(dateConvert);
            calEarnings.add(Calendar.YEAR, 5);
            dateConvert = calEarnings.getTime();

            sString = dateFormat.format(dateConvert);

            return sString;
        }

        calNow = Calendar.getInstance();
        String sYear = String.valueOf(calNow.get(Calendar.YEAR));

        // Turn date object assuming this year
        sString += " " + sYear;
        dateFormat = new SimpleDateFormat("MMM dd yyyy HH:mm:ss",
            Locale.ENGLISH);
        try
        {
            dateNow = new Date();
            dateConvert = dateFormat.parse(sString + " 23:59:59");

            // Check if that put the earnings before today
            if (dateNow.after(dateConvert))
            {
                // earnings are actually in the following year
                //  or, still referring to the last one
                //  either way, add a year
                calEarnings = Calendar.getInstance();
                calEarnings.setTime(dateConvert);
                calEarnings.add(Calendar.YEAR, 1);
                dateConvert = calEarnings.getTime();
            }

            // Convert to string MM/dd/yyyy
            dateFormat = new SimpleDateFormat("MM/dd/yyyy");
            sString = dateFormat.format(dateConvert);
        } catch (ParseException ex)
        {
            //todo: is this an issue
//            ex.printStackTrace();
        }
        return sString;

    }

    public static class MyHostnameVerifier
        implements HostnameVerifier
    {

        /**
         *
         * @param hostname
         * @param session
         *
         * @return
         */
        @Override
        public boolean verify(String hostname, SSLSession session)
        {
            // verification off
            return true;
        }
    }
}

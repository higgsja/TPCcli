package com.hpi.appcontrollers;

import com.hpi.ofxFileHandling.FixSGML2XML;
import com.hpi.ofxFileHandling.OfxFileController;
import com.hpi.hpiUtils.CMProgressBarCLI;
import com.hpi.hpiUtils.CMHPIUtils;
import com.hpi.TPCCMcontrollers.*;
import com.hpi.TPCCMprefs.*;
import com.hpi.TPCCMsql.*;
import java.io.*;
import java.net.*;
import java.net.http.*;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.text.*;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.*;
import javax.swing.JOptionPane;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class OfxDirectDLController
//      extends DBCore
{

    private Document doc;
    private static final CMProgressBarCLI PROGRESS_BAR_CLI;
    /*
     * Singleton
     *
     */
    private static OfxDirectDLController instance;

    static
    {
        OfxDirectDLController.instance = null;
        PROGRESS_BAR_CLI = new CMProgressBarCLI(CmdLineController.
            getsCLIProgressBar());
    }

    protected OfxDirectDLController()
    {
        // protected prevents instantiation outside of package
    }

    public synchronized static OfxDirectDLController getInstance()
    {
        if (OfxDirectDLController.instance == null)
        {
            OfxDirectDLController.instance = new OfxDirectDLController();
        }
        return OfxDirectDLController.instance;
    }
    //***

    public void doDirectOfx()
    {
        PROGRESS_BAR_CLI.barLabel("Processing Financial Institutions:");
        // loop through financial institutions
        for (CMOfxDLFIModel fi : CMOfxDirectModel.getFIMODELS())
        {
            if (fi.getActive().equalsIgnoreCase("Yes"))
            {
                PROGRESS_BAR_CLI.barLabel(
                    "  Processing Financial Institution: " + fi.getFiName());
            } else
            {
                PROGRESS_BAR_CLI.barLabel("  Skipping Financial Institution: " + fi.getFiName());
                continue;
            }

            PROGRESS_BAR_CLI.barLabel("    Processing Accounts:");

            // loop through accounts
            for (CMOfxDLAccountModel acct : fi.getAccountModels())
            {
                if (acct.getAcctActive().equalsIgnoreCase("Yes"))
                {
                    PROGRESS_BAR_CLI.barLabel("      Processing Account: " + acct.getAcctName());
                    // get response from the server into doc
                    this.getInvStmtResponse(fi, acct);
                } else
                {
                    PROGRESS_BAR_CLI.barLabel("      Skipping Account: " + acct.getAcctName());
                }
            }
        }
    }

    public void getInvStmtResponse(CMOfxDLFIModel fi, CMOfxDLAccountModel acct)
    {
        LocalDate startDate;
        DateTimeFormatter formatter;

        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        if (CmdLineController.sDate.isEmpty())
        {
            startDate = getMaxAcctDate(acct);
        } else
        {
            startDate = LocalDate.parse(CmdLineController.sDate, formatter);

        }

        System.out.println("        Start Date: " + startDate.toString());

        // check database for latest date to construct request
        this.getInvStmtData(fi, acct, startDate);
    }

    private LocalDate getMaxAcctDate(CMOfxDLAccountModel acct)
    {
        String sql;
        ResultSet rs;
        LocalDate startDate;
        Integer userId;

        userId = CMDBModel.getUserId();

        sql = "select max(DtTrade) as MaxDate from hlhtxc5_dbOfx.InvTran, Accounts where Accounts.InvAcctIdFi = '"
            + acct.getAcctNumber() + "' " + "and JoomlaId = '" + userId + "' limit 1;";

        try ( Connection con = CMDBController.getConnection();
             PreparedStatement pStmt = con.prepareStatement(sql))
        {
            pStmt.clearWarnings();
            rs = pStmt.executeQuery();

            sql = "";
            while (rs.next())
            {
                // yyyymmdd
                if (rs.getString("MaxDate") != null)
                {
                    sql = rs.getString("MaxDate").substring(0, 8);
                }
            }
            pStmt.close();
            con.close();
        } catch (SQLException ex)
        {
            sql = String.format(
                CMLanguageController.getErrorProp("Formatted14"),
                ex.getMessage());

            CMHPIUtils.showDefaultMsg(
                CMLanguageController.getDBErrorProp("Title"),
                Thread.currentThread().getStackTrace()[1].getClassName(),
                Thread.currentThread().getStackTrace()[1].getMethodName(),
                sql,
                JOptionPane.ERROR_MESSAGE);

            throw new CMDAOException(
                CMLanguageController.getDBErrorProp("Title"),
                Thread.currentThread().getStackTrace()[1].getClassName(),
                Thread.currentThread().getStackTrace()[1].getMethodName(),
                ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        }

        if (!sql.isEmpty())
        {
            startDate = LocalDate.of(
                Integer.parseInt(sql.substring(0, 4)),
                Integer.parseInt(sql.substring(4, 6)),
                Integer.parseInt(sql.substring(6, 8)));
        } else
        {
            startDate = LocalDate.of(1970, Month.JANUARY, 1);
        }

        return startDate;
    }

    private StringBuilder buildRequest(CMOfxDLFIModel fi, CMOfxDLAccountModel acct, LocalDate startDate)
    {

        StringBuilder sbRequest;
        String s;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");

        // build the request
        sbRequest = new StringBuilder();
        sbRequest.append(OfxDirectDLController.OFX_REQUEST_HEADER);

        sbRequest.append("<OFX>");
        sbRequest.append(System.lineSeparator());

        // these need some string work to replace variables
        s = String.format(OfxDirectDLController.OFX_SIGNON,
            CMHPIUtils.getLongDate(), // date of request
            acct.getAcctUId(), // user id
            acct.getAcctPW(), // user password
            fi.getFiOrg(), // financial institution organization
            fi.getFiId() // financial institution ID
        );
        sbRequest.append(s);

        s = String.format(OfxDirectDLController.OFX_INVSTMT_REQUEST,
            CMHPIUtils.getLongDate(), //   transaction number
            fi.getBrokerId(), //           broker id
            acct.getAcctNumber(), //       account number
            simpleDateFormat.format(Date.valueOf(startDate.toString())),    //dtStart
//            simpleDateFormat.format(Date.valueOf("2023-04-30")),    //dtEnd
            "Y", //                        include transactions
            "N", //                        include open orders
            "Y", //                        include positions
            "Y" //                         include balances
        );
        sbRequest.append(s);

        sbRequest.append("</OFX>");
        sbRequest.append(System.lineSeparator());

        return sbRequest;
    }

    private void getInvStmtData(CMOfxDLFIModel fi, CMOfxDLAccountModel acct, LocalDate startDate)
    {
        StringBuilder sbRequest;
        StringBuilder sbResponse;

        sbRequest = buildRequest(fi, acct, startDate);

        sbResponse = getResponse(fi, sbRequest);

        this.handleFiResponse(acct, sbResponse);
    }

    private StringBuilder getResponse(CMOfxDLFIModel fi, StringBuilder sbRequest)
    {
        HttpClient client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .build();
        StringBuilder sbResponse = new StringBuilder();

        
        try
        {
            HttpRequest request = HttpRequest.newBuilder(new URI(fi.getFiUrl()))
                .version(HttpClient.Version.HTTP_2)
                .headers("Content-Type", "text/plain;charset=UTF-8")
                .POST(BodyPublishers.ofString(sbRequest.toString()))
                .build();
            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

            // either gets data or error response
            // does not get here if url is wrong; exception instead
            sbResponse.append(response.body());
            
            //todo: check response for error code
            if (response.statusCode() != 200)
            {

                String line = "HTTPS error code: " + Integer.toString(response.statusCode())
                    + "\n" + "HTTPS Message: " + sbResponse.toString() + "\n";

                String s = String.format(CMLanguageController.
                    getErrorProp("HttpError"),
                    Integer.toString(response.statusCode()),
                    line + System.lineSeparator());

                CMHPIUtils.showDefaultMsg(
                    CMLanguageController.getAppProp("Title") + CMLanguageController.getErrorProp("Title"),
                    Thread.currentThread().getStackTrace()[1].
                        getClassName(),
                    Thread.currentThread().getStackTrace()[1].
                        getMethodName(),
                    s,
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (URISyntaxException | IOException | InterruptedException e)
        {
            sbResponse.append(e.getMessage());
        }

        return sbResponse;
    }

    private void handleFiResponse(CMOfxDLAccountModel acct, StringBuilder sbResponse)
    {
        FixSGML2XML fixSGML;
        String s;
        OfxFileController ofxFileController;

//        PROGRESS_BAR_CLI.barLabel(
//            "        Processing response from financial institution ...");
        // Write raw return to file
//        PROGRESS_BAR_CLI.barLabel("        Writing raw return file ...");
        writeRaw(sbResponse.toString(), acct.getAcctName());

        fixSGML = FixSGML2XML.getInstance();
        doc = fixSGML.doSGMLDoc2XML(Jsoup.parse(sbResponse.toString()));
        if (doc == null || (doc.select("ofx").first()) == null)
        {
            s = String.format(CMLanguageController.
                getErrorProp("OfxResponseEmpty"));

            CMHPIUtils.showDefaultMsg(
                CMLanguageController.getAppProp("Title") + CMLanguageController.getErrorProp("Title"),
                Thread.currentThread().getStackTrace()[1].getClassName(),
                Thread.currentThread().getStackTrace()[1].getMethodName(),
                s,
                JOptionPane.ERROR_MESSAGE);

//            throw new UnsupportedOperationException(s);
        }

        // Write adjusted return to file
//        PROGRESS_BAR_CLI.barLabel("        Writing XML return file ...");
        writeAdjusted(acct.getAcctName());

        ofxFileController = OfxFileController.getInstance();

        if (this.doc != null)
        {
            ofxFileController.processOfxDoc2SQLSetup(this.doc, PROGRESS_BAR_CLI);
        }
    }

    private void writeRaw(String aString, String sAcct)
    {
        File fileWrite;
        String s;

        fileWrite = new File(CMDirectoriesModel.getInstance().
            getModelProp("Reports") + java.io.File.separator + sAcct + ".OFX");
        try ( BufferedWriter writerRaw = new BufferedWriter(
            new OutputStreamWriter(new FileOutputStream(fileWrite),
                StandardCharsets.UTF_8)))
        {
            writerRaw.write(aString);
            writerRaw.flush();
        } catch (IOException e)
        {
            s = String.format(CMLanguageController.
                getErrorProp("GeneralError"),
                e.toString());

            CMHPIUtils.showDefaultMsg(
                CMLanguageController.getAppProp("Title") + CMLanguageController.getErrorProp("Title"),
                Thread.currentThread().getStackTrace()[1].getClassName(),
                Thread.currentThread().getStackTrace()[1].getMethodName(),
                s,
                JOptionPane.ERROR_MESSAGE);

            throw new UnsupportedOperationException(s);
        }
    }

    private void writeAdjusted(String sAcct)
    {
        File fileWrite;
        String s;

        if (doc == null)
        {
            return;
        }
        fileWrite = new File(CMDirectoriesModel.getInstance().
            getModelProp("Reports") + java.io.File.separator + sAcct + ".txt");

        try ( BufferedWriter writerRaw = new BufferedWriter(
            new OutputStreamWriter(new FileOutputStream(fileWrite),
                StandardCharsets.UTF_8)))
        {
            writerRaw.write(doc.body().toString());
            writerRaw.flush();
        } catch (IOException e)
        {
            s = String.format(CMLanguageController.
                getErrorProp("GeneralError"),
                e.toString());

            CMHPIUtils.showDefaultMsg(
                CMLanguageController.getAppProp("Title") + CMLanguageController.getErrorProp("Title"),
                Thread.currentThread().getStackTrace()[1].getClassName(),
                Thread.currentThread().getStackTrace()[1].getMethodName(),
                s,
                JOptionPane.ERROR_MESSAGE);

            throw new UnsupportedOperationException(s);
        }
    }
    private final static String OFX_REQUEST_HEADER = "OFXHEADER:100\n" + "DATA:OFXSGML\n" + "VERSION:102\n"
        + "SECURITY:NONE\n" + "ENCODING:USASCII\n" + "CHARSET:1252\n" + "COMPRESSION:NONE\n" + "OLDFILEUID:NONE\n"
        + "NEWFILEUID:NONE\n\n";
    private final static String OFX_SIGNON = "<SIGNONMSGSRQV1>\n" + "<SONRQ>\n" + "<DTCLIENT>%s\n" + "<USERID>%s\n"
        + "<USERPASS>%s\n" + "<LANGUAGE>ENG\n" + "<FI>\n" + "<ORG>%s\n" + "<FID>%s\n" + "</FI>\n" + "<APPID>Money\n"
        + "<APPVER>1600\n" + "</SONRQ>\n" + "</SIGNONMSGSRQV1>\n\n";
    private final static String OFX_INVSTMT_REQUEST = "<INVSTMTMSGSRQV1>\n" + "<INVSTMTTRNRQ>\n" + "<TRNUID>%s-01\n"
        + "<INVSTMTRQ>\n" + "<INVACCTFROM>\n" + "<BROKERID>%s\n" + "<ACCTID>%s\n" + "</INVACCTFROM>\n" + "<INCTRAN>\n"
        + "<DTSTART>%s\n" 
//        + "<DTEND>%s\n" 
        + "<INCLUDE>%s\n" + "</INCTRAN>\n" + "<INCOO>%s\n" + "<INCPOS>\n" + "<INCLUDE>%s\n"
        + "</INCPOS>\n" + "<INCBAL>%s\n" + "</INVSTMTRQ>\n" + "</INVSTMTTRNRQ>\n" + "</INVSTMTMSGSRQV1>\n";
}

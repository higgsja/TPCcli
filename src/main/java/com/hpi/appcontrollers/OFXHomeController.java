package com.hpi.appcontrollers;

import com.hpi.entities.OFXInstitutionModel;
import com.hpi.hpiUtils.CMHPIUtils;
import com.hpi.TPCCMcontrollers.*;
import java.io.*;
import java.net.*;
import java.net.http.*;
import java.net.http.HttpResponse.BodyHandlers;
// import java.util.*;
import javax.swing.JOptionPane;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;

/**
 * Handles getting financial institute data
 * <p>
 */
public class OFXHomeController //      extends DBCore
{

    /*
     * Singleton
     *
     */
    private static OFXHomeController instance;
    // private final ArrayList<String> ofxList;

    protected OFXHomeController()
    {
        // protected prevents instantiation outside of package
        // this.ofxList = new ArrayList<>();
    }

    public synchronized static OFXHomeController getInstance()
    {
        if (OFXHomeController.instance == null)
        {
            OFXHomeController.instance = new OFXHomeController();
        }
        return OFXHomeController.instance;
    }

    void getOfxList()
    {
        String httpString, charsetString, s;
        URI uri;
        HttpRequest request;
        HttpClient client;
        HttpResponse<String> response;
        Document doc;
        DocumentBuilderFactory factory;
        DocumentBuilder builder;

        httpString = "http://www.ofxhome.com/api.php?all=yes";
        charsetString = "UTF-8";
        // sXmlString = "";

        try
        {
            uri = new URI(httpString);
            request = HttpRequest.newBuilder()
                .uri(uri)
                .version(HttpClient.Version.HTTP_2)
                .header("Content-Type", "application/x-www-form-urlencoded;charset=" + charsetString)
                .header("Accept-Charset", charsetString)
                .header("Accept-Language", "en-US,en;q=0.8")
                .header("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.9"
                    + ".2.3) Gecko/20100401")
                .GET()
                .build();

//            url = new URL(httpString);
//            httpConnection = (HttpURLConnection) url.openConnection();
//            httpConnection.setRequestProperty("Content-Type",
//                "application/x-www-form-urlencoded;charset=" + charsetString);
//            httpConnection.setRequestMethod("GET");
//            httpConnection.setRequestProperty("Accept-Charset", charsetString);
//            httpConnection.addRequestProperty("Accept-Language",
//                "en-US,en;q=0.8");
//            httpConnection.addRequestProperty("User-Agent",
//                "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.9"
//                + ".2.3) Gecko/20100401");

//httpConnection.setDoOutput(false);

            client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();
            
            response = client
                .send(request, BodyHandlers.ofString());
            
            if(response.statusCode() !=  HttpURLConnection.HTTP_OK)
                

//            intStatus = httpConnection.getResponseCode();
//            if (intStatus != HttpURLConnection.HTTP_OK)
            {
                s = String.format(CMLanguageController.
                    getErrorProps().getProperty("GeneralError"),
                    Integer.toString(response.statusCode()));

                CMHPIUtils.showDefaultMsg(CMLanguageController.
                    getErrorProps().
                    getProperty("Title"),
                    Thread.currentThread().getStackTrace()[1].
                        getClassName(),
                    Thread.currentThread().getStackTrace()[1].
                        getMethodName(),
                    s,
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            factory = DocumentBuilderFactory.newInstance();
            try
            {
//                doc = Jsoup.parse(response.body(), "");
//                Jsoup.parse(in, charsetString, s)
//                    Jsoup.parse
                builder = factory.newDocumentBuilder();
//
////                doc = builder.parse(httpConnection.getInputStream());
                doc = builder.parse(response.body());

                doc.getDocumentElement().normalize();
            } catch (ParserConfigurationException | IOException | SAXException e)
            {
                //response has <!--?xml version="1.0" encoding="utf-8"?-->
                //so, it is commented out and therefore no protocol found by the parser
                //all you get is unreadable
                s = String.format(CMLanguageController.
                    getErrorProps().getProperty("GeneralError"),
                    e.toString());

                CMHPIUtils.showDefaultMsg(CMLanguageController.
                    getErrorProps().
                    getProperty("Title"),
                    Thread.currentThread().getStackTrace()[1].
                        getClassName(),
                    Thread.currentThread().getStackTrace()[1].
                        getMethodName(),
                    s,
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (InterruptedException | URISyntaxException | IOException e)
        {
            s = String.format(CMLanguageController.
                getErrorProps().getProperty("GeneralError"),
                e.toString());

            CMHPIUtils.showDefaultMsg(CMLanguageController.
                getErrorProps().
                getProperty("Title"),
                Thread.currentThread().getStackTrace()[1].
                    getClassName(),
                Thread.currentThread().getStackTrace()[1].
                    getMethodName(),
                s,
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        // put institutions into array
        processOfxList(doc);

        // get further details on each institution
        processOfxInstitution();

        // array has all complete objects
        // send to the database
        processSQL();

        System.out.println("--ofxHome Finished--");
    }

    void processOfxList(Document doc)
    {
        OFXInstitutionModel tmpOFXInstitutionModel;
        NodeList nodeList;
        Node node;

        OFXInstitutionModel.OFXINSTITUTION_MODELS.clear();
        nodeList = doc.getElementsByTagName("institutionid");

        for (int i = 0; i < nodeList.getLength(); i++)
        {
            node = nodeList.item(i);

            if (node.getNodeType() == Node.ELEMENT_NODE)
            {

                tmpOFXInstitutionModel = new OFXInstitutionModel(
                    node.getAttributes().getNamedItem("name").getNodeValue(),
                    node.getAttributes().getNamedItem("id").getNodeValue());

                OFXInstitutionModel.getDataList().add(tmpOFXInstitutionModel);
            }
        }
    }

    void processOfxInstitution()
    {
        String httpString, s, charsetString;
        URL url;
        StringBuilder sb;
        HttpURLConnection httpConnection;
        Integer intStatus;
        DocumentBuilderFactory factory;
        DocumentBuilder builder;
        Document doc;

        charsetString = "UTF-8";
        // sXmlString = "";
        sb = new StringBuilder();

        // use array to get detail from web site
        for (OFXInstitutionModel model : OFXInstitutionModel.getDataList())
        {
            httpString = "http://www.ofxhome.com/api.php?lookup="
                + model.getfIdString();
            // sXmlString = "";
            sb.setLength(0);

            try
            {
                url = new URL(httpString);

                httpConnection = (HttpURLConnection) url.openConnection();
                httpConnection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded;charset=" + charsetString);
                httpConnection.setRequestMethod("GET");
                httpConnection.setRequestProperty("Accept-Charset",
                    charsetString);
                httpConnection.addRequestProperty("Accept-Language",
                    "en-US,en;q=0.8");
                httpConnection.addRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.9"
                    + ".2.3) Gecko/20100401");
                httpConnection.setDoOutput(false);

                intStatus = httpConnection.getResponseCode();
                if (intStatus != HttpURLConnection.HTTP_OK)
                {
                    s = String.format(CMLanguageController.
                        getErrorProps().getProperty("GeneralError"),
                        Integer.toString(intStatus));

                    CMHPIUtils.showDefaultMsg(CMLanguageController.
                        getErrorProps().
                        getProperty("Title"),
                        Thread.currentThread().getStackTrace()[1].
                            getClassName(),
                        Thread.currentThread().getStackTrace()[1].
                            getMethodName(),
                        s,
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }

                factory = DocumentBuilderFactory.newInstance();
                try
                {
                    builder = factory.newDocumentBuilder();
                    doc = builder.parse(httpConnection.getInputStream());
                    doc.getDocumentElement().normalize();
                } catch (ParserConfigurationException | IOException | SAXException e)
                {
                    // bb&t does not escape the & so fails
                    s = String.format(CMLanguageController.
                        getErrorProps().getProperty("GeneralError"),
                        e.toString());

                    CMHPIUtils.showDefaultMsg(CMLanguageController.
                        getErrorProps().
                        getProperty("Title"),
                        Thread.currentThread().getStackTrace()[1].
                            getClassName(),
                        Thread.currentThread().getStackTrace()[1].
                            getMethodName(),
                        s,
                        JOptionPane.ERROR_MESSAGE);
                    // ignore errors; there are weird ones
                    continue;
                }
            } catch (IOException e)
            {
                s = String.format(CMLanguageController.
                    getErrorProps().getProperty("GeneralError"),
                    e.toString());

                CMHPIUtils.showDefaultMsg(CMLanguageController.
                    getErrorProps().
                    getProperty("Title"),
                    Thread.currentThread().getStackTrace()[1].
                        getClassName(),
                    Thread.currentThread().getStackTrace()[1].
                        getMethodName(),
                    s,
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            // put detail into each object
            processOfxInstitutionXml(model, doc);
        }
    }

    void processOfxInstitutionXml(OFXInstitutionModel model, Document doc)
    {
        NodeList nodeList;
        Node node;
        String s;

        nodeList = doc.getElementsByTagName("*");

        for (int i = 0; i < nodeList.getLength(); i++)
        {
            node = nodeList.item(i);

            if (node.getNodeType() == Node.ELEMENT_NODE)
            {

                switch (node.getNodeName().toLowerCase())
                {
                    case "institution":
                        break;
                    case "name":
                        break;
                    case "fid":
                        model.setfIdString(node.getTextContent());
                        break;
                    case "org":
                        model.setOrgString(node.getTextContent());
                        break;
                    case "brokerid":
                        model.setBrokerIdString(node.getTextContent());
                        break;
                    case "url":
                        model.setUrlString(node.getTextContent());
                        break;
                    case "ofxfail":
                        model.setOfxFailString(node.getTextContent());
                        break;
                    case "sslfail":
                        model.setSslFailString(node.getTextContent());
                        break;
                    case "lastofxvalidation":
                        model.setLastOfxValidationString(node.getTextContent());
                        break;
                    case "lastsslvalidation":
                        model.setLastSslValidationString(node.getTextContent());
                        break;
                    case "profile":
                        // has multiple attributes
                        // and we really do not need them
                        break;
                    default:
                        s = String.format(CMLanguageController.
                            getErrorProp(
                                "Formatted3"), node.getNodeType()
                            == Node.ELEMENT_NODE);

                        CMHPIUtils.showDefaultMsg(
                            CMLanguageController.getAppProp("Title")
                            + CMLanguageController.getErrorProp("Title"),
                            this.getClass().getName(),
                            Thread.currentThread().
                                getStackTrace()[1].getMethodName(),
                            s,
                            JOptionPane.ERROR_MESSAGE);

                        throw (new UnsupportedOperationException());
                }
            }
        }
    }

    void processSQL()
    {
        String sUpdateSQL, sInsertSQL;

        // OFXInstitutionModel.getDataList() has the array of data
        for (OFXInstitutionModel model : OFXInstitutionModel.getDataList())
        {
            sUpdateSQL = OFXInstitutionModel.SQLUPDATE + "Name = \"";
            sUpdateSQL += model.getNameString().replace("'", "''");
            sUpdateSQL += "\", ";
            sUpdateSQL += "Org = \"";
            sUpdateSQL += model.getOrgString() == null
                ? "\", " : model.getOrgString() + "\", ";
            sUpdateSQL += "BrokerId = \"";
            sUpdateSQL += model.getBrokerIdString() == null
                ? "\", " : model.getBrokerIdString() + "\", ";
            sUpdateSQL += "Url = \"";
            sUpdateSQL += model.getUrlString() == null
                ? "\", " : model.getUrlString() + "\", ";
            sUpdateSQL += "OfxFail = \"";
            sUpdateSQL += model.getOfxFailString() == null
                ? "\", " : model.getOfxFailString() + "\", ";
            sUpdateSQL += "SslFail = \"";
            sUpdateSQL += model.getSslFailString() == null
                ? "\", " : model.getSslFailString() + "\", ";
            sUpdateSQL += "LastOfxValidation = \"";
            sUpdateSQL += model.getLastOfxValidationString() == null
                ? "\", " : model.getLastOfxValidationString() + "\", ";
            sUpdateSQL += "LastSslValidation = \"";
            sUpdateSQL += model.getLastSslValidationString() == null
                ? "\" " : model.getLastSslValidationString() + "\" ";
            sUpdateSQL += "where FId = \"" + model.getfIdString() + "\";";

            sInsertSQL = OFXInstitutionModel.SQLINSERT;
            sInsertSQL += "\"";
            sInsertSQL += model.getNameString().replace("'", "''");
            sInsertSQL += "\", \"";
            sInsertSQL += model.getfIdString() + "\", \"";
            sInsertSQL += model.getOrgString() == null
                ? "\", \"" : model.getOrgString() + "\", \"";
            sInsertSQL += model.getBrokerIdString() == null
                ? "\", \"" : model.getBrokerIdString() + "\", \"";
            sInsertSQL += model.getUrlString() == null
                ? "\", \"" : model.getUrlString() + "\", \"";
            sInsertSQL += model.getOfxFailString() == null
                ? "\", \"" : model.getOfxFailString() + "\", \"";
            sInsertSQL += model.getSslFailString() == null
                ? "\", \"" : model.getSslFailString() + "\", \"";
            sInsertSQL += model.getLastOfxValidationString() == null
                ? "\", \"" : model.getLastOfxValidationString()
                + "\", \"";
            sInsertSQL += model.getLastSslValidationString() == null
                ? "\");" : model.getLastSslValidationString() + "\");";

            CMDBController.upsertRow(sUpdateSQL, sInsertSQL);
        }
    }

    /**
     * Use equityInfo table for tickers; update price data in equityHistory
     * Both tables are global tables
     */
    public void doOfxData()
    {
        System.out.println("--ofxHome Started--");

        // this.ofxList = new ArrayList<>();
        // todo: seems like some do not get through other than bb&T
        this.getOfxList();
    }
}

package com.hpi.ofxFileHandling;

import com.hpi.TPCCMcontrollers.CMLanguageController;
import com.hpi.hpiUtils.CMHPIUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import javax.swing.JOptionPane;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;

/**
 *
 * @author blue
 */
public class FixSGML2XML
{

    private final List<SGMLNode> topList;
    //*** Singleton
    private static FixSGML2XML instance;

    protected FixSGML2XML()
    {
        this.topList = new ArrayList<>();
    }

    public synchronized static FixSGML2XML getInstance()
    {
        if (FixSGML2XML.instance == null)
        {
            FixSGML2XML.instance = new FixSGML2XML();
        }
        return FixSGML2XML.instance;
    }
    //***

    /**
     * Document with either SGML or XML, ensures XML
     *
     * @param docSource JSoup Document
     *
     * @return JSoup Document
     */
    Document doSGMLFile2XML(String sFilename)
    {
        // confirm file exists
        // confirm file has an ofx header
        // do the conversion
        Document docSource;
        Path path;
        String s;
        // Element e1;

        path = Paths.get(sFilename);
        try (InputStream input = Files.newInputStream(
                path, StandardOpenOption.READ))
        {
            docSource = Jsoup.parse(input, "UTF-8", "");
            // it is possible that doc is returned null without errors
            if (docSource == null ||
                  docSource.select("ofx").first() == null)
            {
                // invalid ofx file
                s = String.format(CMLanguageController.
                        getErrorProps().getProperty("Formatted8"),
                        sFilename);
                CMHPIUtils.showDefaultMsg(CMLanguageController.getErrorProps().
                                getProperty("Title"),
                        Thread.currentThread().getStackTrace()[1].getClassName(),
                        Thread.currentThread().getStackTrace()[1].getMethodName(),
                        s,
                        JOptionPane.ERROR_MESSAGE);
                return null;
            }
        }
        catch (IOException ex)
        {
            s = String.format(CMLanguageController.
                    getErrorProps().getProperty("Formatted9"),
                    sFilename);

            CMHPIUtils.showDefaultMsg(CMLanguageController.getErrorProps().
                            getProperty("Title"),
                    Thread.currentThread().getStackTrace()[1].getClassName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName(),
                    s,
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }

        docSource = this.doSGMLDoc2XML(docSource);

        if (docSource == null
                || docSource.select("ofx").first() == null)
        {
            s = String.format(CMLanguageController.getErrorProp("Formatted8"),
                    sFilename);

            CMHPIUtils.showDefaultMsg(CMLanguageController.getErrorProp("Title"),
                    Thread.currentThread().getStackTrace()[1].getClassName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName(),
                    s,
                    JOptionPane.ERROR_MESSAGE);

            throw new UnsupportedOperationException(s);
        }

        return docSource;

//        e1 = docSource.select("ofx").first().clone();
//
//        SGMLNode topNode = new SGMLNode();
//        topNode.setElementSGMLNode(e1);
//        topList.add(topNode);
//        if (!topNode.readSGMLNode())
//        {
//            s = String.format(LanguageModel.getInstance().
//                    getErrorProps().getProperty("Formatted8"),
//                    sFilename);
//            CMHPIUtils.showDefaultMsg(
//                    LanguageModel.getInstance().getErrorProps().
//                            getProperty("Title"),
//                    Thread.currentThread().getStackTrace()[1].getClassName(),
//                    Thread.currentThread().getStackTrace()[1].getMethodName(),
//                    s,
//                    JOptionPane.ERROR_MESSAGE);
//            return null;
//        }
//        // when this finally returns, all nodes in memory
//        for (SGMLNode aNode : topList)
//        {
//            if (!aNode.writeSGMLNode())
//            {
//                s = String.format(LanguageModel.getInstance().
//                        getErrorProps().getProperty("Formatted8"),
//                        sFilename);
//                CMHPIUtils.showDefaultMsg(
//                        LanguageModel.getInstance().getErrorProps().
//                                getProperty("Title"),
//                        Thread.currentThread().getStackTrace()[1].getClassName(),
//                        Thread.currentThread().getStackTrace()[1].getMethodName(),
//                        s,
//                        JOptionPane.ERROR_MESSAGE);
//                return null;
//            }
//        }
//
//        String aS = topNode.getSbFix().toString();
//        // sbFix is static so need to clear it between accounts
//        topNode.getSbFix().delete(0, topNode.getSbFix().length());
//        topList.clear();
//        docSource = Jsoup.parse(aS);
//        return docSource;
    }

    public Document doSGMLDoc2XML(Document docSource)
    {
        // String s;
        Element e1;

        //nothing to process
        if (docSource.select("ofx").first() == null ) return null;
        e1 = docSource.select("ofx").first().clone();

        SGMLNode topNode = new SGMLNode();
        topNode.setElementSGMLNode(e1);
        topList.add(topNode);

        if (!topNode.readSGMLNode())
        {
            return null;
        }

        // when this finally returns, all nodes in memory
        for (SGMLNode aNode : topList)
        {
            if (!aNode.writeSGMLNode())
            {
                return null;
            }
        }

        String aS = topNode.getSbFix().toString();

        // sbFix is static so need to clear it between accounts
        topNode.getSbFix().delete(0, topNode.getSbFix().length());
        topList.clear();

        docSource = Jsoup.parse(aS);
        return docSource;
    }
}

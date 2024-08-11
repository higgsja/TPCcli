/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hpi.ofxAggregates;

import com.hpi.TPCCMcontrollers.CMLanguageController;
import java.util.Iterator;
import org.jsoup.nodes.Element;




/**
 *
 * @author Joe@Higgs-Tx.com
 */
public class OfxSonRS
        extends OfxAggregateBase
{

    OfxStatus status;
    // keep this as a string; dateTime of server access for report
    String dtServer;
    String userKey;
    String dtKeyExpire;
    String language;
    String dtProfUp;
    String dtAcctUp;
    OfxFI ofxFI;
    String sessCookie;
    String accessKey;

    // private final String errorPrefix;

    /**
     *
     */
    public OfxSonRS()
    {
        this.status = new OfxStatus();
        this.dtServer = "";
        this.userKey = "";
        this.dtKeyExpire = "";
        this.language = "";
        this.dtProfUp = "";
        this.dtAcctUp = "";
        this.ofxFI = new OfxFI();
        this.sessCookie = "";
        this.accessKey = "";

        // this.errorPrefix = this.getClass().getName();

//        PropertyConfigurator.configure("/home/white/Documents/Dev/AppTWS/log4j.properties");
    }

    /**
     *
     * @param aElement
     * @param invStmtMsgsRSv1
     * @return
     */
    public Boolean doData(Element aElement, OfxInvStmtMsgsRSv1 invStmtMsgsRSv1)
    {
        //tested
        String s;
        Element element;
        Iterator<Element> iterator;

        // aElement points to <sonrs>
        iterator = aElement.children().iterator();

        while (iterator.hasNext())
        {
            element = iterator.next();

            switch (element.tagName().toLowerCase())
            {
                case "status":
                    this.status.doData(element);
                    break;
                case "dtserver":
                    this.dtServer = element.ownText();
                    break;
                case "userkey":
                    this.userKey = element.ownText();
                    break;
                case "tskeyexpire":
                    this.dtKeyExpire = element.ownText();
                    break;
                case "language":
                    this.language = element.ownText();
                    break;
                case "dtprofup":
                    this.dtProfUp = element.ownText();
                    break;
                case "dtacctup":
                    this.dtAcctUp = element.ownText();
                    break;
                case "fi":
                    this.ofxFI.doData(element, invStmtMsgsRSv1);
                    break;
                case "sesscookie":
                    this.sessCookie = element.ownText();
                    break;
                case "accesskey":
                    this.accessKey = element.ownText();
                    break;
                case "intu.bid":
                case "intu:userid":
                    break;
                default:
                    // actually do not care that there are extra elements
                    //  but let's log them
                    s = String.format(CMLanguageController.
                            getErrorProps().getProperty("Formatted3"),
                            element.tagName());

                    //Logger.getLogger(this.getClass()).info(s);
            }
        }
        return true;
    }

    /**
     *
     * @return
     */
    public OfxStatus getStatus()
    {
        return status;
    }

    /**
     *
     * @return
     */
    public String getDateTimeServer()
    {
        return dtServer;
    }

    /**
     *
     * @return
     */
    public String getUserKey()
    {
        return userKey;
    }

    /**
     *
     * @return
     */
    public String getDateTimeKeyExpire()
    {
        return dtKeyExpire;
    }

    /**
     *
     * @return
     */
    public String getLanguage()
    {
        return language;
    }

    /**
     *
     * @return
     */
    public String getDateTimeProfUp()
    {
        return dtProfUp;
    }

    /**
     *
     * @return
     */
    public String getDateTimeAcctUp()
    {
        return dtAcctUp;
    }

    /**
     *
     * @return
     */
    public OfxFI getOfxFI()
    {
        return ofxFI;
    }

    /**
     *
     * @return
     */
    public String getSessCookie()
    {
        return sessCookie;
    }

    /**
     *
     * @return
     */
    public String getAccessKey()
    {
        return accessKey;
    }
}

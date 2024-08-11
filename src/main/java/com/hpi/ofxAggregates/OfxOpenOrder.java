/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hpi.ofxAggregates;

import com.hpi.TPCCMcontrollers.CMLanguageController;
import com.hpi.hpiUtils.CMHPIUtils;
import java.util.Iterator;
import javax.swing.JOptionPane;
import org.jsoup.nodes.Element;




/**
 *
 * @author Joe@Higgs-Tx.com
 */
public class OfxOpenOrder
{

    String fiTId;
    String srvrTId;
    OfxSecId secId;
    String dtPlaced;
    Double units;
    String subAcct;
    String duration;
    String restriction;
    Double minUnits;
    Double limitPrice;
    Double stopPrice;
    String memo;
    OfxCurrency currency;
    String inv401kSource;
    
    private final String errorPrefix;

    public OfxOpenOrder()
    {
        this.fiTId = null;
        this.srvrTId = null;
        this.secId = new OfxSecId();
        this.dtPlaced = null;
        this.units = null;
        this.subAcct = null;
        this.duration = null;
        this.restriction = null;
        this.minUnits = null;
        this.limitPrice = null;
        this.stopPrice = null;
        this.memo = null;
        this.currency = new OfxCurrency();
        this.inv401kSource = null;

        this.errorPrefix = this.getClass().getName();
        
    }

    /**
     *
     * @param aElement
     * @return
     */
    public Boolean doData(Element aElement)
    {
        String s;
        Element element;
        Iterator<Element> iterator;
        CMHPIUtils.showDefaultMsg(CMLanguageController.getAppProps().getProperty("Title"),
                errorPrefix,
                "doData",
                "Not tested.",
                JOptionPane.ERROR_MESSAGE);

        // aElement points to <oo>
        iterator = aElement.children().iterator();

        while (iterator.hasNext())
        {
            element = iterator.next();

            switch (element.tagName().toLowerCase())
            {
                case "srvrtid":
                    this.srvrTId = element.ownText();
                    break;
                case "secid":
                    this.secId.doData(element);
                    break;
                case "dtplaced":
                    this.dtPlaced = element.ownText();
                    break;
                case "units":
                    this.units = Double.parseDouble(element.ownText());
                    break;
                case "subacct":
                    this.subAcct = element.ownText();
                    break;
                case "duration":
                    this.duration = element.ownText();
                    break;
                case "restriction":
                    this.restriction = element.ownText();
                    break;
                case "minunits":
                    this.minUnits = Double.parseDouble(element.ownText());
                    break;
                case "limitprice":
                    this.limitPrice = Double.parseDouble(element.ownText());
                    break;
                case "stopprice":
                    this.stopPrice = Double.parseDouble(element.ownText());
                    break;
                case "memo":
                    this.memo = element.ownText();
                    break;
                case "currency":
                    this.currency.doData(element);
                    break;
                case "inv401ksource":
                    this.inv401kSource = element.ownText();
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
}

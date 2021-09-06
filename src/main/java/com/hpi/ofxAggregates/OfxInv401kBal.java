/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hpi.ofxAggregates;

import com.hpi.TPCCMcontrollers.CMLanguageController;
import com.hpi.hpiUtils.CMHPIUtils;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.JOptionPane;
import org.jsoup.nodes.Element;

/**
 *
 * @author Joe@Higgs-Tx.com
 */
public class OfxInv401kBal {

    Double cashBal;
    Double preTax;
    Double afterTax;
    Double match;
    Double profitSharing;
    Double rollover;
    Double otherVest;
    Double otherNonVest;
    Double total;
    ArrayList<OfxBal> balList;

    private final String errorPrefix;
    private String fErrorPrefix;

    public OfxInv401kBal() {
        this.cashBal = null;
        this.preTax = null;
        this.afterTax = null;
        this.match = null;
        this.profitSharing = null;
        this.rollover = null;
        this.otherVest = null;
        this.otherNonVest = null;
        this.total = null;
        this.balList = new ArrayList<>();

        this.errorPrefix = this.getClass().getName();

        this.fErrorPrefix = null;
    }

    /**
     *
     * @param aElement
     * @return
     */
    public Boolean doData(Element aElement) {
        fErrorPrefix = Thread.currentThread().getStackTrace()[1].getMethodName();

        String s;
        Element element;
        Iterator<Element> iterator;
        CMHPIUtils.showDefaultMsg(CMLanguageController.getAppProps().
                getProperty("Title"),
                errorPrefix,
                fErrorPrefix,
                "Not implemented.",
                JOptionPane.ERROR_MESSAGE);

        // aElement points to <inv401kbal>
        iterator = aElement.children().iterator();

        while (iterator.hasNext()) {
            element = iterator.next();

            switch (element.tagName().toLowerCase()) {
                case "cashbal":
                    this.cashBal = Double.parseDouble(element.ownText());
                    break;
                case "pretax":
                    this.preTax = Double.parseDouble(element.ownText());
                    break;
                case "aftertax":
                    this.afterTax = Double.parseDouble(element.ownText());
                    break;
                case "match":
                    this.match = Double.parseDouble(element.ownText());
                    break;
                case "profitsharing":
                    this.profitSharing = Double.parseDouble(element.ownText());
                    break;
                case "rollover":
                    this.rollover = Double.parseDouble(element.ownText());
                    break;
                case "othervest":
                    this.otherVest = Double.parseDouble(element.ownText());
                    break;
                case "othernonvest":
                    this.otherNonVest = Double.parseDouble(element.ownText());
                    break;
                case "total":
                    this.total = Double.parseDouble(element.ownText());
                    break;
                case "ballist":
                    if (!doBalList(element)) {
                        return false;
                    }
                    break;
                default:
                    // actually do not care that there are extra elements
                    //  but let's log them
                    s = String.format(CMLanguageController.
                            getErrorProps().getProperty("Formatted3"),
                            element.tagName());

//                    //Logger.getLogger(this.getClass()).info(s);
            }
        }
        return true;
    }

    private Boolean doBalList(Element aElement) {
        fErrorPrefix = Thread.currentThread().getStackTrace()[1].getMethodName();

        OfxBal bal;
        String s;
        Element element;
        Iterator<Element> iterator;

        // aElement points to <ballist>
        iterator = aElement.children().iterator();

        while (iterator.hasNext()) {
            element = iterator.next();

            switch (element.tagName().toLowerCase()) {
                case "bal":
                    bal = new OfxBal();
                    if (bal.doData(element)) {
                        this.balList.add(bal);
                    }
                    break;
                default:
                    // actually do not care that there are extra elements
                    //  but let's log them
                    s = String.format(CMLanguageController.
                            getErrorProps().getProperty("Formatted3"),
                            element.tagName());

//                    //Logger.getLogger(this.getClass()).info(s);
            }
        }
        return true;
    }
}

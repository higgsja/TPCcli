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
public class OfxSecListMsgsRSv1
{

    OfxSecList secList;

    // private final String errorPrefix;

    public OfxSecListMsgsRSv1()
    {
        this.secList = new OfxSecList();

        // this.errorPrefix = this.getClass().getName();

    }

    /**
     *
     * @param aElement
     * @return
     */
    public Boolean doData(Element aElement)
    {
        //tested
        String s;
        Element element;
        Iterator<Element> iterator;

        // OfxDebtInfo debtInfo;
        // OfxMFInfo mfInfo;
        // OfxOptInfo optInfo;
        // OfxOtherInfo otherInfo;
        // OfxStockInfo stockInfo;

        // aElement points to <seclistmsgsrsv1>
        iterator = aElement.children().iterator();

        while (iterator.hasNext())
        {
            element = iterator.next();

            switch (element.tagName().toLowerCase())
            {
                case "seclist":
                    this.secList.doData(element);
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

    public OfxSecList getSecList()
    {
        return secList;
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hpi.ofxAggregates;

import com.hpi.TPCCMcontrollers.CMLanguageController;
import java.util.ArrayList;
import java.util.Iterator;
import org.jsoup.nodes.Element;




/**
 *
 * @author Joe@Higgs-Tx.com
 */
public class OfxSecList
{

    ArrayList<OfxDebtInfo> secDebtList;
    ArrayList<OfxMFInfo> secMFList;
    ArrayList<OfxOptInfo> secOptList;
    ArrayList<OfxOtherInfo> secOtherList;
    ArrayList<OfxStockInfo> secStockList;

    // private final String errorPrefix;

    public OfxSecList()
    {
        this.secDebtList = new ArrayList<>();
        this.secMFList = new ArrayList<>();
        this.secOptList = new ArrayList<>();
        this.secOtherList = new ArrayList<>();
        this.secStockList = new ArrayList<>();

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

        OfxDebtInfo debtInfo;
        // OfxMFInfo mfInfo;
        OfxOptInfo optInfo;
        OfxOtherInfo otherInfo;
        OfxStockInfo stockInfo;

        // aElement points to <seclist>
        iterator = aElement.children().iterator();

        while (iterator.hasNext())
        {
            element = iterator.next();

            switch (element.tagName().toLowerCase())
            {
                case "debtinfo":
                    debtInfo = new OfxDebtInfo();
                    if (debtInfo.doData(element))
                    {
                        this.secDebtList.add(debtInfo);
                    }
                    break;
                case "mfinfo":
                    //todo: mfinfo
//                    mfInfo = new OfxMFInfo();
//                    if (mfInfo.doData(element))
//                    {
//                        this.secMFList.add(mfInfo);
//                    }
                    break;
                case "optinfo":
                    optInfo = new OfxOptInfo();
                    if (optInfo.doData(element))
                    {
                        this.secOptList.add(optInfo);
                    }
                    break;
                case "otherinfo":
                    otherInfo = new OfxOtherInfo();
                    if (otherInfo.doData(element))
                    {
                        this.secOtherList.add(otherInfo);
                    }
                    break;
                case "stockinfo":
                    stockInfo = new OfxStockInfo();
                    if (stockInfo.doData(element))
                    {
                        this.secStockList.add(stockInfo);
                    }
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
     * @param invAcctFrom
     * @return
     */
    public Boolean doSQL(OfxInvAcctFrom invAcctFrom)
    {
        OfxDebtInfo ofxDebtInfo;
        OfxMFInfo ofxMFInfo;
        OfxOptInfo ofxOptInfo;
        OfxOtherInfo ofxOtherInfo;
        OfxStockInfo ofxStockInfo;

        Iterator<OfxDebtInfo> iteratorDebt = this.secDebtList.iterator();

        while (iteratorDebt.hasNext())
        {
            ofxDebtInfo = (OfxDebtInfo) iteratorDebt.next();

            ofxDebtInfo.doSQL(invAcctFrom);
        }

        Iterator<OfxMFInfo> iteratorMF = this.secMFList.iterator();

        while (iteratorMF.hasNext())
        {
            ofxMFInfo = (OfxMFInfo) iteratorMF.next();

            ofxMFInfo.doSQL(invAcctFrom);
        }

        Iterator<OfxOptInfo> iteratorOpt = this.secOptList.iterator();

        while (iteratorOpt.hasNext())
        {
            ofxOptInfo = (OfxOptInfo) iteratorOpt.next();

            ofxOptInfo.doSQL(invAcctFrom);
        }

        Iterator<OfxOtherInfo> iteratorOther = this.secOtherList.iterator();

        while (iteratorOther.hasNext())
        {
            ofxOtherInfo = (OfxOtherInfo) iteratorOther.next();

            ofxOtherInfo.doSQL(invAcctFrom);
        }

        Iterator<OfxStockInfo> iteratorStock = this.secStockList.iterator();

        while (iteratorStock.hasNext())
        {
            ofxStockInfo = (OfxStockInfo) iteratorStock.next();

            ofxStockInfo.doSQL(invAcctFrom);
        }

        return true;
    }
}

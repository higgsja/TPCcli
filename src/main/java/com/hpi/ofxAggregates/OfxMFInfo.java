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
public class OfxMFInfo
        extends OfxAggregateBase
        implements IOfxSQL
{

    OfxSecInfo secInfo;
    String mfType;
    Double yield;
    String dtYieldAsOf;
    OfxMFAssetClass mfAssetClass;
    OfxFiMFAssetClass fiMFAssetClass;

    // private final String errorPrefix;

    public OfxMFInfo()
    {
        this.secInfo = new OfxSecInfo();
        this.mfType = null;
        this.yield = null;
        this.dtYieldAsOf = null;
        this.mfAssetClass = new OfxMFAssetClass();
        this.fiMFAssetClass = new OfxFiMFAssetClass();

        // this.errorPrefix = this.getClass().getName();

    }

    public Boolean doData(Element aElement)
    {
        //tested
        String s;
        Element element;
        Iterator<Element> iterator;
        // OfxMFPortion mfPortion;
        //OfxFiMFPortion fiMFPortion;

        // aElement points to <mfinfo>
        iterator = aElement.children().iterator();

        while (iterator.hasNext())
        {
            element = iterator.next();

            switch (element.tagName().toLowerCase())
            {
                case "secinfo":
                    this.secInfo.doData(element);
                    break;
                case "mftype":
                    this.mfType = element.ownText();
                    break;
                case "yield":
                    this.yield = Double.parseDouble(element.ownText());
                    break;
                case "dtyieldasof":
                    this.dtYieldAsOf = element.ownText();
                    break;
                case "mfassetclass":
                    //todo: mfassetclass
//                    this.mfAssetClass.doData(element);
                    break;
                case "fimfassetclass":
                    this.fiMFAssetClass.doData(element);
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

    @Override
    public Boolean doSQL(OfxInvAcctFrom invAcctFrom)
    {
        //tested
        this.secInfo.doSQL(invAcctFrom, false);

        String sTable = "hlhtxc5_dbOfx.MFInfo";

        String[] keys =
        {
            "BrokerID", "SecId",
            "MFType", "Yield", "DtYieldAsOf"
        };
        String[] values =
        {
            invAcctFrom.brokerId.toString(), this.secInfo.secId.uniqueId,
            this.mfType, String.valueOf(this.yield), this.dtYieldAsOf
        };

        return (this.doSQL(sTable, keys, values, 2)
                && this.mfAssetClass.doSQL(invAcctFrom.brokerId,
                      this.secInfo.secId.uniqueId)
                && this.fiMFAssetClass.doSQL(invAcctFrom.brokerId,
                      this.secInfo.secId.uniqueId));
    }
}

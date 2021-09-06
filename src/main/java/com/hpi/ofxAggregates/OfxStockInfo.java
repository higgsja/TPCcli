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
public class OfxStockInfo
        extends OfxAggregateBase
        implements IOfxSQL
{

    OfxSecInfo secInfo;
    String stockType;
    Double yield;
    String dtYieldAsOf;
    String assetClass;
    String fiAssetClass;

    // private final String errorPrefix;

    public OfxStockInfo()
    {
        this.secInfo = new OfxSecInfo();
        this.stockType = "";
        this.yield = 0.0;
        this.dtYieldAsOf = "";
        this.assetClass = "";
        this.fiAssetClass = "";

        // this.errorPrefix = this.getClass().getName();

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

        // aElement points to <stockinfo>
        iterator = aElement.children().iterator();

        while (iterator.hasNext())
        {
            element = iterator.next();

            switch (element.tagName().toLowerCase())
            {
                case "secinfo":
                    this.secInfo.doData(element);
                    break;
                case "stocktype":
                    this.stockType = element.ownText();
                    break;
                case "yield":
                    this.yield = Double.parseDouble(element.ownText());
                    break;
                case "dtyieldasof":
                    this.dtYieldAsOf = element.ownText();
                    break;
                case "assetclass":
                    this.assetClass = element.ownText();
                    break;
                case "fiassetclass":
                    this.fiAssetClass = element.ownText();
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
        this.secInfo.doSQL(invAcctFrom, false);

        String sTable = "hlhtxc5_dbOfx.StockInfo";

        String[] keys =
        {
            "BrokerId", "SecId", "StockType", "Yield",
            "dtYieldAsOf", "AssetClass", "FiAssetClass"
        };
        String[] values =
        {
            invAcctFrom.brokerId.toString(), this.secInfo.secId.uniqueId,
            this.stockType, String.valueOf(this.yield), this.dtYieldAsOf,
            this.assetClass, this.fiAssetClass
        };

        return this.doSQL(sTable, keys, values, 2);
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hpi.ofxAggregates;

import com.hpi.TPCCMcontrollers.CMLanguageController;
import com.hpi.hpiUtils.*;
import java.text.*;
import java.util.*;
import java.util.logging.*;

import org.jsoup.nodes.Element;

/**
 *
 * @author Joe@Higgs-Tx.com
 */
public class OfxOptInfo
    extends OfxAggregateBase
    implements IOfxSQL
{

    OfxSecInfo secInfo;
    String optType;
    Double strikePrice;
    String dtExpire;
    Integer shPerCtrct;
//    OfxSecId secId;
    String assetClass;
    String fiAssetClass;

    // private final String errorPrefix;
    public OfxOptInfo()
    {
        this.secInfo = new OfxSecInfo();
        this.optType = null;
        this.strikePrice = null;
        this.dtExpire = null;
        this.shPerCtrct = null;
//        this.secId = new OfxSecId();
        this.assetClass = null;
        this.fiAssetClass = null;

        // this.errorPrefix = this.getClass().getName();
    }

    /**
     *
     * @param aElement
     *
     * @return
     */
    public Boolean doData(Element aElement)
    {
        String s;
        Element element;
        Iterator<Element> iterator;

        // aElement points to <optinfo>
        iterator = aElement.children().iterator();

        while (iterator.hasNext())
        {
            element = iterator.next();

            switch (element.tagName().toLowerCase())
            {
                case "secinfo":
                    this.secInfo.doData(element);
                    break;
                case "opttype":
                    this.optType = element.ownText();
                    break;
                case "strikeprice":
                    this.strikePrice = Double.parseDouble(element.ownText());
                    // spurious listings with strike price equal to 0
                    if (this.strikePrice.equals(0.0))
                    {
                        return false;
                    }
                    break;
                case "dtexpire":
                    this.dtExpire = element.ownText();
                    break;
                case "shperctrct":
                    this.shPerCtrct = Integer.parseInt(element.ownText());
                    break;
//                case "secid":
//                    //never hit, part of secInfo
//                    this.secId.doData(element);
//                    break;
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
        /*
         * eTrade issue with options expiration date
         * 200320 is sometimes coming across as 200321
         */

        this.secInfo.doSQL(invAcctFrom, true);

        /*
         * solve etrade issue with dtExpire
         *  uniqueId is more accurate
         */
        if (invAcctFrom.getBrokerIdFi().equalsIgnoreCase("etrade.com"))
        {
            //todo: change to remove magic number
            this.dtExpire = "20" + this.secInfo.secId.uniqueId.substring(0, 6);
        }

        String sTable1 = "hlhtxc5_dbOfx.OptInfo";
        String sTable2 = "hlhtxc5_dbOfx.SecInfo";

        String[] keys1 =
        {
            "BrokerId", "SecId", "OptType", "StrikePrice", "DtExpire",
            "ShPerCtrct", "SecIdUnderlying", "AssetClass", "FiAssetClass"
        };

        String[] keys2 =
        {
            "BrokerId", "SecId", "EquityId"
        };

        String[] values1 =
        {
            invAcctFrom.brokerId.toString(), this.secInfo.secId.uniqueId,
            this.optType, String.valueOf(this.strikePrice),
            this.dtExpire, String.valueOf(this.shPerCtrct),
            this.secInfo.secId.uniqueId, this.assetClass,
            this.fiAssetClass
        };

        //get the underlying ticker
        //convert date string to java.util Date
        
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH);
        Date date = null;
        try
        {
            date = formatter.parse(this.dtExpire);
        } catch (ParseException ex)
        {
            Logger.getLogger(OfxOptInfo.class.getName()).log(Level.SEVERE, null, ex);
        }

        String[] values2 =
        {
            invAcctFrom.brokerId.toString(), this.secInfo.secId.uniqueId,
            CMHPIUtils.getOCCTicker(this.secInfo.ticker, date, this.optType, this.strikePrice)
        };

        return ((this.doSQL(sTable1, keys1, values1, 2)) && (this.doSQL(sTable2, keys2, values2, 2)));
    }
}

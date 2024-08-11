/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hpi.ofxAggregates;

import com.hpi.TPCCMcontrollers.CMLanguageController;
import static java.lang.Character.isLetter;
import java.util.Iterator;
import org.jsoup.nodes.Element;

/**
 *
 * @author Joe@Higgs-Tx.com
 */
public class OfxSecInfo
    extends OfxAggregateBase
    implements IOfxSQL
{

    OfxSecId secId;
    String secName;
    String ticker;
    String fiId;
    String rating;
    Double unitPrice;
    String dtAsOf;
    OfxCurrency currency;
    String memo;

    // private final String errorPrefix;
    public static final String FID_TRADESTATION = "11777";
    public static final int DEBTINFO = 0;
    public static final int MFINFO = 1;
    public static final int OPTINFO = 2;
    public static final int OTHERINFO = 3;
    public static final int STOCKINFO = 4;

    public OfxSecInfo()
    {
        this.secId = new OfxSecId();
        this.secName = null;
        this.ticker = null;
        this.fiId = null;
        this.rating = null;
        this.unitPrice = null;
        this.dtAsOf = null;
        this.currency = new OfxCurrency();
        this.memo = null;

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

        // aElement points to <secinfo>
        iterator = aElement.children().iterator();

        while (iterator.hasNext())
        {
            element = iterator.next();

            switch (element.tagName().toLowerCase())
            {
                case "secid":
                    this.secId.doData(element);
                    break;
                case "secname":
                    this.secName = element.ownText();
                    break;
                case "ticker":
                    this.ticker = element.ownText();
                    break;
                case "fiid":
                    this.fiId = element.ownText();
                    break;
                case "rating":
                    this.rating = element.ownText();
                    break;
                case "unitprice":
                    this.unitPrice = Double.parseDouble(element.ownText());
                    break;
                case "dtasof":
                    this.dtAsOf = element.ownText();
                    break;
                case "currency":
                    this.currency.doData(element);
                    break;
                case "memo":
                    this.memo = element.ownText();
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
        throw new UnsupportedOperationException("Do not use");
    }

    public Boolean doSQL(OfxInvAcctFrom invAcctFrom, boolean bIsOption)
    {
        String[] keys;
        String sTable;
        String[] values;
        int i;

        this.secId.doSQL(invAcctFrom);
        this.currency.doSQL(invAcctFrom);
        sTable = "hlhtxc5_dbOfx.SecInfo";

        //todo: works for all now given some tweaking of SecName
        // to ensure it begins with the underlying ticker
        if (bIsOption)
        {
            switch (invAcctFrom.FId)
            {
                case OfxSecInfo.FID_TRADESTATION:
                    // issue here is that they have CALL/PUT as first
                    // characters in SecName.
                    // CALL COST 175.0000 20181019
                    // PUT COST
                    if (this.secName.substring(0, 3).equalsIgnoreCase("put"))
                    {
                        this.secName = this.secName.substring(4);
                    } else
                    {
                        this.secName = this.secName.substring(5);
                    }
                    break;
                default:
            }

            for (i = 0; i < this.secName.length(); i++)
            {
                if (!isLetter(this.secName.charAt(i)))
                {
                    this.ticker = this.secName.substring(0, i--);
                    this.ticker = this.ticker.trim();
                    break;
                }
            }

            keys = new String[]
            {
                "BrokerId", "SecId", "SecName", "Ticker", "FiId",
                "Rating", "UnitPrice", "DtAsOf", "CurSym", "Memo"
            };

            //when an option cannot update secInfo.EquityId, not enough info here
            values = new String[10];
            values[0] = invAcctFrom.brokerId.toString();
            values[1] = this.secId.uniqueId;
            values[2] = this.secName;
            values[3] = this.ticker;
            values[4] = this.fiId;
            values[5] = this.rating;
            values[6] = String.valueOf(this.unitPrice);
            values[7] = this.dtAsOf;
            values[8] = this.currency.curSym;
            values[9] = this.memo;

            // only 1 primary designated so we can update the name
            return this.doSQL(sTable, keys, values, 2);
        }

        //not an option; use this opportunity to update secInfo.EquityId field
        keys = new String[]
        {
            "BrokerId", "SecId", "EquityId", "SecName", "Ticker", "FiId",
            "Rating", "UnitPrice", "DtAsOf", "CurSym", "Memo"
        };

        //when an option cannot update secInfo.EquityId, not enough info here
        values = new String[11];
        values[0] = invAcctFrom.brokerId.toString();
        values[1] = this.secId.uniqueId;
        values[2] = this.ticker;
        values[3] = this.secName;
        values[4] = this.ticker;
        values[5] = this.fiId;
        values[6] = this.rating;
        values[7] = String.valueOf(this.unitPrice);
        values[8] = this.dtAsOf;
        values[9] = this.currency.curSym;
        values[10] = this.memo;

        // only 1 primary designated so we can update the name
        return this.doSQL(sTable, keys, values, 2);
    }
}

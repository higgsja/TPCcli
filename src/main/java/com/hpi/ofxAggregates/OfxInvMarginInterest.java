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
public class OfxInvMarginInterest
        extends OfxAggregateBase
        implements IOfxSQL
{

OfxInvTran invTran;
Double total;
String subAcctFund;
OfxCurrency currency;
OfxCurrency origCurrency;


    // private final String errorPrefix;

    public OfxInvMarginInterest()
    {
        this.invTran = new OfxInvTran();
        this.total = null;
        this.subAcctFund = null;
        this.currency = new OfxCurrency();
        this.origCurrency = new OfxCurrency();

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

        // aElement points to <margininterest>
        iterator = aElement.children().iterator();

        while (iterator.hasNext())
        {
            element = iterator.next();

            switch (element.tagName().toLowerCase())
            {
                case "invtran":
                    this.invTran.doData(element);
                    break;
                case "total":
                    this.total = Double.parseDouble(element.ownText());
                    break;
                case "subacctfund":
                    this.subAcctFund = element.ownText();
                    break;
                case "currency":
                    this.currency.doData(element);
                    break;
                case "origcurrency":
                    this.origCurrency.doData(element);
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
        this.invTran.doSQL(invAcctFrom);
        this.currency.doSQL(invAcctFrom);
        this.origCurrency.doSQL(invAcctFrom);

        String sTable = "hlhtxc5_dbOfx.MarginInterest";
        String[] keys =
        {
            "AcctId", "FiTId",
            "Total", "SubAcctFund",
            "Currency", "OrigCurrency"
        };
        String[] values =
        {
            invAcctFrom.invAcctId.toString(), this.invTran.fiTId,
            String.valueOf(this.total), this.subAcctFund,
            this.currency.curSym, this.origCurrency.curSym
        };

        return this.doSQL(sTable, keys, values, 2);
    }
}

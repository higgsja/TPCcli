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
public class OfxInvBuyOther
        extends OfxAggregateBase
        implements IOfxSQL
{

    OfxInvBuy invBuy;

    // private final String errorPrefix;

    public OfxInvBuyOther()
    {
        this.invBuy = new OfxInvBuy();

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

        // aElement points to <buyother>
        iterator = aElement.children().iterator();

        while (iterator.hasNext())
        {
            element = iterator.next();

            switch (element.tagName().toLowerCase())
            {
                case "invbuy":
                    this.invBuy.doData(element);
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

    @Override
    public Boolean doSQL(OfxInvAcctFrom invAcctFrom)
    {
        this.invBuy.doSQL(invAcctFrom);

        String sTable = "hlhtxc5_dbOfx.BuyOther";
        String[] keys =
        {
            "AcctId", "FiTId"
        };
        String[] values =
        {
            invAcctFrom.invAcctId.toString(), this.invBuy.invTran.fiTId
        };

        return this.doSQL(sTable, keys, values, 2);
    }
}

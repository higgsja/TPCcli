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
public class OfxInvSellStock
        extends OfxAggregateBase
        implements IOfxSQL
{

    OfxInvSell invSell;
    String sellType;

    // private final String errorPrefix;

    public OfxInvSellStock()
    {
        this.invSell = new OfxInvSell();
        this.sellType = null;

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

        // aElement points to <sellstock>
        iterator = aElement.children().iterator();

        while (iterator.hasNext())
        {
            element = iterator.next();

            switch (element.tagName().toLowerCase())
            {
                case "invsell":
                    this.invSell.doData(element);
                    break;
                case "selltype":
                    this.sellType = element.ownText();
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
        this.invSell.doSQL(invAcctFrom);

        String sTable = "hlhtxc5_dbOfx.SellStock";
        String[] keys =
        {
            "AcctId", "FiTId", "SellType"
        };
        String[] values =
        {
            invAcctFrom.invAcctId.toString(), this.invSell.invTran.fiTId,
            this.sellType
        };

        return this.doSQL(sTable, keys, values, 2);
    }
}

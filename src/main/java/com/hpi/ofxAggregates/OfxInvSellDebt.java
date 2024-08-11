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
public class OfxInvSellDebt
        extends OfxAggregateBase
        implements IOfxSQL
{

    OfxInvSell invSell;
    String sellReason;
    Double accrdInt;

    // private final String errorPrefix;

    public OfxInvSellDebt()
    {
        this.invSell = new OfxInvSell();
        this.sellReason = null;
        this.accrdInt = null;

        // this.errorPrefix = this.getClass().getName();

    }

    public Boolean doData(Element aElement)
    {
        String s;
        Element element;
        Iterator<Element> iterator;

        // aElement points to <selldebt>
        iterator = aElement.children().iterator();

        while (iterator.hasNext())
        {
            element = iterator.next();

            switch (element.tagName().toLowerCase())
            {
                case "invsell":
                    this.invSell.doData(element);
                    break;
                case "sellreason":
                    this.sellReason = element.ownText();
                    break;
                case "accrdint":
                    this.accrdInt = Double.parseDouble(element.ownText());
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

        String sTable = "hlhtxc5_dbOfx.SellDebt";
        String[] keys =
        {
            "AcctId", "FiTId", "SellReason", "AccrdInt"
        };
        String[] values =
        {
            invAcctFrom.invAcctId.toString(), this.invSell.invTran.fiTId,
            this.sellReason, String.valueOf(this.accrdInt)
        };

        return this.doSQL(sTable, keys, values, 2);
    }
}

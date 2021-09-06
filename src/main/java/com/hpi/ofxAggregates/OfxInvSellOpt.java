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
public class OfxInvSellOpt
        extends OfxAggregateBase
        implements IOfxSQL
{

    OfxInvSell invSell;
    String optSellType;
    Integer shPerCtrct;
    String relFiTId;
    String relType;
    String secured;

    // private final String errorPrefix;

    public OfxInvSellOpt()
    {
        this.invSell = new OfxInvSell();
        this.optSellType = null;
        this.shPerCtrct = null;
        this.relFiTId = null;
        this.relType = null;
        this.secured = null;

        // this.errorPrefix = this.getClass().getName();

    }

    public Boolean doData(Element aElement)
    {
        //tested
        String s;
        Element element;
        Iterator<Element> iterator;

        // aElement points to <sellopt>
        iterator = aElement.children().iterator();

        while (iterator.hasNext())
        {
            element = iterator.next();

            switch (element.tagName().toLowerCase())
            {
                case "invsell":
                    this.invSell.doData(element);
                    break;
                case "optselltype":
                    this.optSellType = element.ownText();
                    break;
                case "shperctrct":
                    this.shPerCtrct = Integer.parseInt(element.ownText());
                    break;
                case "relfitid":
                    this.relFiTId = element.ownText();
                    break;
                case "reltype":
                    this.relType = element.ownText();
                    break;
                case "secured":
                    this.secured = element.ownText();
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

        String sTable = "hlhtxc5_dbOfx.SellOpt";
        String[] keys =
        {
            "AcctId", "FiTId", "OptSellType", "ShPerCtrct", "RelFiTId",
            "RelType", "Secured"
        };
        String[] values =
        {
            invAcctFrom.invAcctId.toString(), this.invSell.invTran.fiTId,
            this.optSellType, String.valueOf(this.shPerCtrct), this.relFiTId,
            this.relType, this.secured
        };

        return this.doSQL(sTable, keys, values, 2);
    }
}

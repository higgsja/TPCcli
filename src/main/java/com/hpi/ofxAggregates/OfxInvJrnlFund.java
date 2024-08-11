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
public class OfxInvJrnlFund
        extends OfxAggregateBase
        implements IOfxSQL
{

    OfxInvTran invTran;
    String subAcctTo;
    String subAcctFrom;
    Double total;

    // private final String errorPrefix;

    public OfxInvJrnlFund()
    {
        this.invTran = new OfxInvTran();
        this.subAcctTo = null;
        this.subAcctFrom = null;
        this.total = null;

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
        // aElement points to <jrnlfund>
        iterator = aElement.children().iterator();

        while (iterator.hasNext())
        {
            element = iterator.next();

            switch (element.tagName().toLowerCase())
            {
                case "invtran":
                    this.invTran.doData(element);
                    break;
                case "subacctto":
                    this.subAcctTo = element.ownText();
                    break;
                case "subacctfrom":
                    this.subAcctFrom = element.ownText();
                    break;
                case "total":
                    this.total = Double.parseDouble(element.ownText());
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
        //tested
        this.invTran.doSQL(invAcctFrom);

        String sTable = "hlhtxc5_dbOfx.JrnlFund";
        String[] keys =
        {
            "AcctId", "FiTId",
            "SubAcctTo", "SubAcctFrom", "Total"
        };
        String[] values =
        {
            invAcctFrom.invAcctId.toString(), this.invTran.fiTId,
            this.subAcctTo, this.subAcctFrom, String.valueOf(this.total)
        };

        return this.doSQL(sTable, keys, values, 2);
    }
}

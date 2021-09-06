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
public class OfxPosOther
        extends OfxAggregateBase
        implements IOfxSQL
{

    OfxInvPos invPos;

    // private final String errorPrefix;

    public OfxPosOther()
    {
        this.invPos = new OfxInvPos();

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

        // aElement points to <posdebt>
        iterator = aElement.children().iterator();

        while (iterator.hasNext())
        {
            element = iterator.next();

            switch (element.tagName().toLowerCase())
            {
                case "invpos":
                    this.invPos.doData(element);
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

    /**
     *
     * @param invAcctFrom
     * @return
     */
    @Override
    public Boolean doSQL(OfxInvAcctFrom invAcctFrom)
    {
        //tested
        this.invPos.doSQL(invAcctFrom, false);

        String sTable = "hlhtxc5_dbOfx.PosOther";
        String[] keys =
        {
            "AcctId", "InvPosId"
        };
        String[] values =
        {
            invAcctFrom.invAcctId.toString(), this.invPos.invPosId.toString()
        };
        // posxxx tables are special. they are dropped prior to processing
        // so there will never be an update.

        return this.doSQL(sTable, keys, values, 2);
    }
}

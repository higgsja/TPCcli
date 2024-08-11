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
public class OfxPosStock
        extends OfxAggregateBase
        implements IOfxSQL
{

    OfxInvPos invPos;
    Double unitsStreet;
    Double unitsUser;
    String reinvDiv;

    // private final String errorPrefix;

    public OfxPosStock()
    {
        this.invPos = new OfxInvPos();
        this.unitsStreet = null;
        this.unitsUser = null;
        this.reinvDiv = null;

        // this.errorPrefix = this.getClass().getName();

    }

    /**
     *
     * @param aElement
     * @return
     */
    public Boolean doData(Element aElement)
    {
        //tested
        String s;
        Element element;
        Iterator<Element> iterator;

        // aElement points to <posstock>
        iterator = aElement.children().iterator();

        while (iterator.hasNext())
        {
            element = iterator.next();

            switch (element.tagName().toLowerCase())
            {
                case "invpos":
                    this.invPos.doData(element);
                    break;
                case "unitsstreet":
                    this.unitsStreet = Double.parseDouble(element.ownText());
                    break;
                case "unitsuser":
                    this.unitsUser = Double.parseDouble(element.ownText());
                    break;
                case "reinvdiv":
                    this.reinvDiv = element.ownText();
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
        this.invPos.doSQL(invAcctFrom, false);
        String sTable = "hlhtxc5_dbOfx.PosStock";
        String[] keys =
        {
            "AcctId", "InvPosId", "UnitsStreet", "UnitsUser", "ReinvDiv"
        };
        String[] values =
        {
            invAcctFrom.invAcctId.toString(), this.invPos.invPosId.toString(),
            String.valueOf(this.unitsStreet), String.valueOf(this.unitsUser),
            this.reinvDiv
        };
        // posxxx tables are special. they are dropped prior to processing
        // so there will never be an update.

        return this.doSQL(sTable, keys, values, 2);
    }
}

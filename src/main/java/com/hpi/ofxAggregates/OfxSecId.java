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
public class OfxSecId
        extends OfxAggregateBase
        implements IOfxSQL
{

    String uniqueId;
    String uniqueIdType;

    // private final String errorPrefix;

    public OfxSecId()
    {
        this.uniqueId = null;
        this.uniqueIdType = null;

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

        // aElement points to <secid>
        iterator = aElement.children().iterator();

        while (iterator.hasNext())
        {
            element = iterator.next();

            switch (element.tagName().toLowerCase())
            {
                case "uniqueid":
                    this.uniqueId = element.ownText();
                    break;
                case "uniqueidtype":
                    this.uniqueIdType = element.ownText();
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
        // deal with FI differences in data for Options
        // FI SecId differences have already been handled

        String sTable = "hlhtxc5_dbOfx.SecId";

        String[] keys =
        {
            "BrokerId", "SecId", "UniqueType"
        };

        String[] values =
        {
            invAcctFrom.brokerId.toString(), this.uniqueId,
            this.uniqueIdType
        };

        return this.doSQL(sTable, keys, values, 2);
    }
}

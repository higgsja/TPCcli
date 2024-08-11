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
public class OfxOfx
{

    OfxSignOnMsgsRSv1 signOnMsgsRSv1;
    OfxInvStmtMsgsRSv1 invStmtMsgsRSv1;
    OfxSecListMsgsRSv1 secListMsgsRSv1;

    // private final String errorPrefix;

    public OfxOfx()
    {
        this.signOnMsgsRSv1 = new OfxSignOnMsgsRSv1();
        this.invStmtMsgsRSv1 = new OfxInvStmtMsgsRSv1();
        this.secListMsgsRSv1 = new OfxSecListMsgsRSv1();

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

        // aElement points to <ofx>
        iterator = aElement.children().iterator();

        while (iterator.hasNext())
        {
            element = iterator.next();

            switch (element.tagName().toLowerCase())
            {
                case "signonmsgsrsv1":
                    this.signOnMsgsRSv1.doData(element, this.invStmtMsgsRSv1);
                    break;
                case "seclistmsgsrsv1":
                    this.secListMsgsRSv1.doData(element);
                    break;
                case "invstmtmsgsrsv1":
                    this.invStmtMsgsRSv1.doData(element, signOnMsgsRSv1);
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

    public OfxSignOnMsgsRSv1 getSignOnMsgsRSv1()
    {
        return signOnMsgsRSv1;
    }

    public OfxInvStmtMsgsRSv1 getInvStmtMsgsRSv1()
    {
        return invStmtMsgsRSv1;
    }

    public OfxSecListMsgsRSv1 getSecListMsgsRSv1()
    {
        return secListMsgsRSv1;
    }


}

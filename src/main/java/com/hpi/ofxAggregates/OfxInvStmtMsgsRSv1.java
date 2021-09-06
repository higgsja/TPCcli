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
public class OfxInvStmtMsgsRSv1
{

    OfxInvStmtTrnRS invStmtTrnRS;
    //OfxInvMailTrnRS invMailTrnRS;
    //OfxInvMailSyncRS invMailSyncRS;
    //OfxInvStmtEndTrnRS invStmtEndTrnRS;

    // private final String errorPrefix;

    public OfxInvStmtMsgsRSv1()
    {
        this.invStmtTrnRS = new OfxInvStmtTrnRS();

        // this.errorPrefix = this.getClass().getName();

    }

    public Boolean doData(Element aElement, OfxSignOnMsgsRSv1 signOnMsgsRSv1)
    {
        String s;
        Element element;
        Iterator<Element> iterator;

        // aElement points to <invstmtmsgsrsv1>
        iterator = aElement.children().iterator();

        while (iterator.hasNext())
        {
            element = iterator.next();

            switch (element.tagName().toLowerCase())
            {
                case "invstmttrnrs":
                    this.invStmtTrnRS.doData(element, signOnMsgsRSv1);
                    break;
                default:
                    // ignore extra elements but log them
                    s = String.format(CMLanguageController.
                            getErrorProps().getProperty("Formatted3"),
                            element.tagName());

                    //Logger.getLogger(this.getClass()).info(s);
            }
        }
        return true;
    }

    public OfxInvStmtTrnRS getInvStmtTrnRS()
    {
        return invStmtTrnRS;
    }
}

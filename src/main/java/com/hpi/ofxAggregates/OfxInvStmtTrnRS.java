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
public class OfxInvStmtTrnRS
{

    String truUId;
    OfxStatus status;
    String cltCookie;
    OfxInvStmtRS invStmtRS;

    // private final String errorPrefix;

    public OfxInvStmtTrnRS()
    {
        this.truUId = null;
        this.status = new OfxStatus();
        this.cltCookie = null;
        this.invStmtRS = new OfxInvStmtRS();

        // this.errorPrefix = this.getClass().getName();

    }

    /**
     *
     * @param aElement
     * @param signOnMsgsRSv1
     * @return
     */
    public Boolean doData(Element aElement, OfxSignOnMsgsRSv1 signOnMsgsRSv1)
    {
        //tested
        String s;
        Element element;
        Iterator<Element> iterator;

        // aElement points to <invstmttrnrs>
        iterator = aElement.children().iterator();

        while (iterator.hasNext())
        {
            element = iterator.next();

            switch (element.tagName().toLowerCase())
            {
                case "trnuid":
                    this.truUId = element.ownText();
                    break;
                case "status":
                    this.status.doData(element);
                    break;
                case "cltcookie":
                    this.cltCookie = element.ownText();
                    break;
                case "invstmtrs":
                    this.invStmtRS.doData(element, signOnMsgsRSv1);
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

    public OfxInvStmtRS getInvStmtRS()
    {
        return invStmtRS;
    }
}

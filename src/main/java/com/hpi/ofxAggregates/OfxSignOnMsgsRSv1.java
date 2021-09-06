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
 * This class handles data from the request response. Data is not persistent.
 *
 * @author Joe@Higgs-Tx.com
 */
public class OfxSignOnMsgsRSv1
{

    OfxSonRS sonRS;

    // private final String errorPrefix;

    public OfxSignOnMsgsRSv1()
    {
        //tested
        this.sonRS = new OfxSonRS();

        // this.errorPrefix = this.getClass().getName();

    }

    /**
     *
     * @param aElement
     * @return
     */
    public Boolean doData(Element aElement, OfxInvStmtMsgsRSv1 invStmtMsgsRSv1)
    {
        //tested
        String s;
        Element element;
        Iterator<Element> iterator;

        // aElement points to <signonmsgsrsv1>
        iterator = aElement.children().iterator();

        while (iterator.hasNext())
        {
            element = iterator.next();

            switch (element.tagName().toLowerCase())
            {
                case "sonrs":
                    this.sonRS.doData(element, invStmtMsgsRSv1);
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

    public OfxSonRS getSonRS()
    {
        return sonRS;
    }
}

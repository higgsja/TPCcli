/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hpi.ofxAggregates;

import com.hpi.TPCCMcontrollers.CMLanguageController;
import java.util.ArrayList;
import java.util.Iterator;
import org.jsoup.nodes.Element;




/**
 *
 * @author Joe@Higgs-Tx.com
 */
public class OfxFiMFAssetClass
        extends OfxAggregateBase
{

    ArrayList<OfxFiMFPortion> fiMFPortionList;

    // private final String errorPrefix;

    public OfxFiMFAssetClass()
    {
        this.fiMFPortionList = new ArrayList<>();

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
        OfxFiMFPortion fiMFPortion;

        // aElement points to <mffiassetclass>
        iterator = aElement.children().iterator();

        while (iterator.hasNext())
        {
            element = iterator.next();

            switch (element.tagName().toLowerCase())
            {
                case "fiportion":
                    fiMFPortion = new OfxFiMFPortion();
                    if (fiMFPortion.doData(element))
                    {
                        this.fiMFPortionList.add(fiMFPortion);
                    }
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

    public Boolean doSQL(Integer BrokerId, String secId)
    {
        //tested
        Iterator<OfxFiMFPortion> iterator;

        iterator = this.fiMFPortionList.iterator();

        while (iterator.hasNext())
        {
            iterator.next().doSQL(BrokerId, secId);
        }
        return true;
    }
}

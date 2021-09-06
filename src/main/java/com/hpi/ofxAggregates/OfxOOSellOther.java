/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hpi.ofxAggregates;

import com.hpi.TPCCMcontrollers.CMLanguageController;
import com.hpi.hpiUtils.CMHPIUtils;
import java.util.Iterator;
import javax.swing.JOptionPane;
import org.jsoup.nodes.Element;




/**
 *
 * @author Joe@Higgs-Tx.com
 */
public class OfxOOSellOther
{

    OfxOpenOrder openOrder;
    String unitType;
    
    private final String errorPrefix;

    public OfxOOSellOther()
    {
        this.openOrder = new OfxOpenOrder();
        this.unitType = null;
        
        this.errorPrefix = this.getClass().getName();
        
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
        CMHPIUtils.showDefaultMsg(CMLanguageController.getAppProps().getProperty("Title"),
                errorPrefix,
                "doData",
                "Not tested.",
                JOptionPane.ERROR_MESSAGE);

        // aElement points to <oosellother>
        iterator = aElement.children().iterator();

        while (iterator.hasNext())
        {
            element = iterator.next();

            switch (element.tagName().toLowerCase())
            {
                case "oo":
                    this.openOrder.doData(element);
                    break;
                case "unittype":
                    this.unitType = element.ownText();
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
}

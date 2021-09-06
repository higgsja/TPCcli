/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hpi.ofxAggregates;

import com.hpi.TPCCMcontrollers.CMLanguageController;
import com.hpi.hpiUtils.CMHPIUtils;
// import java.util.Iterator;
import javax.swing.JOptionPane;
// 
import org.jsoup.nodes.Element;

public class OfxMFPortion
        extends OfxAggregateBase
{

    String assetClass;
    Double percent;

    private final String errorPrefix;

    public OfxMFPortion()
    {
        this.assetClass = null;
        this.percent = null;

        this.errorPrefix = this.getClass().getName();

    }

    /**
     *
     * @param aElement
     * @return
     */
    public Boolean doData(Element aElement)
    {
        //tested
        // String s;
        // Element element;
        // Iterator<Element> iterator;
        CMHPIUtils.showDefaultMsg(CMLanguageController.getAppProps().
                    getProperty("Title"),
                errorPrefix,
                "doData",
                "Not tested.",
                JOptionPane.ERROR_MESSAGE);

        // aElement points to <portion>
        // todo: handle mfinfo
        return true;
//        iterator = aElement.children().iterator();
//
//        while (iterator.hasNext())
//        {
//            element = iterator.next();
//
//            switch (element.tagName().toLowerCase())
//            {
//                case "assetclass":
//                    this.assetClass = element.ownText();
//                    break;
//                case "percent":
//                    this.percent = Double.parseDouble(element.ownText());
//                    break;
//                default:
//                    // actually do not care that there are extra elements
//                    //  but let's log them
//                    s = String.format(CMLanguageController.
//                            getErrorProps().getProperty("Formatted3"),
//                            element.tagName());
//
//                    //Logger.getLogger(this.getClass()).info(s);
//            }
//        }
//        return true;
    }

    public Boolean doSQL(Integer BrokerId, String secId)
    {
         CMHPIUtils.showDefaultMsg(CMLanguageController.
                     getAppProps().getProperty("Title"),
                errorPrefix,
                "doSQL",
                "Not tested.",
                JOptionPane.ERROR_MESSAGE);

        String sTable = "hlhtxc5_dbOfx.MFPortion";
        String [] keys =
        {
            "BrokerId", "SecId", "AssetClass", "Percent"
        };
        String [] values =
        {
            BrokerId.toString(), secId, this.assetClass, this.percent.toString()
        };

        return (this.doSQL(sTable, keys, values, 2));
    }
}

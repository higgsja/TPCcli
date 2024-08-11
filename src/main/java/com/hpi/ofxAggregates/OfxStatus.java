/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hpi.ofxAggregates;

import com.hpi.TPCCMcontrollers.CMLanguageController;
// import com.hpi.hpiUtils.CMHPIUtils;
import java.util.Iterator;
// import javax.swing.JOptionPane;
import org.jsoup.nodes.Element;




/**
 *
 * @author Joe@Higgs-Tx.com
 */
public class OfxStatus
{

    Integer code;
    String severity;
    String message;


    // private final String errorPrefix;

    public OfxStatus()
    {
        code = 0;
        severity = "";
        message = "";

        // this.errorPrefix = this.getClass().getName();

    }

    /**
     *
     * @param aElement
     * @return
     */
    public Boolean doData(Element aElement)
    {
        // tested
        String s;
        Element element;
        Iterator<Element> iterator;

        // aElement points to <status>
        iterator = aElement.children().iterator();

        while (iterator.hasNext())
        {
            element = iterator.next();

            switch (element.tagName().toLowerCase())
            {
                case "code":
                    this.code = Integer.parseInt(element.ownText());
                    break;
                case "severity":
                    this.severity = element.ownText();
                    break;
                case "message":
                    this.message = element.ownText();
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

    public Integer getCode()
    {
        return code;
    }

    public String getSeverity()
    {
        return severity;
    }

    public String getMessage()
    {
        return message;
    }
}

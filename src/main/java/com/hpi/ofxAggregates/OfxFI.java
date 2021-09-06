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
public class OfxFI {

    String org;
    String fid;

    // private final String errorPrefix;
    public OfxFI() {
        this.org = null;
        this.fid = null;

        // this.errorPrefix = this.getClass().getName();
    }

    /**
     *
     * @param aElement
     * @param invStmtMsgsRSv1
     * @return
     */
    public Boolean doData(Element aElement, OfxInvStmtMsgsRSv1 invStmtMsgsRSv1) {
        String s;
        Element element;
        Iterator<Element> iterator;

        // aElement points to <fi>
        iterator = aElement.children().iterator();

        while (iterator.hasNext()) {
            element = iterator.next();

            switch (element.tagName().toLowerCase()) {
                case "org":
                    this.org = element.ownText();
                    break;
                case "fid":
                    this.fid = element.ownText();
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

    public String getOrg() {
        return this.org;
    }

    public String getFid() {
        return this.fid;
    }
}

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
public class OfxImageData {

    String imageType;
    String imageRef;
    String imageRefType;
    String imageDelay;
    String dtImageAvail;
    String imageTTL;
    String checkSup;

    // private final String errorPrefix;
    public OfxImageData() {
        this.imageType = null;
        this.imageRef = null;
        this.imageRefType = null;
        this.imageDelay = null;
        this.dtImageAvail = null;
        this.imageTTL = null;
        this.checkSup = null;

        // this.errorPrefix = this.getClass().getName();
    }

    public Boolean doData(Element aElement) {
        String s;
        Element element;
        Iterator<Element> iterator;

        // aElement points to <fi>
        iterator = aElement.children().iterator();

        while (iterator.hasNext()) {
            element = iterator.next();

            switch (element.tagName().toLowerCase()) {
                case "imagetype":
                    this.imageType = element.ownText();
                    break;
                case "imageref":
                    this.imageRef = element.ownText();
                    break;
                case "imagereftype":
                    this.imageRefType = element.ownText();
                    break;
                case "imagedelay":
                    this.imageDelay = element.ownText();
                    break;
                case "dtimageavail":
                    this.dtImageAvail = element.ownText();
                    break;
                case "imagettl":
                    this.imageTTL = element.ownText();
                    break;
                case "checksup":
                    this.checkSup = element.ownText();
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
}

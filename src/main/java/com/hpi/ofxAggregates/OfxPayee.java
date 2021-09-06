package com.hpi.ofxAggregates;

import com.hpi.TPCCMcontrollers.CMLanguageController;
import com.hpi.hpiUtils.CMHPIUtils;
import java.util.Iterator;
import javax.swing.JOptionPane;
import org.jsoup.nodes.Element;




public class OfxPayee
        extends OfxAggregateBase
        implements IOfxSQL
{

    Integer ofxPayeeId;
    String name;
    String addr1;
    String addr2;
    String addr3;
    String city;
    String state;
    String postalCode;
    String country;
    String phone;
    
    private final String errorPrefix;

    public OfxPayee()
    {
        this.ofxPayeeId = null;
        this.name = null;
        this.addr1 = null;
        this.addr2 = null;
        this.addr3 = null;
        this.city = null;
        this.state = null;
        this.postalCode = null;
        this.country = null;
        this.phone = null;

        this.errorPrefix = this.getClass().getName();
        
    }

    /**
     *
     * @param aElement
     * @return
     */
    public Boolean doData(Element aElement)
    {
        CMHPIUtils.showDefaultMsg(CMLanguageController.getAppProps().getProperty("Title"),
                errorPrefix,
                "doData",
                "Not tested.",
                JOptionPane.ERROR_MESSAGE);
        
        String s;

        Element element;
        Iterator<Element> iterator;

        // aElement points to <payee>
        iterator = aElement.children().iterator();

        while (iterator.hasNext())
        {
            element = iterator.next();

            switch (element.tagName().toLowerCase())
            {
                case "name":
                    this.name = element.ownText();
                    break;
                case "addr1":
                    this.addr1 = element.ownText();
                    break;
                case "addr2":
                    this.addr2 = element.ownText();
                    break;
                case "addr3":
                    this.addr3 = element.ownText();
                    break;
                case "city":
                    this.city = element.ownText();
                    break;
                case "state":
                    this.state = element.ownText();
                    break;
                case "postalcode":
                    this.postalCode = element.ownText();
                    break;
                case "country":
                    this.country = element.ownText();
                    break;
                case "phone":
                    this.phone = element.ownText();
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

    @Override
    public Boolean doSQL(OfxInvAcctFrom invAcctFrom)
    {
        String checkSQL;

        CMHPIUtils.showDefaultMsg(CMLanguageController.getAppProps().getProperty("Title"),
                errorPrefix,
                "doSQL",
                "Not tested.",
                JOptionPane.ERROR_MESSAGE);

        String sTable = "Payee";
        String[] keys =
        {
            "OfxPayeeId", "Name", "Addr1", "Addr2", "Addr3", "City",
            "State", "PostalCode", "Country", "Phone"
        };
        String[] values =
        {
            null, this.name, this.addr1, this.addr2, this.addr3, this.city,
            this.state, this.postalCode, this.country, this.phone
        };

        checkSQL = String.format(CMLanguageController.getOfxSqlProp(
                "OfxSQLPayeeTableQueryOfxPayeeId"),
                this.name, this.addr1, this.city, this.state, this.postalCode);

        this.ofxPayeeId = this.doSQLAuto(sTable, keys, values, checkSQL);
        return this.ofxPayeeId != null;
    }
}

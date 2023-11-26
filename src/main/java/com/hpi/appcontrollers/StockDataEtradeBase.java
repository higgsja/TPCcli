package com.hpi.appcontrollers;

//Can be a lot of data so write to file then sql the file
import com.etrade.exampleapp.v1.oauth.model.*;
import java.util.*;
import lombok.*;
import org.springframework.beans.factory.annotation.*;

//Using EquityHistoryModel and equityHistory table
@Setter
@Getter
public class StockDataEtradeBase
{

    static @Autowired TPCcliAppController oauthManager;
    static @Autowired ApiResource apiResource;

    /* Array of symbols */
    static ArrayList<String> symbols;

    public static String getHttpMethod()
    {
        return "GET";
    }
//
    public static String getURL(String symbol)
    {
        return String.format("%s%s", getURL(), symbol);
    }
//
    public static String getQueryParam()
    {
        return null;
    }
//
    public static String getURL()
    {
        return String.format("%s%s", apiResource.getApiBaseUrl(), apiResource.getQuoteUri());
    }
}

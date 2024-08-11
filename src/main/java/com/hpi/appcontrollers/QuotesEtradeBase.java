package com.hpi.appcontrollers;

//Can be a lot of data so write to file then sql the file
import com.etrade.exampleapp.config.*;
import com.etrade.exampleapp.v1.oauth.*;
import com.etrade.exampleapp.v1.oauth.model.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import java.io.*;
import java.time.format.*;
import java.util.*;
import lombok.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;

//Using EquityHistoryModel and equityHistory table
@Setter
@Getter
public abstract class QuotesEtradeBase
{

//    @Autowired TPCcliAppController oauthManager;
    @Autowired ApiResource apiResource;

    /* Array of symbols */
    ArrayList<String> symbols;

    AnnotationConfigApplicationContext ctx;
    SecurityContext securityContext;
    Resource resource;
    AppController appController;

    final ObjectMapper objectMapper = getDefaultObjectMapper();

    final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");

    public QuotesEtradeBase()
    {
        this.symbols = new ArrayList<>();
        this.ctx = new AnnotationConfigApplicationContext();
        this.ctx.register(OOauthConfig.class);
        this.ctx.refresh();

        this.securityContext = ctx.getBean(SecurityContext.class);
        this.resource = securityContext.getResources();
        this.resource.setConsumerKey("a7f522d5dcec23c63a173ad3eb2e93cc");
        this.resource.setSharedSecret("22fece510eb9661ccc1469161200e041db7e28ff20e8d84d456bf1e55749906a");

        this.appController = ctx.getBean(AppController.class);
    }

    public String getHttpMethod()
    {
        return "GET";
    }

    public String getURL(String symbol)
    {
        return String.format("%s%s", getURL(), symbol);
    }

    public String getQueryParam()
    {
        return null;
    }

    public String getURL()
    {
        return String.format("%s%s", apiResource.getApiBaseUrl(), apiResource.getQuoteUri());
    }

    private ObjectMapper getDefaultObjectMapper()
    {
        ObjectMapper defaultObjectMapper = new ObjectMapper();

        // ignore attributes we do not care about rather than fail
        defaultObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // load module for java time 
//        defaultObjectMapper.registerModule(new JavaTimeModule());
        return defaultObjectMapper;
    }

    JsonNode parse(String src)
    {
        try
        {
            return objectMapper.readTree(src);
        } catch (IOException e)
        {
            System.exit(-1);
        }
        return null;
    }

    public <A> A fromJson(JsonNode node, Class<A> clazz)
    {
        try
        {
            return objectMapper.treeToValue(node, clazz);
        } catch (IllegalArgumentException | JsonProcessingException ex)
        {
            java.util.logging.Logger.getLogger(StockQuotesEtradeController.class.getName())
                .log(java.util.logging.Level.SEVERE, null, ex);
            System.exit(-1);
        }
        return null;
    }

    public void progress(String outText)
    {
        if (CmdLineController.getsCLIProgressBar().equalsIgnoreCase("true"))
        {
            System.out.println(outText);
        }
    }

    public String getQuoteUrl()
    {
        //return String.format("%s%s", apiResource.getApiBaseUrl(), apiResource.getQuoteUri());
        return String.format("%s%s", "https://api.etrade.com", "/v1/market/quote/");
    }

    public String getQuoteUrl(String symbol)
    {
        return String.format("%s%s", getQuoteUrl(), symbol);
    }
}

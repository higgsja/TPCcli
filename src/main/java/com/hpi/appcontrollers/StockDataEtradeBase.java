package com.hpi.appcontrollers;

//Can be a lot of data so write to file then sql the file
import com.etrade.exampleapp.v1.oauth.*;
import com.etrade.exampleapp.v1.oauth.model.*;
import java.util.*;
import lombok.*;
import org.springframework.beans.factory.annotation.*;

//Using EquityHistoryModel and equityHistory table
@Setter
@Getter
public class StockDataEtradeBase
{

    static @Autowired ApiController oauthManager;
    static @Autowired ApiResource apiResource;

//    //Oauth related components
//    static private RequestTokenService requestTokenService;
//
//    //Oauth related components
//    static private AuthorizationService authorizationService;
//
//    //Oauth related components
//    static private AccessTokenService accessTokenService;
//
//    /* context to store the oauth token */
//    static private SecurityContext context;
//
//    /* Resttemplate to make api call */
//    static private CustomRestTemplate customRestTemplate;

    /* Array of symbols */
    static ArrayList<String> symbols;

//    /*api used by all clients*/
//    public static String invoke(Message message)
//    {
////        log.debug(" invoke method controller...." + context.isIntialized());
//
//        if (!context.isIntialized() && message.getOauthRequired() == OauthRequired.YES)
//        {
////            log.debug(" Starting oauth handshake...");
//
//            if (requestTokenService != null)
//            {
//
//                //chaining the oauth call RequestToken -> Authorization -> AccessToken
//                requestTokenService.handleNext(authorizationService);
//                authorizationService.handleNext(accessTokenService);
//
//                Message msg = createRequestTokenMessage();
//
//                try
//                {
//                    requestTokenService.handleMessage(msg, context);
//                } catch (ApiException e)
//                {
//                    String s = "Oauth initialization failure, only delayed quote request is available";
//
//                    CMHPIUtils.showDefaultMsg(
//                        CMLanguageController.getErrorProps().
//                            getProperty("Title"),
//                        Thread.currentThread().getStackTrace()[1].
//                            getClassName(),
//                        Thread.currentThread().getStackTrace()[1].
//                            getMethodName(),
//                        s,
//                        JOptionPane.ERROR_MESSAGE);
//                    return null;
//                }
//            }
//        }
//
//        OAuth1Template oauthTemplate = new OAuth1Template(context, message);
//        String response = "";
//        try
//        {
//            oauthTemplate
//                .computeOauthSignature(message.getHttpMethod(), message.getUrl(), message.getQueryString());
//        } catch (Exception e)
//        {
//            String s = e.getMessage();
//
//            CMHPIUtils.showDefaultMsg(
//                CMLanguageController.getErrorProps().
//                    getProperty("Title"),
//                Thread.currentThread().getStackTrace()[1].
//                    getClassName(),
//                Thread.currentThread().getStackTrace()[1].
//                    getMethodName(),
//                s,
//                JOptionPane.ERROR_MESSAGE);
//            return null;
//        }
//
//        message.setOauthHeader(oauthTemplate.getAuthorizationHeader());
//
//        try
//        {
//            response = customRestTemplate.doExecute(message);
//        } catch (ResourceAccessException e)
//        {
//            if (ApiException.class.isAssignableFrom(e.getCause().getClass()))
//            {
//                String s = e.getMessage();
//
//                CMHPIUtils.showDefaultMsg(
//                    CMLanguageController.getErrorProps().
//                        getProperty("Title"),
//                    Thread.currentThread().getStackTrace()[1].
//                        getClassName(),
//                    Thread.currentThread().getStackTrace()[1].
//                        getMethodName(),
//                    s,
//                    JOptionPane.ERROR_MESSAGE);
//                return null;
//            } else
//            {
//                String s = "Internal Failure";
//
//                CMHPIUtils.showDefaultMsg(
//                    CMLanguageController.getErrorProps().
//                        getProperty("Title"),
//                    Thread.currentThread().getStackTrace()[1].
//                        getClassName(),
//                    Thread.currentThread().getStackTrace()[1].
//                        getMethodName(),
//                    s,
//                    JOptionPane.ERROR_MESSAGE);
//                return null;
//            }
//        } catch (Exception e)
//        {
////            log.error(" Error Calling service api ", e);
////            log.error("Exception class name " + e.getClass().getName());
//            if (ApiException.class.isAssignableFrom(e.getCause().getClass()))
//            {
////                log.error(" ApiException found ");
//                String s = e.getMessage();
//
//                CMHPIUtils.showDefaultMsg(
//                    CMLanguageController.getErrorProps().
//                        getProperty("Title"),
//                    Thread.currentThread().getStackTrace()[1].
//                        getClassName(),
//                    Thread.currentThread().getStackTrace()[1].
//                        getMethodName(),
//                    s,
//                    JOptionPane.ERROR_MESSAGE);
//                return null;
//            } else
//            {
//                String s = "Internal Failure";
//
//                CMHPIUtils.showDefaultMsg(
//                    CMLanguageController.getErrorProps().
//                        getProperty("Title"),
//                    Thread.currentThread().getStackTrace()[1].
//                        getClassName(),
//                    Thread.currentThread().getStackTrace()[1].
//                        getMethodName(),
//                    s,
//                    JOptionPane.ERROR_MESSAGE);
//                return null;
//            }
//        }
//        return response;
//    }
//
//    private static Message createRequestTokenMessage()
//    {
//        Message msg = new Message();
//        msg.setOauthRequired(OauthRequired.YES);
//        msg.setHttpMethod(context.getResouces().getRequestTokenHttpMethod());
//        msg.setUrl(context.getResouces().getRequestTokenUrl());
//        msg.setContentType(ContentType.APPLICATION_FORM_URLENCODED);
//        return msg;
//    }
//
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

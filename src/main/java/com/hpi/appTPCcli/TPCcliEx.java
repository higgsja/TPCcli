package com.hpi.appTPCcli;

import com.hpi.appcontrollers.AppController;
import org.springframework.boot.autoconfigure.*;

@SpringBootApplication
public class TPCcliEx
{
//    @Autowired private static AppController appController;

    /**
     * Application entry point.
     *
     * @param args application command line arguments
     */
    public static void main(String[] args)
    {

        AppController appCoreController;
        appCoreController = AppController.getInstance();

        appCoreController.startApp(args);
    }
}

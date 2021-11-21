package com.hpi.appTPCcli;

import com.hpi.appcontrollers.AppController;
import org.springframework.boot.autoconfigure.*;

@SpringBootApplication
public class TPCcliEx
{
    /**
     * Application entry point.
     *
     * @param args application command line arguments
     */
    public static void main(String[] args)
    {

        AppController appController;
        appController = AppController.getInstance();
        
        appController.initApp();

        appController.startApp(args);
    }
}

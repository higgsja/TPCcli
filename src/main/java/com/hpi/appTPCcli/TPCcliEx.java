package com.hpi.appTPCcli;

import com.hpi.appcontrollers.TPCcliAppController;
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

        TPCcliAppController appController;
        appController = TPCcliAppController.getInstance();
        
        appController.initApp();

        appController.startApp(args);
    }
}

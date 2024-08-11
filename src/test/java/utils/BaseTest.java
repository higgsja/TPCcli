package utils;

import java.util.logging.Logger;
import org.junit.Before;

public class BaseTest
{

    @SuppressWarnings("NonConstantLogger")
    private Logger logger;

    public BaseTest()
    {
        logger = null;
    }

    @SuppressWarnings("LoggerStringConcat")
    public void println(String sMethod, String s)
    {
        logger.info(this.getClass().toString().
            substring(this.getClass().toString().lastIndexOf(".") + 1)
            + "\n\t" + sMethod + "\n\t\t" + s);
    }

    @Before
    public void setUp()
    {
        this.setLogger(Logger.getLogger(this.getClass().toString()));
    }

    /**
     *
     * @param logger
     */
    public void setLogger(Logger logger)
    {
        this.logger = logger;
    }

}

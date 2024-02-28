// depends: baja; program

import javax.baja.sys.*;
import com.tridium.program.*;

public class RobotImpl
        extends Robot
{

    public void run()
            throws Exception
    {
        process(Sys.getStation());
    }

    public void process(BComponent c)
            throws Exception
    {
        /*
        TODO:
            -check archived histories against discovered points

         */

    }

}

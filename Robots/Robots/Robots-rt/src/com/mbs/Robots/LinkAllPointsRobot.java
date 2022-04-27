// depends: baja; program

import javax.baja.collection.BITable;
import javax.baja.naming.BOrd;
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
        String sourceOrd = "station:|slot:/Drivers/BacnetNetwork/JCI_Integration_Writable_Points/BSB/Sixth_Floor";
        String targetOrd = "";



        }
    }



}
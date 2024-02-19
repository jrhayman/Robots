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
        String ord = "station:|slot:/Drivers/BacnetNetwork/JCI_Integration_Writable_Points/BSB/Sixth_Floor";
        String bql = "bql:select * from control:NumericWritable";
        BOrd query = BOrd.make(ord + "|" + bql);
        BITable result = (BITable) query.resolve().get();
        Cursor crs = result.cursor();
        while(crs.next()){
            BComponent point = (BComponent) crs.get();

            String pointOrd = ord + "/" + point.getName();
            log.println(point.getHandleOrd().toString());
            BLink in16 = new BLink(point.getHandleOrd(), "in16", "in10", true);
            BLink fallback = new BLink(point.getHandleOrd(), "fallback", "in10", true);
            try{
                point.add("RobotLink16", in16);
                point.rename((Property) point.get("in16"), "newname");
            }catch(Exception e){
                point.set("RobotLink16", in16);
            }try{
                point.add("RobotLinkFallback", fallback);
            }catch(Exception e){
                point.set("RobotLinkFallback", fallback);
            }



        }
    }



}

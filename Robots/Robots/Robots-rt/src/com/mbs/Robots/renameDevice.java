// depends: baja; program; bacnet; niagaraDriver

import javax.baja.bacnet.BBacnetDevice;
import javax.baja.collection.BITable;
import javax.baja.naming.BOrd;
import javax.baja.sys.*;


import com.tridium.program.*;

import java.util.ArrayList;



//rename devices from description property
public class RobotImpl
        extends Robot
{

    public void run()
            throws Exception
    {
        process(Sys.getStation());
    }

    public void process(BComponent c)
            throws Exception {


        String base = "ORD to query from";
        BITable t = (BITable) BOrd.make(base + "| bql:select * from bacnet:BacnetDevice").resolve().get();

        Cursor crs =  t.cursor();
        ArrayList<BBacnetDevice> devices = new ArrayList<>();
        while(crs.next()) {
            crs.get();
            BBacnetDevice device = (BBacnetDevice) crs.get();
            devices.add(device);
        }

        for(BBacnetDevice device : devices){
            try{
                BString desc = (BString) device.getConfig().getDeviceObject().get("description");

                BComponent parent = ((BComponent)device.getParent());
                Property p = parent.getProperty(device.getName());
                parent.rename(parent.getProperty(device.getName()), desc);
            }
            catch (Exception e){
                log.println(e.getMessage());
            }
        }

    }
}
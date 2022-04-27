// depends: baja; program; history

import javax.baja.naming.BOrd;
import javax.baja.sys.*;
import com.tridium.program.*;
import javax.baja.history.*;
import javax.baja.bql.*;
import javax.baja.collection.*;




public class RobotImpl
        extends Robot{


    public void run()
            throws Exception {
        process(Sys.getStation());
        log.println("complete");
    }
    public void process(BComponent c)
            throws Exception {
        String Floor = "";
        String Equipment = "";
        String Device = "";
        BOrd query = BOrd.make("local:|station:|slot:/Drivers/BacnetNetwork|bql:select * from history:HistoryConfig");
        BITable result = (BITable)query.resolve().get();
        Cursor csr = result.cursor();
        log.println(query.toString());
        log.println(result.toString());
        //log.println(csr.get().toString());
        while(csr.next()){
            BComponent config = (BComponent) csr.get();
            BComponent parent = (BComponent) csr.get();
            while(!parent.getType().toString().equals("bacnet:BacnetNetwork")){

                if(parent.getType().toString().equals("bacnet:BacnetDevice")){
                    if(parent.getName().contains("RTU")){
                        Equipment = "RTU";
                        Device = parent.getName();
                    }else {
                        Device = parent.getName();
                        Equipment = parent.getParent().getName();
                    }
                }
                else if(parent.getType().toString().equals("baja:Folder")){
                    Equipment = "Misc";
                    Device = parent.getName();
                }
                else if(parent.getType().toString().equals("bacnet:BacnetDeviceFolder")){
                    if(parent.getParent().getType().toString().equals("bacnet:BacnetNetwork")){
                        Floor = parent.getName();
                    }else{
                        Equipment = parent.getName();
                    }
                }
                //log.println(parent.getType().toString());
                parent = parent.getParent().asComponent();
            }
            config.add("Floor", BString.make(Floor));
            config.add("Equipment", BString.make(Equipment));
            config.add("Device", BString.make(Device));
            log.println("Floor Name " + Floor);
            log.println("Equipment Name: " + Equipment);
            log.println("Device Name: "+Device);
            log.println("\n\n");
        }

    }
}

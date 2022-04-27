// depends: baja; program; history

import javax.baja.sys.*;
import com.tridium.program.*;
import javax.baja.history.*;




public class RobotImpl
        extends Robot
{

    public void run()
            throws Exception
    {
        process(Sys.getStation());
        log.println("complete");
    }

    public void process(BComponent c)
            throws Exception
    {

        String Floor = "";
        String Equipment = "";
        String Device = "";


        String numericInterval = "history:NumericIntervalHistoryExt";
        String numericCov = "history:NumericCovHistoryExt";
        String booleanInterval = "history:BooleanIntervalHistoryExt";
        String booleanCov = "history:BooleanCovHistoryExt";
        String enumInterval = "history:EnumIntervalHistoryExt";
        String enumCov = "history:EnumCovHistoryExt";



        // do something here
        //log.println(c.toPathString() + " [" + c.getType() + "]");

        if(c.getType().toString().equals("history:HistoryConfig")){
            String historyType = c.getParent().getType().toString();
            if(historyType.equals(numericInterval) || historyType.equals(numericCov) || historyType.equals(booleanInterval) || historyType.equals(booleanCov) || historyType.equals(enumInterval) || historyType.equals(enumCov)){

                BComponent parent = c.getParent().asComponent();
                while(!parent.getType().toString().equals("bacnet:BacnetNetwork") && !parent.getType().toString().equals("baja:Station")){
                    //go until reaching network driver
                    if(parent.getType().toString().equals("bacnet:BacnetDevice")){
                        if(parent.getName().contains("RTU")){
                            Equipment = "RTU";
                            Device = parent.getName();
                        }else {
                            Device = parent.getName();
                            Equipment = parent.getParent().getName();
                        }
                    }else if(parent.getType().toString().equals("baja:Folder")){
                        Equipment = "Misc";
                        Device = parent.getName();
                    }else if(parent.getType().toString().equals("bacnet:BacnetDeviceFolder")){
                        if(parent.getParent().getType().toString().equals("bacnet:BacnetNetwork")){
                            Floor = parent.getName();
                        }else{
                            Equipment = parent.getName();
                        }
                    }
                    //log.println(parent.getType().toString());
                    parent = parent.getParent().asComponent();
                }



                //String navParent =  c.getParent().getParent().asComponent().getNavParent().getNavName();
                //log.println("Nav Parent: " + navParent);
                log.println("Floor Name " + Floor);
                log.println("Equipment Name: " + Equipment);
                log.println("Device Name: "+Device);
                log.println("\n\n");

            }
        }


        // recurse
        BComponent[] kids = c.getChildComponents();
        for(int i=0; i<kids.length; ++i)
            process(kids[i]);


    }



}
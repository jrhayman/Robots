// depends: baja; program; history

import javax.baja.naming.BOrd;
import javax.baja.sys.*;
import com.tridium.program.*;
import javax.baja.history.*;
import javax.baja.bql.*;
import javax.baja.collection.*;
import javax.baja.util.Queue;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class RobotImpl extends Robot {
/*
    add Device String slot for history grouping
    ONLY WORKS ON BACNET DEVICE TYPE
 */


    public void run()
            throws Exception {
        process(Sys.getStation());
        log.println("complete");
    }
    private class Tuple{
        public String type;
        public Vector<String> numbers;
        public Tuple(String t, Vector<String> n){
            this.type = t;
            this.numbers = n;
        }
    }

    public void process(BComponent c)
            throws Exception {

       /*
           edit ord variable to folder that you wish to add Device String to HistoryConfig
           edit groups for total number of folders {slot name, folder name} not counting device folder.
        */
        String ord = "station:|slot:/Drivers/NiagaraNetwork/BSB/Sixth_Floor/East/North_East";
        String[][] groups = {{"FirstTier", "name"}, {"SecondTier", "name"}, {"ThirdTier", "name"}};
        /*
         *
         *
         *
         *
         *
         */
        String Device = "";
        String bql = "bql:select * from history:HistoryConfig";
        BOrd query = BOrd.make(ord + "|" + bql);
        BITable result = (BITable) query.resolve().get();
        Cursor crs = result.cursor();
        while (crs.next()) {
            BComponent config = (BComponent) crs.get();
            BComponent parent = (BComponent) crs.get();
            while(parent != null && parent.getParent() != null && !parent.getType().toString().equals("niagaraDriver:NiagaraStation")){
                parent = (BComponent) parent.getParent();
            }
            if(parent != null & parent.getParent() != null) {
//                for (String[] g : groups) {
//                    try {
//                        config.add(g[0], BString.make(g[1]));
//                    } catch (LocalizableRuntimeException e) {
//                        config.set(g[0], BString.make(g[1]));
//                    }
//                }
                String displayName = "";
                String deviceName = parent.getName();
                deviceName = deviceName.replace("BSB_", "");
                String[] split =  deviceName.split("_");
                String deviceType = "";
                Vector<String> deviceNumbers = new Vector<>();
                Boolean inDevice = false;
                Boolean inNumber = false;
                Vector<Tuple> devices = new Vector<>();


                for(String s:split){
                    //if Device type
                    if(s.equals("SAV") || s.equals("GEV") || s.equals("PE") || s.equals("FH") || s.equals("FCU")){
                        //if already hit device
                        if(inDevice){
                            devices.add(new Tuple(deviceType, deviceNumbers));
                            deviceType = s;
                            deviceNumbers = new Vector<>();
                        }
                        //if first device
                        else{
                            inDevice = true;
                            deviceType = s;
                        }
                    }else{

                        deviceNumbers.add(s);
                    }
                }

                devices.add(new Tuple(deviceType, deviceNumbers));
                Iterator<Tuple> it = devices.iterator();
                while(it.hasNext()){
                    Tuple t = it.next();
                    deviceType = t.type;
                    deviceNumbers = t.numbers;
                    log.println(deviceType);
                    String numbers = "";
                    //devices.remove(t);
                    //after grabbing next device is devices empty
                    if(!it.hasNext()){
                        log.println("Empty");
                        //if there was only one device
                        if(displayName.equals("")){
                            displayName += deviceType + "-";
                            //   last device of devices
                        }else{
                            displayName += " & " + deviceType;
                        }
                        //there are more devices
                    }else{
                        log.println("{NotEmpty\n" + deviceType + "}\n");
                        //first device
                        if(displayName.equals("")){
                            displayName += deviceType;

                            //some device not first or last
                        }else{
                            displayName += ", " + deviceType;
                        }

                    }
                    int i = 1;
                    Iterator<String> nit = t.numbers.iterator();
                    while(nit.hasNext()){
                        String n = nit.next();
                        //after grabbing next number is there no more numbers
                        log.println(n);
                        if(!nit.hasNext()){
                            //there was only one number
                            log.println(i);
                            if(numbers.equals("")){
                                numbers +=  "-"+n;
                                //number came before it
                            }else{
                                numbers += " & " + n;
                            }
                            //there are more numbers
                        }else{
                            //first number
                            if(numbers.equals("")){
                                numbers += "-"+n;
                                //some number not first or last
                            }else{
                                numbers += ", " + n;
                            }
                        }
                    }
                    displayName += numbers;

                }

                log.println(parent.getName() + ":");
                log.println(displayName +"\n\n");
                try{
                    config.add("Device", BString.make(parent.getName()));
                }catch (LocalizableRuntimeException e){
                    config.set("Device", BString.make(parent.getName()));
                }
            }


        }
    }




}



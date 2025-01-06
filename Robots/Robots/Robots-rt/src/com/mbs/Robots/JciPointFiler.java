// depends: baja; program; bacnet;


import com.tridium.program.Robot;

import javax.baja.bacnet.point.BBacnetPointDeviceExt;
import javax.baja.bacnet.point.BBacnetPointFolder;
import javax.baja.collection.BITable;
import javax.baja.naming.BOrd;
import javax.baja.space.Mark;
import javax.baja.sys.BComponent;
import javax.baja.sys.Cursor;
import javax.baja.sys.Sys;
import java.util.ArrayList;

public class RobotImpl
        extends Robot {

    public void run()
            throws Exception {
        process(Sys.getStation());
    }

    public void process(BComponent c)
            throws Exception {
        String pointsPath = "station:|slot:/Drivers/BacnetNetwork/$351605_Hancock/NAE01/points";
        BBacnetPointDeviceExt pointsDeviceExt = (BBacnetPointDeviceExt) BOrd.make(pointsPath).resolve().get();
        BITable t = (BITable) BOrd.make(pointsPath + "| bql:select * from control:ControlPoint where parent.name = 'points'").resolve().get();
        Cursor crs =  t.cursor();
        ArrayList<BComponent> points = new ArrayList<>();
        while(crs.next()) {
            crs.get();
            BComponent point = (BComponent) crs.get();
            points.add(point);
        }
        for (BComponent p :
                points) {
            log.println("Point: " + p.getDisplayName(null));
            String[] pathSplit = p.getDisplayName(null).split("\\.");
            String pointName = pathSplit[pathSplit.length-1].replace(' ', '_').replace('-', '_');
//            String device = pathSplit[pathSplit.length-2].replace(' ', '_').replace('-', '_');
            String device = pathSplit[pathSplit.length-2].replace(' ', '_').replace('-', '_');
            BBacnetPointFolder deviceFolder = (BBacnetPointFolder) pointsDeviceExt.get(device);
            if(deviceFolder == null){
                deviceFolder = new BBacnetPointFolder();
                pointsDeviceExt.add(device, deviceFolder);
            }
            pointsDeviceExt.rename(pointsDeviceExt.getProperty(p.getName()), pointName);
            Mark mark = new Mark(p);
            mark.moveTo(deviceFolder, null);
        }


        /******************custom****************/
        //pathSplit = pathSplit[pathSplit.length-1].split(" ");
//            pathSplit[pathSplit.length-2] = pathSplit[pathSplit.length-2].replace("151610FAC", "FAC").replace("5151610FAC", "FAC");
        /****************************************/
//        for (String s: pathSplit) {
//            log.print("["+s+"]");
//        }
//        log.print("\n");


    }


}

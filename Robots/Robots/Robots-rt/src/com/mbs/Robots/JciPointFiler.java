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

public class RobotImpl
        extends Robot {

    public void run()
            throws Exception {
        process(Sys.getStation());
    }

    public void process(BComponent c)
            throws Exception {
        String pointsPath = "station:|slot:/Drivers/BacnetNetwork/$351087_Burbank/NCE_07/points";
        BBacnetPointDeviceExt points = (BBacnetPointDeviceExt) BOrd.make(pointsPath).resolve().get();
        BITable t = (BITable) BOrd.make(pointsPath + "| bql:select * from control:ControlPoint where parent.name = 'points'").resolve().get();
       Cursor crs =  t.cursor();
        while(crs.next()){
            BComponent point = (BComponent) crs.get();
            log.println("Point: " + point.getDisplayName(null));
            String[] pathSplit = point.getDisplayName(null).split("\\.");
            for (String s: pathSplit) {
                log.print("["+s+"]");
            }
            log.print("\n");

            String pointName = pathSplit[pathSplit.length-1].replace(' ', '_').replace('-', '_');
            String device = pathSplit[pathSplit.length-2].replace(' ', '_').replace('-', '_');
            BBacnetPointFolder deviceFolder = (BBacnetPointFolder) points.get(device);
            if(deviceFolder == null){
                deviceFolder = new BBacnetPointFolder();
                points.add(device, deviceFolder);
            }
            points.rename(points.getProperty(point.getName()), pointName);
            Mark mark = new Mark(point);
            mark.moveTo(deviceFolder, null);

        }


    }
}
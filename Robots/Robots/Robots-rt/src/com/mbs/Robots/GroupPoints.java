// depends: baja; program; bacnet; niagaraDriver


import com.tridium.program.Robot;

import javax.baja.bacnet.point.BBacnetPointDeviceExt;
import javax.baja.bacnet.point.BBacnetPointFolder;
import javax.baja.collection.BITable;
import javax.baja.control.BControlPoint;
import javax.baja.naming.BOrd;
import javax.baja.space.Mark;
import javax.baja.sys.BComponent;
import javax.baja.sys.Cursor;
import javax.baja.sys.Property;
import javax.baja.sys.Sys;
import javax.baja.collection.TableCursor;
import java.util.Iterator;

public class RobotImpl
        extends Robot {

    public void run()
            throws Exception {
        process(Sys.getStation());
    }

    public void process(BComponent c)
            throws Exception {
        String baseOrd = "station:|slot:/Drivers/BacnetNetwork/LivoniaStMarys/CancerCenter/points";
        String bql = "bql:select * from control:ControlPoint where parent.type = 'bacnet:BacnetPointDeviceExt'";
        BOrd query = BOrd.make(baseOrd + "|" + bql);
        BITable result = (BITable) query.resolve().get();
        TableCursor crs = result.cursor();
        Iterator it = crs.iterator();
        int i = 1;
        while(it.hasNext()){
            BControlPoint point = (BControlPoint) it.next();
            log.println("interation: " + i);
            BBacnetPointFolder pointFolder;
            String folderName = "group_"+point.getName().split("_")[point.getName().split("_").length-1];
            String newPointName = point.getName().replace("_"+point.getName().split("_")[point.getName().split("_").length-1], "");
            try{
                pointFolder = (BBacnetPointFolder) BOrd.make(baseOrd + "/" + folderName).resolve().get();
                log.println("point folder exists");
            }catch (Exception e){
                pointFolder = new BBacnetPointFolder();
                BBacnetPointDeviceExt parent = (BBacnetPointDeviceExt)BOrd.make(baseOrd).resolve().get();
                Property p = parent.add(folderName, pointFolder);
                pointFolder = (BBacnetPointFolder) parent.get(p);
                log.println(pointFolder.getSlotPath());
            }
            try{
                log.println(pointFolder.getSlotPath().toString());
                Mark mark = new Mark(point);
                mark.moveTo(pointFolder, null);
                pointFolder.rename((Property) pointFolder.getSlot(point.getName()), newPointName);
                log.println("point moved and renamed to: " + pointFolder.getSlot(newPointName).getName());
            }catch (Exception ec){
                log.println(ec.getMessage());
            }



        }
        for (BControlPoint p: ((BBacnetPointDeviceExt)BOrd.make(baseOrd).resolve().get()).) {
            log.println(p.getName());
        }
    }

}
// depends: baja; program; bacnet;

import com.tridium.program.Robot;
import jdk.nashorn.internal.runtime.ECMAException;

import javax.baja.bacnet.point.BBacnetPointDeviceExt;
import javax.baja.bacnet.point.BBacnetPointFolder;
import javax.baja.collection.BITable;
import javax.baja.control.BControlPoint;
import javax.baja.naming.BOrd;
import javax.baja.space.Mark;
import javax.baja.sys.BComponent;
import javax.baja.sys.BString;
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

        String base = "station:|slot:/Drivers/BacnetAwsNetwork/West$20Tower$20Floors$201$20and$204";
        String query = "bql:select * from control:ControlPoint where parent.name = 'points'";

        BITable t = (BITable) BOrd.make(base +"|" + query).resolve().get();
        Cursor crs = t.cursor();

        while (crs.next()){
            BControlPoint point = (BControlPoint) crs.get();
            try {
                parseAndMove(point.getDisplayName(null), point, (BBacnetPointDeviceExt) point.getParent());
            }catch (Exception e){
                log.println(e.getMessage());
            }
        }





    }
    /*
        parses name to format and either creates folder or adds point to folder.
     */
    private void parseAndMove(String name, BControlPoint controlPoint, BBacnetPointDeviceExt points) throws Exception {
        // Format: name | device

        String[]split = name.split("\\|");
        if(!(split.length ==2)) {
            log.println(name + " breaks format");
            for(String s : split) log.print(s + " + ");
            return;
        }
        String point = split[0];
        String device = split[1];
        point = point.replace(' ', '_').replace('-', '_').replace(':', '');
        device = device.replace(' ', '_').replace('-', '_');
        BBacnetPointFolder deviceFolder = (BBacnetPointFolder) points.get(device);
        if(deviceFolder == null){
            deviceFolder = new BBacnetPointFolder();
            points.add(device, deviceFolder);
        }

        points.rename(points.getProperty(controlPoint.getName()), point);
        Mark mark = new Mark(controlPoint);
        mark.moveTo(deviceFolder, null);

    }


}

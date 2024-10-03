// depends: baja; kitControl; history; program

import javax.baja.collection.BITable;
import javax.baja.control.BStringPoint;
import javax.baja.history.ext.BHistoryExt;
import javax.baja.naming.BOrd;
import javax.baja.sys.*;
import com.tridium.kitControl.*;

import com.sun.org.apache.xpath.internal.operations.Bool;
import com.tridium.kitControl.logic.BLogic;
import com.tridium.kitControl.math.BAdd;
import com.tridium.kitControl.math.BMath;
import com.tridium.kitControl.util.BMuxSwitch;
import com.tridium.kitControl.util.BNumericSwitch;
import com.tridium.program.*;

import java.util.Arrays;

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
        String ord = "station:|slot:/Drivers/NiagaraNetwork";
        String bql = "bql:select * from control:ControlPoint where type like 'control*' and !(type like '*String*') and proxyExt.type like 'niagara*'";
        BOrd query = BOrd.make(ord + "|" + bql);
        BITable result = (BITable) query.resolve().get();
        Cursor crs = result.cursor();
        while(crs.next()){
            BComponent point = (BComponent) crs.get();
            Boolean hasExt = false;
            String name = point.getName();
            if(name.equals("ValveAControlLevel")|| name.equals("ValveBControlLevel")|| name.equals("ValveCls") || name.equals("ValveOpn")
                    ||  name.equals("ControllerDown")||name.contains("Ovrd") ||name.equals("OxygenAlarm")||name.contains("Color")||name.equals("EasyTrack")||name.contains("ControlLvl")
                    || name.contains("Controller")|| name.contains("Loop") || name.contains("address") || name.contains("ScheduleExh")
                    || name.contains("Override") || name.contains("Drive")){continue;}
            else{
                for (BComponent child : point.getChildComponents()) {
                    hasExt = child instanceof BHistoryExt;
                    if (hasExt) break;
                }
            }
            if(!hasExt) log.println(point.getSlotPath().toString());
        }
    }
}





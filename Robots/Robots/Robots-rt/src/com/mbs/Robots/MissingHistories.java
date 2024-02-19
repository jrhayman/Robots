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
        String ord = "station:|slot:/Drivers/BacnetNetwork";
        String bql = "bql:select * from control:ControlPoint";
        BOrd query = BOrd.make(ord + "|" + bql);
        BITable result = (BITable) query.resolve().get();
        Cursor crs = result.cursor();
        while(crs.next()){
            BComponent point = (BComponent) crs.get();
            Boolean hasExt = false;
            if(!(point instanceof BMath || point instanceof BMuxSwitch || point instanceof BLogic || point instanceof BKitNumericPoint || point instanceof BKitBooleanPoint || point instanceof BKitEnumPoint || point instanceof BStringPoint)) {
                String name = point.getName();
                if(!(name.equals("ColorSelect")||name.equals("Controller")||name.equals("TimeDelay")||name.equals("Forced")||name.equals("DiagCond"))){
                    if(point instanceof BNumericSwitch)
                    {  log.println("why"); }
                    for (BComponent child : point.getChildComponents()) {
                        hasExt = child instanceof BHistoryExt;
                        if (hasExt) break;
                    }
                    if(!hasExt) log.println(point.getSlotPath().toString());
                }
            }



        }
    }



}

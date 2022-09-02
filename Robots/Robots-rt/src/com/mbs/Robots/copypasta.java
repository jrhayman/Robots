// depends: baja; program; niagaraDriver
import javax.baja.collection.BITable;
import javax.baja.control.BIWritablePoint;
import javax.baja.control.BNumericPoint;
import javax.baja.control.BNumericWritable;
import javax.baja.naming.BOrd;
import javax.baja.space.Mark;
import javax.baja.sys.*;
import com.tridium.program.*;

import java.util.Properties;

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
        /********************
         *************
         copys ord given into specific space given from query
         *************
         ***************************/
        String copyOrd = "station:|slot:/Drivers/NiagaraNetwork/Carollton/AHU_1/CRL_VAV_1_1/points/PhysGraphSlider";
        String pasteQuery = "station:|slot:/Drivers/NiagaraNetwork/Meadowview|bql:select * from baja:Component where name = 'HeatScrValve'";
        Mark mark = new Mark((BComponent) BOrd.make(copyOrd).resolve(Sys.getStation()).get());
        BITable t = (BITable) BOrd.make(pasteQuery).resolve().get();
        Cursor crs = t.cursor();
        while(crs.next()) {
            BComponent component = (BComponent) crs.get();

            mark.copyTo(BOrd.make(component.getSlotPath() + "/points/HeatScrValve").resolve(Sys.getStation()).get(), Context.copying);
        }

/*
        remove flags
         */
//            Slot s = component.getSlot("emergencyOverride");
//            if(s != null && !Flags.isHidden(component, s)){
//                //component.setFlags(s, ~Flags.HIDDEN);
//                component.setFlags(s, Flags.HIDDEN);
//                log.println(component.getSlotPath());
//            }
//            s = component.getSlot("emergencyAuto");
//            if(s != null && !Flags.isHidden(component, s)){
//                component.setFlags(s, Flags.HIDDEN);
//                log.println(component.getSlotPath());
//            }
//            s = component.getSlot("set");
//            if(s != null && !Flags.isHidden(component, s)){
//                component.setFlags(s, Flags.HIDDEN);
//                log.println(component.getSlotPath());
//            }
        log.println();
        //log.println(test.getSlotPath());
        //}

    }
}

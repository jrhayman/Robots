// depends: baja; program; niagaraDriver
import javax.baja.collection.BITable;
import javax.baja.control.BIWritablePoint;
import javax.baja.control.BNumericPoint;
import javax.baja.control.BNumericWritable;
import javax.baja.naming.BOrd;
import javax.baja.space.Mark;
import javax.baja.sys.*;
import com.tridium.program.*;
import com.tridium.sys.transfer.TransferStrategy;

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

        String copyOrd = "station:|slot:/Drivers/NiagaraNetwork/EdgeWood/RERVU_1/EGW_RVAV_1_1/Config/Slider_Warmer_Value_Gx";
        String pasteQuery = "station:|slot:/Drivers/NiagaraNetwork/Meadowview|bql:select * from niagaraDriver:NiagaraStation where name like '*VAV*'";
        Mark mark = new Mark((BComponent) BOrd.make(copyOrd).resolve(Sys.getStation()).get());
        BITable t = (BITable) BOrd.make(pasteQuery).resolve().get();
        Cursor crs = t.cursor();
        BComponent params = new BComponent();
        params.add(TransferStrategy.PARAM_KEEP_ALL_LINKS, BBoolean.make("true"));
        while(crs.next()) {
            BComponent component = (BComponent) crs.get();
            log.println(component.getSlotPath());
            mark.copyTo(BOrd.make(component.getSlotPath() + "/points").resolve(Sys.getStation()).get(), Context.copying);
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
//        log.println();
        //log.println(test.getSlotPath());
        //}

    }
}

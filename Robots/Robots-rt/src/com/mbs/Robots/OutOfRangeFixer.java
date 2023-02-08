// depends: baja; program; niagaraDriver; bacnet

import javax.baja.alarm.ext.BAlarmSourceExt;
import javax.baja.bacnet.point.BBacnetPointFolder;
import javax.baja.collection.BITable;
import javax.baja.control.BControlPoint;
import javax.baja.control.BStringWritable;
import javax.baja.naming.BOrd;
import javax.baja.space.Mark;
import javax.baja.sys.*;


import com.tridium.nd.BNiagaraStation;
import com.tridium.nd.point.BNiagaraPointDeviceExt;
import com.tridium.nd.point.BNiagaraPointFolder;
import com.tridium.nd.point.BNiagaraProxyExt;
import com.tridium.program.*;
import com.tridium.sys.transfer.TransferStrategy;


/**** *
 *****
    NEEDS FIX need to hold onto proxy extension of value being changed.
 *****
 ******/

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

         /* links that need to change
        station:|slot:/Drivers/BacnetNetwork/TERMINAL_DEVICES/RAD_45_46_C_7/points/C_7/points/ZN_TMP/Out_of_Range_Alarm/ACTV_HTG_STPT_REF
        station:|slot:/Drivers/BacnetNetwork/TERMINAL_DEVICES/RAD_45_46_C_7/points/C_7/points/ZN_TMP/Out_of_Range_Alarm
        station:|slot:/Drivers/BacnetNetwork/TERMINAL_DEVICES/RAD_45_46_C_7/points/C_7/points/ZN_TMP/Bad_Sensor_Alarm
         */
        String b = "station:|slot:/Drivers/BacnetNetwork/TERMINAL_DEVICES";
        String q = "bql:select * from bacnet:BacnetPointFolder where name = 'points'";
        String copyOrd = "station:|slot:/Drivers/BacnetNetwork/TERMINAL_DEVICES/RAD_45_46_C_7/points/RAD_45_46/points/ZN_TMP";

        BComponent orig = (BComponent) BOrd.make(copyOrd).resolve(Sys.getStation()).get();
        Slot orig_stpt = orig.getSlot("ACTV_HTG_STPT");
        log.println(orig.getName());
        log.println(orig.getParent().getName());
        Slot orig_alrm_src_name= ((BComponent) orig.getParent().get("EQUIP_INFO")).getSlot("Alarm_Source_Name");
        log.println(orig_alrm_src_name);
        BOrd source_name_ord = ((BComponent)((BComponent) orig.getParent().get("EQUIP_INFO")).get("Alarm_Source_Name")).getHandleOrd();

        Mark mark = new Mark((BComponent) BOrd.make(copyOrd).resolve(Sys.getStation()).get());
        BComponent params = new BComponent();
        params.add(TransferStrategy.PARAM_KEEP_ALL_LINKS, BBoolean.make("true"));
        BITable t = (BITable) BOrd.make(b + "|" + q).resolve().get();
        Cursor crs = t.cursor();
        crs.next();
        while(crs.next()){
            BBacnetPointFolder comp = (BBacnetPointFolder) crs.get();
            try{
                comp.remove("ZN_TMP");
                mark.copyTo(comp, params, Context.copying);
                BComponent source_name = (BComponent) ((BComponent) comp.get("EQUIP_INFO")).get("Alarm_Source_Name");
                BComponent htg_setpt = (BComponent) comp.get("ACTV_HTG_STPT");
                BComponent zn_tmp = (BComponent) comp.get("ZN_TMP");
                BAlarmSourceExt oor_Alarm = (BAlarmSourceExt) zn_tmp.get("Out_of_Range_Alarm");
                log.println("here");
                BLink[] links = ((BComponent)oor_Alarm.get("ACTV_HTG_STPT_REF")).getLinks();
                for (BLink l:links) {
                    l.setSourceOrd(htg_setpt.getHandleOrd());
                    log.println(l.toString());
                }
                links = oor_Alarm.getLinks();
                for(BLink l: links){
                    if(l.getSourceOrd().equals(source_name_ord)){
                        l.setSourceOrd(source_name.getHandleOrd());
                    }

                    log.println(l.toString());
                }

            }catch (Exception e){
                log.println(e.getMessage());
            }


        }

    }
}

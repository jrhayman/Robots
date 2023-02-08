// depends: baja; program; niagaraDriver

import javax.baja.collection.BITable;
import javax.baja.control.BControlPoint;
import javax.baja.control.BIWritablePoint;
import javax.baja.control.BNumericWritable;
import javax.baja.naming.BOrd;
import javax.baja.space.Mark;
import javax.baja.sys.*;


import com.tridium.nd.BNiagaraStation;
import com.tridium.nd.point.BNiagaraPointFolder;
import com.tridium.nd.point.BNiagaraProxyExt;
import com.tridium.program.*;

public class RobotImpl
        extends Robot{

    @Override
    public void run() throws Exception {
        process(Sys.getStation());
    }
    public void process(BComponent c)
            throws Exception {
        String baseOrd = "station:|slot:/Drivers/NiagaraNetwork/Meadowview";
        String query = baseOrd + "| bql:select * from niagaraDriver:NiagaraStation where name like '*VAV*'";
        BITable t = (BITable) BOrd.make(query).resolve().get();
        Cursor crs = t.cursor();

        while(crs.next()){
            BComponent point = (BComponent) crs.get();
            BComponent heatingLoop = (BComponent) ((BNiagaraStation) crs.get()).getPoints().get("HeatingLoop");
            BComponent damperPosition = (BComponent) ((BNiagaraStation) crs.get()).getPoints().get("DamperPosition");
            BComponent auxHeat = (BComponent) ((BNiagaraStation) crs.get()).getPoints().get("AO2");
            if(heatingLoop == null){
                log.println(point.getName() + " heatingLoop doesn't exist");
            }else if(damperPosition == null) {
                log.println(point.getName() + " damperPosition doesn't exist");
            }
            else if (auxHeat == null){
                log.println(point.getName() + ": auxHeat doesn't exist");
            }
            else{
                Slot heatingEO = heatingLoop.getSlot("emergencyOverride");
                Slot damperEO = damperPosition.getSlot("emergencyOverride");
                Slot auxHeatEO = auxHeat.getSlot("emergencyOverride");
                if(heatingEO == null && damperEO == null && auxHeatEO == null){
                    //good
                }
                else {
                    if(heatingEO != null){log.println(point.getName() + ": heating");}
                    else if(damperEO != null){log.println(point.getName()+ ": damper");}
                    else if(auxHeatEO != null){log.println(point.getName() + ": aux heat");}
                    else if (
                            !Flags.isHidden(heatingLoop, heatingEO) || !Flags.isHidden(damperPosition, damperEO) || !Flags.isHidden(auxHeat,auxHeatEO)
                    ) {
                        log.println(((BComponent) crs.get()).getName() + ": all");
                    }
                }

            }
        }
    }
}
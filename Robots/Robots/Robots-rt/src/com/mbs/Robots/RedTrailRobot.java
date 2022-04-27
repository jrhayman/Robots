// depends: baja; program; vykonPro

import javax.baja.collection.BITable;
import javax.baja.naming.BOrd;
import javax.baja.sys.*;
import com.tridium.program.*;

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
        String ord = "station:|slot:/Drivers/NiagaraNetwork/BSB/Sixth_Floor";
        String bql = "bql:select * from vykonPro:BooleanBql";
        BOrd query = BOrd.make(ord + "|" + bql);
        BITable result = (BITable) query.resolve().get();
        Cursor crs = result.cursor();
        while(crs.next()){
            BComponent alarmQurey = (BComponent) crs.get();
            BComponent parent = alarmQurey;
            String redTrailOrd = "";
            String stationName = "";
            while(parent!=null && !parent.getType().toString().equals("niagaraDriver:NiagaraStation")){
                parent = (BComponent) parent.getParent();
            }
            if(parent!=null){
                stationName = parent.getName();
                redTrailOrd = stationName;
                parent = (BComponent) parent.getParent();
            }
            while(parent != null && !parent.getName().equals("Sixth_Floor")){
                redTrailOrd = parent.getName() + "/" + redTrailOrd;
            }
            if(!stationName.equals("")){
                redTrailOrd = "station:|slot:/Drivers/NiagaraNetwork/BSB/Sixth_Floor/" + redTrailOrd;
                String redTrailQuery = redTrailOrd + "|bql:select * from control:BooleanPoint where name like '*ALM*'";
                log.println(stationName);
                log.println(redTrailOrd + "\n" + redTrailQuery + "\n");
                try {
                    alarmQurey.set("pointQuery", BOrd.make(redTrailQuery));
                }catch (Exception e){
                    log.println(e.toString());
                }
            }
        }
    }

}

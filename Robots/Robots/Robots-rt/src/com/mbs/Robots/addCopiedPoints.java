// depends: baja; program; bacnet; niagaraDriver

import javax.baja.bacnet.point.BBacnetPointFolder;
import javax.baja.collection.BITable;
import javax.baja.control.BControlPoint;
import javax.baja.control.BStringPoint;
import javax.baja.naming.BOrd;
import javax.baja.space.Mark;
import javax.baja.sys.*;


import com.tridium.nd.BNiagaraStation;
import com.tridium.nd.point.BNiagaraPointFolder;
import com.tridium.nd.point.BNiagaraProxyExt;
import com.tridium.program.*;
import com.tridium.sys.transfer.TransferStrategy;

import java.lang.reflect.Array;

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


        /************COPY SINGLE DAMPER TO HD/ND OUTPUTS LOGIC AND RELINK************************/
//        Mark mark = new Mark((BComponent[]) new BComponent[]{BOrd.make("        station:|slot:/Drivers/BacnetNetwork/Alex_Haley/NAE01/points/AHU1/BooleanSelect").resolve().getComponent(),
//                BOrd.make("station:|slot:/Drivers/BacnetNetwork/Alex_Haley/NAE01/points/AHU2_MixingBoxes/MB1/Rm108/ND_O").resolve().getComponent(),
//                BOrd.make("station:|slot:/Drivers/BacnetNetwork/Alex_Haley/NAE01/points/AHU2_MixingBoxes/MB1/Rm108/HD_O").resolve().getComponent()});
//
//        BITable t = (BITable) BOrd.make("station:|slot:/Drivers/BacnetNetwork/$351510_BES/NCE01/points/AHU_1_Zones | bql:select * from bacnet:BacnetPointFolder ").resolve(Sys.getStation()).get();
//        BBacnetPointFolder folder;
//        BComponent params = new BComponent();
//        params.add(TransferStrategy.PARAM_KEEP_ALL_LINKS, BBoolean.make("true"));
//        Cursor crs = t.cursor();
//        while(crs.next()) {
//            folder = (BBacnetPointFolder) crs.get();
//            Boolean exists = false;
//            for(BComponent cmp : folder.getChildComponents()){
//                if(cmp.getName().equals("HD_O"))
//                    exists = true;
//            }
//            if(!exists) {
//                log.println(folder.getName());
//                mark.copyTo(folder, params, Context.copying);
//                //change links from original damper output
//                /***Subtract Component***/
//                ((BLink) folder.get("Subtract").asComponent().getLinks()[0]).setSourceOrd((BOrd) folder.get("ZND_O").asComponent().getHandleOrd());
//                /***HD_***/
//                ((BLink) folder.get("HD_O").asComponent().getLinks()[0]).setSourceOrd((BOrd) folder.get("ZND_O").asComponent().getHandleOrd());
//            }
//        }
        /*****************************END********************************************************/

        /************COPY SINGLE DAMPER TO HD/CD OUTPUTS LOGIC AND RELINK************************/
        Mark mark = new Mark((BComponent[]) new BComponent[]{BOrd.make("station:|slot:/Drivers/BacnetNetwork/$351521_GW/RTU1/points/Zones/Zn1/CD_Damper").resolve().getComponent(),
                BOrd.make("station:|slot:/Drivers/BacnetNetwork/$351521_GW/RTU1/points/Zones/Zn1/HD_Damper").resolve().getComponent(),
                BOrd.make("station:|slot:/Drivers/BacnetNetwork/$351521_GW/RTU1/points/Zones/Zn1/Subtract").resolve().getComponent()});
        BITable t = (BITable) BOrd.make("station:|slot:/Drivers/BacnetNetwork/$351618_JohnHope/NCE01/points/RTU_1E/RTU1E_Zones | bql:select * from bacnet:BacnetPointFolder where parent.name like 'RH*'").resolve(Sys.getStation()).get();
        BBacnetPointFolder folder;
        BComponent params = new BComponent();
        params.add(TransferStrategy.PARAM_KEEP_ALL_LINKS, BBoolean.make("true"));
        Cursor crs = t.cursor();
        while(crs.next()) {
            folder = (BBacnetPointFolder) crs.get();
            Boolean exists = false;
            for(BComponent cmp : folder.getChildComponents()){
                if(cmp.getName().equals("HD_Damper"))
                    exists = true;
            }
            if(!exists) {
                log.println(folder.getName());
                mark.copyTo(folder, params, Context.copying);
                //change links from original damper output
                /***Subtract Component***/
                ((BLink) folder.get("Subtract").asComponent().getLinks()[0]).setSourceOrd((BOrd) folder.get("D").asComponent().getHandleOrd());
                /***HD_***/
                ((BLink) folder.get("CD_Damper").asComponent().getLinks()[0]).setSourceOrd((BOrd) folder.get("D").asComponent().getHandleOrd());
            }

        /*****************************END********************************************************/

        /************COPY ENUM OCC TO BOOL OCC LOGIC************************/
//        Mark mark = new Mark((BComponent[]) new BComponent[]{BOrd.make("station:|slot:/Drivers/BacnetNetwork/Alex_Haley/NAE01/points/AHU1/BooleanSelect").resolve().getComponent(),
//                BOrd.make("station:|slot:/Drivers/BacnetNetwork/Alex_Haley/NAE01/points/AHU1/Occ").resolve().getComponent()});
//
//        BITable t = (BITable) BOrd.make("station:|slot:/Drivers/BacnetNetwork/$351510_BES | bql:select * from bacnet:BacnetPointFolder").resolve(Sys.getStation()).get();
//
//        BBacnetPointFolder folder;
//        BComponent params = new BComponent();
//        params.add(TransferStrategy.PARAM_KEEP_ALL_LINKS, BBoolean.make("true"));
//        Cursor crs = t.cursor();
//        while(crs.next()) {
//
//            folder = (BBacnetPointFolder) crs.get();
//            //see if EFF_OCC exists to continue
//            try {
//                BComponent EFF_OCC = (BComponent) folder.get("EFF_OCC");
//            }catch (Exception e){
//                continue;
//            }
//
//            log.println(folder.getName());
//            mark.copyTo(folder, params, Context.copying);
//            //change links from original Occupancy point
//            /*BoolSelect*/
//            ((BLink) folder.get("BooleanSelect").asComponent().getLinks()[0]).setSourceOrd((BOrd) folder.get("EFF_OCC").asComponent().getHandleOrd());
        }
        /*****************************END********************************************************/

    }
}

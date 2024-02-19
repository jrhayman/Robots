package com.mbs.Robots;// depends: baja; program; niagaraDriver

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

//        Mark mark = new Mark((BComponent) BOrd.make("station:|slot:/Drivers/NiagaraNetwork/Shepard_Hills/ERVU_1/SHH_VAV_1_1/SliderEnable").resolve(Sys.getStation()).get());
//        BITable t = (BITable) BOrd.make("station:|slot:/Drivers/NiagaraNetwork/Shepard_Hills/ERVU_1 | bql:select * from niagaraDriver:NiagaraStation where name like '*VAV*'").resolve().get();
//        Cursor crs = t.cursor();
//        crs.next();
//        while(crs.next()){
//            mark.copyTo((BComponent)crs.get(), Context.copying);
//            BComponent source = (BComponent) BOrd.make("station:|slot:/Drivers/NiagaraNetwork/Shepard_Hills/ERVU_1/"+((BComponent)crs.get()).getName()+"/SliderEnable/SliderEnable").resolve(Sys.getStation()).get();
//            BComponent target = (BComponent) BOrd.make("station:|slot:/Drivers/NiagaraNetwork/Shepard_Hills/ERVU_1/"+((BComponent)crs.get()).getName()+"/Global/DisableSliders").resolve(Sys.getStation()).get();
//            target.add("SliderLogicLink", new BLink(source.getHandleOrd(), "out", "in10", true));
//        }
//        BComponent SliderEnableLogicPoint = (BComponent) BOrd.make("station:|slot:/Drivers/NiagaraNetwork/Shepard_Hills/ERVU_1/SHH_VAV_1_1/SliderEnable/SliderEnable").resolve(Sys.getStation()).get();
//        BComponent DeviceSliderEnable = (BComponent) BOrd.make("station:|slot:/Drivers/NiagaraNetwork/Shepard_Hills/ERVU_1/SHH_VAV_1_1/Global/DisableSliders").resolve(Sys.getStation()).get();
//        BLink newLink = new BLink(SliderEnableLogicPoint.getHandleOrd(), "out", "in10", true);
//        DeviceSliderEnable.add("SliderLogicLink", newLink);
//        Mark mark = new Mark((BComponent) BOrd.make("station:|slot:/Drivers/NiagaraNetwork/Carollton/AHU_1/CRL_VAV_1_1/Setpoints/SliderSp").resolve(Sys.getStation()).get());
//        BITable t = (BITable) BOrd.make("station:|slot:/Drivers/NiagaraNetwork/High_School | bql:select * from niagaraDriver:NiagaraStation where name like '*VAV*'").resolve().get();
//        Cursor crs = t.cursor();
////        crs.next();
//        while(crs.next()){
//            BComponent component = (BComponent) crs.get();
//            mark.copyTo(BOrd.make(component.getSlotPath()+"/Setpoints").resolve(Sys.getStation()).get(), Context.copying);
//            log.println(component.getName());
//        }
        String b = "station:|slot:/Drivers/NiagaraNetwork/MahoneBldg1/points/AHUs/AC010/VAVs";
        String q = "bql:select * from niagaraDriver:NiagaraPointFolder where name like 'VAV*'";
        BITable t = (BITable) BOrd.make(b+"|"+q).resolve().get();
        //Mark m = new Mark((BComponent) BOrd.make("station:|slot:/Drivers/NiagaraNetwork/PleasantPrairieBldg/points/AHUs/AHU$2d3/VAVs/FPB_01/location").resolve(Sys.getStation()).get());
        String point = "VAV112";
        String location = "station:|slot:/Drivers/NiagaraNetwork/MahoneBldg1/points/AHUs/AC010/VAVs/VAV112/location";
        String desc = "station:|slot:/Drivers/NiagaraNetwork/MahoneBldg1/points/AHUs/AC010/VAVs/VAV112/description";

        Mark m = new Mark(new BObject[]{BOrd.make(location).resolve(Sys.getStation()).get(), BOrd.make(desc).resolve(Sys.getStation()).get()});
        Cursor crs = t.cursor();
        crs.next();
        while(crs.next()){
            BComponent p = (BComponent) crs.get();
            log.println(((BComponent)crs.get()).getName() + ":");
            try{
                m.copyTo(p, Context.copying);
                BNiagaraProxyExt l = (BNiagaraProxyExt) ((BStringPoint)BOrd.make(location).resolve(Sys.getStation()).get()).getProxyExt();
                BNiagaraProxyExt d = (BNiagaraProxyExt) ((BStringPoint)BOrd.make(desc).resolve(Sys.getStation()).get()).getProxyExt();
                ((BNiagaraProxyExt)((BStringPoint)p.get("location")).getProxyExt()).setPointId(l.getPointId().replace(point, p.getName()));
                ((BNiagaraProxyExt)((BStringPoint)p.get("description")).getProxyExt()).setPointId(d.getPointId().replace(point, p.getName()));
                log.println(((BStringPoint) p.get("location")).getOut().getValue());
                log.println(((BStringPoint) p.get("description")).getOut().getValue());
            }catch ( Exception e){
                log.println(e.getMessage());

            }

//            BComponent  point = (BComponent)crs.get();
//            log.println(point.getSlotPath());
//            if(point.get("NumericInterval") == null){
//                m.copyTo(point, Context.copying);
//            }
        }

    }
}

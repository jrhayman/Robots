// depends: baja; control; history; bacnet; program

import javax.baja.bacnet.point.BBacnetProxyExt;
import javax.baja.collection.BITable;
import javax.baja.control.*;
import javax.baja.history.ext.BHistoryExt;
import javax.baja.naming.BOrd;
import javax.baja.sys.*;

import com.sun.org.apache.bcel.internal.generic.SWITCH;
import com.tridium.program.*;

import java.util.Arrays;


/*
    this robot finds all writeable points with bacnet proxy extentions within the query space,
    creates a readonly point with a copy of the proxy and history extensions.
    deletes the writable point and adds the readonly to the parent.
 */
public class RobotImpl
        extends Robot {

    public void run()
            throws Exception {
        process(Sys.getStation());
    }

    public void process(BComponent c)
            throws Exception {

        String ord = "station:|slot:/Drivers/BacnetNetwork/$351459/Jace_2/points/AHU_2";
        String bql = "bql:select * from bacnet:BacnetProxyExt where parent.type = 'control:NumericWritable' or parent.type = 'control:BooleanWritable' or parent.type = 'control:EnumWritable'";
        BOrd query = BOrd.make(ord + "|" + bql);
        BITable result = (BITable) query.resolve().get();

        Cursor crs = result.cursor();
        while(crs.next()){
            BControlPoint newPoint;
            BControlPoint point = (BControlPoint) ((BComponent) crs.get()).getParent();
            BBacnetProxyExt proxy = (BBacnetProxyExt) crs.get();
            BHistoryExt history = null;
            String name = point.getName();
            BComponent parent = (BComponent) point.getParent();
            log.println(point.getSlotPath().toString());

            //decide point type
            if(point instanceof BBooleanPoint){
                newPoint = new BBooleanPoint();

            } else if (point instanceof BEnumPoint) {
                newPoint = new BEnumPoint();

            } else{
                newPoint = new BNumericPoint();
            }

            //find history ext
            for (BComponent comp: point.getChildComponents()) {
                if(comp instanceof BHistoryExt){
                    history = (BHistoryExt) newPoint.get(newPoint.add(comp.getName(), comp.newCopy()));
                }
            }
            BBacnetProxyExt newExt = ((BBacnetProxyExt) proxy.newCopy());
            newExt.set(BBacnetProxyExt.writeStatus, BString.make("Read Only"));
            newPoint.setProxyExt(newExt);
            Property propPointer = parent.add("temp", newPoint);

            //Check for Links
            for(Knob knob:point.getKnobs()){
                log.println("in knob loop");
                for(BLink link : knob.getTargetComponent().getLinks()){
                    log.println("in link loop");
                    if(link.getSourceOrd().equals(point.getHandleOrd())){
                        link.setSourceOrd(newPoint.getHandleOrd());
                        log.println("link moved");
                    }
                }
            }
            log.println(point.getFacets().toString());
            newPoint.setFacets((BFacets) point.getFacets().newCopy());
            log.println(newPoint.getFacets().toString());
            parent.remove(point.getName());
            parent.rename(propPointer, name);
            ((BBacnetProxyExt)newPoint.getProxyExt()).setEnabled(true);
            if(history != null)
                history.setEnabled(true);
            else
                log.println("missing history");
            log.println("------------------");


        }
    }
}
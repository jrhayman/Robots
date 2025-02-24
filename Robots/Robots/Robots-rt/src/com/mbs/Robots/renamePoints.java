// depends: baja; program; bacnet; niagaraDriver

import javax.baja.bacnet.BBacnetDevice;
import javax.baja.bacnet.enums.BBacnetPropertyIdentifier;
import javax.baja.bacnet.point.BBacnetPointDeviceExt;
import javax.baja.bacnet.point.BBacnetProxyExt;
import javax.baja.bacnet.point.BBacnetStringProxyExt;
import javax.baja.collection.BITable;
import javax.baja.control.BControlPoint;
import javax.baja.control.BStringPoint;
import javax.baja.control.ext.BAbstractProxyExt;
import javax.baja.naming.BOrd;
import javax.baja.sys.*;


import com.tridium.program.*;

import java.util.ArrayList;
import java.util.Arrays;


//rename devices from description property
public class RobotImpl
        extends Robot
{
    Thread thread;
    public void run()
            throws Exception
    {
//        Thread thread = new Thread(this, getComponent().getName());
//        thread.start();
        process(Sys.getStation());
    }

    public void process(BComponent c)
            throws Exception {


        /*************************************
         * This robot is meant to rename BACnet points from their descriptions.
         * After point discovery niagara doesn't hold on to point descriptions,
         * this is solved by adding a string point with the same objectID and a propertyID set to the points desciption.
         * the robot creates a BACnet string point inside each bacnet point
         * and sets the Bacnet point to the out value of the string point
         * this will increase the bacnet point count for the license until the station is restarted
         *************************************/

        String base = "ORD to query from";
        BITable t = (BITable) BOrd.make(base + "| bql:select * from bacnet:BacnetProxyExt where parent.type != 'control:StringPoint'").resolve().get();

        Cursor crs =  t.cursor();
        ArrayList<BBacnetProxyExt> proxys = new ArrayList<>();
        while(crs.next()) {
            crs.get();
            BBacnetProxyExt proxy = (BBacnetProxyExt) crs.get();
            proxys.add(proxy);
        }
/****************
 * run the section below first to add string points
 *
 */
        for(BBacnetProxyExt p : proxys){
            BStringPoint desc = new BStringPoint();
            BBacnetProxyExt descProxy = new BBacnetStringProxyExt();
            descProxy.setObjectId(p.getObjectId());
            descProxy.setPropertyId(BDynamicEnum.make(BBacnetPropertyIdentifier.description));
            desc.setProxyExt(descProxy);
            ((BBacnetProxyExt)desc.getProxyExt()).setEnabled(true);
            log.println(desc.getOut().getValue());
            try{
                ((BComponent)p.getParent()).add("desc",desc);
                log.println(((BStringPoint)((BComponent)p.getParent()).get("desc")).getOut().getValue());
            }
            catch (Exception e){
                log.println(e.getMessage());
            }
        }
        /************
         * run the section below after string points added and polled
         */
        for(BBacnetProxyExt p : proxys){
            BControlPoint point = p.getParentPoint();
            BBacnetPointDeviceExt points = (BBacnetPointDeviceExt) point.getParent();
            String newName = ((BStringPoint) point.get("desc")).getOut().getValue();
            try {
                if (newName.contains("(")) {
                    newName = newName.split("\\(")[0].substring(0, newName.split("\\(")[0].length() - 2);
                }
                newName = newName.replace(' ', '_').replace('-', '_').replace('\\', '_').replace('/', '_').replace("%", "pct").replace(".", "");
                points.rename(points.getProperty(point.getName()),newName);
            }catch (Exception e){
                log.println(newName);
                log.println(e.getMessage());
            }

        }
        /************************************************************/
    }
}
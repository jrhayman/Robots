// depends: baja; program; control; bacnet


import javax.baja.bacnet.datatypes.BBacnetObjectIdentifier;
import javax.baja.collection.BITable;
import javax.baja.control.BControlPoint;
import javax.baja.naming.BOrd;
import javax.baja.sys.*;

import com.tridium.bacnet.stack.server.BBacnetExportFolder;
import com.tridium.program.*;
import javax.baja.bacnet.export.*;
import java.util.Vector;

public class RobotImpl
        extends Robot
{
    /*
     *   This Robot will generate export table points from a given ord, and add them to a given Export Table Folder's Ord
     *   This generator will not create any export points for outputs(a point ending in "_O" based on naming convention)
     *   This generator will not create any export points for points in the Setpoints folder unless its an OCC_CMD point.
     *   This generator does not create writable point descriptors.
     *
     *
     */

    public void run()
            throws Exception
    {
        process(Sys.getStation());
    }

    public void process(BComponent c)
            throws Exception
    {
        String baseOrd = "set ord here to get points from";
        String tableFolderOrd = "set ord of Export Table Folder here";

        //set starting instance number here
        int instNumSet = 6000;
        String deviceType = "niagaraDriver:NiagaraStation";

        String bql = "bql:select * from control:ControlPoint where type like '*Numeric*' or type like '*Boolean*'";
        BOrd query = BOrd.make(baseOrd + "|" + bql);
        BITable result = (BITable) query.resolve().get();
        Cursor crs = result.cursor();




        BBacnetExportFolder exportFolder = (BBacnetExportFolder) BOrd.make(tableFolderOrd).resolve().get();
        log.println(exportFolder.getName());
        int bvNum = instNumSet;
        int biNum = instNumSet;
        int avNum = instNumSet;
        int aiNum = instNumSet;

        while(crs.next()){
            BComponent point = (BComponent) crs.get();
            String objectName = "";

            if((!point.getName().endsWith("_O")) && !point.getSlotPath().toString().contains("Red_Trail") && (point.getName().contains("OCC_CMD") ||!point.getSlotPath().toString().contains("Setpoints"))){
                log.println(point.getSlotPath().toString());
                objectName = point.getSlotPath().toString().replace("slot:/", "").replace("/", ".");
                log.println(objectName );
                BControlPoint controlPoint = (BControlPoint) point;
                log.println(controlPoint.getType().toString());
                BComponent parentDevice = (BComponent) point.getParent();
                while(parentDevice!=null && !parentDevice.getType().toString().equals(deviceType)){
                    parentDevice = (BComponent) parentDevice.getParent();
                }

                BBacnetPointDescriptor indesc;
                Property[] properties = new Property[]{BBacnetAnalogInputDescriptor.pointOrd, BBacnetAnalogInputDescriptor.objectId, BBacnetAnalogInputDescriptor.objectName};
                BValue[] values = {};
                //if the points parent is the folder NrioNetwork or EdgeNetwork its an Input type
                if(point.getParent().getName().equals("NrioNetwork") || point.getParent().getName().equals("EdgeNetwork")) {

                        //find out if its analog or binary
                        if (controlPoint.getType().toString().equals("control:NumericPoint")) {
                            values = new BValue[]{point.getHandleOrd(), BBacnetObjectIdentifier.make(0,aiNum), BString.make(objectName)};
                            indesc = new BBacnetAnalogInputDescriptor();
                            indesc.set(properties,values,null);
                            aiNum++;
                        } else {
                            values = new BValue[]{point.getHandleOrd(), BBacnetObjectIdentifier.make(3,biNum), BString.make(objectName)};
                            indesc = new BBacnetBinaryInputDescriptor();
                            indesc.set(properties,values,null);
                            biNum++;
                        }

                //point is a Value type
                }else{

                        if (controlPoint.getType().toString().equals("control:NumericPoint")) {
                            values = new BValue[]{point.getHandleOrd(), BBacnetObjectIdentifier.make(2,avNum), BString.make(objectName)};
                            indesc = new BBacnetAnalogValueDescriptor();
                            indesc.set(properties,values,null);
                            avNum++;
                        } else {
                            values = new BValue[]{point.getHandleOrd(), BBacnetObjectIdentifier.make(5,bvNum), BString.make(objectName)};
                            indesc = new BBacnetBinaryValueDescriptor();
                            indesc.set(properties,values,null);
                            bvNum++;
                        }



                    }


                try {
                    exportFolder.add(indesc.getObjectId().toString().replace(":", "_"), indesc);
                    log.println("add\n\n");
                }catch (Exception e){
                    exportFolder.set(indesc.getObjectId().toString().replace(":", "_"), indesc);
                    log.println("set\n\n");
                }
            }
        }

    }
//  public static final BBacnetObjectIdentifier DEFAULT_ANALOG_INPUT = new BBacnetObjectIdentifier(0, -1);
//  public static final BBacnetObjectIdentifier DEFAULT_ANALOG_OUTPUT = new BBacnetObjectIdentifier(1, -1);
//  public static final BBacnetObjectIdentifier DEFAULT_ANALOG_VALUE = new BBacnetObjectIdentifier(2, -1);
//  public static final BBacnetObjectIdentifier DEFAULT_BINARY_INPUT = new BBacnetObjectIdentifier(3, -1);
//  public static final BBacnetObjectIdentifier DEFAULT_BINARY_OUTPUT = new BBacnetObjectIdentifier(4, -1);
//  public static final BBacnetObjectIdentifier DEFAULT_BINARY_VALUE = new BBacnetObjectIdentifier(5, -1);
}
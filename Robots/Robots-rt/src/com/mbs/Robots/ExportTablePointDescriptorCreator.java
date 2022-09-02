// depends: baja; program; control; bacnet


import javax.baja.bacnet.datatypes.BBacnetObjectIdentifier;
import javax.baja.collection.BITable;
import javax.baja.control.BControlPoint;
import javax.baja.naming.BOrd;
import javax.baja.sys.*;

import com.tridium.bacnet.stack.server.BBacnetExportFolder;
import com.tridium.program.*;
import javax.baja.bacnet.export.*;


public class RobotImpl
        extends Robot
{
    /*
     *   This Robot will generate export table points from a given ord, and add them to a given Export Table Folder's Ord
     *   This generator will not create any export points for outputs(a point ending in "_O" based on naming convention)
     *   This generator will not create any export points for points in the Setpoints folder unless its an OCC_CMD point.
     *   This generator does not create writable point descriptors.
     *   This generator will create unique ObjectIDs for each different type of point (analog input, analog value, binary input, binary value)
     *   the ObjectIDs will be incremented by 1.
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

        //set starting instance number here:there cannot be duplicates in export table.
        int instNumSet = 6000;
        //DeviceType
        String deviceType = "niagaraDriver:NiagaraStation";


        //generating table of control points from baseOrd
        String bql = "bql:select * from control:ControlPoint where type like '*Numeric*' or type like '*Boolean*'";
        BOrd query = BOrd.make(baseOrd + "|" + bql);
        BITable result = (BITable) query.resolve().get();
        Cursor crs = result.cursor();



        //generating ExportFolder from tableFolderOrd
        BBacnetExportFolder exportFolder = (BBacnetExportFolder) BOrd.make(tableFolderOrd).resolve().get();
        log.println(exportFolder.getName());
        //creating instance numbers for binary input, analog input, binary value, analog value.
        int bvNum = instNumSet;
        int biNum = instNumSet;
        int avNum = instNumSet;
        int aiNum = instNumSet;


        //loop through table of control points
        while(crs.next()){
            //getting pointer object and casting it to a BComponent
            BComponent point = (BComponent) crs.get();
            String objectName = "";

            //expression that makes sure point is allowed to be added to export folder.
            // does point not end with "_O" AND point not from Red Trail folder AND (point name  equals "OCC_CMD" OR point not from Setpoints Folder.)
            if((!point.getName().endsWith("_O")) && !point.getSlotPath().toString().contains("Red_Trail") && (point.getName().contains("OCC_CMD") ||!point.getSlotPath().toString().contains("Setpoints"))){
                //get ObjectName value for export point: pretty much slot path but with '.' instead of '/'
                objectName = point.getSlotPath().toString().replace("slot:/", "").replace("/", ".");
                //cast point as a BControlPoint
                BControlPoint controlPoint = (BControlPoint) point;

                //this is the object type of points in export folder.
                BBacnetPointDescriptor indesc;
                //creating array of properties that are associated with Object Type naturally. These properties will be set to values when point type is known.
                Property[] properties = new Property[]{BBacnetPointDescriptor.pointOrd, BBacnetAnalogInputDescriptor.objectId, BBacnetAnalogInputDescriptor.objectName};
                BValue[] values = {};
                //if the points parent is the folder NrioNetwork or EdgeNetwork its an Input type based on standardized point organization
                if(point.getParent().getName().equals("NrioNetwork") || point.getParent().getName().equals("EdgeNetwork")) {

                        //find out if its analog or binary
                        if (controlPoint.getType().toString().equals("control:NumericPoint")) {
                            //creating the array of values associated with array of properties, ObjectIdentifier: Knowing what ObjectType value to use can be found at bottom of program.
                            values = new BValue[]{point.getHandleOrd(), BBacnetObjectIdentifier.make(0,aiNum), BString.make(objectName)};
                            //subclass of BBacnetPointDescriptor
                            indesc = new BBacnetAnalogInputDescriptor();
                            //setting PointDescriptorProperties.
                            indesc.set(properties,values,null);
                            aiNum++;
                        } else {
                            //creating the array of values associated with array of properties
                            values = new BValue[]{point.getHandleOrd(), BBacnetObjectIdentifier.make(3,biNum), BString.make(objectName)};
                            //subclass of BBacnetPointDescriptor
                            indesc = new BBacnetBinaryInputDescriptor();
                            //setting PointDescriptorProperties.
                            indesc.set(properties,values,null);
                            biNum++;
                        }

                //point is a Value type
                }else{

                        if (controlPoint.getType().toString().equals("control:NumericPoint")) {
                            //creating the array of values associated with array of properties
                            values = new BValue[]{point.getHandleOrd(), BBacnetObjectIdentifier.make(2,avNum), BString.make(objectName)};
                            //subclass of BBacnetPointDescriptor
                            indesc = new BBacnetAnalogValueDescriptor();
                            //setting PointDescriptorProperties.
                            indesc.set(properties,values,null);
                            avNum++;
                        } else {
                            //creating the array of values associated with array of properties
                            values = new BValue[]{point.getHandleOrd(), BBacnetObjectIdentifier.make(5,bvNum), BString.make(objectName)};
                            //subclass of BBacnetPointDescriptor
                            indesc = new BBacnetBinaryValueDescriptor();
                            //setting PointDescriptorProperties.
                            indesc.set(properties,values,null);
                            bvNum++;
                        }



                    }

                //add newly created PointDescriptor to Export folder with the property name of ObjectID but replacing the ':' with '_' (ex. AnalogInput:0000 ->  AnaologInput_0000)
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
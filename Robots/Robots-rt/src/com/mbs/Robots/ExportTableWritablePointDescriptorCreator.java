// depends: baja; program; control; bacnet


import com.tridium.bacnet.stack.server.BBacnetExportFolder;
import com.tridium.program.Robot;
import javax.baja.bacnet.datatypes.BBacnetObjectIdentifier;
import javax.baja.bacnet.export.BBacnetAnalogValuePrioritizedDescriptor;
import javax.baja.collection.BITable;
import javax.baja.control.BNumericWritable;
import javax.baja.naming.BOrd;
import javax.baja.status.BStatusNumeric;
import javax.baja.sys.*;

public class RobotImpl
        extends Robot
{
    /*
     *   This Robot will generate export table writable points from a given ord, and add them to a given Export Table Folder's Ord
     *   links all points in priority array
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

        String bql = "bql:select * from control:ControlPoint";
        BOrd query = BOrd.make(baseOrd + "|" + bql);
        BITable result = (BITable) query.resolve().get();
        Cursor crs = result.cursor();



        BBacnetExportFolder exportFolder = (BBacnetExportFolder) BOrd.make(tableFolderOrd).resolve().get();
        log.println(exportFolder.getName());
        int avNum = instNumSet;

        Property[] properties = new Property[]{BBacnetAnalogValuePrioritizedDescriptor.pointOrd, BBacnetAnalogValuePrioritizedDescriptor.objectId, BBacnetAnalogValuePrioritizedDescriptor.objectName};

        while(crs.next()){
            BComponent point = (BComponent) crs.get();
            BBacnetAnalogValuePrioritizedDescriptor descriptor = new BBacnetAnalogValuePrioritizedDescriptor();
            String objectName = point.getSlotPath().toString().replace("slot:/", "").replace("/", ".");
            BValue[] values = new BValue[]{point.getHandleOrd(), BBacnetObjectIdentifier.make(2,avNum), BString.make(objectName)};
            descriptor.set(properties,values,null);
            String name = descriptor.getObjectId().toString().replace(":", "_");
            avNum++;
            try {
                exportFolder.add(name, descriptor);
                log.println("add\n\n");
            }catch (Exception e){
                exportFolder.set(name, descriptor);
                log.println("set\n\n");
            }
            BOrd source = ((BComponent) exportFolder.get(name)).getHandleOrd();
            log.println(source);
            descriptor.add("bacnetValueIn1", new BStatusNumeric());
            point.add("bacnetin1", new BLink(source, "bacnetValueIn1", BNumericWritable.in1.getName(), true));

            descriptor.add("bacnetValueIn2", new BStatusNumeric());
            point.add("bacnetin2", new BLink(source, "bacnetValueIn2", BNumericWritable.in2.getName(), true));

            descriptor.add("bacnetValueIn3", new BStatusNumeric());
            point.add("bacnetin3", new BLink(source, "bacnetValueIn3", BNumericWritable.in3.getName(), true));

            descriptor.add("bacnetValueIn4", new BStatusNumeric());
            point.add("bacnetin4", new BLink(source, "bacnetValueIn4", BNumericWritable.in4.getName(), true));

            descriptor.add("bacnetValueIn5", new BStatusNumeric());
            point.add("bacnetin5", new BLink(source, "bacnetValueIn5", BNumericWritable.in5.getName(), true));

            descriptor.add("bacnetValueIn6", new BStatusNumeric());
            point.add("bacnetin6", new BLink(source, "bacnetValueIn6", BNumericWritable.in6.getName(), true));

            descriptor.add("bacnetValueIn7", new BStatusNumeric());
            point.add("bacnetin7", new BLink(source, "bacnetValueIn7", BNumericWritable.in7.getName(), true));

            descriptor.add("bacnetValueIn8", new BStatusNumeric());
            point.add("bacnetin8", new BLink(source, "bacnetValueIn8", BNumericWritable.in8.getName(), true));

            descriptor.add("bacnetValueIn9", new BStatusNumeric());
            point.add("bacnetin9", new BLink(source, "bacnetValueIn9", BNumericWritable.in9.getName(), true));

            descriptor.add("bacnetValueIn10", new BStatusNumeric());
            point.add("bacnetin10", new BLink(source, "bacnetValueIn10", BNumericWritable.in10.getName(), true));

            descriptor.add("bacnetValueIn11", new BStatusNumeric());
            point.add("bacnetin11", new BLink(source, "bacnetValueIn11", BNumericWritable.in11.getName(), true));

            descriptor.add("bacnetValueIn12", new BStatusNumeric());
            point.add("bacnetin12", new BLink(source, "bacnetValueIn12", BNumericWritable.in12.getName(), true));

            descriptor.add("bacnetValueIn13", new BStatusNumeric());
            point.add("bacnetin13", new BLink(source, "bacnetValueIn13", BNumericWritable.in13.getName(), true));

            descriptor.add("bacnetValueIn14", new BStatusNumeric());
            point.add("bacnetin14", new BLink(source, "bacnetValueIn14", BNumericWritable.in14.getName(), true));

            descriptor.add("bacnetValueIn15", new BStatusNumeric());
            point.add("bacnetin15", new BLink(source, "bacnetValueIn15", BNumericWritable.in15.getName(), true));

            descriptor.add("bacnetValueIn16", new BStatusNumeric());
            point.add("bacnetin16", new BLink(source, "bacnetValueIn16", BNumericWritable.in16.getName(), true));

        }
    }

}
//  public static final BBacnetObjectIdentifier DEFAULT_ANALOG_INPUT = new BBacnetObjectIdentifier(0, -1);
//  public static final BBacnetObjectIdentifier DEFAULT_ANALOG_OUTPUT = new BBacnetObjectIdentifier(1, -1);
//  public static final BBacnetObjectIdentifier DEFAULT_ANALOG_VALUE = new BBacnetObjectIdentifier(2, -1);
//  public static final BBacnetObjectIdentifier DEFAULT_BINARY_INPUT = new BBacnetObjectIdentifier(3, -1);
//  public static final BBacnetObjectIdentifier DEFAULT_BINARY_OUTPUT = new BBacnetObjectIdentifier(4, -1);
//  public static final BBacnetObjectIdentifier DEFAULT_BINARY_VALUE = new BBacnetObjectIdentifier(5, -1);

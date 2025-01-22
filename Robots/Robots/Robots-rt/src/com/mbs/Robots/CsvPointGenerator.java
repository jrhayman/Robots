// depends: baja; program; bacnet; niagaraDriver

import com.tridium.program.Robot;

import javax.baja.bacnet.config.BBacnetAnalogOutput;
import javax.baja.bacnet.datatypes.BBacnetObjectIdentifier;
import javax.baja.bacnet.enums.BBacnetObjectType;
import javax.baja.bacnet.export.BBacnetPointDescriptor;
import javax.baja.bacnet.point.*;
import javax.baja.control.BBooleanPoint;
import javax.baja.control.BControlPoint;
import javax.baja.control.BEnumPoint;
import javax.baja.control.BNumericPoint;
import javax.baja.driver.point.BProxyExt;
import javax.baja.file.BIFile;
import javax.baja.naming.BOrd;
import javax.baja.nre.util.FileUtil;
import javax.baja.status.BStatus;
import javax.baja.status.BStatusValue;
import javax.baja.sys.BComponent;
import javax.baja.sys.BEnum;
import javax.baja.sys.Context;
import javax.baja.sys.Sys;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

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

        String pointsOrd = "";
        String csvFileOrd = "";


        BBacnetPointDeviceExt points = (BBacnetPointDeviceExt) BOrd.make(pointsOrd).resolve().get();
        BIFile csvFile = (BIFile) BOrd.make(csvFileOrd).resolve().get();
        InputStreamReader inputStreamReader = new InputStreamReader(csvFile.getInputStream());
        String[] lines = FileUtil.readLines(inputStreamReader);

        String header = lines[0];
        int objectId = -1;
        int updatedObjectName = -1;
        int parentFolder = -1;
        String[] columns = header.split(",");

        for(int i = 0; i<columns.length; i++){
            if(columns[i].equals("Object ID")) objectId = i;
            if (columns[i].equals("UpdatedObjectName")) updatedObjectName = i;
            if(columns[i].equals("parentFolder")) parentFolder = i;
        }
        if(objectId==-1||updatedObjectName==-1||parentFolder==-1){
            log.println("missing required column");
        }else{
            for (int i = 1; i < lines.length; i++) {
                log.println("objectId: " + objectId);
                log.println("updatedObjectName: " + updatedObjectName);
                log.println("parentFolder: " + parentFolder);
                String[] line = lines[i].split(",");
                log.println("line length:" + line.length);
                log.println(line[objectId].split(":")[1]);
                BControlPoint point;
                if(line[objectId].contains("analog")) {
//                    point = new BNumericPoint();
                    BBacnetNumericProxyExt proxyExt = new BBacnetNumericProxyExt();
                    if (line[objectId].contains("Input")) {
                        point = new BNumericPoint();
                        proxyExt.setObjectId(BBacnetObjectIdentifier.make(BBacnetObjectType.ANALOG_INPUT, Integer.parseInt(line[objectId].split(":")[1])));
                        point.setProxyExt(proxyExt);
                    } else if (line[objectId].contains("Output")) {
                        point = new BNumericPoint();
                        proxyExt.setObjectId(BBacnetObjectIdentifier.make(BBacnetObjectType.ANALOG_OUTPUT, Integer.parseInt(line[objectId].split(":")[1])));
                        point.setProxyExt(proxyExt);
                    } else {
                        point = new BNumericPoint();
                        proxyExt.setObjectId(BBacnetObjectIdentifier.make(BBacnetObjectType.ANALOG_OUTPUT, Integer.parseInt(line[objectId].split(":")[1])));
                        point.setProxyExt(proxyExt);
                    }
                } else if (line[objectId].contains("binary")) {
                    point = new BBooleanPoint();
                    BBacnetBooleanProxyExt proxyExt = new BBacnetBooleanProxyExt();
                    if (line[objectId].contains("Input")) {
                        proxyExt.setObjectId(BBacnetObjectIdentifier.make(BBacnetObjectType.BINARY_INPUT, Integer.parseInt(line[objectId].split(":")[1])));
                    } else if (line[objectId].contains("Output")) {
                        proxyExt.setObjectId(BBacnetObjectIdentifier.make(BBacnetObjectType.BINARY_OUTPUT, Integer.parseInt(line[objectId].split(":")[1])));
                    }else{
                        proxyExt.setObjectId(BBacnetObjectIdentifier.make(BBacnetObjectType.BINARY_VALUE, Integer.parseInt(line[objectId].split(":")[1])));
                    }
                }else if (line[objectId].contains("multiState")){
                    point = new BEnumPoint();
                    BBacnetEnumProxyExt proxyExt = new BBacnetEnumProxyExt();
                    if(line[objectId].contains("Input")){
                        proxyExt.setObjectId(BBacnetObjectIdentifier.make(BBacnetObjectType.MULTI_STATE_INPUT, Integer.parseInt(line[objectId].split(":")[1])));
                    } else if (line[objectId].contains("Output")) {
                        proxyExt.setObjectId(BBacnetObjectIdentifier.make(BBacnetObjectType.MULTI_STATE_OUTPUT, Integer.parseInt(line[objectId].split(":")[1])));
                    } else{
                        proxyExt.setObjectId(BBacnetObjectIdentifier.make(BBacnetObjectType.MULTI_STATE_VALUE, Integer.parseInt(line[objectId].split(":")[1])));
                    }
                }else{
                    log.println(line[updatedObjectName] + "-" + line[objectId] + ": unacceptable type");
                    break;
                }
                try{
                    points.add(line[parentFolder],new BBacnetPointFolder());
                }catch (Exception e){
                    ((BBacnetPointFolder)points.get(line[parentFolder])).add(line[updatedObjectName], point);
                }
            }
        }
    }
}


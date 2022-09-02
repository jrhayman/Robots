// depends: baja; program; control; bacnet; onyxxDriver; nrio; driver; alarm; kitControl


import com.lynxspring.onyxxDriver.conv.BTabularThermistorConversion;
import com.lynxspring.onyxxDriver.devices.BOnyxx414Device;
import com.lynxspring.onyxxDriver.devices.BOnyxx514Device;
import com.lynxspring.onyxxDriver.devices.BOnyxx534Device;
import com.lynxspring.onyxxDriver.devices.BOnyxxDevice;
import com.lynxspring.onyxxDriver.devices.versioning.BOnyxx414FirmwareVersion;
import com.lynxspring.onyxxDriver.network.BOnyxxNetwork;
import com.lynxspring.onyxxDriver.point.*;
import com.tridium.basicdriver.BBasicNetwork;
import com.tridium.json.XML;
import com.tridium.kitControl.util.BStatusDemux;
import com.tridium.ndriver.BNDevice;
import com.tridium.nrio.*;
import com.tridium.nrio.conv.BNrioTabularThermistorConversion;
import com.tridium.nrio.enums.BUniversalInputTypeEnum;
import com.tridium.nrio.points.*;
import com.tridium.program.Robot;

import javax.baja.alarm.ext.BAlarmSourceExt;
import javax.baja.collection.BITable;
import javax.baja.collection.TableCursor;
import javax.baja.control.*;
import com.lynxspring.onyxxDriver.*;

import javax.baja.driver.BDeviceExt;
import javax.baja.driver.BDriverContainer;
import javax.baja.driver.point.BProxyConversion;
import javax.baja.driver.point.BProxyExt;
import com.tridium.nrio.points.BNrioRelayOutputProxyExt;
import com.tridium.sys.station.Station;
import jdk.nashorn.internal.runtime.ECMAException;

import javax.baja.file.BLocalFileStore;
import javax.baja.history.ext.BHistoryExt;
import javax.baja.naming.BOrd;
import javax.baja.naming.BSlotScheme;
import javax.baja.naming.SlotPath;
import javax.baja.nre.util.Array;
import javax.baja.sys.*;
import javax.baja.util.BNameMap;
import javax.baja.util.BWsAnnotation;
import java.lang.invoke.SwitchPoint;

public class RobotImpl
        extends Robot
{

    /*
    NOT COMPLETE
     */


    public void run()
            throws Exception
    {
        process(Sys.getStation());
    }

    public void process(BComponent c)
            throws Exception {
            addNrioDevices(createNrioNetwork());



    }
    public Cursor getOnyxxDriver(){
        String query = "station:|slot:/Drivers|bql:select * from onyxxDriver:OnyxxNetwork";
        BITable result = (BITable) BOrd.make(query).resolve().get();

        return result.cursor();
    }
    public BNrioNetwork createNrioNetwork(){
        BDriverContainer driverContainer = (BDriverContainer)BOrd.make("station:|slot:/Drivers").resolve(Sys.getStation()).get();
        BBasicNetwork nrioNetwork = new BNrioNetwork();
        Cursor cursor = getOnyxxDriver();
        //log.println(cursor.toString());
            while(cursor.next()) {
                BOnyxxNetwork onyxxNetwork = (BOnyxxNetwork) cursor.get();
                String onyxxName = onyxxNetwork.getName();
                driverContainer.rename(driverContainer.getProperty(onyxxName), onyxxName + "1");
                driverContainer.add(onyxxName, nrioNetwork);
            }
        return (BNrioNetwork) nrioNetwork;
    }
    public void addNrioDevices(BNrioNetwork nrioNetwork){
        Cursor cursor = getOnyxxDriver();
        try {
            cursor.next();
            BOnyxxNetwork onyxxNetwork = (BOnyxxNetwork) cursor.get();
            for (BOnyxxDevice onyxxDevice:onyxxNetwork.getOnyxxDevices()) {
                log.println(onyxxDevice.getPoints().getSlotPath().toString());
                Cursor onyxxPoints = ((BITable)BOrd.make(onyxxDevice.getPoints().getSlotPath().toString() + " | bql:select * from control:ControlPoint where proxyExt.pointType.type = 'onyxxDriver:PointTypeEnum'").resolve(Sys.getStation()).get()).cursor();
                int totalIns = 0;
                int totalAOuts = 0;
                int totalBOuts = 0;
                while(onyxxPoints.next()){
                    if(((BControlPoint)onyxxPoints.get()).isWritablePoint()){
                        totalIns++;
                    }else if(((BControlPoint)onyxxPoints.get()).getType().equals(BNumericPoint.TYPE)){
                        totalAOuts++;
                    }
                    else{
                        totalBOuts++;
                    }
                }
                //onyxxDevice is main device
                if(onyxxDevice.getType().equals(BOnyxx534Device.TYPE)){
                    //IO-R-16: 8 ins, 4 ana outs, 4 binary outs
                    if(totalIns<=8 && totalAOuts<=4 && totalBOuts<=4){
                        BNrio16Module nrioDevice = new BNrio16Module();
                        buildNrioDevice(nrioDevice, onyxxDevice);
                        nrioNetwork.add(onyxxDevice.asComponent().getName(),nrioDevice);
                    }
                    //IO-R-34: 16 ins, 8 ana outs, 10 binary outs
                    else{
                        BNrio34Module nrioDevice  = new BNrio34Module();
                        buildNrioDevice(nrioDevice, onyxxDevice);
                        nrioNetwork.add(onyxxDevice.getName(), nrioDevice);
                    }

                }else if(onyxxDevice.getType().equals(BOnyxx414Device.TYPE)){
                    //TODO

                }else if(onyxxDevice.getType().equals(BOnyxx514Device.TYPE)){
                    //TODO
                }
                //onyxDevice is addon
                else{
                    //IO-R-16: 8 ins, 4 ana outs, 4 binary outs
                    if(totalIns<=8 && totalAOuts<=4 && totalBOuts<=4){
                        BNrio16Module nrioDevice = new BNrio16Module();
                        buildNrioDevice(nrioDevice, onyxxDevice);
                        nrioNetwork.add(onyxxDevice.asComponent().getName(),nrioDevice);
                    }
                    //IO-R-34: 16 ins, 8 ana outs, 10 binary outs
                    else{
                        BNrio34Module nrioDevice  = new BNrio34Module();
                        buildNrioDevice(nrioDevice, onyxxDevice);
                        nrioNetwork.add(onyxxDevice.getName(), nrioDevice);
                    }
=
                }
            }
        }catch (Exception e){
            e.printStackTrace(log);
        }
    }
    public void buildNrioDevice(BNrioDevice nrioDevice, BOnyxxDevice onyxxDevice){
        BNrioPointDeviceExt nrioPoints = nrioDevice.getPoints();
        BOnyxxPointDeviceExt onyxxPoints = onyxxDevice.getPoints();
        for (BControlPoint onyxxPoint:onyxxPoints.getPoints()) {
            BControlPoint nrioPoint = buildPoint(onyxxPoint);
            nrioPoints.add(onyxxPoint.getName(), nrioPoint);
        }
    }
    public  BControlPoint buildPoint(BControlPoint onyxxPoint){
        BControlPoint nrioPoint;
        if(onyxxPoint.isWritablePoint()){
            if(onyxxPoint.getType().toString().contains("Boolean")){
                nrioPoint = new BBooleanWritable();
            }else{
                nrioPoint = new BNumericWritable();
            }
        }else{
            if(onyxxPoint.getType().toString().contains("Boolean")){
                nrioPoint = new BBooleanPoint();
            }else{
                nrioPoint = new BNumericPoint();
            }
        }
        nrioPoint.set(BControlPoint.proxyExt, buildProxyExt(onyxxPoint));
        return nrioPoint;
    }
    public BProxyExt buildProxyExt(BControlPoint onyxxPoint){
        BProxyExt proxyExt = new BNrioProxyExt();
        Type type = onyxxPoint.get(BControlPoint.proxyExt).getType();
        if (BOnyxxBooleanOutputProxyExt.TYPE.equals(type)) {
            proxyExt = new BNrioRelayOutputProxyExt();
            proxyExt.set(BNrioProxyExt.conversion, onyxxPoint.getProxyExt().get(BOnyxxProxyExt.conversion));
        } else if (BOnyxxVoltageOutputProxyExt.TYPE.equals(type)) {
            proxyExt = new BNrioVoltageOutputProxyExt();
            proxyExt.set(BNrioProxyExt.conversion, onyxxPoint.getProxyExt().get(BOnyxxProxyExt.conversion));
        } else if (BOnyxxCurrentInputProxyExt.TYPE.equals(type)) {
            proxyExt = new BNrioVoltageInputProxyExt();
            proxyExt.set(BNrioProxyExt.conversion, onyxxPoint.getProxyExt().get(BOnyxxProxyExt.conversion));
        } else if (BOnyxxCounterInputProxyExt.TYPE.equals(type)) {
            proxyExt = new BNrioCounterInputProxyExt();
            proxyExt.set(BNrioProxyExt.conversion, onyxxPoint.getProxyExt().get(BOnyxxProxyExt.conversion));
        } else if (BOnyxxResistiveInputProxyExt.TYPE.equals(type)) {
            proxyExt = new BNrioResistiveInputProxyExt();
            proxyExt.setConversion(BNrioTabularThermistorConversion.make());
            BNrioTabularThermistorConversion conversion = (BNrioTabularThermistorConversion) proxyExt.getConversion();
            BTabularThermistorConversion onyxxConversion = (BTabularThermistorConversion) ((BOnyxxProxyExt) onyxxPoint.getProxyExt()).getConversion();
            conversion.getPoints().clear();
            conversion.getPoints().addAll(onyxxConversion.getPoints());
        } else if (BOnyxxVoltageInputProxyExt.TYPE.equals(type)) {
            proxyExt.set(BNrioProxyExt.conversion, onyxxPoint.getProxyExt().get(BOnyxxProxyExt.conversion));
        } else {
            proxyExt.set(BNrioProxyExt.conversion, onyxxPoint.getProxyExt().get(BOnyxxProxyExt.conversion));
        }
            proxyExt.set(BNrioProxyExt.deviceFacets, onyxxPoint.getProxyExt().get(BOnyxxProxyExt.deviceFacets));
            proxyExt.set(BNrioProxyExt.instance, onyxxPoint.getProxyExt().get(BOnyxxProxyExt.portAddress));
            proxyExt.set(BNrioProxyExt.tuningPolicyName, onyxxPoint.getProxyExt().get(BOnyxxProxyExt.tuningPolicyName));
            proxyExt.set(BNrioProxyExt.pollFrequency, onyxxPoint.getProxyExt().get(BOnyxxProxyExt.pollFrequency));

        return  proxyExt;

    }
    public void buildOtherProperties(BControlPoint nrioPoint, BControlPoint onyxxPoint){
        Cursor props = onyxxPoint.getProperties();
        while (props.next()){
            if(props.get() instanceof  BAlarmSourceExt){
                try{
                    nrioPoint.add(((BAlarmSourceExt) props.get()).getName(), ((BAlarmSourceExt) props.get()).newCopy());
                }catch (Exception e){
                    nrioPoint.set(((BAlarmSourceExt) props.get()).getName(), ((BAlarmSourceExt) props.get()).newCopy());
                }
            }
            if(props.get() instanceof BHistoryExt){
                try{
                    nrioPoint.add(((BHistoryExt) props.get()).getName(), ((BHistoryExt) props.get()).newCopy());
                }catch (Exception e){
                    nrioPoint.set(((BHistoryExt) props.get()).getName(), ((BHistoryExt) props.get()).newCopy());
                }
            }
            if(props.get() instanceof  BNameMap){
                try{
                    nrioPoint.add("displayNames", (BNameMap)props.get());
                }catch (Exception e){
                    nrioPoint.set("displayNames", (BNameMap)props.get());
                }
            }
            if(props.get() instanceof  BFacets){
                nrioPoint.set(BControlPoint.facets, onyxxPoint.get(BControlPoint.facets));
            }
        }
    }
}
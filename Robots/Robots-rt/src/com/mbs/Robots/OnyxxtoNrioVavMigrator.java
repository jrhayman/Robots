// depends: baja; program; control; bacnet; onyxxDriver; nrio; driver; alarm; kitControl; edgeIo


import com.lynxspring.onyxxDriver.conv.BTabularThermistorConversion;
import com.lynxspring.onyxxDriver.devices.BOnyxx414Device;
import com.lynxspring.onyxxDriver.devices.BOnyxx514Device;
import com.lynxspring.onyxxDriver.devices.BOnyxx534Device;
import com.lynxspring.onyxxDriver.devices.BOnyxxDevice;
import com.lynxspring.onyxxDriver.devices.versioning.BOnyxx414FirmwareVersion;
import com.lynxspring.onyxxDriver.learn.BOnyxxPointEntry;
import com.lynxspring.onyxxDriver.network.BOnyxxNetwork;
import com.lynxspring.onyxxDriver.point.*;
import com.tridium.basicdriver.BBasicNetwork;
import com.tridium.edgeIo.BEdgeIoDevice;
import com.tridium.edgeIo.BEdgeIoNetwork;
import com.tridium.edgeIo.conv.BEdgeIoTabularThermistorConversion;
import com.tridium.edgeIo.conv.*;
import com.tridium.edgeIo.enums.BEdgeIoModeEnum;
import com.tridium.edgeIo.enums.BEdgeIoTypeEnum;
import com.tridium.edgeIo.point.BEdgeIoAnalogProxyExt;
import com.tridium.edgeIo.point.BEdgeIoDigitalProxyExt;
import com.tridium.edgeIo.point.BEdgeIoPointDeviceExt;
import com.tridium.edgeIo.point.BEdgeIoProxyExt;
import com.tridium.edgeIo.*;
import com.tridium.json.XML;
import com.tridium.kitControl.util.BStatusDemux;
import com.tridium.ndriver.BNDevice;
import com.tridium.nrio.*;
import com.tridium.nrio.conv.BNrioTabularThermistorConversion;
import com.tridium.nrio.enums.BNrioDeviceTypeEnum;
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
import com.tridium.sys.transfer.TransferStrategy;
import jdk.nashorn.internal.runtime.ECMAException;

import javax.baja.driver.point.conv.BLinearConversion;
import javax.baja.file.BLocalFileStore;
import javax.baja.history.ext.BHistoryExt;
import javax.baja.naming.BOrd;
import javax.baja.naming.BSlotScheme;
import javax.baja.naming.SlotPath;
import javax.baja.nre.util.Array;
import javax.baja.space.Mark;
import javax.baja.sys.*;
import javax.baja.util.BNameMap;
import javax.baja.util.BWsAnnotation;
import javax.swing.text.EditorKit;
import java.io.IOException;
import java.lang.invoke.SwitchPoint;
import java.util.Arrays;
import java.util.Vector;

public class RobotImpl
        extends Robot {


    public void run()
            throws Exception {
        process(Sys.getStation());
    }

    /*
    NOT COMPLETE
     */

    public void process(BComponent c)
            throws Exception {
        BOnyxxNetwork onyxxNetwork = new BOnyxxNetwork();
        BEdgeIoNetwork edgeIoNetwork = new BEdgeIoNetwork();
        BDriverContainer driverContainer = (BDriverContainer)BOrd.make("station:|slot:/Drivers").resolve(Sys.getStation()).get();
        BITable oN = (BITable) BOrd.make("station:|slot:/Drivers | bql:select * from onyxxDriver:OnyxxNetwork" ).resolve().get();
        Cursor crs = oN.cursor();
        while(crs.next()) {
            onyxxNetwork = (BOnyxxNetwork) crs.get();
        }
        String name = onyxxNetwork.getName();
        driverContainer.rename(driverContainer.getProperty(onyxxNetwork.getName()), onyxxNetwork.getName() + "1");
        driverContainer.add(name, edgeIoNetwork);
        BOnyxxPointDeviceExt onyxxPointsContainer = onyxxNetwork.getOnyxxDevices()[0].getPoints();
        convertIO(migrateLogic(edgeIoNetwork, onyxxPointsContainer), onyxxPointsContainer, 2);
        cleanLinks(onyxxNetwork, 2);

    }
    public Vector<BControlPoint> getSequenceInputs(int sequence, BOnyxxPointDeviceExt onyxxPointsContainer){
        Vector<BControlPoint> points = new Vector<>();
        //sequence 1:

        //sequence 2:

        //sequence 3"


        return points;
    }
    public BEdgeIoNetwork migrateLogic(BEdgeIoNetwork edge, BOnyxxPointDeviceExt onyxxPointsContainer){

        BEdgeIoDevice edgeIoDevice = edge.getLocal();
        BEdgeIoPointDeviceExt edgePointsContainer = edgeIoDevice.getPoints();
        boolean skip;
        CopyHints hints = new CopyHints();
        hints.keepHandles = true;
        BComponent[] components = {};
        BITable t = (BITable) BOrd.make(onyxxPointsContainer.getSlotPath().toString() +"| bql:select * from baja:Component where parent.name = '" + edgePointsContainer.getName()+"'").resolve(Sys.getStation()).get();
        Cursor crs = t.cursor();

        while(crs.next()){

            BComponent component = (BComponent) crs.get();


            BComponent [] componentsClone = components.clone();
            components = new BComponent[componentsClone.length+1];
            for(int i = 0; i<componentsClone.length; i++){
                components[i] = componentsClone[i];
            }
            components[componentsClone.length] = component;

        }

        Mark mark = new Mark(components);
        BComponent params = new BComponent();
        params.add(TransferStrategy.PARAM_KEEP_ALL_LINKS, BBoolean.make("true"));
        try {
            mark.copyTo(edgePointsContainer, params, Context.copying);
        }catch (Exception e){
            e.printStackTrace(log);
        }

        return edge;
    }
    /**************************************
     Sequence 1:
     AO1 -> AO1  (VAV DMP)
     AO3 -> AO2  (HW VLV)
     IN1 -> IN1  (ZN TMP)
     IN2 -> IN2  (ZN TMP SP)
     IN3 -> IN3  (SA TMP)
     PRS IN -> IN4  (PRS SNR)
     IN5 -> IN5  (VAV DMP FDBK)

     Sequence 2:
     AO1 -> AO1  (VAV DMP)
     AO3 -> AO2  (HW VLV)
     AO2 -> AO1{NRIO} (RP VLV)
     IN1 -> IN1  (ZN TMP)
     IN2 -> IN2  (ZN TMP SP)
     IN3 -> IN3  (SA TMP)
     PRS IN -> IN4  (PRS SNR)
     IN5 -> IN5  (VAV DMP FDBK)


     Sequence 3:
     AO1 -> AO1  (VAV DMP)
     AO3 -> AO2  (HW VLV)
     BO4 -> BO1  (CUH CMD)
     BO4 -> BO1  (VLV CUH)
     IN1 -> IN1  (ZN TMP)
     IN2 -> IN2  (ZN TMP SP)
     IN3 -> IN3  (SA TMP)
     PRS IN -> IN4  (PRS SNR)
     IN5 -> IN5  (VAV DMP FDBK)
     **************************************/



    public void convertIO(BEdgeIoNetwork edge, BOnyxxPointDeviceExt onyxxPointsContainer, int sequence){
        BEdgeIoPointDeviceExt edgePointsContainer = edge.getLocal().getPoints();
        BEdgeIoProxyExt edgeProxy = new BEdgeIoProxyExt();
        BControlPoint edgeIoPoint;
        switch (sequence) {
            case 1:
                convertIOSequence1(edgePointsContainer,onyxxPointsContainer);
                break;
            case 2:
                convertIOSequence2(edgePointsContainer, onyxxPointsContainer);
                break;
            case 3:
                convertIOSequence3(edgePointsContainer, onyxxPointsContainer);
                break;


        }


    }
    public void convertIOSequence1(BEdgeIoPointDeviceExt edgePointsContainer, BOnyxxPointDeviceExt onyxxPointsContainer){

        log.println("in convert method");
        for (BControlPoint onyxxContainerPoint : onyxxPointsContainer.getPoints()) {
            BEdgeIoProxyExt edgeProxyExt;
            log.println(onyxxContainerPoint.getName());
            if(onyxxContainerPoint instanceof BIWritablePoint){
                edgeProxyExt = new BEdgeIoAnalogProxyExt();
                //VAV DMP
                if(((BOnyxxProxyExt)onyxxContainerPoint.getProxyExt()).getPortAddress() == 1){
                    edgeProxyExt.setPointId(301);
                    edgeProxyExt.setPointLabel("AO1");
                    edgeProxyExt.setConversion((BProxyConversion) ((BOnyxxProxyExt) onyxxContainerPoint.getProxyExt()).getConversion().newCopy());
                    edgeProxyExt.setDeviceFacets((BFacets) ((BOnyxxProxyExt) onyxxContainerPoint.getProxyExt()).getDeviceFacets().newCopy());
                    edgeProxyExt.setPointType(BEdgeIoTypeEnum.analogOut);
                    edgeProxyExt.setPointMode(BDynamicEnum.make(BEdgeIoModeEnum.voltageOutput));
                    ((BControlPoint)edgePointsContainer.get(onyxxContainerPoint.getName())).setProxyExt(edgeProxyExt);

                }
                if(((BOnyxxProxyExt)onyxxContainerPoint.getProxyExt()).getPortAddress() == 3){
                    edgeProxyExt.setPointId(303);
                    edgeProxyExt.setPointLabel("AO3");
                    edgeProxyExt.setConversion((BProxyConversion) ((BOnyxxProxyExt) onyxxContainerPoint.getProxyExt()).getConversion().newCopy());
                    edgeProxyExt.setDeviceFacets((BFacets) ((BOnyxxProxyExt) onyxxContainerPoint.getProxyExt()).getDeviceFacets().newCopy());
                    edgeProxyExt.setPointType(BEdgeIoTypeEnum.analogOut);
                    edgeProxyExt.setPointMode(BDynamicEnum.make(BEdgeIoModeEnum.voltageOutput));
                    ((BControlPoint)edgePointsContainer.get(onyxxContainerPoint.getName())).setProxyExt(edgeProxyExt);
                }
            }else if(!(onyxxContainerPoint.getProxyExt().getType() instanceof  BOnyxxVavPressureInputProxyExt)){
                BOnyxxProxyExt onyxxExt = (BOnyxxProxyExt) onyxxContainerPoint.getProxyExt();
                switch(((BOnyxxProxyExt)onyxxContainerPoint.getProxyExt()).getPortAddress()){
                    //zn temp
                    case 1:
                        edgeProxyExt = new BEdgeIoAnalogProxyExt();
                        edgeProxyExt.setDeviceFacets((BFacets) onyxxExt.getDeviceFacets().newCopy());
                        edgeProxyExt.setPointId(201);
                        edgeProxyExt.setPointType(BEdgeIoTypeEnum.universalInput);
                        edgeProxyExt.setPointMode(BDynamicEnum.make(BEdgeIoModeEnum.resistanceInput));
                        edgeProxyExt.setConversion(BEdgeIoTabularThermistorConversion.make(BEdgeIoTabularThermistorConversion.DEFAULT.encodeToString()));
                        ((BControlPoint)edgePointsContainer.get(onyxxContainerPoint.getName())).setProxyExt(edgeProxyExt);
                        break;
                    case 2:
                        edgeProxyExt = new BEdgeIoAnalogProxyExt();
                        edgeProxyExt.setDeviceFacets((BFacets) onyxxExt.getDeviceFacets().newCopy());
                        edgeProxyExt.setPointId(202);
                        edgeProxyExt.setPointType(BEdgeIoTypeEnum.universalInput);
                        edgeProxyExt.setPointMode(BDynamicEnum.make(BEdgeIoModeEnum.resistanceInput));
                        edgeProxyExt.setConversion(BEdgeIoTabularThermistorConversion.make(BEdgeIoTabularThermistorConversion.DEFAULT.encodeToString()));
                        ((BControlPoint)edgePointsContainer.get(onyxxContainerPoint.getName())).setProxyExt(edgeProxyExt);
                        break;
                    case 3:
                        edgeProxyExt = new BEdgeIoAnalogProxyExt();
                        edgeProxyExt.setDeviceFacets((BFacets) onyxxExt.getDeviceFacets().newCopy());
                        edgeProxyExt.setPointId(203);
                        edgeProxyExt.setPointType(BEdgeIoTypeEnum.universalInput);
                        edgeProxyExt.setPointMode(BDynamicEnum.make(BEdgeIoModeEnum.resistanceInput));
                        edgeProxyExt.setConversion(BEdgeIoTabularThermistorConversion.make(BEdgeIoTabularThermistorConversion.DEFAULT.encodeToString()));
                        ((BControlPoint)edgePointsContainer.get(onyxxContainerPoint.getName())).setProxyExt(edgeProxyExt);
                        break;
                    case 5:
                        edgeProxyExt = new BEdgeIoAnalogProxyExt();
                        edgeProxyExt.setDeviceFacets((BFacets) onyxxExt.getDeviceFacets().newCopy());
                        edgeProxyExt.setPointId(205);
                        edgeProxyExt.setPointType(BEdgeIoTypeEnum.universalInput);
                        edgeProxyExt.setPointMode(BDynamicEnum.make(BEdgeIoModeEnum.voltageInput));
                        edgeProxyExt.setConversion((BProxyConversion) onyxxExt.getConversion().newCopy());
                        ((BControlPoint)edgePointsContainer.get(onyxxContainerPoint.getName())).setProxyExt(edgeProxyExt);
                    default: break;
                }
            }else if(onyxxContainerPoint.getProxyExt().getType() instanceof  BOnyxxVavPressureInputProxyExt){
                BOnyxxProxyExt onyxxExt = (BOnyxxProxyExt) onyxxContainerPoint.getProxyExt();
                edgeProxyExt = new BEdgeIoAnalogProxyExt();
                edgeProxyExt.setDeviceFacets((BFacets) onyxxExt.getDeviceFacets().newCopy());
                edgeProxyExt.setPointId(204);
                edgeProxyExt.setPointType(BEdgeIoTypeEnum.universalInput);
                edgeProxyExt.setPointMode(BDynamicEnum.make(BEdgeIoModeEnum.voltageInput));
                edgeProxyExt.setConversion(BLinearConversion.make(250,0));
            }
        }
    }
    public void convertIOSequence2(BEdgeIoPointDeviceExt edgePointsContainer, BOnyxxPointDeviceExt onyxxPointsContainer){
        convertIOSequence1(edgePointsContainer, onyxxPointsContainer);
        BDriverContainer driverContainer = (BDriverContainer)BOrd.make("station:|slot:/Drivers").resolve(Sys.getStation()).get();
        BNrioNetwork nrioNetwork = new BNrioNetwork();
        BNrioDevice device = new BNrio16Module();
        device.setDeviceType(BNrioDeviceTypeEnum.io16);
        driverContainer.add("NrioNetwork", nrioNetwork);
        ((BNrioNetwork)driverContainer.get("NrioNetwork")).add("Io16_01", device);
        for(BControlPoint point : onyxxPointsContainer.getPoints()){
            if(((BOnyxxProxyExt)point.getProxyExt()).getPortAddress() == 2 && point instanceof BNumericWritable){
                Mark mark  = new Mark(point);
                BComponent params = new BComponent();
                params.add(TransferStrategy.PARAM_KEEP_ALL_LINKS, BBoolean.make(true));
                try {
                    mark.copyTo(device.getPoints(), params, Context.copying);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                BNrioVoltageOutputProxyExt proxyExt = new BNrioVoltageOutputProxyExt();
                proxyExt.setDeviceFacets(((BOnyxxProxyExt) point.getProxyExt()).getDeviceFacets());
                proxyExt.setInstance(1);
                proxyExt.setConversion((BProxyConversion) ((BOnyxxProxyExt) point.getProxyExt()).getConversion().newCopy());
                ((BControlPoint)device.getPoints().get(point.getName())).setProxyExt(proxyExt);
            }
        }


    }
    public void convertIOSequence3(BEdgeIoPointDeviceExt edgePointsContainer, BOnyxxPointDeviceExt onyxxPointsContainer){
        convertIOSequence1(edgePointsContainer, onyxxPointsContainer);
        for(BControlPoint point : onyxxPointsContainer.getPoints()){
            if( ((BOnyxxProxyExt)point.getProxyExt()).getPortAddress() == 4 && point instanceof BBooleanWritable){
                BEdgeIoDigitalProxyExt proxyExt = new BEdgeIoDigitalProxyExt();
                proxyExt.setPointId(501);
                proxyExt.setPointType(BEdgeIoTypeEnum.digitalOut);
                proxyExt.setPointMode(BDynamicEnum.make(BEdgeIoModeEnum.dryContactOutput));
                proxyExt.setDeviceFacets((BFacets) ((BOnyxxProxyExt) point.getProxyExt()).getDeviceFacets().newCopy());
                ((BControlPoint)edgePointsContainer.get(point.getName())).setProxyExt(proxyExt);
            }
        }
    }

    public void cleanLinks(BOnyxxNetwork onyxxNetwork, int sequence){

    }


}
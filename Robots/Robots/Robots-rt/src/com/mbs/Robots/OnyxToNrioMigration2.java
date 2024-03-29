package com.mbs.Robots;// depends: baja; program; control; bacnet; onyxxDriver; nrio; driver; alarm


import com.lynxspring.onyxxDriver.devices.versioning.BOnyxx414FirmwareVersion;
import com.lynxspring.onyxxDriver.point.BOnyxxProxyExt;
import com.tridium.nrio.*;
import com.tridium.nrio.enums.BUniversalInputTypeEnum;
import com.tridium.nrio.points.*;
import com.tridium.program.Robot;

import javax.baja.alarm.ext.BAlarmSourceExt;
import javax.baja.collection.BITable;
import javax.baja.control.*;
import com.lynxspring.onyxxDriver.*;

import javax.baja.driver.BDeviceExt;
import javax.baja.driver.alarm.BAlarmDeviceExt;
import javax.baja.driver.point.BProxyExt;
import com.tridium.nrio.points.BNrioRelayOutputProxyExt;

import javax.baja.history.ext.BHistoryExt;
import javax.baja.naming.BOrd;
import javax.baja.naming.SlotPath;
import javax.baja.sys.*;
import javax.baja.util.BNameMap;

public class RobotImpl
        extends Robot
{
    /************************************************************
     * This program converts OnyxxDevice Points to NRIO Device Points
     *
     *
     *
     *************************************************************/

    public void run()
            throws Exception
    {
        process(Sys.getStation());
    }

    public void process(BComponent c)
            throws Exception {

        BOrd query = BOrd.make("station:|slot:/Drivers/OnyxxNetwork2/RERVU_2/points | bql:select * from onyxxDriver:OnyxxProxyExt");
        //where parent.parent.type like '*Numeric*' or parent.parent.type like '*Boolean*'
        BITable result = (BITable) query.resolve().get();
        Cursor crs = result.cursor();
        BNrioPointDeviceExt nrioNetworkPoints = (BNrioPointDeviceExt) BOrd.make("station:|slot:/Drivers/OnyxxNetwork/RERVU_2/points").resolve().get();
        int inputInstance = 1;
        int boolOutputInstance = 1;
        int anOutputInstance = 1;
        while (crs.next()){
            BComponent onyxxPoint = (BComponent)((BComponent)crs.get()).getParent();
            BControlPoint newNrioPoint = null;
            BProxyExt proxyExt = null;

            //sets the proxy extension to the specific type needed. some are not done since type conversion not clear
            log.println(((BOnyxxProxyExt)crs.get()).getPointType().toString());
            switch (((BOnyxxProxyExt)crs.get()).getPointType().toString()){
                case "Resistive Input":
                    log.println("Resistive Input");
                    proxyExt = new BNrioResistiveInputProxyExt();
                    break;
                case "Voltage Input":
                case "Current Input":
                    log.println("Current Input");
                    proxyExt = new BNrioVoltageInputProxyExt();
                    break;
                case "Boolean Resistive Input":
                case "Boolean Voltage Input":
                case "Boolean Current Input":
                    log.println("Current Input");
                    proxyExt = new BNrioBooleanInputProxyExt();
                    break;
                case "Resistive Pulse Input":
                    log.println("Pulse Input conversion not created yet for point " + onyxxPoint.getName());
                    break;
                case "Voltage Output":
                    log.println("Voltage Output");
                    proxyExt = new BNrioVoltageOutputProxyExt();
                    break;
                case "Boolean Output":
                    log.println("Boolean Output");
                    proxyExt = new BNrioRelayOutputProxyExt();
                    break;
                case "Internal Temperature Input":
                    log.println("Internal Temperature conversion not created yet for point " + onyxxPoint.getName());
                    break;
                default:
                    break;

            }
            //if proxy extension was set begin to add proxy extension properties.
            if(proxyExt != null) {
                //log.println("proxy extension is not null");
                proxyExt.set(BNrioProxyExt.deviceFacets, ((BOnyxxProxyExt) ((BControlPoint) onyxxPoint).getProxyExt()).getDeviceFacets());
                //
                //(BOnyxxProxyExt.conversion)((BOnyxxProxyExt) ((BControlPoint) onyxxPoint).getProxyExt()).getConversion().);
                if(!(proxyExt instanceof  BNrioResistiveInputProxyExt)) {
                    proxyExt.set(BNrioProxyExt.conversion, ((BOnyxxProxyExt) ((BControlPoint) onyxxPoint).getProxyExt()).getConversion());
                }
                proxyExt.set(BNrioProxyExt.tuningPolicyName, BString.make(((BOnyxxProxyExt) ((BControlPoint) onyxxPoint).getProxyExt()).getTuningPolicy().getName()));
                proxyExt.set(BNrioProxyExt.pollFrequency, ((BOnyxxProxyExt) ((BControlPoint) onyxxPoint).getProxyExt()).getPollFrequency());

                //find out if Input or Output and boolean or analog for instance value and for point type
                log.println(((BOnyxxProxyExt) crs.get()).getType().toString());
                if (((BOnyxxProxyExt) crs.get()).getType().toString().contains("Input")) {
                    proxyExt.set(BNrioProxyExt.instance, BInteger.make(inputInstance));
                    if (onyxxPoint.getType().toString().contains("Boolean")) {
                        newNrioPoint = new BBooleanPoint();
                    } else {
                        newNrioPoint = new BNumericPoint();
                    }
                    newNrioPoint.set(BControlPoint.proxyExt, proxyExt);
                    inputInstance++;
                } else if(((BOnyxxProxyExt) crs.get()).getType().toString().contains("Output")) {

                    if (onyxxPoint.getType().toString().contains("Boolean")) {
                        newNrioPoint = new BBooleanWritable();
                        proxyExt.set(BNrioProxyExt.instance, BInteger.make(boolOutputInstance));

                        boolOutputInstance++;
                    } else {
                        newNrioPoint = new BNumericWritable();
                        proxyExt.set(BNrioProxyExt.instance, BInteger.make(anOutputInstance));
                        anOutputInstance++;
                    }
                    newNrioPoint.set(BControlPoint.proxyExt, proxyExt);
                } else {
                    log.println("something went wrong on point " + onyxxPoint.getName());
                }
                //add Nriopoint
                if(newNrioPoint != null) {
                    try{
                        nrioNetworkPoints.add(onyxxPoint.getName(), newNrioPoint);
                    }catch (Exception e){
                        nrioNetworkPoints.set(onyxxPoint.getName(), newNrioPoint);
                    }
                    log.println(newNrioPoint.getProxyExt().get(BNrioProxyExt.instance.toString()));


                    //Add all other Point Properties.
                    SlotCursor propCrs = onyxxPoint.getProperties();
                    while (propCrs.next()) {
                        //find the history extension
                        if(propCrs.get() instanceof BHistoryExt){
                            newNrioPoint.add(((BHistoryExt) propCrs.get()).getName(), propCrs.get().newCopy());
                            ((BHistoryExt)newNrioPoint.get(((BHistoryExt) propCrs.get()).getName())).setEnabled(true);
                        }
                        //get displayNames if they exist
                        if(propCrs.get() instanceof BNameMap){
                            newNrioPoint.add("displayNames", propCrs.get());
                        }
                        //get Facets
                        if(propCrs.get() instanceof  BFacets){
                            newNrioPoint.setFacets((BFacets) propCrs.get());
                            log.println(propCrs.get().toString());
                            log.println(newNrioPoint.getFacets().toString());
                        }
                        if(propCrs.get() instanceof BAlarmSourceExt){
                            newNrioPoint.add(((BAlarmSourceExt) propCrs.get()).getName(), propCrs.get().newCopy());
                        }
                    }
                }
            }
        }

        //add links or edit them for all points in the NRIO Network Driver.

        //all points in OnyxxNetwork2
        BOrd Network2PointsQuery = BOrd.make("station:|slot:/Drivers/OnyxxNetwork2 | bql:select * from control:ControlPoint");
        BITable pointsResult = (BITable) Network2PointsQuery.resolve().get();
        Cursor Network2Points = pointsResult.cursor();
        while(Network2Points.next()){
            BComponent point = (BComponent) Network2Points.get();
            //I dont want function blocks that are ControlPoints
            if(point instanceof BNumericWritable || point instanceof  BNumericPoint || point instanceof  BBooleanWritable || point instanceof  BBooleanPoint || point instanceof  BEnumWritable || point instanceof BEnumPoint){
                //check over all links of point and make sure corresponding Network point has the link
                for(BLink link: point.getLinks()){

                    BComponent N2SourceComponent = link.getSourceComponent();
                    BComponent N2TargetComponent = point;
                    String N2SourceSlotName = link.getSourceSlotName();
                    String N2TargetSlotName = link.getTargetSlotName();
                    BComponent correspondingPoint  = (BComponent) BOrd.make(point.getSlotPath().toString().replace("OnyxxNetwork2", "OnyxxNetwork")).resolve(Sys.getStation()).get();
                    Boolean hasLink = false;
                    //check all links of corresponding point and see if link already exists. because of using paste special make sure that corresponding link is actually looking at NRIO network points.
                    for(BLink clink: correspondingPoint.getLinks()){
                        if(clink.getName().equals(link.getName())){
                            if(clink.getSourceComponent().getSlotPath().toString().contains("OnyxxNetwork2")){
                                clink.setSourceOrd(((BComponent)BOrd.make(clink.getSourceComponent().getSlotPath().toString().replace("OnyxxNetwork2", "OnyxxNetwork")).resolve(Sys.getStation()).get()).getHandleOrd());
                            }
                            hasLink = true;
                        }
                    }
                    //if link doesn't exist create it using N2 link information with replaced network slot path.
                    if(!hasLink){
                        BLink newLink = new BLink(((BComponent)BOrd.make(N2SourceComponent.getSlotPath().toString().replace("OnyxxNetwork2", "OnyxxNetwork")).resolve(Sys.getStation()).get()).getHandleOrd(), N2SourceSlotName, N2TargetSlotName,true);
                        correspondingPoint.add(link.getName(), newLink);
                    }


                }
            }
        }
    }
}
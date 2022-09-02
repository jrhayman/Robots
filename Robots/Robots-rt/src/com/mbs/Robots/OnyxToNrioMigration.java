// depends: baja; program; control; bacnet; onyxxDriver; nrio; driver; alarm; kitControl


import com.lynxspring.onyxxDriver.devices.versioning.BOnyxx414FirmwareVersion;
import com.lynxspring.onyxxDriver.point.BOnyxxProxyExt;
import com.tridium.kitControl.util.BStatusDemux;
import com.tridium.nrio.*;
import com.tridium.nrio.enums.BUniversalInputTypeEnum;
import com.tridium.nrio.points.*;
import com.tridium.program.Robot;

import javax.baja.alarm.ext.BAlarmSourceExt;
import javax.baja.collection.BITable;
import javax.baja.control.*;
import com.lynxspring.onyxxDriver.*;

import javax.baja.driver.BDeviceExt;
import javax.baja.driver.point.BProxyExt;
import com.tridium.nrio.points.BNrioRelayOutputProxyExt;

import javax.baja.file.BLocalFileStore;
import javax.baja.history.ext.BHistoryExt;
import javax.baja.naming.BOrd;
import javax.baja.naming.BSlotScheme;
import javax.baja.naming.SlotPath;
import javax.baja.sys.*;
import javax.baja.util.BNameMap;
import javax.baja.util.BWsAnnotation;

public class RobotImpl
        extends Robot
{
    /************************************************************
     * This program converts OnyxxDevice Points to NRIO Device Points
     *  Before running the program the User must change the OnyxxNetwork to OnyxxNetwork2
     *  add an NrioNetwork and name it OnyxxNetwork and add nrio device named the same as onyxx device(Make sure structure is identical. if points are in folder then a folder should be made in the nrioNetwork),
     *  this is done so nothing needs to be changed at the supervisor.
     *  copy any folders under the OnyxxNetwork and paste special into the NrioNetwork keeping all links. make sure pathing is identical.
     *
     *  when setup is completed copy and paste points paths into string array in program directly below. and run in Robot Editor.
     *
     *************************************************************/

    /***********************************
     add other devices when there is another io device like {"OnyxxSLOTPATH1", "OnyxxSLOTPATH2"}
     {"NrioSLOTPATH1", "NrioSLOTPath2"}
     ************************************/
    public String[] OnyxPointsPath = {"station:|slot:/Drivers/OnyxxNetwork2/AHU1/points"};
    public String[] NrioPointsPath = {"station:|slot:/Drivers/OnyxxNetwork/AHU1/points"};








    public void run()
            throws Exception
    {
        process(Sys.getStation());
    }

    public void process(BComponent c)
            throws Exception {

        generatePoint(OnyxPointsPath, NrioPointsPath);


    }









    private void generatePoint(String[] OnyxPointsPaths, String[] NrioPointsPaths){
        if(OnyxPointsPaths.length != NrioPointsPaths.length){
            log.println("Onyxx and Nrio points paths are not equal. There must be the same amount of slot paths.");
        }
        for(int i = 0; i<OnyxPointsPaths.length; i++) {
            BOrd query = BOrd.make(OnyxPointsPaths[i] + " | bql:select * from onyxxDriver:OnyxxProxyExt");
            BITable result = (BITable) query.resolve().get();
            Cursor crs = result.cursor();
            try {
                BNrioPointDeviceExt nrioNetworkPoints = (BNrioPointDeviceExt) BOrd.make(NrioPointsPaths[i]).resolve().get();
                while (crs.next()) {
                    BComponent onyxxPoint = (BComponent) ((BComponent) crs.get()).getParent();
                    BControlPoint newNrioPoint = null;
                    BProxyExt proxyExt = generateProxyExt((BOnyxxProxyExt) crs.get());
                    addProperties(proxyExt, newNrioPoint, (BOnyxxProxyExt) crs.get(), nrioNetworkPoints);
                    propertyCheck(onyxxPoint, newNrioPoint);
                }
                addLinks();
            } catch (ClassCastException e) {
                BNrio16PointFolder nrioNetworkPoints = (BNrio16PointFolder) BOrd.make(NrioPointsPaths[i]).resolve().get();
                while (crs.next()) {
                    BComponent onyxxPoint = (BComponent) ((BComponent) crs.get()).getParent();
                    BControlPoint newNrioPoint = null;
                    BProxyExt proxyExt = generateProxyExt((BOnyxxProxyExt) crs.get());
                    addProperties(proxyExt, newNrioPoint, (BOnyxxProxyExt) crs.get(), nrioNetworkPoints);
                    propertyCheck(onyxxPoint, newNrioPoint);
                }
                addLinks();
            }


        }
    }



    private BProxyExt generateProxyExt(BOnyxxProxyExt onyxxProxyExt){

        //sets the proxy extension to the specific type needed. some are not done since type conversion not clear
        //log.println(((BOnyxxProxyExt)crs.get()).getPointType().toString());
        switch (onyxxProxyExt.getPointType().toString()){
            case "Resistive Input":
                log.println("Resistive Input");
                return new BNrioResistiveInputProxyExt();
            case "Voltage Input":
            case "Current Input":
                log.println("Current Input");
                return new BNrioVoltageInputProxyExt();

            case "Boolean Resistive Input":
            case "Boolean Voltage Input":
            case "Boolean Current Input":
                log.println("Current Input");
                return new BNrioBooleanInputProxyExt();

            case "Resistive Pulse Input":
                log.println("Pulse Input conversion not created yet for point " + onyxxProxyExt.getParent().getName());
                return null;

            case "Voltage Output":
                log.println("Voltage Output");
                return new BNrioVoltageOutputProxyExt();

            case "Boolean Output":
                log.println("Boolean Output");
                return new BNrioRelayOutputProxyExt();

            case "Internal Temperature Input":
                log.println("Internal Temperature conversion not created yet for point " + onyxxProxyExt.getParent().getName());
                return null;

            default:
                return null;


        }
    }



    //if Onyxx points are under the points extension of the network.
    private void addProperties(BProxyExt proxyExt, BControlPoint newNrioPoint, BOnyxxProxyExt onyxxProxyExt, BNrioPointDeviceExt nrioNetworkPoints){
        if(proxyExt != null) {
            //log.println("proxy extension is not null");
            proxyExt.set(BNrioProxyExt.deviceFacets, ((BOnyxxProxyExt) ((BControlPoint) onyxxProxyExt.getParent()).getProxyExt()).getDeviceFacets());
            //
            //(BOnyxxProxyExt.conversion)((BOnyxxProxyExt) ((BControlPoint) onyxxPoint).getProxyExt()).getConversion().);
            if(!(proxyExt instanceof  BNrioResistiveInputProxyExt)) {
                proxyExt.set(BNrioProxyExt.conversion, ((BOnyxxProxyExt) ((BControlPoint) onyxxProxyExt.getParent()).getProxyExt()).getConversion());
            }else{

            }
            proxyExt.set(BNrioProxyExt.tuningPolicyName, BString.make(((BOnyxxProxyExt) ((BControlPoint) onyxxProxyExt.getParent()).getProxyExt()).getTuningPolicy().getName()));
            proxyExt.set(BNrioProxyExt.pollFrequency, ((BOnyxxProxyExt) ((BControlPoint) onyxxProxyExt.getParent()).getProxyExt()).getPollFrequency());

            //find out if Input or Output and boolean or analog for instance value and for point type
            if (onyxxProxyExt.getType().toString().contains("Input")) {

                proxyExt.set(BNrioProxyExt.instance, BInteger.make(onyxxProxyExt.getPortAddress()));
                if (onyxxProxyExt.getParent().getType().toString().contains("Boolean")) {
                    newNrioPoint = new BBooleanPoint();
                } else {
                    newNrioPoint = new BNumericPoint();
                }
                newNrioPoint.set(BControlPoint.proxyExt, proxyExt);

            } else if(onyxxProxyExt.getType().toString().contains("Output")) {

                if (onyxxProxyExt.getParent().getType().toString().contains("Boolean")) {
                    newNrioPoint = new BBooleanWritable();
                    proxyExt.set(BNrioProxyExt.instance, BInteger.make(onyxxProxyExt.getPortAddress()));


                } else {
                    newNrioPoint = new BNumericWritable();
                    proxyExt.set(BNrioProxyExt.instance, BInteger.make(onyxxProxyExt.getPortAddress()));

                }
                newNrioPoint.set(BControlPoint.proxyExt, proxyExt);
            } else {
                log.println("something went wrong on point " + onyxxProxyExt.getParent().getName());
            }
            //add Nriopoint
            if(newNrioPoint != null) {
                //log.println("nrioPoint not null");
                try{
                    nrioNetworkPoints.add(onyxxProxyExt.getParent().getName(), newNrioPoint);
                }catch (Exception e){

                    nrioNetworkPoints.set(onyxxProxyExt.getParent().getName(), newNrioPoint);
                }


                //Add all other Point Properties.
                SlotCursor propCrs = onyxxProxyExt.getParent().getProperties();
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
                    }
                    if(propCrs.get() instanceof BAlarmSourceExt){
                        newNrioPoint.add(((BAlarmSourceExt) propCrs.get()).getName(), propCrs.get().newCopy());
                    }
                }

            }

        }

    }
    //if Onyxx Points are in a Folder.
    private void addProperties(BProxyExt proxyExt, BControlPoint newNrioPoint, BOnyxxProxyExt onyxxProxyExt, BNrio16PointFolder nrioNetworkPoints){
        if(proxyExt != null) {
            //log.println("proxy extension is not null");
            proxyExt.set(BNrioProxyExt.deviceFacets, ((BOnyxxProxyExt) ((BControlPoint) onyxxProxyExt.getParent()).getProxyExt()).getDeviceFacets());
            //
            //(BOnyxxProxyExt.conversion)((BOnyxxProxyExt) ((BControlPoint) onyxxPoint).getProxyExt()).getConversion().);
            if(!(proxyExt instanceof  BNrioResistiveInputProxyExt)) {
                proxyExt.set(BNrioProxyExt.conversion, ((BOnyxxProxyExt) ((BControlPoint) onyxxProxyExt.getParent()).getProxyExt()).getConversion());
            }
            proxyExt.set(BNrioProxyExt.tuningPolicyName, BString.make(((BOnyxxProxyExt) ((BControlPoint) onyxxProxyExt.getParent()).getProxyExt()).getTuningPolicy().getName()));
            proxyExt.set(BNrioProxyExt.pollFrequency, ((BOnyxxProxyExt) ((BControlPoint) onyxxProxyExt.getParent()).getProxyExt()).getPollFrequency());

            //find out if Input or Output and boolean or analog for instance value and for point type
            if (onyxxProxyExt.getType().toString().contains("Input")) {

                proxyExt.set(BNrioProxyExt.instance, BInteger.make(onyxxProxyExt.getPortAddress()));
                if (onyxxProxyExt.getParent().getType().toString().contains("Boolean")) {
                    newNrioPoint = new BBooleanPoint();
                } else {
                    newNrioPoint = new BNumericPoint();
                }
                newNrioPoint.set(BControlPoint.proxyExt, proxyExt);

            } else if(onyxxProxyExt.getType().toString().contains("Output")) {

                if (onyxxProxyExt.getParent().getType().toString().contains("Boolean")) {
                    newNrioPoint = new BBooleanWritable();
                    proxyExt.set(BNrioProxyExt.instance, BInteger.make(onyxxProxyExt.getPortAddress()));


                } else {
                    newNrioPoint = new BNumericWritable();
                    proxyExt.set(BNrioProxyExt.instance, BInteger.make(onyxxProxyExt.getPortAddress()));

                }
                newNrioPoint.set(BControlPoint.proxyExt, proxyExt);
            } else {
                log.println("something went wrong on point " + onyxxProxyExt.getParent().getName());
            }
            //add Nriopoint
            if(newNrioPoint != null) {
                try{
                    nrioNetworkPoints.add(onyxxProxyExt.getParent().getName(), newNrioPoint);
                }catch (Exception e){
                    nrioNetworkPoints.set(onyxxProxyExt.getParent().getName(), newNrioPoint);
                }


                //Add all other Point Properties.
                SlotCursor propCrs = onyxxProxyExt.getParent().getProperties();
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
                    }
                    if(propCrs.get() instanceof BAlarmSourceExt){
                        newNrioPoint.add(((BAlarmSourceExt) propCrs.get()).getName(), propCrs.get().newCopy());
                    }
                }

            }

        }

    }
    private void addLinks(){
        BOrd Network2PointsQuery = BOrd.make("station:|slot:/Drivers | bql:select * from control:ControlPoint");
        BITable pointsResult = (BITable) Network2PointsQuery.resolve().get();
        Cursor Network2Points = pointsResult.cursor();
        while(Network2Points.next()){
            BComponent point = (BComponent) Network2Points.get();

            if(point instanceof BNumericWritable || point instanceof  BNumericPoint || point instanceof  BBooleanWritable || point instanceof  BBooleanPoint || point instanceof  BEnumWritable || point instanceof BEnumPoint || point instanceof BStatusDemux){
                //check over all links of point and make sure corresponding Network point has the link
                for(BLink link: point.getLinks()){
                    ;
                    try {

                        BComponent N2SourceComponent = link.getSourceComponent();
                        BComponent N2TargetComponent = point;
                        String N2SourceSlotName = link.getSourceSlotName();
                        String N2TargetSlotName = link.getTargetSlotName();
                        BComponent correspondingPoint = (BComponent) BOrd.make(point.getSlotPath().toString().replace("OnyxxNetwork2", "OnyxxNetwork")).resolve(Sys.getStation()).get();
                        Boolean hasLink = false;
                        //check all links of corresponding point and see if link already exists. because of using paste special make sure that corresponding link is actually looking at NRIO network points.
                        for (BLink clink : correspondingPoint.getLinks()) {
                            if (clink.getName().equals(link.getName())) {
                                if (clink.getSourceComponent().getSlotPath().toString().contains("OnyxxNetwork2")) {
                                    clink.setSourceOrd(((BComponent) BOrd.make(clink.getSourceComponent().getSlotPath().toString().replace("OnyxxNetwork2", "OnyxxNetwork")).resolve(Sys.getStation()).get()).getHandleOrd());
                                }
                                hasLink = true;
                            }
                        }
                        //if link doesn't exist create it using N2 link information with replaced network slot path.
                        if (!hasLink) {
                            BLink newLink = new BLink(((BComponent) BOrd.make(N2SourceComponent.getSlotPath().toString().replace("OnyxxNetwork2", "OnyxxNetwork")).resolve(Sys.getStation()).get()).getHandleOrd(), N2SourceSlotName, N2TargetSlotName, true);
                            correspondingPoint.add(link.getName(), newLink);
                            for (BLink l: correspondingPoint.getLinks()) {
                                log.println(l.toString());
                            }

                        }
//                        }
                    }catch(Exception e){
                        log.println("BAD SOURCE COMPONENT ON: " + point.getName());
                        log.println(e.toString());
                    }



                }
            }
        }
    }
    private void propertyCheck(BComponent onyxxPoint, BComponent newNrioPoint){
        if(newNrioPoint != null) {
            SlotCursor propCrs = onyxxPoint.getProperties();
            while (propCrs.next()) {
                //log.println(propCrs.get().toString());
                if (!(propCrs.get() instanceof BLink) && !(propCrs.get() instanceof BOnyxxProxyExt) && !(propCrs.get() instanceof BWsAnnotation)) {
                    Boolean containsProp = false;
                    log.println(propCrs.get().getType().toString());
                    SlotCursor nrioPropCrs = newNrioPoint.getProperties();
                    while (nrioPropCrs.next()) {

                        if (nrioPropCrs.property().equals(propCrs.property()) || (nrioPropCrs.property().getType().equals(propCrs.property().getType()) && nrioPropCrs.property().getName().equals(propCrs.property().getName())))
                            containsProp = true;
                    }
                    if (!containsProp) {
                        log.println("<\nMissing Property: " + propCrs.property().getName() + "\nof Point " + onyxxPoint.getName() + "\n>");
                    }
                }
            }
            log.println("\n");
        }
    }
}
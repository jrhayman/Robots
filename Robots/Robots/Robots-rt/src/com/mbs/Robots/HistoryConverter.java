// depends: baja; program; bacnet; history

import com.tridium.program.Robot;
import javax.baja.collection.BITable;
import javax.baja.control.BNumericPoint;
import javax.baja.history.BHistoryService;
import javax.baja.history.HistorySpaceConnection;
import javax.baja.history.db.BHistoryDatabase;
import javax.baja.history.ext.BHistoryExt;
import javax.baja.history.ext.BNumericCovHistoryExt;
import javax.baja.naming.BOrd;
import javax.baja.sys.BComponent;
import javax.baja.sys.Cursor;
import javax.baja.sys.Sys;

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

        /**********
         * Based on a given query this Robot will convert the NumericInterval History Extension for a Change of Value Extension set to the standard 50 records
         * and a 5% Change Tolerance For outputs, 2 degree for DAT
         ************/
        String Base = "station:|slot:/Drivers/BacnetNetwork/$351573_MicheleClark/NAE01/points/RTUs/RTU4/RTU4_Zones/Zn1";
        String Query = "bql:select * from history:NumericIntervalHistoryExt where (parent.name like '*_O' or parent.name = 'HD_T' or parent.name = 'CD_T' or parent.name like '*damper*' or  parent.name like '*Damper*' or " +
                "parent.name like '*_D*' or parent.name = 'SA_T' or parent.name = 'DA_T' or parent.name = 'RA_T' or " +
                "parent.name = 'SAT' or parent.name = 'DAT' or parent.name = 'RAT') and !(parent.name like '*SP*' or parent.name like '*min*')";
        Boolean notEmpty = true;
//                Extra loop because cursor sometimes doesn't run through full query sometimes
//                do{

        BITable t = (BITable) BOrd.make(Base + "|" + Query).resolve().get();
        Cursor crs = t.cursor();
        BHistoryService service = (BHistoryService)Sys.getService(BHistoryService.TYPE);

        while(notEmpty = crs.next()){
                BHistoryExt original = (BHistoryExt) crs.get();
                BNumericPoint point = (BNumericPoint) original.getParent();
                log.println(point.getSlotPath());
                BHistoryDatabase db = service.getDatabase();
                HistorySpaceConnection space = db.getConnection(null);
                //delete original history
                try {
                    space.deleteHistory(original.getHistory().getId());
                    original.setEnabled(false);
                }catch (Exception e){
                    log.println("no history?");
                }
                //create COV copy of numeric extension.
//                try {
//                    space.getHistory(original.getHistory().getId());
//                } catch (Exception e) {
//                    log.println("should exception be thrown when history doesn't exist in db?");
//                }
                BNumericCovHistoryExt COV = new BNumericCovHistoryExt();
                COV.setHistoryName(original.getHistoryName());
                String parentName = original.getParent().getName();
                //Conditions for either temp  or cmd point - can't assume Facets exist
                if (parentName.contains("_T") || parentName.contains("Temp") || parentName.contains("_temp") || parentName.equals("SAT") || parentName.equals("DAT")) {
                    //Temprature
                    COV.setChangeTolerance(2);
                } else {
                    //Percentage Cmd
                    COV.setChangeTolerance(5);
                }
                try{
                    point.add("NumericCov", COV);
                    COV.setEnabled(true);
                }catch (Exception e){
                    log.println("already exists");
                }

                //remove interval? seems to cause duplicateID fault overtime.
                point.remove(original.getName());


        }

//
//                    BITable t = (BITable) BOrd.make(Base + "|" + Query).resolve().get();
//                    Cursor crs = t.cursor();
//                    notEmpty = crs.next();

//                }while(notEmpty);

    }
}
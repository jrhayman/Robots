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
import java.util.ArrayList;

public class RobotImpl
        extends Robot {

    public void run()
            throws Exception {
        process(Sys.getStation());
    }

    public void process(BComponent c)
            throws Exception {
        String Base = "station:|slot:/Drivers/BacnetNetwork";
        String Query = "bql:select * from history:HistoryExt where faultCause like '*>*'";
        BITable t = (BITable) BOrd.make(Base + "|" + Query).resolve().get();
        Cursor crs = t.cursor();
        BHistoryService service = (BHistoryService) Sys.getService(BHistoryService.TYPE);
        BHistoryDatabase db = service.getDatabase();
        HistorySpaceConnection space = db.getConnection(null);
        ArrayList<BHistoryExt> list = new ArrayList();
        while (crs.next()) {
            list.add((BHistoryExt) crs.get());
            log.println(((BHistoryExt) crs.get()).getHistoryConfig().getId());
        }
        for(BHistoryExt history: list){
            space.deleteHistory(history.getHistory().getId());
        }
    }
}



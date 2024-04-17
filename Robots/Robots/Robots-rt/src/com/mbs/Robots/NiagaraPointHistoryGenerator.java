// depends: baja; control; history; niagara; program

import com.tridium.nd.BNiagaraNetwork;
import com.tridium.nd.BNiagaraStation;
import com.tridium.nd.BNiagaraStationFolder;
import com.tridium.program.Robot;

import javax.baja.collection.BITable;
import javax.baja.history.db.BHistoryDatabase;
import javax.baja.naming.BOrd;
import javax.baja.sys.BComponent;
import javax.baja.sys.Cursor;
import javax.baja.sys.Sys;

public class RobotImpl
        extends Robot {

    private BHistoryDatabase HistoryDB;

    //can be Network, Station Folder or Station.
    private String root = "station:|slot:/Drivers/NiagaraNetwork";
    private BComponent rtComponent = (BComponent) BOrd.make(root).resolve().get();
    public void run()
            throws Exception {
        process(Sys.getStation());
    }

    public void process(BComponent c)
            throws Exception {

        if(rtComponent instanceof BNiagaraNetwork || rtComponent instanceof BNiagaraStationFolder){
            BITable stationsTable = (BITable) BOrd.make(root +"| bql:select * from niagraDriver:NiagraStation").resolve().get();
            Cursor cursor = stationsTable.cursor();

            while(cursor.next()){
                historyChecker((BNiagaraStation) cursor.get());
            }
        }else if(rtComponent instanceof BNiagaraStation){
            historyChecker((BNiagaraStation) rtComponent);
        }else log.println("Error incorrect root.");



    }

    private void historyChecker(BNiagaraStation station){
        BHistoryDatabase db =


    }


}

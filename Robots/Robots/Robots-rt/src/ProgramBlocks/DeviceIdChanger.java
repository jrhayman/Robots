/* Auto-generated ProgramImpl Code */

import java.util.*;              /* java Predefined*/
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.baja.bacnet.BBacnetDevice;
import javax.baja.bacnet.datatypes.BBacnetObjectIdentifier;
import javax.baja.collection.BITable;
import javax.baja.history.ext.BHistoryExt;
import javax.baja.naming.BOrd;
import javax.baja.nre.util.*;    /* nre Predefined*/
import javax.baja.sys.*;         /* baja Predefined*/
import javax.baja.status.*;      /* baja Predefined*/
import javax.baja.util.*;        /* baja Predefined*/

import com.tridium.program.*;    /* program-rt Predefined*/
import org.jetbrains.annotations.Nullable;

import javax.baja.history.*;

public class ProgramImpl
        extends com.tridium.program.ProgramBase
{

////////////////////////////////////////////////////////////////
// Program Source
////////////////////////////////////////////////////////////////


    private class Group extends KeyValueTuple{

        public Group(int id, BBacnetDevice[] values) {
            super(id, values);
        }

        public BComponent getCurrent(){
            for (BBacnetDevice cmp:(BBacnetDevice[]) this.value) {
                if(cmp.getConfig().getDeviceObject().getObjectId().getId() != -1)
                    return cmp;
            }
            return null;
        }
    }


    /*
     creating a data structure of <pointer, Group> where the Group is <DeviceID, [ORDS]>
     */

    Hashtable<Integer, Group> groups = new Hashtable<Integer,Group>();
    private Worker worker;
    private javax.baja.util.Queue queue = new javax.baja.util.Queue();
    Group group1 = new Group(1, new BBacnetDevice[]{
            (BBacnetDevice) BOrd.make("ord1").resolve().get(),
            (BBacnetDevice) BOrd.make("ord2").resolve().get(),
    });
    Group group2 = new Group(10000, new BBacnetDevice[]{
            (BBacnetDevice) BOrd.make("ord1").resolve().get(),
            (BBacnetDevice) BOrd.make("Ord2").resolve().get(),
            (BBacnetDevice) BOrd.make("Ord3").resolve().get()
    });
    Group group3 = new Group(20000, new BBacnetDevice[]{
            (BBacnetDevice) BOrd.make("ord1").resolve().get(),
            (BBacnetDevice) BOrd.make("ord2").resolve().get(),
            (BBacnetDevice) BOrd.make("Ord3").resolve().get()
    });
    Group group4 = new Group(11000, new BBacnetDevice[]{
            (BBacnetDevice) BOrd.make("ord1").resolve().get(),
            (BBacnetDevice) BOrd.make("ord2").resolve().get()
    });
    public Integer findEnabled(Group g){
        int i = 0;
        for (BBacnetDevice device:(BBacnetDevice[]) g.value) {
            Logger.getLogger(this.getComponent().getTypeDisplayName(null)).log(Level.INFO,device.getName()+": ID - " + device.getConfig().getDeviceObject().getObjectId().getInstanceNumber());
            if(((Integer)g.key).equals(device.getConfig().getDeviceObject().getObjectId().getInstanceNumber())){
                return i;
            }
            i++;
        }
        Logger.getLogger(this.getComponent().getTypeDisplayName(null)).log(Level.INFO, "All values for group ID " + g.key.toString() + " are -1");

        return 0;
    }



    public void onStart() throws Exception
    {
        groups.clear();
        //add and remove created groups as needed.
        groups.put(findEnabled(group1), group1);
        groups.put(findEnabled(group2), group2);
        groups.put(findEnabled(group3), group3);
        groups.put(findEnabled(group4), group4);

        if(worker == null){
            worker = new Worker(queue);
            worker.start(getComponent().getName());
        }
        Thread thread = new Thread(this, getComponent().getName());
        thread.start();
    }

    public void onExecute() throws Exception
    {
        // execute code (set executeOnChange flag on inputs)
        queue.enqueue(this);

    }

    public void onStop() throws Exception
    {
        // shutdown code here
    }
    public void run(){
        // Groups: <pointer, Group> where the Group is <DeviceID, [ORDS]>
        //pointer is to specific ORD

        groups.forEach((integer, group) -> {
            //current
            System.out.println("Group: " + group.key);
            (((BBacnetDevice[]) group.value)[integer]).getConfig().getDeviceObject().setObjectId(BBacnetObjectIdentifier.make(8,-1));
            //next
            System.out.println("Old Device: " + (((BBacnetDevice[]) group.value)[integer]).getSlotPath() +"\n" +
                    "Pointer Value: " + integer);
            integer = integer+1;
            integer = (integer+1)%((BBacnetDevice[])group.value).length;
            ((BBacnetDevice[]) group.value)[integer].getConfig().getDeviceObject().setObjectId(BBacnetObjectIdentifier.make(8,(Integer) group.key));
            System.out.println("New Device: " + (((BBacnetDevice[]) group.value)[integer]).getSlotPath() +"\n" +
                    "Pointer Value: " + integer + "\n");


        });

        // how to let history program block know of current device?
        // wait and run histories in a thread?
        System.out.println("started task ["+Thread.currentThread().getName()+"]");
        //allow time for points to subscribe
        try{Thread.sleep(90000);}
        catch (InterruptedException e) {/*do nothing*/}


        groups.forEach((integer, group) -> {

           BOrd q = BOrd.make("station:|"+(((BBacnetDevice[])group.value)[integer]).getSlotPathOrd().toString()+ "|bql:select * from history:HistoryConfig");
            BITable t = (BITable)q.resolve().get();
            Cursor crs = t.cursor();
            while (crs.next()){
                BHistoryConfig config = (BHistoryConfig) crs.get();
                BHistoryExt ext = (BHistoryExt) config.getParent();
                ext.activate();
            }
        });
        System.out.println("ended task ["+Thread.currentThread().getName()+"]");
    }

}

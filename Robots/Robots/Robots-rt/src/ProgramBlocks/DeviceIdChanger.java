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

import com.tridium.nre.util.tuple.Pair;
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

    }
    private class Tuple<A,B>{
        A a;
        B b;

        public Tuple(A a, B b){
            this.a = a;
            this.b = b;
        }
    }


    /*

                        ********** SETUP *********
      - Disable all histories of faulted histories.
      - Create 'Groups' of common device IDs,
      follow template below for new groups add them to Group List.
      - Depending on how often the block is triggered you may want
      to consider the length of the interval for histories since they
      are disabled.
                        ********** Process *********
      when the block starts it will clear the Group List and add All groups.
      when the block is executed it will disable current device histories,
      enable the next device in each groups list, then wait (90) seconds
      before activating each groups new Device histories.

     */





    /*
     data structure of <pointer, Group> where the Group is <DeviceID, [ORDS]>
     */

    ArrayList<Tuple<Integer, Group>> groups = new ArrayList<>();
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
    //Used during OnStart this will
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
        groups.add(new Tuple<>(findEnabled(group1), group1));
        groups.add(new Tuple<>(findEnabled(group2), group2));
        groups.add(new Tuple<>(findEnabled(group3), group3));
        groups.add(new Tuple<>(findEnabled(group4), group4));

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
        Iterator it = groups.iterator();
        while(it.hasNext()){
            Tuple<Integer, Group> g = (Tuple<Integer, Group>) it.next();

            System.out.println("Group: " + g.b.key);
            //set current group's device to -1
            ((BBacnetDevice[])g.b.value)[g.a].getConfig().getDeviceObject().setObjectId(BBacnetObjectIdentifier.make(8, -1));
            //Deactivate histories for all points in this device
            BOrd q = BOrd.make("station:|"+(((BBacnetDevice[])g.b.value)[g.a]).getSlotPathOrd().toString()+ "|bql:select * from history:HistoryConfig");
            BITable t = (BITable)q.resolve().get();
            Cursor crs = t.cursor();
            while(crs.next()){
                BHistoryConfig config = (BHistoryConfig) crs.get();
                BHistoryExt ext = (BHistoryExt) config.getParent();
                ext.setEnabled(false);
//               ext.deactivate();
            }
            System.out.println("Old Device: " + (((BBacnetDevice[]) g.b.value)[g.a]).getSlotPath() +"\n" +
                    "Pointer Value: " + g.a);
            //increment pointer and set next device to group's device ID
            g.a =  (g.a+1)%((BBacnetDevice[])g.b.value).length;
            ((BBacnetDevice[]) g.b.value)[g.a].getConfig().getDeviceObject().setObjectId(BBacnetObjectIdentifier.make(8,(Integer) g.b.key));
            System.out.println("New Device: " + (((BBacnetDevice[]) g.b.value)[g.a]).getSlotPath() +"\n" +
                    "Pointer Value: " + g.a + "\n");
        }
        // how to let history program block know of current device?
        // wait and run histories in a thread?
        System.out.println("started task ["+Thread.currentThread().getName()+"]");
        //allow time for points to subscribe set to 1:30 minutes
        try{Thread.sleep(90000);}
        catch (InterruptedException e) {/*do nothing*/}
        //activate histories of the current operating devices points.
        it = groups.iterator();
        while(it.hasNext()){
            Tuple<Integer, Group> g = (Tuple<Integer, Group>) it.next();
            BOrd q = BOrd.make("station:|"+(((BBacnetDevice[])g.b.value)[g.a]).getSlotPathOrd().toString()+ "|bql:select * from history:HistoryConfig");
            BITable t = (BITable)q.resolve().get();
            Cursor crs = t.cursor();
            while (crs.next()){
                BHistoryConfig config = (BHistoryConfig) crs.get();
                BHistoryExt ext = (BHistoryExt) config.getParent();
//              ext.activate();
                ext.setEnabled(true);
            }
        }
        System.out.println("ended task ["+Thread.currentThread().getName()+"]");
    }

}

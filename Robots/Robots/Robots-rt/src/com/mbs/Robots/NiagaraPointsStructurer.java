// depends: baja; niagaraDriver; program

import javax.baja.collection.BITable;
import javax.baja.sys.*;
import com.tridium.program.*;
import com.tridium.nd.point.*;
import javax.baja.control.*;
import javax.baja.space.*;
import javax.baja.naming.*;

public class RobotImpl
        extends Robot
{

    public void run()
            throws Exception
    {
        process(Sys.getStation());
    }

    public void process(BComponent c)
            throws Exception
    {

        String  deviceExtString = "station:|slot:/Drivers/NiagaraNetwork/Julian_51637/points";
        BNiagaraPointDeviceExt deviceExt = (BNiagaraPointDeviceExt) BOrd.make(deviceExtString).resolve().get();
        BOrd query = BOrd.make(deviceExtString + "|" + "bql:select * from control:ControlPoint where parent.type = 'niagaraDriver:NiagaraPointDeviceExt' and proxyExt.type = niagaraDriver:NiagaraProxyExt");
        BITable t = (BITable) query.resolve().get();
        Cursor crs = t.cursor();
        BNiagaraPointFolder targetFolder;
        while(crs.next()){
            BControlPoint point = (BControlPoint) crs.get();
            if(point.getName().contains("act_clg_set_pt") || point.getName().contains("act_Htg_set_pt")){
                continue;
            }
            BNiagaraProxyExt ext = (BNiagaraProxyExt) point.getProxyExt();
            String path = determineFolderPathFromPointId(ext.getPointId());
            try{
                targetFolder = (BNiagaraPointFolder) BOrd.make(deviceExtString + path).resolve().get();
            }catch (Exception e){
                BComponent parent = deviceExt;
                log.println(path);
                for (String s: path.split("/")) {
                    System.out.println(s);
                    if(parent.get(s)==null){
                        System.out.println("Making folder");
                        BNiagaraPointFolder newFolder = new BNiagaraPointFolder();
                        parent.add(s, newFolder);
                        parent = newFolder;
                    }
                    else{
                        parent = (BComponent) parent.get(s);
                    }
                }
                //parent should be lastFolder
                targetFolder = (BNiagaraPointFolder) parent;

            }
            Mark mark = new Mark(point);
            mark.moveTo(targetFolder,null);
            assert targetFolder != null;
            targetFolder.rename((Property) targetFolder.getSlot(point.getName()), extractLastNameFromPointId(ext.getPointId()));
        }
    }

    private String extractLastNameFromPointId(String pointId) {
        String[] segments = pointId.split("/");
        return segments[segments.length - 1];
    }

    private String determineFolderPathFromPointId(String pointId) {
        //they decided to put '/' in their point name.
        log.println(pointId);
        String[] segments = pointId.split("/");
        String folderPath = "";
        boolean startAppending = false;
        for (int i = 0;  i < segments.length - 1;
             i++){
            if (segments[i].equals("Drivers")) {
                startAppending = true;
                continue;
            }
            if (startAppending) {
                log.print(segments[i] + " ");

                if (!segments[i].equals("points")) {
                    if (!folderPath.isEmpty()) {
                        folderPath = folderPath.concat("/");
                    }
                    folderPath = folderPath.concat(segments[i]);
                }
            }
        }
        log.println(folderPath);
        return folderPath;
    }

}
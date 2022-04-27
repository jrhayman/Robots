/* Auto-generated ProgramImpl Code */

import java.util.*;              /* java Predefined*/
import javax.baja.nre.util.*;    /* nre Predefined*/
import javax.baja.sys.*;         /* baja Predefined*/
import javax.baja.status.*;      /* baja Predefined*/
import javax.baja.util.*;        /* baja Predefined*/
import com.tridium.program.*;    /* program-rt Predefined*/

public class ProgramImpl
        extends com.tridium.program.ProgramBase
{

////////////////////////////////////////////////////////////////
// Getters
////////////////////////////////////////////////////////////////

    public int getBase() { return getInt("Base"); }
    public int getSwing() { return getInt("Swing"); }
    public int getPeak() { return getInt("Peak"); }
    public boolean getNeedNext() { return getBoolean("needNext"); }
    public boolean getNeedLess() { return getBoolean("needLess"); }
    public boolean getChiller1ENA() { return getBoolean("Chiller1ENA"); }
    public boolean getChiller2ENA() { return getBoolean("Chiller2ENA"); }
    public boolean getChiller3ENA() { return getBoolean("Chiller3ENA"); }
    public boolean getChiller1ENA_Out() { return getBoolean("Chiller1ENA_Out"); }
    public boolean getChiller2ENA_Out() { return getBoolean("Chiller2ENA_Out"); }
    public boolean getChiller3ENA_Out() { return getBoolean("Chiller3ENA_Out"); }
    public BRelTime getChiller1RunTime() { return (BRelTime)get("Chiller1RunTime"); }
    public BRelTime getChiller2RunTime() { return (BRelTime)get("Chiller2RunTime"); }
    public BRelTime getChiller3RunTime() { return (BRelTime)get("Chiller3RunTime"); }

////////////////////////////////////////////////////////////////
// Setters
////////////////////////////////////////////////////////////////

    public void setBase(int v) { setInt("Base", v); }
    public void setSwing(int v) { setInt("Swing", v); }
    public void setPeak(int v) { setInt("Peak", v); }
    public void setNeedNext(boolean v) { setBoolean("needNext", v); }
    public void setNeedLess(boolean v) { setBoolean("needLess", v); }
    public void setChiller1ENA(boolean v) { setBoolean("Chiller1ENA", v); }
    public void setChiller2ENA(boolean v) { setBoolean("Chiller2ENA", v); }
    public void setChiller3ENA(boolean v) { setBoolean("Chiller3ENA", v); }
    public void setChiller1ENA_Out(boolean v) { setBoolean("Chiller1ENA_Out", v); }
    public void setChiller2ENA_Out(boolean v) { setBoolean("Chiller2ENA_Out", v); }
    public void setChiller3ENA_Out(boolean v) { setBoolean("Chiller3ENA_Out", v); }
    public void setChiller1RunTime(javax.baja.sys.BRelTime v) { set("Chiller1RunTime", v); }
    public void setChiller2RunTime(javax.baja.sys.BRelTime v) { set("Chiller2RunTime", v); }
    public void setChiller3RunTime(javax.baja.sys.BRelTime v) { set("Chiller3RunTime", v); }

////////////////////////////////////////////////////////////////
// Program Source
////////////////////////////////////////////////////////////////

    public void onStart() throws Exception
    {
        // start up code here


    }

    public void onExecute() throws Exception
    {
        /*
          Code that is excuted when needNext turns true, or needLess turns true, or if Base, Swing, or Peak Change
    
          Program is designed to decide what chilled needs to be enabled or disabled
    
        */

        // execute code (set executeOnChange flag on inputs)
        int Peak = getPeak();
        int Swing = getSwing();
        int Base = getBase();
        Vector<Integer> Normals = new Vector<>();
        boolean BaseENA = false;
        boolean SwingENA = false;
        boolean PeakENA = false;
        //setting Base Swing and Peak Enable
        switch(Base){
            case 1: BaseENA = getChiller1ENA(); break;
            case 2: BaseENA = getChiller2ENA(); break;
            case 3: BaseENA = getChiller3ENA(); break;
            default: break;
        }
        switch(Peak){
            case 1: PeakENA = getChiller1ENA(); break;
            case 2: PeakENA = getChiller2ENA(); break;
            case 3: PeakENA = getChiller3ENA(); break;
            default: break;
        }
        switch(Swing){
            case 1: SwingENA = getChiller1ENA(); break;
            case 2: SwingENA = getChiller2ENA(); break;
            case 3: SwingENA = getChiller3ENA(); break;
            default: break;
        }

        //finding Normal Type Chillers
        for(int i = 1; i < 4; i++){
            //if i does not equal peak swing or base add i to Normals
            if(i != Peak  && i != Swing && i != Base) Normals.add(i);


        }
        //if another chiller needs to come on
        if(getNeedNext()){
            //if Base Chiller exists and Base Chiller is not enabled
            if(Base!=0 && !BaseENA){
                switch(Base){
                    case 1: setChiller1ENA_Out(true);break;
                    case 2: setChiller2ENA_Out(true);break;
                    case 3: setChiller3ENA_Out(true);break;
                    default: break;
                }

            }
            //if Swing Chiller exists and Swing Chiller is not enabled
            else if(Swing!=0 && !SwingENA){
                switch(Swing){
                    case 1: setChiller1ENA_Out(true);break;
                    case 2: setChiller2ENA_Out(true);break;
                    case 3: setChiller3ENA_Out(true);break;
                    default: break;
                }
            }

            //if Normal Chiller(s) exist | if all normals are on check if Peak exists and turn it on
            else if(Normals.size() > 0) {


                int n = 0;
                BRelTime currentTime = BRelTime.make(0);
                BRelTime lowestTime = BRelTime.make(0);
                for (int i : Normals) {
                    switch (i) {
                        case 1:
                            if (!getChiller1ENA()) {
                                currentTime = getChiller1RunTime();
                                if (lowestTime.compareTo(currentTime) > 0) {
                                    n = 1;
                                    lowestTime = currentTime;
                                    break;
                                }
                            }break;
                        case 2:
                            if (!getChiller2ENA()) {
                                currentTime = getChiller2RunTime();
                                if (lowestTime.compareTo(currentTime) > 0) {
                                    n = 2;
                                    lowestTime = currentTime;
                                    break;
                                }
                            }break;
                        case 3:
                            if (!getChiller3ENA()) {
                                currentTime = getChiller3RunTime();
                                if (lowestTime.compareTo(currentTime) > 0) {
                                    n = 3;
                                    lowestTime = currentTime;
                                    break;
                                }
                            }break;

                        default:
                            break;
                    }



                    //if all Normal Chillers are enabled
                    if (n == 0) {
                        //if Peak Chiller exists
                        if (Peak != 0 && !PeakENA) {
                            switch (Peak) {
                                case 1:
                                    setChiller1ENA_Out(true);
                                    break;
                                case 2:
                                    setChiller2ENA_Out(true);
                                    break;
                                case 3:
                                    setChiller3ENA_Out(true);
                                    break;
                                default:
                                    break;
                            }

                        }
                        //wants more cooling but everything is on
                        else {
                            //Demand > Capacity
                        }

                    }
                    //if Swing swing exists and is enabled then if normal chiller can be enabled disable Swing
                    else if (Swing != 0 && SwingENA) {
                        switch (Swing) {
                            case 1:
                                setChiller1ENA_Out(false);
                                break;
                            case 2:
                                setChiller2ENA_Out(false);
                                break;
                            case 3:
                                setChiller3ENA_Out(false);
                                break;
                            default:
                                break;
                        }


                        switch (n) {
                            case 1:
                                setChiller1ENA_Out(false);
                                break;
                            case 2:
                                setChiller2ENA_Out(false);
                                break;
                            case 3:
                                setChiller3ENA_Out(false);
                                break;
                            default:
                        }

                        //Swing doesn't exist
                    } else {

                    }
                }
            }
            //if Peak Chiller exists and Peak Chiller not enabled
            else if(Peak!=0 && !PeakENA){
                switch (Peak) {
                    case 1:
                        setChiller1ENA_Out(true);
                        break;
                    case 2:
                        setChiller2ENA_Out(true);
                        break;
                    case 3:
                        setChiller3ENA_Out(true);
                        break;
                    default:
                        break;
                }

            }
            //wants more cooling but everything is on
            else{
                //Demand > Capacity
            }
        }
        //if another chiller needs to be turned off
        else if(getNeedLess()){
            if(Peak!=0 && PeakENA){
                switch (Peak){
                    case 1: setChiller1ENA_Out(false); break;
                    case 2: setChiller2ENA_Out(false); break;
                    case 3: setChiller3ENA_Out(false); break;
                    default: break;
                }
            }
            else if(Swing !=0 && SwingENA){
                switch (Swing){
                    case 1: setChiller1ENA_Out(false); break;
                    case 2: setChiller2ENA_Out(false); break;
                    case 3: setChiller3ENA_Out(false); break;
                    default: break;
                }
            }
            else if(Normals.size()>0) {
                int n = 0;
                BRelTime currentTime = BRelTime.make(0);
                BRelTime greatestTime = BRelTime.make(0);
                for (int i : Normals) {
                    switch (i) {
                        case 1:
                            if (greatestTime.compareTo(getChiller1RunTime()) < 0 && getChiller1ENA()) {
                                greatestTime = getChiller1RunTime();
                                n = 1;
                            }
                            break;
                        case 2:
                            if (greatestTime.compareTo(getChiller2RunTime()) < 0 && getChiller2ENA()) {
                                greatestTime = getChiller2RunTime();
                                n = 2;
                            }
                        case 3:
                            if (greatestTime.compareTo(getChiller3RunTime()) < 0 && getChiller3ENA()) {
                                greatestTime = getChiller3RunTime();
                                n = 3;
                            }
                        default:
                            break;

                    }
                }
                if (Swing != 0) {
                    switch (Swing) {
                        case 1:
                            setChiller1ENA_Out(true);
                            break;
                        case 2:
                            setChiller2ENA_Out(true);
                            break;
                        case 3:
                            setChiller3ENA_Out(true);
                            break;
                        default:
                            break;
                    }


                    switch (n) {
                        case 1:
                            setChiller1ENA_Out(true);
                            break;
                        case 2:
                            setChiller2ENA_Out(true);
                            break;
                        case 3:
                            setChiller3ENA_Out(true);
                            break;
                        default:
                            break;
                    }
                }
            }
            else if(Base!=0 && BaseENA){
                switch (Base) {
                    case 1:
                        setChiller1ENA_Out(true);
                        break;
                    case 2:
                        setChiller2ENA_Out(true);
                        break;
                    case 3:
                        setChiller3ENA_Out(true);
                        break;
                    default:
                        break;
                }
            }
            else{
                //all chillers are disabled all ready
            }

        }
    }


    public void onStop() throws Exception
    {
        // shutdown code here
    }
}

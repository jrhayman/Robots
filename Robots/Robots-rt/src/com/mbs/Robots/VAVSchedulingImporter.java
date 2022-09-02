/* Auto-generated ProgramImpl Code */

import java.io.InputStreamReader;
import java.util.*;              /* java Predefined*/
import javax.baja.file.BIFile;
import javax.baja.naming.BOrd;
import javax.baja.nre.util.*;    /* nre Predefined*/
import javax.baja.sys.*;         /* baja Predefined*/
import javax.baja.status.*;      /* baja Predefined*/
import javax.baja.util.*;        /* baja Predefined*/
import com.tridium.program.*;    /* program-rt Predefined*/

public class ProgramImpl
        extends com.tridium.program.ProgramBase
{

////////////////////////////////////////////////////////////////
// Program Source
////////////////////////////////////////////////////////////////

    public void onStart() throws Exception
    {
        // start up code here
    }

    public void onExecute() throws Exception
    {
        // execute code (set executeOnChange flag on inputs)
        /*
        *   This programs purpose is to take an excel spread sheet of VAV schedule information and set the points value to the given values.
        *   To make sure that the program understands exactly what points and points of what vav to set values to,
        *   you should make sure that the table header starts with the vav's name then the names of all points you wish to edit.
        *   To do this
         */





        BOrd ford = BOrd.make("file:^testing/vavSchedule.csv");

        BIFile file = (BIFile)ford.resolve().get();

        InputStreamReader reader = new InputStreamReader(file.getInputStream());

        String[] rows = FileUtil.readLines(reader);

        for(int i=0; i<rows.length; i++)

        {

            String[] props = TextUtil.splitAndTrim(rows[i], ',');

// loop through the values and decide what to add in the station

        }
    }

    public void onStop() throws Exception
    {
        // shutdown code here
    }

}
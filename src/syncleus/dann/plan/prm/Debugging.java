/* Copyright (C) 2004 Matthias S. Benkmann <msbREMOVE-THIS@winterdrache.de>

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; version 2
of the License (ONLY THIS VERSION).

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/

package prpvis.core;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Date;

/**
* The class <code>Debugging</code> carries global switches to turn debugging
* tests on or off.
*/
public class Debugging
{
  /**
  * If set to <code>false</code>, all debugging tests are disabled.
  */
  public final static boolean enabled=false;
  
  /**
  * If set to <code>true</code>, some methods catch exceptions and write
  * crashlog data (if <code>{@link #enabled}==true</code>).
  */
  public final static boolean catchExceptions=true;
  
  /**
  * If set to <code>true</code>, expensive debugging tests are enabled
  * (if <code>{@link #enabled}==true</code>).
  */
  public final static boolean expensiveTestsEnabled=true;
  
  public final static void crashLog(Throwable ex, String message)
  {
    if (Debugging.enabled)
    {
      try{
        FileWriter fw=new FileWriter("crashlog-for-debugging-MSB-program",true);
        PrintWriter pw=new PrintWriter(fw);
        pw.println((new Date()).toString()+":\n"+message);
        if (ex!=null) ex.printStackTrace(pw);
        pw.println();
        pw.close();
      }catch(Exception e){};
    };  
  };
};


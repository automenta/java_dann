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

/**
* <p>Classes that implement this interface implement an algorithm with pseudocode
* that can be displayed and traced, e.g. via a {@link prpvis.ui.JCodeTracer}.</p>
*/
public interface Traceable
{
  /**
  * <p>Returns the pseudocode that is being traced, 1 {@link String} per line.</p>
  */
  public String[] pseudocode();
  
  /** 
  * <p>Returns the index into the array returned by {@link #pseudocode()} of
  * line that will be executed by the next call to {@link #step()} or
  * <code>-1</code> if {@link #endOfCode()}.</p> 
  */
  public int currentLine();
  
  /**
  * <p>Executes the current line of the algorithm being traced.</p>
  */
  public void step();
  
  /**
  * <p>Restarts the algorithm from the beginning by resetting all variables and
  * setting the current line to be the 1st line to be executed 
  * (which may or may not be the 1st line returned by {@link #pseudocode()}).</p>
  */
  public void restart();
  
  /** 
  * <p>Returns <code>true</code> if there are no more algorithm lines to be executed.</p> 
  */
  public boolean endOfCode();
};




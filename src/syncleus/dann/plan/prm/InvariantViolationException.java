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
  * <p><code>InvariantViolationException</code> is thrown when a class's
  * <code>checkInvariant()</code> method detects an error.</p>
  */
public class InvariantViolationException extends RuntimeException
{
  /** Constructs an <code>InvariantViolationException</code> with no detail message. */
  public InvariantViolationException(){};
  /** Constructs an <code>InvariantViolationException</code> with the specified 
  * detail message */
  public InvariantViolationException(String s){super(s);};
};


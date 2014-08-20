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

import prpvis.struc.*;

/**
* <p>Classes that implement this interface can generate intermediate steps for moving a
* robot from one {@link RobotOrientation} to another {@link RobotOrientation}.</p>
*/
public interface RobotAnimator
{
  /** 
  * <p>Initializes the animation of <code>robot</code>, moving from <code>o1</code> to
  * <code>o2</code> with the given <code>resolution</code>. The parameter <code>o3</code>, 
  * if non-<code>null</code> causes the path to be optimized for the case that
  * the robot's real target is <code>o3</code> with <code>o2</code> being only an
  * intermediate waypoint. This optimization may cause the animation to stop at a
  * {@link RobotOrientation} different from <code>o2</code>! To continue such an
  * optimized path, the next call to <code>initAnimationFromTo()</code> must pass
  * the last orientation of the previous animation as <code>o1</code>.</p> 
  *
  * @return an {@link Object} that must be passed on each invokation of 
  *         {@link #nextAnimationStep}.
  */
  public Object initAnimationFromTo(PolyPoly robot, 
                                    RobotOrientation o1, RobotOrientation o2, RobotOrientation o3,
                                    double resolution);
  /**
  * <p>Returns the next step for the robot on its way, or <code>null</code> if target
  * position has been reached.</p>
  *
  * @param animationObj the {@link Object} returned by {@link #initAnimationFromTo}.
  */
  public RobotOrientation nextAnimationStep(Object animationObj);
};



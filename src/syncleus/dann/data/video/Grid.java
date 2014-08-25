/**
 * Copyright 2013 Dan Oprescu
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package syncleus.dann.data.video;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;

public class Grid implements Iterable<BoundingBox>{	
	static final float GOOD_OVERLAP = 0.6f;
	static final float BAD_OVERLAP = 0.2f;
	
	private static final float SHIFT = 0.1f;
	private static final float[] SCALES = {
             		0.16151f, 0.19381f, 0.23257f, 0.27908f, 0.33490f, 0.40188f, 0.48225f,
	  			0.57870f, 0.69444f, 0.83333f, 1f, 1.20000f, 1.44000f, 1.72800f,
	  			2.07360f, 2.48832f, 2.98598f, 3.58318f, 4.29982f, 5.15978f, 6.19174f};	
	
	
	public final List<BoundingBox> grid = new ArrayList<BoundingBox>();
	public final List<Size> trackedBoxScales = new ArrayList<Size>();
	public final List<BoundingBox> goodBoxes = new ArrayList<BoundingBox>();	//bboxes with overlap > GOOD_OVERLAP
	public final List<BoundingBox> badBoxes = new ArrayList<BoundingBox>();	//bboxes with overlap < BAD_OVERLAP
	public BoundingBox bbHull = new BoundingBox(); // hull of good_boxes
	public BoundingBox bestBox; // maximum overlapping bbox
	
	public Grid(){
	}
	
	
	public Grid(Mat img, Rect trackedBox, int minWinSide){
		// TODO why do we generate so many BAD boxes, only to remove them later on !?
		// OR do we need them to re-asses which ones are bad later on ?
		for(int s=0; s<SCALES.length; s++){
			final int width = Math.round(trackedBox.width * SCALES[s]);
			final int height = Math.round(trackedBox.height * SCALES[s]);
			final int minBbSide = Math.min(height, width);
			
			// continue ONLY if the future box is "reasonable": bigger than the min window and smaller than the full image !
			if(minBbSide >= minWinSide && width <= img.cols() && height <= img.rows()){
				trackedBoxScales.add(new Size(width, height));
				final int shift = Math.round(SHIFT * minBbSide);
				
				for(int row=1; row<(img.rows() - height); row+=shift){
					for(int col=1; col<(img.cols() - width); col+=shift){
						final BoundingBox bbox = new BoundingBox();
						bbox.x = col;
						bbox.y = row;
						bbox.width = width;
						bbox.height = height;
						bbox.scaleIdx = trackedBoxScales.size() - 1; // currently last one in this list
						
						grid.add(bbox);
					}
				}
			}
		}
	}
	
	
	/**
	 * goodBoxes OUTPUT
	 * badBoxes OUTPUT
	 * 
	 * This should be called AFTER updateOverlap(lastBox) so that the overlap numbers are relative to this lastBox, NOT the initial one...
	 */
	public void updateGoodBadBoxes(final Rect trackedBox, final int numClosest) {
		// start by updating the overlap numbers
		for(BoundingBox box : grid){
			box.overlap = box.calcOverlap(trackedBox);
		}		
		
		goodBoxes.clear();
		badBoxes.clear();
		
		float maxOverlap = 0f;
		for(BoundingBox box : grid){
			if(box.overlap > maxOverlap){
				maxOverlap = box.overlap;
				bestBox = box;
			}
			
			if(box.overlap > GOOD_OVERLAP){
				goodBoxes.add(box);
			}else if(box.overlap < BAD_OVERLAP){
				badBoxes.add(box);
			}
		}
		
		// keep only the best numClosest (10) items in goodBoxes
		TLDUtil.keepBestN(goodBoxes, numClosest, new Comparator<BoundingBox>(){
				@Override
				public int compare(BoundingBox bb1, BoundingBox bb2) {
					return Float.valueOf(bb1.overlap).compareTo(bb2.overlap);
				}
			});
		
		System.out.println("Found " + goodBoxes.size() + " good boxes, " + badBoxes.size() + " bad boxes.");
		System.out.println("Best Box: " + bestBox);		
		
		updateBBHull();
		System.out.println("Bounding box hull " + bbHull);
	}
	
	
	private void updateBBHull(){
		//if(goodBoxes.isEmpty()) throw new IllegalStateException("Can't Calculate the BBHull without at least 1 good box !");			
		int x1 = Integer.MAX_VALUE, x2 = 0;
		int y1 = Integer.MAX_VALUE, y2 = 0;
		for (BoundingBox goodBox : goodBoxes) {
			x1 = Math.min(goodBox.x, x1);
			y1 = Math.min(goodBox.y, y1);
			x2 = Math.max(goodBox.x + goodBox.width, x2);
			y2 = Math.max(goodBox.y + goodBox.height, y2);
		}
		
		bbHull.x = x1;
		bbHull.y = y1;
		bbHull.width = x2 - x1;
		bbHull.height = y2 - y1;
	}
	
	
	public BoundingBox[] getGoodBoxes(){
		return goodBoxes.toArray(new BoundingBox[goodBoxes.size()]);
	}
	
	public BoundingBox[] getBadBoxes(){
		return badBoxes.toArray(new BoundingBox[badBoxes.size()]);
	}
	
	public BoundingBox getBestBox(){
		return bestBox;
	}
	
	public BoundingBox getBBhull(){
		return bbHull;
	}
	
	public Size[] getTrackedBoxScales(){
		return trackedBoxScales.toArray(new Size[trackedBoxScales.size()]);
	}
	
	public int getSize(){
		return grid.size();
	}
	
	public BoundingBox getBox(int idx){
		return grid.get(idx);
	}


	@Override
	public Iterator<BoundingBox> iterator() {
		return grid.iterator();
	}
}

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

import java.util.Properties;

public class Parameters {
	protected final Properties props;
	
	public static class ParamsTld extends Parameters{
		int min_win;
		int patch_size;
		
		// initial parameters for positive examples
		int num_closest_init;
		int num_warps_init;
		int noise_init;
		float angle_init;
		float shift_init;
		float scale_init;
		
		// update parameters for positive examples
		int num_closest_update;
		int num_warps_update;
		int noise_update;
		float angle_update;
		float shift_update;
		float scale_update;

		// parameters for negative examples
		float num_bad_patches;
		
		
		float tracker_stability_FBerrMax;
		
		protected ParamsTld(){
			super(null);		
		}
		
		public ParamsTld(Properties props) {
			super(props);
			
			// Bounding Box Parameters
			min_win = getInt("min_win");
			
			// Generator Parameters
			// initial parameters for positive examples
			patch_size = getInt("patch_size");
			num_closest_init = getInt("num_closest_init");
			num_warps_init = getInt("num_warps_init");
			noise_init = getInt("noise_init");
			angle_init = getFloat("angle_init");
			shift_init = getFloat("shift_init");
			scale_init = getFloat("scale_init");
			// update parameters for positive examples
			num_closest_update = getInt("num_closest_update");
			num_warps_update = getInt("num_warps_update");
			noise_update = getInt("noise_update");
			angle_update = getFloat("angle_update");
			shift_update = getFloat("shift_update");
			scale_update = getFloat("scale_update");
			// parameters for negative examples
			num_bad_patches = getInt("num_bad_patches");
			
			tracker_stability_FBerrMax = getFloat("tracker_stability_FBerrMax");
		}	
	}	
	
	
	

	public Parameters(Properties props) {
		this.props = props;
	}
	
	protected int getInt(String propName){
		if(props.containsKey(propName)){
			return Integer.valueOf(props.getProperty(propName));
		}
		
		throw new IllegalArgumentException("Parameter " + propName + " has NOT been provided.");
	}
	
	protected float getFloat(String propName){
		if(props.containsKey(propName)){
			return Float.valueOf(props.getProperty(propName));
		}
		
		throw new IllegalArgumentException("Parameter " + propName + " has NOT been provided.");
	}	
	
	protected float getFloat(String propName, float defaultValue){
		if(props.containsKey(propName)){
			return Float.valueOf(props.getProperty(propName));
		}
		
		return defaultValue;
	}
}

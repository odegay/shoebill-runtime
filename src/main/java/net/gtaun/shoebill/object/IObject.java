/**
 * Copyright (C) 2011 MK124
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

package net.gtaun.shoebill.object;

import net.gtaun.shoebill.data.Location;
import net.gtaun.shoebill.data.LocationRotational;
import net.gtaun.shoebill.util.event.IEventDispatcher;

/**
 * @author MK124
 *
 */

public interface IObject extends IDestroyable
{
	IEventDispatcher getEventDispatcher();
	
	int getId();
	int getModel();
	float getSpeed();
	float getDrawDistance();
	
	IPlayer getAttachedPlayer();
	IObject getAttachedObject();
	IVehicle getAttachedVehicle();
	
	LocationRotational getPosition();
	void setPosition( Location position );
	void setPosition( LocationRotational position );
	void setRotate( float rx, float ry, float rz );

	boolean isMoving();
	int move( float x, float y, float z, float speed );
	void stop();
	
	void attach( IPlayer player, float x, float y, float z, float rx, float ry, float rz );
	void attach( IObject object, float x, float y, float z, float rx, float ry, float rz, boolean syncRotation );
	void attach( IVehicle vehicle, float x, float y, float z, float rx, float ry, float rz );
}

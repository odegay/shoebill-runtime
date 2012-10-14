/**
 * Copyright (C) 2011 MK124
 * Copyright (C) 2011 JoJLlmAn
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

package net.gtaun.shoebill.object.impl;

import net.gtaun.shoebill.SampObjectPoolImpl;
import net.gtaun.shoebill.ShoebillImpl;
import net.gtaun.shoebill.data.Location;
import net.gtaun.shoebill.data.Point3D;
import net.gtaun.shoebill.exception.CreationFailedException;
import net.gtaun.shoebill.object.primitive.ObjectPrim;
import net.gtaun.shoebill.object.primitive.PlayerPrim;
import net.gtaun.shoebill.object.primitive.VehiclePrim;
import net.gtaun.shoebill.samp.SampNativeFunction;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * @author MK124, JoJLlmAn
 *
 */

public class ObjectImpl implements ObjectPrim
{	
	private int id = INVALID_ID;
	private int modelId;
	private Location location;
	private float speed = 0.0F;
	private float drawDistance = 0.0F;

	@SuppressWarnings("unused")
	private Point3D attachedOffset;
	@SuppressWarnings("unused")
	private Point3D attachedRotate;
	
	private PlayerPrim attachedPlayer;
	private ObjectPrim attachedObject;
	private VehiclePrim attachedVehicle;
	
	
	public ObjectImpl( int modelId, float x, float y, float z, float rx, float ry, float rz ) throws CreationFailedException
	{
		initialize( modelId, new Location(x, y, z), new Point3D(rx, ry, rz), drawDistance );
	}
	
	public ObjectImpl( int modelId, float x, float y, float z, float rx, float ry, float rz, float drawDistance ) throws CreationFailedException
	{
		initialize( modelId, new Location(x, y, z), new Point3D(rx, ry, rz), drawDistance );
	}
	
	public ObjectImpl( int modelId, Location loc, float rx, float ry, float rz ) throws CreationFailedException
	{
		initialize( modelId, new Location(loc), new Point3D(rx, ry, rz), drawDistance );
	}
	
	public ObjectImpl( int modelId, Location loc, float rx, float ry, float rz, float drawDistance) throws CreationFailedException
	{
		initialize( modelId, new Location(loc), new Point3D(rx, ry, rz), drawDistance );
	}
	
	public ObjectImpl( int modelId, Location loc, Point3D rot ) throws CreationFailedException
	{
		initialize( modelId, new Location(loc), new Point3D(rot), drawDistance );
	}
	
	public ObjectImpl( int modelId, Location loc, Point3D rot, float drawDistance ) throws CreationFailedException
	{
		initialize( modelId, new Location(loc), new Point3D(rot), drawDistance );
	}
	
	private void initialize( int modelId, Location loc, Point3D rot, float drawDistance ) throws CreationFailedException
	{
		this.modelId = modelId;
		this.location = loc;
		this.drawDistance = drawDistance;
		
		id = SampNativeFunction.createObject( modelId, loc.getX(), loc.getY(), loc.getZ(), rot.getX(), rot.getY(), rot.getZ(), drawDistance );
		if( id == INVALID_ID ) throw new CreationFailedException();
		
		SampObjectPoolImpl pool = (SampObjectPoolImpl) ShoebillImpl.getInstance().getManagedObjectPool();
		// pool.setObject( id, this );
	}
	
	public void processObjectMoved()
	{
		speed = 0.0F;
		SampNativeFunction.getObjectPos( id, location );
	}

	@Override
	public String toString()
	{
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}
	
	@Override
	public void destroy()
	{
		if( isDestroyed() ) return;
		
		SampNativeFunction.destroyObject( id );

		SampObjectPoolImpl pool = (SampObjectPoolImpl) ShoebillImpl.getInstance().getManagedObjectPool();
		pool.setObject( id, null );
		
		id = INVALID_ID;
	}
	
	@Override
	public boolean isDestroyed()
	{
		return id == INVALID_ID;
	}

	@Override
	public int getId()
	{
		return id;
	}
	
	@Override
	public int getModelId()
	{
		return modelId;
	}
	
	@Override
	public float getSpeed()
	{
		if( isDestroyed() ) return 0.0F;
		
		if( attachedPlayer != null && attachedPlayer.isOnline() )				return attachedPlayer.getVelocity().speed3d();
		if( attachedObject != null && attachedObject.isDestroyed() == false )	return attachedObject.getSpeed();
		if( attachedVehicle != null && attachedVehicle.isDestroyed() == false )	return attachedVehicle.getVelocity().speed3d();
		
		return speed;
	}
	
	@Override
	public float getDrawDistance()
	{
		return drawDistance;
	}
	
	@Override
	public PlayerPrim getAttachedPlayer()
	{
		return attachedPlayer;
	}
	
	@Override
	public ObjectPrim getAttachedObject()
	{
		return attachedObject;
	}
	
	@Override
	public VehiclePrim getAttachedVehicle()
	{
		return attachedVehicle;
	}
	
	@Override
	public Location getLocation()
	{
		if( isDestroyed() ) return null;
		
		if( attachedPlayer != null )			location.set( attachedPlayer.getLocation() );
		else if( attachedVehicle != null )		location.set( attachedVehicle.getLocation() );
		else									SampNativeFunction.getObjectPos( id, location );
		
		return location.clone();
	}
	
	@Override
	public void setLocation( Point3D pos )
	{
		if( isDestroyed() ) return;
		
		speed = 0.0F;
		location.set( pos );
		SampNativeFunction.setObjectPos( id, pos.getX(), pos.getY(), pos.getZ() );
	}
	
	@Override
	public void setLocation( Location loc )
	{
		if( isDestroyed() ) return;

		speed = 0.0F;
		location.set( loc );
		SampNativeFunction.setObjectPos( id, loc.getX(), loc.getY(), loc.getZ() );
	}
	
	@Override
	public Point3D getRotate()
	{
		if( isDestroyed() ) return null;
	
		Point3D rotate = new Point3D();
		SampNativeFunction.getObjectRot( id, rotate );
		
		return rotate;
	}
	
	@Override
	public void setRotate( float rx, float ry, float rz )
	{
		if( isDestroyed() ) return;
		
		SampNativeFunction.setObjectRot( id, rx, ry, rz );
	}
	
	@Override
	public void setRotate( Point3D rot )
	{
		setRotate( rot.getX(), rot.getY(), rot.getZ() );
	}
	
	@Override
	public boolean isMoving()
	{
		if( isDestroyed() ) return false;
		return SampNativeFunction.isObjectMoving( id );
	}

	@Override
	public int move( float x, float y, float z, float speed )
	{
		if( isDestroyed() ) return 0;
		
		if(attachedPlayer == null && attachedVehicle == null) this.speed = speed;
		return SampNativeFunction.moveObject( id, x, y, z, speed, -1000.0f, -1000.0f, -1000.0f );
	}
	
	@Override
	public int move( float x, float y, float z, float speed, float rotX, float rotY, float rotZ )
	{
		if( isDestroyed() ) return 0;
		
		if(attachedPlayer == null && attachedVehicle == null) this.speed = speed;
		return SampNativeFunction.moveObject( id, x, y, z, speed, rotX, rotY, rotZ );
	}
	
	@Override
	public int move( Point3D pos, float speed )
	{
		return move( pos.getX(), pos.getY(), pos.getZ(), speed );
	}
	
	@Override
	public int move( Point3D pos, float speed, Point3D rot )
	{
		return move( pos.getX(), pos.getY(), pos.getZ(), speed, rot.getX(), rot.getY(), rot.getZ() );
	}
	
	@Override
	public void stop()
	{
		if( isDestroyed() ) return;
		
		speed = 0.0F;
		SampNativeFunction.stopObject( id );
	}

	@Override
	public void attach( PlayerPrim player, float x, float y, float z, float rx, float ry, float rz )
	{
		if( isDestroyed() ) return;
		if( player.isOnline() == false ) return;
		
		SampNativeFunction.attachObjectToPlayer( id, player.getId(), x, y, z, rx, ry, rz );

		speed = 0.0F;
		attachedOffset = new Point3D( x, y, z );
		attachedRotate = new Point3D( rx, ry, rz );
		
		attachedPlayer = player;
		attachedObject = null;
		attachedVehicle = null;
	}

	@Override
	public void attach( PlayerPrim player, Point3D pos, Point3D rot )
	{
		attach( player, pos.getX(), pos.getY(), pos.getZ(), rot.getX(), rot.getY(), rot.getZ() );
	}

	@Override
	public void attach( ObjectPrim object, float x, float y, float z, float rx, float ry, float rz, boolean syncRotation )
	{
		if( isDestroyed() ) return;
		if( object.isDestroyed() ) return;
		
		if( object instanceof PlayerObjectImpl ) throw new UnsupportedOperationException();
		SampNativeFunction.attachObjectToObject( id, object.getId(), x, y, z, rz, ry, rz, syncRotation?1:0 );

		speed = 0.0F;
		attachedOffset = new Point3D( x, y, z );
		attachedRotate = new Point3D( rx, ry, rz );
		
		attachedPlayer = null;
		attachedObject = object;
		attachedVehicle = null;
	}
	

	@Override
	public void attach( ObjectPrim object, Point3D pos, Point3D rot, boolean syncRotation )
	{
		attach( object, pos.getX(), pos.getY(), pos.getZ(), rot.getX(), rot.getY(), rot.getZ(), syncRotation );
	}
	
	@Override
	public void attach( VehiclePrim vehicle, float x, float y, float z, float rx, float ry, float rz )
	{
		if( isDestroyed() ) return;
		if( vehicle.isDestroyed() ) return;
		
		SampNativeFunction.attachObjectToVehicle( id, vehicle.getId(), x, y, z, rx, ry, rz );

		speed = 0.0F;
		attachedOffset = new Point3D( x, y, z );
		attachedRotate = new Point3D( rx, ry, rz );
		
		attachedPlayer = null;
		attachedObject = null;
		attachedVehicle = vehicle;
	}
	
	@Override
	public void attach( VehiclePrim vehicle, Point3D pos, Point3D rot )
	{
		attach( vehicle, pos.getX(), pos.getY(), pos.getZ(), rot.getX(), rot.getY(), rot.getZ() );
	}
}

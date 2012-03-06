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

import java.util.Collection;

import net.gtaun.shoebill.SampObjectPool;
import net.gtaun.shoebill.Shoebill;
import net.gtaun.shoebill.data.Color;
import net.gtaun.shoebill.data.Location;
import net.gtaun.shoebill.exception.CreationFailedException;
import net.gtaun.shoebill.samp.SampNativeFunction;

/**
 * @author MK124
 *
 */

public class Label implements ILabel
{
	static final int INVALID_ID =				0xFFFF;
	
	
	public static Collection<ILabel> get()
	{
		return Shoebill.getInstance().getManagedObjectPool().getLabels();
	}
	
	public static <T extends ILabel> Collection<T> get( Class<T> cls )
	{
		return Shoebill.getInstance().getManagedObjectPool().getLabels( cls );
	}
	
	
	private int id = INVALID_ID;
	private String text;
	private Color color;
	private Location location;
	private float drawDistance;
	private boolean testLOS;
	
	private float offsetX, offsetY, offsetZ;
	private IPlayer attachedPlayer;
	private IVehicle attachedVehicle;
	
	
	@Override public int getId()								{ return id; }
	@Override public String getText()							{ return text; }
	@Override public Color getColor()							{ return color.clone(); }
	@Override public float getDrawDistance()					{ return drawDistance; }
	@Override public IPlayer getAttachedPlayer()				{ return attachedPlayer; }
	@Override public IVehicle getAttachedVehicle()				{ return attachedVehicle; }
	
	
	public Label( String text, Color color, Location location, float drawDistance, boolean testLOS ) throws CreationFailedException
	{
		if( text == null ) throw new NullPointerException();
		
		this.text = text;
		this.color = color.clone();
		this.location = location.clone();
		this.drawDistance = drawDistance;
		this.testLOS = testLOS;
		
		initialize();
	}
	
	private void initialize() throws CreationFailedException
	{
		id = SampNativeFunction.create3DTextLabel( text, color.getValue(), location.getX(), location.getY(), location.getZ(), drawDistance, location.getWorldId(), testLOS );
		if( id == INVALID_ID ) throw new CreationFailedException();
		
		SampObjectPool pool = (SampObjectPool) Shoebill.getInstance().getManagedObjectPool();
		pool.setLabel( id, this );
	}
	
	
	@Override
	public void destroy()
	{
		if( isDestroyed() ) return;
		
		SampNativeFunction.delete3DTextLabel( id );
		
		SampObjectPool pool = (SampObjectPool) Shoebill.getInstance().getManagedObjectPool();
		pool.setLabel( id, null );

		id = INVALID_ID;
	}
	
	@Override
	public boolean isDestroyed()
	{
		return id == INVALID_ID;
	}

	@Override
	public Location getLocation()
	{
		if( isDestroyed() ) return null;
		
		Location loc = null;
		
		if( attachedPlayer != null )	loc = attachedPlayer.getLocation();
		if( attachedVehicle != null )	loc = attachedVehicle.getLocation();
		
		if( loc != null )
		{
			location.setX( loc.getX() + offsetX );
			location.setY( loc.getY() + offsetY );
			location.setZ( loc.getZ() + offsetZ );
			location.setInteriorId( loc.getInteriorId() );
			location.setWorldId( loc.getWorldId() );
		}
		
		return location.clone();
	}

	@Override
	public void attach( IPlayer player, float x, float y, float z )
	{
		if( isDestroyed() ) return;
		if( player.isOnline() == false ) return;
		
		offsetX = x;
		offsetY = y;
		offsetZ = z;
		
		SampNativeFunction.attach3DTextLabelToPlayer( id, player.getId(), x, y, z );
		attachedPlayer = player;
		attachedVehicle = null;
	}

	@Override
	public void attach( IVehicle vehicle, float x, float y, float z )
	{
		if( isDestroyed() ) return;
		if( vehicle.isDestroyed() ) return;
		
		offsetX = x;
		offsetY = y;
		offsetZ = z;
		
		SampNativeFunction.attach3DTextLabelToVehicle( id, vehicle.getId(), x, y, z );
		attachedPlayer = null;
		attachedVehicle = vehicle;
	}

	@Override
	public void update( Color color, String text )
	{
		if( isDestroyed() ) return;
		if( text == null ) throw new NullPointerException();
		
		this.color = color.clone();
		this.text = text;
		
		SampNativeFunction.update3DTextLabelText( id, color.getValue(), text );
	}
}

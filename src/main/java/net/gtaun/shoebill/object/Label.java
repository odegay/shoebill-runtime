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

import java.util.Vector;

import net.gtaun.shoebill.SampNativeFunction;
import net.gtaun.shoebill.data.Color;
import net.gtaun.shoebill.data.Point;
import net.gtaun.shoebill.data.PointAngle;
import net.gtaun.shoebill.data.PointRange;

/**
 * @author MK124
 *
 */

public class Label implements IDestroyable
{
	public static Vector<Label> get()
	{
		return Gamemode.getInstances(Gamemode.instance.labelPool, Label.class);
	}
	
	public static <T> Vector<T> get( Class<T> cls )
	{
		return Gamemode.getInstances(Gamemode.instance.labelPool, cls);
	}
	
	
	int id = -1;
	String text;
	Color color;
	PointRange position;
	boolean testLOS;
	
	float offsetX, offsetY, offsetZ;
	Player attachedPlayer;
	Vehicle attachedVehicle;

	public int getId()						{ return id; } public String getText()					{ return text; } public Color getColor()					{ return color.clone(); }
	public Player getAttachedPlayer()		{ return attachedPlayer; }
	public Vehicle getAttachedVehicle()	{ return attachedVehicle; }
	

	Label()
	{
		
	}
	
	public Label( String text, Color color, Point point, float drawDistance, boolean testLOS )
	{
		if( text == null ) throw new NullPointerException();
		
		this.text = text;
		this.color = color.clone();
		this.position = new PointRange( point, drawDistance );
		this.testLOS = testLOS;
		
		init();
	}
	
	public Label( String text, Color color, PointRange point, boolean testLOS )
	{
		if( text == null ) throw new NullPointerException();
		
		this.text = text;
		this.color = color.clone();
		this.position = point.clone();
		this.testLOS = testLOS;
		
		init();
	}
	
	private void init()
	{
		id = SampNativeFunction.create3DTextLabel( text, color.getValue(),
				position.x, position.y, position.z, position.distance, position.world, testLOS );
		
		Gamemode.instance.labelPool[id] = this;
	}
	
//---------------------------------------------------------
	
	@Override
	public void destroy()
	{
		SampNativeFunction.delete3DTextLabel( id );
		Gamemode.instance.labelPool[ id ] = null;
		
		id = -1;
	}
	
	@Override
	public boolean isDestroyed()
	{
		return id == -1;
	}

	public PointRange getPosition()
	{
		PointAngle pos = null;
		
		if( attachedPlayer != null )	pos = attachedPlayer.position;
		if( attachedVehicle != null )	pos = attachedVehicle.getPosition();
		
		if( pos != null )
		{
			position.x = pos.x + offsetX;
			position.y = pos.y + offsetY;
			position.z = pos.z + offsetZ;
			position.interior = pos.interior;
			position.world = pos.world;
		}
		
		return position.clone();
	}

	public void attach( Player player, float x, float y, float z )
	{
		offsetX = x;
		offsetY = y;
		offsetZ = z;
		
		SampNativeFunction.attach3DTextLabelToPlayer( id, player.id, x, y, z );
		attachedPlayer = player;
		attachedVehicle = null;
	}

	public void attach( Vehicle vehicle, float x, float y, float z )
	{
		offsetX = x;
		offsetY = y;
		offsetZ = z;
		
		SampNativeFunction.attach3DTextLabelToVehicle( id, vehicle.id, x, y, z );
		attachedPlayer = null;
		attachedVehicle = vehicle;
	}

	public void update( Color color, String text )
	{
		if( text == null ) throw new NullPointerException();
		
		this.color = color.clone();
		this.text = text;
		
		SampNativeFunction.update3DTextLabelText( id, color.getValue(), text );
	}
}

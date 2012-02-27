/**
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

package net.gtaun.shoebill.data;

import java.io.Serializable;

/**
 * @author JoJLlmAn
 *
 */

public class Vector2D implements Cloneable, Serializable
{
	private static final long serialVersionUID = 3303330394405245831L;
	
	
	public float x, y;
	
	
	public Vector2D()
	{
		
	}
	
	public Vector2D( float x, float y )
	{
		this.x = x;
		this.y = y;
	}
	
	@Override
	public boolean equals( Object obj )
	{
		if( obj == this )						return true;
		if( obj instanceof Vector2D == false )	return false;
		
		Vector2D vector2d = (Vector2D) obj;
		
		if( vector2d.x != x )	return false;
		if( vector2d.y != y )	return false;
		
		return true;
	}
	
	@Override
	public Vector2D clone()
	{
		return new Vector2D( x, y );
	}
	
}

package ma.util;

import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public class Hitbox
{
	public static boolean Entity_In_Box(Entity _entity, Vector _min, Vector _max)
	{ return Entity_In_Box(_entity, _min, _max, 0.0); }
	public static boolean Entity_In_Box(Entity _entity, Vector _min, Vector _max, double _range)
	{
		Vector loc = _entity.getLocation().toVector();
		Vector min = new Vector(
				Math.min(_min.getX(), _max.getX()),
				Math.min(_min.getY(), _max.getY()),
				Math.min(_min.getZ(), _max.getZ()));
		Vector max = new Vector(
				Math.max(_min.getX(), _max.getX()),
				Math.max(_min.getY(), _max.getY()),
				Math.max(_min.getZ(), _max.getZ()));
		
		if (min.getX() - _range <= loc.getX() && loc.getX() <= max.getX() + _range
		&& 	min.getY() - _range <= loc.getY() && loc.getY() <= max.getY() + _range
		&& 	min.getZ() - _range <= loc.getZ() && loc.getZ() <= max.getZ() + _range )
		{
			return true;
		}
		return false;
	}
}

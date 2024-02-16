package de.exentra.explore.basic.importer.pojo;

import java.util.ArrayList;
import java.util.List;

public class Knot
{
	Node node;
	List<Knot> children;

	public Knot(Node node)
	{
		this.node = node;
		this.children = new ArrayList<>();
	}

	public List<Knot> getChildren()
	{
		return children;
	}

	public Node getNode()
	{
		return node;
	}
}

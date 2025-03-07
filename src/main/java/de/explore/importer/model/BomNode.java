package de.explore.importer.model;

import java.util.ArrayList;
import java.util.List;

public class BomNode
{
	private BomItem item;
	private List<BomNode> children = new ArrayList<>();

	public BomNode(BomItem item)
	{
		this.item = item;
	}

	public BomItem getItem()
	{
		return item;
	}

	public void setItem(BomItem item)
	{
		this.item = item;
	}

	public List<BomNode> getChildren()
	{
		return children;
	}

	public void setChildren(List<BomNode> children)
	{
		this.children = children;
	}

	public void addChild(BomNode child)
	{
		children.add(child);
	}

	public void print(String prefix)
	{
		System.out.println(prefix + item);
		for (BomNode child : children)
		{
			child.print(prefix + " ");
		}
	}
}

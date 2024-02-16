package de.exentra.explore.basic.importer.pojo;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class Node
{
	String pname;
	String pnguid;
	String pvname;

	String bzw;

	String pntype;

	@SerializedName("name")
	String name;

	@SerializedName("pnguid_parent")
	String pnguidParent;

	@SerializedName("node_adapters")
	List<NodeAdapter> nodeAdapter;
	public String getPname()
	{
		return pname;
	}

	public String getBzw()
	{
		return bzw;
	}

	public String getPnguid()
	{
		return pnguid;
	}

	public String getPvname()
	{
		return pvname;
	}

	public String getPnguidParent()
	{
		return pnguidParent;
	}

	public String getName()
	{
		return name;
	}

	public String getPntype()
	{
		return pntype;
	}

	public List<NodeAdapter> getNodeAdapters()
	{
		return nodeAdapter;
	}

	@Override
	public String toString()
	{
		return "Node{" +
			"pname='" + pname + '\'' +
			", pnguid='" + pnguid + '\'' +
			", pvname='" + pvname + '\'' +
			", bzw='" + bzw + '\'' +
			", pntype='" + pntype + '\'' +
			", name='" + name + '\'' +
			", pnguidParent='" + pnguidParent + '\'' +
			", nodeAdapter=" + nodeAdapter +
			'}';
	}
}

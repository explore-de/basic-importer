package de.exentra.explore.basic.importer.pojo;

import com.google.gson.annotations.SerializedName;

public class NodeAdapter
{
	@SerializedName("adapter_name")
	String adapterName;
	String pntype;
	@SerializedName("matrix")
	String matrix;
	@SerializedName("matrix_x")
	Double matrixX;
	@SerializedName("matrix_xx")
	Double matrixXx;
	@SerializedName("matrix_xy")
	Double matrixXy;
	@SerializedName("matrix_xz")
	Double matrixXz;

	@SerializedName("matrix_y")
	Double matrixY;
	@SerializedName("matrix_yx")
	Double matrixYx;
	@SerializedName("matrix_yy")
	Double matrixYy;
	@SerializedName("matrix_yz")
	Double matrixYz;

	@SerializedName("matrix_z")
	Double matrixZ;
	@SerializedName("matrix_zx")
	Double matrixZx;
	@SerializedName("matrix_zy")
	Double matrixZy;
	@SerializedName("matrix_zz")
	Double matrixZz;

	@SerializedName("bzw")
	String bzw;

	@SerializedName("CatPart")
	String catPart;

	public String getAdapterName()
	{
		return adapterName;
	}

	public String getPntype()
	{
		return pntype;
	}

	public String getMatrix()
	{
		return matrix;
	}

	public Double getMatrixX()
	{
		return matrixX;
	}

	public Double getMatrixXx()
	{
		return matrixXx;
	}

	public Double getMatrixXy()
	{
		return matrixXy;
	}

	public Double getMatrixXz()
	{
		return matrixXz;
	}

	public Double getMatrixY()
	{
		return matrixY;
	}

	public Double getMatrixYx()
	{
		return matrixYx;
	}

	public Double getMatrixYy()
	{
		return matrixYy;
	}

	public Double getMatrixYz()
	{
		return matrixYz;
	}

	public Double getMatrixZ()
	{
		return matrixZ;
	}

	public Double getMatrixZx()
	{
		return matrixZx;
	}

	public Double getMatrixZy()
	{
		return matrixZy;
	}

	public Double getMatrixZz()
	{
		return matrixZz;
	}

	public String getBzw()
	{
		return bzw;
	}

	public String getCatPart()
	{
		return catPart;
	}
}

package de.explore.importer.model;

import java.util.Objects;

public class BomItem
{
	private int level;
	private String partId;
	private String component;
	private String description;
	private int quantity;
	private String material;
	private String supplier;
	private double unitCost;
	private double totalCost;
	private String model3D;

	public BomItem()
	{
	}

	public BomItem(int level, String partId, String component, String description, int quantity, String material, String supplier, double unitCost, double totalCost, String model3D)
	{
		this.level = level;
		this.partId = partId;
		this.component = component;
		this.description = description;
		this.quantity = quantity;
		this.material = material;
		this.supplier = supplier;
		this.unitCost = unitCost;
		this.totalCost = totalCost;
		this.model3D = model3D;
	}

	public int getLevel()
	{
		return level;
	}

	public void setLevel(int level)
	{
		this.level = level;
	}

	public String getPartId()
	{
		return partId;
	}

	public void setPartId(String partId)
	{
		this.partId = partId;
	}

	public String getComponent()
	{
		return component;
	}

	public void setComponent(String component)
	{
		this.component = component;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public int getQuantity()
	{
		return quantity;
	}

	public void setQuantity(int quantity)
	{
		this.quantity = quantity;
	}

	public String getMaterial()
	{
		return material;
	}

	public void setMaterial(String material)
	{
		this.material = material;
	}

	public String getSupplier()
	{
		return supplier;
	}

	public void setSupplier(String supplier)
	{
		this.supplier = supplier;
	}

	public double getUnitCost()
	{
		return unitCost;
	}

	public void setUnitCost(double unitCost)
	{
		this.unitCost = unitCost;
	}

	public double getTotalCost()
	{
		return totalCost;
	}

	public void setTotalCost(double totalCost)
	{
		this.totalCost = totalCost;
	}

	public String getModel3D()
	{
		return model3D;
	}

	public void setModel3D(String model3D)
	{
		this.model3D = model3D;
	}

	@Override
	public boolean equals(Object o)
	{
		if (o == null || getClass() != o.getClass()) return false;
		BomItem bomItem = (BomItem)o;
		return level == bomItem.level && quantity == bomItem.quantity && Double.compare(unitCost, bomItem.unitCost) == 0 && Double.compare(totalCost, bomItem.totalCost) == 0 && Objects.equals(partId, bomItem.partId) && Objects.equals(component, bomItem.component) && Objects.equals(description, bomItem.description) && Objects.equals(material, bomItem.material) && Objects.equals(supplier, bomItem.supplier) && Objects.equals(model3D, bomItem.model3D);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(level, partId, component, description, quantity, material, supplier, unitCost, totalCost, model3D);
	}

	@Override
	public String toString()
	{
		return "BomItem{" +
			"level=" + level +
			", partId='" + partId + '\'' +
			", component='" + component + '\'' +
			", description='" + description + '\'' +
			", quantity=" + quantity +
			", material='" + material + '\'' +
			", supplier='" + supplier + '\'' +
			", unitCost=" + unitCost +
			", totalCost=" + totalCost +
			", model3D='" + model3D + '\'' +
			'}';
	}
}

package de.explore.importer.service;

import de.explore.importer.model.BomItem;
import de.explore.importer.model.BomNode;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.BadRequestException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

@ApplicationScoped
public class CsvService
{
	public List<BomNode> parseCSV(byte[] csvData)
	{
		try (
			ByteArrayInputStream bais = new ByteArrayInputStream(csvData);
			InputStreamReader isr = new InputStreamReader(bais))
		{
			CSVFormat format = CSVFormat.DEFAULT.builder()
				.setHeader("Level", "Part ID", "Component", "Description", "Quantity", "Material", "Supplier", "Unit Cost", "Total Cost", "3D Modell")
				.setSkipHeaderRecord(true)
				.setDelimiter(',')
				.build();

			Iterable<CSVRecord> records = format.parse(isr);

			List<BomNode> roots = new ArrayList<>();
			Deque<BomNode> stack = new ArrayDeque<>();

			records.forEach(csvRecord -> {
				int level = Integer.parseInt(csvRecord.get("Level"));
				String partId = csvRecord.get("Part ID");
				String component = csvRecord.get("Component");
				String description = csvRecord.get("Description");
				String quantity = csvRecord.get("Quantity");
				String material = csvRecord.get("Material");
				String supplier = csvRecord.get("Supplier");
				String unitCost = csvRecord.get("Unit Cost");
				String totalCost = csvRecord.get("Total Cost");
				String model3D = csvRecord.get("3D Modell");

				BomItem item = new BomItem(level, partId, component, description, Integer.parseInt(quantity), material, supplier, Double.parseDouble(unitCost), Double.parseDouble(totalCost), model3D);
				BomNode node = new BomNode(item);

				while (!stack.isEmpty() && stack.getFirst().getItem().getLevel() >= level)
				{
					stack.removeFirst();
				}

				if (stack.isEmpty())
				{
					// No parent node found → this node is a root
					roots.add(node);
				}
				else
				{
					// Add the node as a child of the current top node
					stack.getFirst().addChild(node);
				}

				// Push the current node onto the stack
				stack.addFirst(node);
			});

			return roots;
		}
		catch (IOException e)
		{
			throw new BadRequestException(e);
		}
	}
}

package de.tub.pes.syscir.analysis.syscdg.export;

import java.util.Scanner;

import de.tub.pes.syscir.analysis.dependencies.BinarySecurityLevel;
import de.tub.pes.syscir.analysis.syscdg.SystemDependenceGraph;
import de.tub.pes.syscir.analysis.syscdg.edge.SdgEdgeData;
import de.tub.pes.syscir.analysis.syscdg.node.SdgNodeData;

public class App {

	public static void main(String[] args) {
		try (Scanner scanner = new Scanner(System.in)) {
			System.out.print("Absolute path to XML file: ");
			String xmlPath = scanner.nextLine().trim();

			System.out.print("Absolute path to destination folder: ");
			String destinationPath = scanner.nextLine().trim();
			
			System.out.print("Name of the json-file: ");
			String jsonFileName = scanner.nextLine().trim();

			SystemDependenceGraph<BinarySecurityLevel, SdgNodeData<?>, SdgEdgeData> sdg =
					SdgGenerator.generate(xmlPath);
			SdgJsonExporter.exportToJson(sdg, destinationPath + "/"+ jsonFileName +".json");

			System.out.println("JSON exported to: " + destinationPath + "/" + jsonFileName);
		}
	}
}
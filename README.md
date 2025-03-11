# Example importer service

This project showcases how to import a basic BOM structure into the explore 
platform. It sends the pre-built BOM tree structure to a Kafka topic, where 
it will be consumed and imported by the explore platform.

## Introduction

### Purpose
This howto aims to provide external developers with a practical example demonstrating how to prepare data and upload it to 
the explore platform using the importer-client library within a Java service built with Quarkus.
For more information on Quarkus, please refer to the [Quarkus website](https://quarkus.io/).

### Target Audience
This documentation is intended for developers, software engineers and technical professionals 
who are looking to integrate the importer-client library into their Java services.

### Requirements

Before starting, ensure that you have the following tools installed and configured on your system:

- **Java**: JDK 21 or later is recommended.
- **Git**: For version control and repository management.
- **Maven**: For building the project and managing dependencies.
- **Quarkus CLI (optional)**: For running the application in development mode.

## Overview of the Example Service

The example service is designed to create a hierarchical tree representation of a train based on input CSV data. It processes files that list various train components, organizes them according to their levels, and transforms the raw data into a format suitable for import into the Explore platform via the client.

### Architecture Overview

![Architecture Diagram](import-service-architecture.svg "Architecture Diagram")

The architecture of the importer service is designed to seamlessly integrate data from a source system into the Explore platform. 
The following diagram illustrates the high-level flow and key components of the system:

1. **Data Ingestion from Source System**  
   The service receives data from an external source system. This data includes:
   - A CSV file containing structured BOM data.
   - Accompanying 3D CAD files.

2. **Data Processing and Transformation**  
   The service processes the incoming data by:
   - Extracting and parsing the CSV file.
   - Transforming the CSV data into a hierarchical tree structure, adhering to an AP-242-like format.
   - Preparing the 3D CAD files for storage.

3. **Data Distribution via Kafka**  
   Once the hierarchical tree is constructed, the service publishes the tree data to a Kafka topic. This asynchronous communication channel allows the PLM system to consume and process the data efficiently.

4. **Storing 3D CAD Files in Object Storage**  
   The accompanying 3D CAD files are uploaded to an object storage solution (MinIO), ensuring scalable and reliable storage for large binary assets.

#### Key Components

- **Importer Service**: Orchestrates the entire data ingestion, transformation, and distribution process.
- **Source System**: The origin of the data to be imported.
- **Kafka Topic**: Serves as the messaging backbone for transmitting the hierarchical tree data to the PLM system.
- **PLM System**: Consumes the structured data from Kafka to integrate into the product lifecycle management process.
- **Object Storage (MinIO)**: Handles the storage of 3D CAD files, offering efficient access and scalability.

### Sample Data Format

The CSV files typically follow this structure:

```csv
Level,Part ID,Component,Description,Quantity,Material,Supplier,Unit Cost,Total Cost,3D Modell
1,TR-001,Zug Gesamt,Kompletter Zug bestehend aus Lokomotive und Wagen,1,Composite/Aluminium/Steel,DeutscheZug GmbH,0,0,CAD_TR-001.cad
2,LO-001,Lokomotive,Diesel- oder Elektroantriebseinheit,1,Steel/Aluminium,LocomotiveWorks,500000,500000,CAD_LO-001.cad
3,LO-001-01,Motorblock,Diesel- oder Elektromotor,1,Steel/Cast Iron,EngineWorks,200000,200000,CAD_LO-001-01.cad
3,LO-001-02,Getriebe,Übersetzungsverhältnis-Optimierer,1,Steel,GearTech,80000,80000,CAD_LO-001-02.cad
3,LO-001-03,Kupplung,Verbindet Motor und Getriebe,1,Steel,ClutchMasters,15000,15000,CAD_LO-001-03.cad
3,LO-001-04,Elektroniksteuerung,Steuert Motorleistung und Energie,1,PCB/Chipset,ElectroControl,25000,25000,CAD_LO-001-04.cad
3,LO-001-05,Kühlaggregat,Motor- und Getriebe-Kühlung,1,Aluminium,CoolTech,12000,12000,CAD_LO-001-05.cad
3,LO-001-06,Bremssystem,Hydraulische Bremsen,1,Steel,BrakeSystems,18000,18000,CAD_LO-001-06.cad
3,LO-001-07,Treibwerksrahmen,Strukturelles Gerüst der Lokomotive,1,Steel,FrameTech,35000,35000,CAD_LO-001-07.cad
3,LO-001-08,Kraftstoffsystem,Tank Leitungen und Filter,1,Composite/Steel,FuelSystems,10000,10000,CAD_LO-001-08.cad
3,LO-001-09,Auspuffsystem,Abgasanlage,1,Steel,ExhaustWorks,8000,8000,CAD_LO-001-09.cad
3,LO-001-10,Elektrisches Bordnetz,Verkabelung und Sicherungen,1,Copper/Plastic,WiringTech,9000,9000,CAD_LO-001-10.cad
3,LO-001-11,Fahrerkabine,Steuer- und Bedienbereich,1,Composite/Glass,CabinWorks,22000,22000,CAD_LO-001-11.cad
3,LO-001-12,Drehzahlregler,Steuert die Motordrehzahl,1,Steel/Electronic,SpeedControl,7000,7000,CAD_LO-001-12.cad
3,LO-001-13,Kühlmittelpumpe,Zirkuliert Kühlmittel,1,Steel/Plastic,PumpTech,4000,4000,CAD_LO-001-13.cad
```

*Note: Actual CSV files might vary slightly in content or structure depending on specific project requirements.*

### Additional Information

The complete `train-bom.csv` file, which contains all the train components data used in this example, can be found in the 
test resources folder of the project. This file is useful for testing and validating the import process.

### Data Transformation: AP-242-Like Format

The raw CSV data must be transformed into a format that aligns with the AP-242 standard.
This process involves mapping the various data fields—such as component hierarchy, part numbers, descriptions, and technical details—to an AP-242-like structure.

**What is AP-242?**  
AP-242, also known as ISO 10303-242, is an international standard for the digital representation and exchange of product data. 
It is widely used in industries such as aerospace and automotive to ensure consistency and interoperability when sharing complex 3D models and associated metadata.

By converting the CSV data into an AP-242-like format, we facilitate seamless integration with systems that expect data to be structured in a standardized, industry-accepted manner.

## Data Processing Workflow

### Using the importer.client Library

For the import of data into the Explore platform, the `importer.client` library is used. 
This library simplifies the process by providing APIs to transform, validate, and transmit the hierarchical data tree to the PLM system.

#### Maven Dependency

To include the library in your project, add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>de.explore</groupId>
    <artifactId>import.client</artifactId>
    <version>${import.client.version}</version>
</dependency>
```

### Import Steps

The example service processes incoming data through the following steps:

1. **Project Verification and Creation**  
   Upon receiving the request, the service checks if a project with the provided name (sent along with the ZIP file) already exists. 
   If it does, that project is used for the current import. Otherwise, a new project is created to serve as a container for all data related to the import session.

2. **Zip Extraction and CSV Parsing**  
   The service exposes a REST endpoint that accepts a ZIP file. This ZIP file contains:
   - A `train-bom.csv` file, which holds the bill of materials (BOM) for the train.
   - Exemplary CAD files arranged in a flat structure.
   
   Once the ZIP file is received, the service extracts its contents and parses the `train-bom.csv` file to read the component data and build an initial data structure.

3. **Building the Hierarchical Tree and Importing Data**  
   Using the importer-client library, the service transforms the parsed CSV data into a hierarchical tree that represents the train's assembly. 
   This structured tree is then sent to the PLM (Product Lifecycle Management) system, ensuring that the imported data conforms to the required standards.
   After constructing the hierarchical tree and completing the import process, the final data is serialized into the Apache Avro format. 
   Apache Avro is a data serialization system that offers a compact, fast, and binary data format ideal for storing and exchanging large volumes of structured data.
   [Apache Avro](https://avro.apache.org/)

4. **Uploading CAD Files**  
   After processing the BOM data, the service uploads the accompanying 3D CAD files to the file service.
   This step ensures that all digital assets are properly stored and linked to the corresponding components in the PLM system.

## Further Documentation

The client library is available on the Explore GitLab repository, where you can find detailed documentation. 
For any additional information or specific inquiries regarding its usage and integration, please reach out to the Explore development team.

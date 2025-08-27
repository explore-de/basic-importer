# Basic Importer Service

> [!IMPORTANT]
> This `README.md` is a copy of the official documentation that is also shipped 
> with your explore instance.

## Introduction and Overview

This documentation outlines how to implement a **basic importer service** using
**Java** and **Quarkus** following the <GlossaryCard term="Ambassador" />
Pattern. It demonstrates how to integrate the provided `importer-client` library
to seamlessly import data into the explore platform.

We recommend **forking** this repository and customizing it to fit your specific
requirements.

The service uses a CSV file as input. This is just an example, but you can
easily adapt it to your own use case, e.g. by using a database or a third party
REST API.

### Target Audience

This documentation is intended for developers, software engineers, and technical
professionals who are looking to integrate the importer-client library into
their Java services.

## System Overview

This chapter provides a high-level overview of the system architecture for our
basic importer service, which is built with Quarkus and leverages the
importer-client library for data importation. 

The architecture of the importer service is designed to seamlessly integrate
data from a source system into the Explore platform. The following diagram
illustrates the high-level flow and key components of the system:

![import-service-architecture.svg](import-service-architecture.svg)

### Data Ingestion from Source System

The service receives data from an external source system. This data includes:

- A CSV file containing structured BOM data.
- Accompanying 3D CAD files.

### Data Processing and Transformation

The service processes the incoming data by:

- Extracting and parsing the CSV file.
- Transforming the CSV data into a hierarchical tree structure, adhering to
  the [explore Data Model](../../concept/Concept/03_data-model.mdx)
- Preparing the 3D CAD files for storage.

### Data Distribution via Kafka

Once the hierarchical tree is constructed, the service **publishes the tree data
to a Kafka topic**. This asynchronous communication channel allows downstream
services of explore to consume the data more efficiently.

You don't need to manage the Kafka connection manually in your code.  
The **explore SDK** handles all necessary Kafka integration, so you can focus
entirely on building and structuring the data. See
[ImportRessource.java](https://github.com/explore-de/basic-importer/blob/1f5716a9c75bad447ed2ba88d32e5c9f8b5777db/src/main/java/de/explore/importer/rest/ImporterResource.java#L77-L85)
for an example. The `de.explore.importer.service.ImportService` does the heavy
lifting.

```Java
// import de.explore.importer.service.ImportService
importService.sendAvroTreeToPlm(avroTreeWrapper, projectSyncObjectBuilder);
```

### Storing 3D CAD Files in Object Storage

The accompanying 3D CAD files are uploaded to an S3 compatible object storage
service. This allows downstream services to access the files directly without
having to download them from the importer service. The SDK takes care, that the
Document objects are created properly (see
[explore Data Model](../../concept/Concept/03_data-model.mdx)).

```Java
// import de.explore.importer.service.ImportService
importService.uploadToFileService(filename, bytes, id);
```

## Setup and Prerequisites

This chapter guides you through the initial setup required to run the sample
service, including all necessary prerequisites and instructions for accessing
the project code hosted in our GitHub repository.

### Prerequisites
Before you begin, ensure that you have the following installed on your development machine:

- Java Development Kit (JDK): Version 21 or later.
- Maven: For managing project dependencies and building the application.
- Git: To clone the repository from GitHub.
- Quarkus CLI (optional): For running the application in development mode.

These tools are essential to compile, build, and run the sample service.

### Cloning the GitHub Repository

The complete source code for this sample implementation is available in our
GitHub repository:
[GitHub Example Importer Service](https://github.com/explore-de/basic-importer).
To clone the repository, execute the following command in your terminal:

```shell
git clone git@github.com:explore-de/basic-importer.git
```

or
```shell
git clone https://github.com/explore-de/basic-importer.git
```

### Environment Setup and Build

After cloning the repository, navigate to the project directory:

```shell
cd basic-importer
```

and build the project with Maven:

```shell
mvn clean install
```

This command compiles the code, runs any tests, and packages the application.
Once the build is successful, you can start the service using Quarkus in
development mode:

```shell
mvn quarkus:dev
```

### Development Environment

While the command-line tools are enough for building and running the service,
you are free to use your preferred Integrated Development Environment
(IDE) such as IntelliJ IDEA.

## Detailed Explanation of the Code

The basic-importer-service is designed to create a hierarchical tree
representation of a train based on input CSV data. It processes files that list
various train components, organizes them according to their levels, and
transforms the raw data into a format suitable for import into the explore
platform via the Java SDK.

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

*Note: Actual CSV files might vary slightly in content or structure depending on
specific project requirements.*

#### Additional Information

The complete `train-bom.csv` file, which contains all the train components data
used in this example, can be found in the test resources folder of the project.
This file is useful for testing and validating the import process.

#### Data Transformation

The raw CSV data must be transformed into a format that aligns with explor's
data model
(see [Data Model](../../concept/Concept/03_data-model.mdx). The client SDK
offers ready-to-use Java classes for this purpose. They all end with
`SyncObjectBuilder` and offer a fluent API.

### Using the Java SDK

For the import of data into the Explore platform,
the [import.client](35_importer-client.md) library is used. This library
simplifies
the process by providing APIs to transform, validate, and transmit the
hierarchical data tree to the PLM system.

:::info

To get access to our Java SDK, you need to configure our **Maven repository**.
Please contact your Customer Success Manager to get your personal credentials.

:::

To include the library in your project, add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>de.explore</groupId>
    <artifactId>import.client</artifactId>
    <version>${import.client.version}</version>
</dependency>
```

The basic-importer-service processes incoming data through the following steps:

##### 1. Project Verification and Creation

Upon receiving the request, the service checks if a project with the provided
name (sent along with the ZIP file) already exists. If it does, that project is
used for the current import. Otherwise, a new project is created to serve as a
container for all data related to the import session.

##### 2. Zip Extraction and CSV Parsing

The service exposes a REST endpoint that accepts a ZIP file. This ZIP file
contains:

- A `train-bom.csv` file, which holds the bill of materials (BOM) for the train.
- Exemplary CAD files arranged in a flat structure.

Once the ZIP file is received, the service extracts its contents and parses the
`train-bom.csv` file to read the component data and build an initial data
structure.

##### 4. Building the Hierarchical Tree and Importing Data

Using the Java SDK, the service transforms the parsed CSV data into a
hierarchical tree that represents the train's assembly. This structured tree is
then sent to the explore system, ensuring that the imported data conforms to the
required data model. After constructing the hierarchical tree and completing the
import process, the final data is serialized into
the <GlossaryCard term="Apache Avro" /> format.

Apache Avro is a data serialization system that offers a compact, fast, and
binary data format ideal for storing and exchanging large volumes of structured
data (see
[Apache Avro](https://avro.apache.org/)).

> [!IMPORTANT]
> The serialization and the sending of the Avro data to Kafka is handled by the
explore SDK. Yet it is important to understand the underlying concepts and how
they are used in the service.

##### 4. Uploading CAD Files

After processing the BOM data, the service uploads the accompanying 3D CAD files
to the file service. This step ensures that all digital assets are properly
stored and linked to the corresponding components in the PLM system.

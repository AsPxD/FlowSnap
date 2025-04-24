# SchemaViz: Advanced ER Diagram Generator

> **SchemaViz** is a powerful, intuitive database visualization tool that transforms complex SQL schemas into interactive, visually appealing diagrams. Designed for database architects, developers, and analysts, this tool bridges the gap between technical database structures and clear visual representations.

An interactive Entity-Relationship (ER) Diagram generator that creates visual database schema diagrams from SQL CREATE TABLE statements.

## Key Features at a Glance

- **SQL to ER Diagram Conversion**: Parse SQL CREATE TABLE statements and automatically generate ER diagrams
- **Interactive Canvas**: Drag entities to rearrange the diagram for better visualization
- **Relationship Visualization**: Automatic detection and display of relationships with proper cardinality (one-to-one, one-to-many, many-to-many)
- **Primary and Foreign Key Highlighting**: Clear visual distinction between primary keys and foreign keys
- **Zoom and Pan**: Easily navigate large diagrams with zoom in/out and panning functionality
- **Auto Layout**: Automatically arrange entities for optimal visualization
- **Export as Image**: Save your diagram as a PNG image file
- **DFD Creation**: Built-in Data Flow Diagram capabilities for visualizing system processes
- **Example SQL**: Use the provided example SQL to see how the application works

## Requirements

- Java 11 or higher
- JavaFX (included with the Maven build)
- Minimum 4GB RAM recommended (8GB for large schemas)
- 100MB free disk space
- Graphics card supporting OpenGL 2.0 or higher
- 1280x720 minimum screen resolution (1920x1080 recommended)

## Installation and Running

### System Compatibility
- Windows 10/11 (64-bit)
- macOS 10.14+ (Mojave or newer)
- Linux with GTK3 (Ubuntu 18.04+, Fedora 30+)

### Using Maven

1. Clone the repository:
```
git clone https://github.com/yourusername/er-diagram-generator.git
cd er-diagram-generator
```

2. Build the project:
```
mvn clean package
```

3. Run the application:
```
mvn javafx:run
```

### Using the JAR file

1. Download the latest JAR file from the releases page
2. Ensure Java 11+ is installed and in your PATH
3. Run the application:
```
java -jar schemaviz-pro-1.0-SNAPSHOT.jar
```

### Installation Troubleshooting
- **JavaFX Missing**: If you encounter "Error: JavaFX runtime components are missing", download JavaFX SDK separately and add to module path
- **Graphics Issues**: Update your graphics drivers if diagrams appear distorted
- **Permission Errors**: On Linux/macOS, ensure execute permissions with `chmod +x schemaviz-pro-1.0-SNAPSHOT.jar`

## Usage

1. Enter SQL CREATE TABLE statements in the text area on the left side
   - Make sure to include PRIMARY KEY and FOREIGN KEY constraints to properly visualize relationships
   - Each CREATE TABLE statement should end with a semicolon (;)

2. Click the "Generate ER Diagram" button to create the diagram

3. Interact with the diagram:
   - Drag entities to reposition them
   - Use the mouse wheel to zoom in/out
   - Use the "Auto Layout" button to arrange entities automatically
   - Use the "Export as Image" button to save the diagram as a PNG file

## Example SQL

Click the "Load Example SQL" button to load a sample database schema with customers, products, orders, and categories.

## Relationship Types

The application detects and displays the following relationship types:

- **One-to-One**: Displayed with vertical bars at both ends
- **One-to-Many**: Displayed with a vertical bar at one end and a crow's foot at the other
- **Many-to-Many**: Displayed with crow's feet at both ends

## Data Flow Diagrams (DFD)

The application now supports interactive Data Flow Diagrams to visualize how data moves through your system:

- **Process Visualization**: Identify and visualize key processes in your system
- **Data Stores**: Show where data is stored throughout your application
- **Data Flow Paths**: Illustrate how data travels between processes and storage
- **Multiple Levels**: Create hierarchical DFDs (Context, Level 0, Level 1, Level 2) to show increasing detail
- **External Entities**: Clearly mark system boundaries and external actors

### Interactive DFD Features

- **Drag & Drop**: Easily position elements by dragging them around the canvas
- **Element Selection**: Click any element to select and edit its properties
- **Direct Editing**: Change names, IDs, and descriptions of any element
- **Zoom Control**: Zoom in/out using Ctrl+MouseWheel or toolbar buttons
- **Context Menus**: Right-click for quick access to element creation options
- **Arrow Connections**: Create data flows between elements with visual arrow indicators
- **Auto Layout**: Automatically arrange diagram elements with a single click
- **Export as Image**: Save your DFD as PNG for documentation and presentations

### Using DFD Features

1. Click the "Switch to DFD Mode" button in the toolbar
2. Use the toolbox to add DFD elements:
   - Processes (circles/rounded rectangles)
   - Data stores (open-ended rectangles)
   - External entities (squares)
   - Data flows (arrows)
3. Label each element by selecting it and editing properties
4. Move elements by dragging them to desired positions
5. Create connections by clicking the "Create Connection" button, then select source and target elements
6. Export your DFD alongside your ER diagram for comprehensive documentation

### Benefits of DFDs

- **System Analysis**: Understand how data transforms as it moves through your system
- **Communication Tool**: Share system design with technical and non-technical stakeholders
- **Identify Bottlenecks**: Spot potential performance issues in data processing
- **Security Assessment**: Map data paths to evaluate potential vulnerabilities

## Troubleshooting

If you encounter any issues:

1. **SQL Parsing Error**: Make sure your SQL statements follow standard SQL syntax and each statement ends with a semicolon
2. **Display Issues**: Try the "Auto Layout" button to reorganize the diagram
3. **Performance Problems**: For very large schemas, try splitting the SQL into smaller parts

## License

This project is licensed under the MIT License - see the LICENSE file for details. 
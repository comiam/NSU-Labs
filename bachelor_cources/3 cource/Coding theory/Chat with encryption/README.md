# Chat
Full Chat with Server and Client side with message encryption

## How to run project
Compile server and app to jar files.

After that excute in terminal next:
- For Client app: `java -jar --module-path "your_path_to_javafx/javafx-sdk-11.0.2/lib" --add-modules=javafx.controls,javafx.fxml ClientSide.jar`
- For ServerApp: `java -jar ServerSide.jar config.conf`. Use `config.conf` from the root.

Use java of version 11 or higher.

## Technologies
In project used:
* Log4J 2.13
* JavaFX 11.0.2
* GSON 2.8.6

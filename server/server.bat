set "PATH_TO_JAVAFX=C:\Users\User\Downloads\openjfx-21.0.3_windows-x64_bin-sdk\javafx-sdk-21.0.3\lib"
set "PATH_TO_JAR=%cd%\target\server-1.0-SNAPSHOT.jar"
java -p %PATH_TO_JAVAFX% --add-modules javafx.controls,javafx.fxml -jar %PATH_TO_JAR%
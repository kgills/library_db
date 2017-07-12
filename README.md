# library_db
Library database.

# Building
ant -f /home/kgills/Workspace/library_db/library_app -Dnb.internal.action.name=run run

# Running
java -jar "/home/kgills/Workspace/library_db/library_app/dist/library_app.jar"

For best results building and running use the included NetBeans project.

# Dependencies
MySQL JDBC
mysql-connector-java-5.1.42-bin.jar

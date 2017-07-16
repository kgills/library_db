# library_db
Library database.

# Building
ant -f library_app -Dnb.internal.action.name=run run

# Running
java -jar "library_app/dist/library_app.jar"

# Dependencies
MySQL JDBC
	mysql-connector-java-5.1.42-bin.jar
MySQL
	Will use root user with asdflkj set as the password

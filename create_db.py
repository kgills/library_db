#!/usr/bin/env python
import MySQLdb
import sys
import os.path
import csv

# Test the command line arguments
usage_line = "Usage: "+sys.argv[0]+" <borrowers.csv> <books.csv>"

if(len(sys.argv) != 3):
	print usage_line
	sys.exit()

if (os.path.isfile(sys.argv[1]) == False):
	print sys.argv[1]+" does not exist!"
	print usage_line
	sys.exit()

if (os.path.isfile(sys.argv[2]) == False):
	print sys.argv[2]+" does not exist!"
	print usage_line
	sys.exit()

borrow_file = sys.argv[1]
book_file = sys.argv[2]

# Connect
db = MySQLdb.connect(host="localhost",
                     user="root",
                     passwd="asdflkj")

cursor = db.cursor()

# Create the DB
cursor.execute("DROP DATABASE IF EXISTS library")
cursor.execute("CREATE DATABASE library")
cursor.execute("USE library")

# Create the borrowers table
cursor.execute("CREATE TABLE BORROWER ( Card_id int NOT NULL, Ssn int NOT NULL, Bname varchar(100) NOT NULL, Address varchar(100) NOT NULL, Phone bigint NOT NULL, PRIMARY KEY (Card_id))")

# Sample borrower
# cursor.execute("INSERT INTO BORROWER VALUES('1', '850473740', 'Mark Morgan', '5677 Coolidge Street,Plano,TX', '4699041438')")

# Read the entries from the borrowers CSV
with open(borrow_file, 'rb') as csvfile:
	reader = csv.reader(csvfile, delimiter=',')
	for row in reader:

		# print row[0]+" | "+row[1]+" | "+row[2]+" | "+row[3]+" | "+row[4]+" | "+row[5]+" | "+row[6]+" | "+row[7]+" | "+row[8]

		# Strip ID, convert to int, back to string
		card_id = str(int(row[0][2:]))
		# print "Card_id = "+card_id

		# Strip the - characters
		ssn = row[1].replace("-", "")
		# print "SSN = "+ssn

		# Combine the names
		name = row[2]+" "+row[3]
		# print "Name = "+name

		# Combine the address, skip email
		addr = row[5]+", "+row[6]+", "+row[7]
		# print "Addr = "+addr

		# Strip non digits from the phone number
		phone=row[8].replace("(", "")
		phone=phone.replace(")", "")
		phone=phone.replace(" ", "")
		phone=phone.replace("-", "")
		# print "Phone = "+phone

		cursor.execute("INSERT INTO BORROWER VALUES('"+card_id+"', '"+ssn+"', '"+name+"', '"+addr+"', '"+phone+"')")


# Commit changes
db.commit()

# Get the number of rows in the resultset
cursor.execute("SELECT * FROM BORROWER")
numrows = cursor.rowcount

# Get and display one row at a time
for x in range(0, numrows):
    row = cursor.fetchone()
    print row[0], "|", row[1], "|", row[2], "|", row[3], "|", row[4]



# Add the borrowers

# Create the Authors table

# Add authors

# Create the Book table

# Add books

# Create the book_autors table

# Populate the Book_authors table


# Close the connection
db.close()
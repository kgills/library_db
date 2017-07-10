#!/usr/bin/env python
import MySQLdb
import sys
import os.path
import csv
import re

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
# cursor.execute("SELECT * FROM BORROWER")
# numrows = cursor.rowcount

# Get and display one row at a time
# for x in range(0, numrows):
    # row = cursor.fetchone()
    # print row[0], "|", row[1], "|", row[2], "|", row[3], "|", row[4]


# Create the Authors table
cursor.execute("CREATE TABLE AUTHORS ( Author_id bigint NOT NULL, Name varchar(100) NOT NULL , PRIMARY KEY (Author_id))")

# Create the Book table
cursor.execute("CREATE TABLE BOOK ( Isbn bigint NOT NULL, Title varchar(250) NOT NULL, PRIMARY KEY (Isbn))")

# Create the Book_authors table
cursor.execute("CREATE TABLE BOOK_AUTHORS ( Author_id bigint NOT NULL, Isbn bigint NOT NULL, PRIMARY KEY (Author_id))")

author_id = 1

# Read the entries from the books CSV
with open(book_file, 'rb') as csvfile:
	reader = csv.reader(csvfile, delimiter='\t')
	for row in reader:
		# print row[0]+" | "+row[1]+" | "+row[2]+" | "+row[3]+" | "+row[4]+" | "+row[5]+" | "+row[6]

		# Use ISBN13 for the book ISBN
		isbn = row[1]

		# Title, insert an escape character for ''
		title = row[2].replace("'", "\\'")

		# Get the authors
		authors = row[3].split(",")

		for author in authors: 

			if(author == ""):
				continue

			author = re.sub(r"\(.*\)", "", author)
			author = re.sub(r"\(.*", "", author)
			author = author.lstrip()

			# Replace the periods and spaces
			author = author.replace(".", " ")
			author = author.replace("'", "\\'")
			author = author.replace("   ", " ")
			author = author.replace("  ", " ")
			author = author.replace(";", ",")
			author = author.replace(", ", ",")
			author = author.replace(" ,", ",")

			# Split into first, last ... names
			author_names=author.split(" ")
			if(" " in author_names):
				author_names.remove(" ")

			if("" in author_names):
				author_names.remove("")

			

			# Search for the author to see if we already have him/her in the DB
			exe_string = "SELECT * from AUTHORS WHERE Name LIKE BINARY\'"
			count = 0;
			if(len(author_names) < 3):
				exe_string=exe_string+author

			else:
				for name in author_names:
					if(count < 1):
						exe_string=exe_string+name[0]+"%"
					else:
						exe_string=exe_string+name+"%"
					count = count+1

			exe_string=exe_string+"\'"
			cursor.execute(exe_string)

			# Get the number of rows in the resultset
			numrows = cursor.rowcount

			if(numrows == 2):
				# More than one match, we need to merge the authors
				print ""
				print author
				print exe_string
				# Get and display one row at a time
				# for x in range(0, numrows):
				#     row = cursor.fetchone()
				#     print row[0], "|", row[1]


				author_len = 0
				author_large_id = 0
				for x in range(0, numrows):
					row = cursor.fetchone()

					if(len(row[1]) > author_len):
						author_len = len(row[1])
						author_large_id = row[0]

				print author_large_id

			elif(numrows == 0):
				# Add this author
				exe_string = "INSERT INTO AUTHORS VALUES('"+str(author_id)+"', '"+author+"')"
				author_id = author_id +1
				# print exe_string
				cursor.execute(exe_string)

			# Add Author if necessary to the AUTHORS table

			# Add the Author to the BOOK_AUTHORS table


		# print "Authors = "+"|".join(authors)

		# print "isbn = "+isbn+" title = "+title

		cursor.execute("INSERT INTO BOOK VALUES('"+isbn+"', '"+title+"')")


# Commit changes
db.commit()

# Get the number of rows in the resultset
# cursor.execute("SELECT * FROM BOOK")
# numrows = cursor.rowcount

# # Get and display one row at a time
# for x in range(0, numrows):
    # row = cursor.fetchone()
    # print row[0], "|", row[1]

# Add authors


# Create the book_autors table

# Populate the Book_authors table


# Close the connection
db.close()
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
cursor.execute("""CREATE TABLE BORROWER ( 
	Card_id int NOT NULL, 
	Ssn int NOT NULL UNIQUE, 
	Bname varchar(100) NOT NULL, 
	Address varchar(100) NOT NULL, 
	Phone bigint NOT NULL, 

	PRIMARY KEY (Card_id))
""")

# Create the Authors table
cursor.execute("""CREATE TABLE AUTHORS ( 
	Author_id bigint NOT NULL, 
	Name varchar(100) NOT NULL,

	PRIMARY KEY (Author_id))
""")

# Create the Book table
cursor.execute("""CREATE TABLE BOOK ( 
	Isbn bigint NOT NULL, 
	Title varchar(250) NOT NULL, 

	PRIMARY KEY (Isbn))
""")

# Create the Book_authors table
cursor.execute("""CREATE TABLE BOOK_AUTHORS ( 
	Author_id bigint NOT NULL, 
	Isbn bigint NOT NULL, 
 
	FOREIGN KEY (Author_id) 
		REFERENCES AUTHORS(Author_id) 
		ON DELETE CASCADE)
""")

# Create the Book_Loans table
cursor.execute("""CREATE TABLE BOOK_LOANS ( 
	Loan_id bigint NOT NULL, 
	Isbn bigint NOT NULL, 
	Card_id bigint NOT NULL, 
	Date_out DATE NOT NULL, 
	Due_date DATE NOT NULL, 
	Date_in DATE, 

	PRIMARY KEY (Loan_id))
""")

# Creat the fines table
cursor.execute("""CREATE TABLE FINES ( 
	Loan_id bigint NOT NULL, 
	Fine_amt DECIMAL(10,2), 
	Paid BOOLEAN NOT NULL,

	PRIMARY KEY (Loan_id),
	FOREIGN KEY (Loan_id) 
	 	REFERENCES BOOK_LOANS(Loan_id) 
	 	ON DELETE CASCADE)
""")

# Read the entries from the borrowers CSV
with open(borrow_file, 'rb') as csvfile:
	reader = csv.reader(csvfile, delimiter=',')
	for row in reader:

		# Strip ID, convert to int, back to string
		card_id = str(int(row[0][2:]))

		# Strip the - characters
		ssn = row[1].replace("-", "")

		# Combine the names
		name = row[2]+" "+row[3]

		# Combine the address, skip email
		addr = row[5]+", "+row[6]+", "+row[7]

		# Strip non digits from the phone number
		phone=row[8].replace("(", "")
		phone=phone.replace(")", "")
		phone=phone.replace(" ", "")
		phone=phone.replace("-", "")

		cursor.execute("INSERT INTO BORROWER VALUES('"+card_id+"', '"+ssn+"', '"+name+"', '"+addr+"', '"+phone+"')")


# Commit changes
db.commit()

author_id = 1

# Read the entries from the books CSV
with open(book_file, 'rb') as csvfile:
	reader = csv.reader(csvfile, delimiter='\t')
	for row in reader:

		# Use ISBN13 for the book ISBN
		isbn = row[1]

		# Title, insert an escape character for ''
		title = row[2].replace("'", "\\'")

		# Get the authors
		authors = row[3].split(",")

		for author in authors: 
	
			this_author_id = 0
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
			exe_string = "SELECT * from AUTHORS WHERE Name LIKE BINARY \'"
			count = 1;
			if(len(author_names) < 3):
				exe_string=exe_string+author
			else:
				for name in author_names:
					if(count < len(author_names)):
						exe_string=exe_string+name[0]+"% "
					else:
						exe_string=exe_string+name
					count = count+1
			
			exe_string=exe_string+"\'"
			# print exe_string
			cursor.execute(exe_string)

			# Get the number of rows in the resultset
			numrows = cursor.rowcount
			if(numrows == 2):
				# More than one match, we need to merge the 3 author names
				print "Need to merge the rows"
				print author
				print exe_string

				row0 = cursor.fetchone()
				row1 = cursor.fetchone()
				print row0[1]+" "+str(row0[0])+"  | "+row1[1]+" "+str(row1[0])
				
			elif(numrows == 0):
				# Add this author
				exe_string = "INSERT INTO AUTHORS VALUES('"+str(author_id)+"', '"+author+"')"
				this_author_id = str(author_id)
				author_id = author_id +1
				# print exe_string
				cursor.execute(exe_string)
			elif(numrows > 1):
				print "Too Many Rows "+str(numrows)
			else:
				# This author already exists
				row = cursor.fetchone()
				this_author_id = str(row[0])
				

			# Add the Author to the BOOK_AUTHORS table
			exe_string = "INSERT INTO BOOK_AUTHORS VALUES('"+this_author_id+"', '"+str(isbn)+"')"
			cursor.execute(exe_string)

		cursor.execute("INSERT INTO BOOK VALUES('"+isbn+"', '"+title+"')")


# Commit changes
db.commit()

# Close the connection
db.close()

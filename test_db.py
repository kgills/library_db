#!/usr/bin/env python
import MySQLdb


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


# Insert some sample data
cursor.execute("INSERT INTO BORROWER VALUES('1', '850473740', 'Mark Morgan', '5677 Coolidge Street,Plano,TX', '4699041438')")
cursor.execute("INSERT INTO BORROWER VALUES('2', '123452688', 'Peter Parker', '123 4th ave, New York, NY', '1234567894')")
cursor.execute("INSERT INTO BORROWER VALUES('3', '789456123', 'Bruce Wayne', '345 11th ave, Gotham, NY', '1234565554')")

cursor.execute("INSERT INTO AUTHORS VALUES('1', 'J K Rowling')")
cursor.execute("INSERT INTO AUTHORS VALUES('2', 'Stephen Hawking')")
cursor.execute("INSERT INTO AUTHORS VALUES('3', 'Dr Seus')")

cursor.execute("INSERT INTO BOOK VALUES('123456789', 'Harry Potter and the Sourcers Stone')")
cursor.execute("INSERT INTO BOOK VALUES('123456788', 'Harry Potter and the Goblet of Fire')")
cursor.execute("INSERT INTO BOOK VALUES('123456787', 'The Cat In the Hat')")
cursor.execute("INSERT INTO BOOK VALUES('123456786', 'Green Eggs and Ham')")
cursor.execute("INSERT INTO BOOK VALUES('123456785', 'A short history of the universe')")

cursor.execute("INSERT INTO BOOK_AUTHORS VALUES('1', '123456789')")
cursor.execute("INSERT INTO BOOK_AUTHORS VALUES('1', '123456788')")
cursor.execute("INSERT INTO BOOK_AUTHORS VALUES('2', '123456785')")
cursor.execute("INSERT INTO BOOK_AUTHORS VALUES('3', '123456787')")
cursor.execute("INSERT INTO BOOK_AUTHORS VALUES('3', '123456786')")

cursor.execute("INSERT INTO BOOK_LOANS VALUES('1', '123456789', '1', '20170214', '20170228', '20170220')")
cursor.execute("INSERT INTO BOOK_LOANS VALUES('2', '123456789', '1', '20170704', '20170720', NULL)")
cursor.execute("INSERT INTO BOOK_LOANS VALUES('3', '123456788', '2', '20170703', '20170719', NULL)")
cursor.execute("INSERT INTO BOOK_LOANS VALUES('4', '123456787', '3', '20170703', '20170719', NULL)")
cursor.execute("INSERT INTO BOOK_LOANS VALUES('5', '123456786', '3', '20170601', '20170615', NULL)")

# Automatically create the fine entry with the book loans, leave NULL until it becomes due
cursor.execute("INSERT INTO FINES VALUES('1', NULL, False)")
cursor.execute("INSERT INTO FINES VALUES('2', NULL, False)")
cursor.execute("INSERT INTO FINES VALUES('3', NULL, False)")
cursor.execute("INSERT INTO FINES VALUES('4', NULL, False)")
cursor.execute("INSERT INTO FINES VALUES('5', 1.25, False)")




# Commit changes
db.commit()

# Close the connection
db.close()
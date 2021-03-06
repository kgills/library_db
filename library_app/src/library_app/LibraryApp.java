/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package library_app;

import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

class SearchEntry {

    String ISBN;
    String author;
    String title;
    String dueDate;

    public SearchEntry(String ISBN, String author, String title, String dueDate) {
        this.ISBN = ISBN;
        this.author = author;
        this.title = title;
        this.dueDate = dueDate;
    }

    public int compare(SearchEntry o1, SearchEntry o2) {
        if (o1.ISBN.equals(o2.ISBN)
                && o1.author.equals(o2.author)
                && o1.title.equals(o2.title)
                && o1.dueDate.equals(o2.dueDate)) {
            return 1;
        }

        return 0;
    }

    @Override
    public boolean equals(Object o) {

        SearchEntry obj = (SearchEntry) o;

        return (this.ISBN.equals(obj.ISBN)
                && this.author.equals(obj.author)
                && this.title.equals(obj.title)
                && this.dueDate.equals(obj.dueDate));
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + Objects.hashCode(this.ISBN);
        hash = 71 * hash + Objects.hashCode(this.author);
        hash = 71 * hash + Objects.hashCode(this.title);
        hash = 71 * hash + Objects.hashCode(this.dueDate);
        return hash;
    }
}

/**
 *
 * @author kgills
 */
public class LibraryApp extends javax.swing.JFrame {

    private Connection connect = null;
    private Statement statement0 = null;
    private Statement statement1 = null;
    private Statement statement2 = null;

    private long userID;
    private long loanID;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

    public static int daysBetween(Calendar endDate, Calendar startDate) {
        long end = endDate.getTimeInMillis();
        long start = startDate.getTimeInMillis();
        return (int) TimeUnit.MILLISECONDS.toDays(Math.abs(end - start));
    }

    /**
     * Creates new form LibraryApp
     */
    public LibraryApp() {
        initComponents();
        this.setTitle("Library App");

        try {
            System.out.println("Opening localhost/library database");
            // Import the database, assuming that it was already populated
            Class.forName("com.mysql.jdbc.Driver");
            connect = DriverManager.getConnection("jdbc:mysql://localhost/library?"
                    + "user=root&password=asdflkj&"
                    + "useSSL=false");

            if (connect == null) {
                System.out.println("Error openening localhost/library database");
                System.exit(1);
            } else {
                System.out.println("Database Open!");
            }

            // Statements allow to issue SQL queries to the database
            statement0 = connect.createStatement();
            statement1 = connect.createStatement();
            statement2 = connect.createStatement();

            // Update the next userID and LoanID values
            String query;

            query = "SELECT Card_id "
                    + "FROM BORROWER "
                    + "ORDER BY Card_id DESC;";
            System.out.println(query);

            ResultSet borrowersResultSet = statement0.executeQuery(query);

            userID = 1;

            if (borrowersResultSet.next()) {
                userID = Long.parseLong(borrowersResultSet.getString(1)) + 1;
            } else {
                userID = 1;
            }

            query = "SELECT Loan_id "
                    + "FROM BOOK_LOANS "
                    + "ORDER BY Loan_id DESC;";
            System.out.println(query);

            ResultSet bookLoansResultSet = statement0.executeQuery(query);

            if (bookLoansResultSet.next()) {
                loanID = Long.parseLong(bookLoansResultSet.getString(1)) + 1;
            } else {
                loanID = 1;
            }

            System.out.println("Next userID = " + userID);
            System.out.println("Next loanID = " + loanID);

        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(LibraryApp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void executeSearch() {
        ResultSet bookResultSet;
        ResultSet bookAuthorsResultSet;
        ResultSet authorsResultSet;
        ResultSet bookLoansResultSet;
        String query;

        Set<SearchEntry> searchResults;
        searchResults = new HashSet<>();
        String textResults = "";

        try {
            // Query the database for authors that match what's in the serach bar
            query = "SELECT * "
                    + "FROM AUTHORS "
                    + "WHERE LOWER(AUTHORS.name) LIKE '%" + SearchField.getText() + "%';";
            System.out.println(query);

            authorsResultSet = statement0.executeQuery(query);

            while (authorsResultSet.next()) {

                String authorID = authorsResultSet.getString(1);
                String authorName = authorsResultSet.getString(2);
                System.out.println("AuthorID: " + authorID + " AuthorName: " + authorName);

                // Search book_authors for the ISBNs  by this author
                query = "SELECT Isbn "
                        + "FROM BOOK_AUTHORS "
                        + "WHERE Author_id='" + authorID + "';";
                System.out.println(query);

                bookAuthorsResultSet = statement1.executeQuery(query);

                while (bookAuthorsResultSet.next()) {

                    String ISBN = bookAuthorsResultSet.getString(1);
                    System.out.println("ISBN: " + ISBN);

                    // Search books for the titles corresponding to this ISBN
                    query = "SELECT Title "
                            + "FROM BOOK "
                            + "WHERE Isbn='" + ISBN + "';";
                    System.out.println(query);

                    bookResultSet = statement2.executeQuery(query);

                    while (bookResultSet.next()) {
                        String title = bookResultSet.getString(1);
                        System.out.println("Title: " + title);

                        // Need to add the books to the search results
                        searchResults.add(new SearchEntry(ISBN, authorName, title, "Available"));
                    }

                    bookResultSet.close();
                }

                bookAuthorsResultSet.close();
            }

            authorsResultSet.close();

            // Query for books titles that match
            query = "SELECT * "
                    + "FROM BOOK "
                    + "WHERE LOWER(BOOK.Title) LIKE '%" + SearchField.getText() + "%';";
            System.out.println(query);

            bookResultSet = statement0.executeQuery(query);

            while (bookResultSet.next()) {

                String ISBN = bookResultSet.getString(1);
                String title = bookResultSet.getString(2);
                System.out.println("ISBN: " + ISBN + " Title: " + title);

                // Search book_authors for the Author_id from this book
                query = "SELECT Author_id "
                        + "FROM BOOK_AUTHORS "
                        + "WHERE Isbn='" + ISBN + "';";
                System.out.println(query);

                bookAuthorsResultSet = statement1.executeQuery(query);

                while (bookAuthorsResultSet.next()) {

                    String authorID = bookAuthorsResultSet.getString(1);
                    System.out.println("authorID: " + authorID);

                    // Search Authors for the author name corresponding to this author_id
                    query = "SELECT Name "
                            + "FROM AUTHORS "
                            + "WHERE Author_id='" + authorID + "';";
                    System.out.println(query);

                    authorsResultSet = statement2.executeQuery(query);

                    while (authorsResultSet.next()) {
                        String authorName = authorsResultSet.getString(1);
                        System.out.println("Author: " + authorName);

                        // Need to add the books to the search results
                        searchResults.add(new SearchEntry(ISBN, authorName, title, "Available"));
                    }

                    authorsResultSet.close();
                }

                bookAuthorsResultSet.close();
            }

            bookResultSet.close();

            // Query for ISBNs that match
            query = "SELECT Title "
                    + "FROM BOOK "
                    + "WHERE LOWER(BOOK.Isbn) LIKE '" + SearchField.getText() + "';";
            System.out.println(query);

            bookResultSet = statement0.executeQuery(query);

            while (bookResultSet.next()) {

                String ISBN = SearchField.getText();
                String title = bookResultSet.getString(1);
                System.out.println("ISBN: " + ISBN + " Title: " + title);

                // Search book_authors for the Author_id from this book
                query = "SELECT Author_id "
                        + "FROM BOOK_AUTHORS "
                        + "WHERE Isbn='" + ISBN + "';";
                System.out.println(query);

                bookAuthorsResultSet = statement1.executeQuery(query);

                while (bookAuthorsResultSet.next()) {

                    String authorID = bookAuthorsResultSet.getString(1);
                    System.out.println("authorID: " + authorID);

                    // Search Authors for the author name corresponding to this author_id
                    query = "SELECT Name "
                            + "FROM AUTHORS "
                            + "WHERE Author_id='" + authorID + "';";
                    System.out.println(query);

                    authorsResultSet = statement2.executeQuery(query);

                    while (authorsResultSet.next()) {
                        String authorName = authorsResultSet.getString(1);
                        System.out.println("Author: " + authorName);

                        // Need to add the books to the search results
                        searchResults.add(new SearchEntry(ISBN, authorName, title, "Available"));
                    }

                    authorsResultSet.close();
                }

                bookAuthorsResultSet.close();
            }

            bookResultSet.close();

        } catch (SQLException ex) {
            Logger.getLogger(LibraryApp.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Add the items to the textResults
        for (SearchEntry entry : searchResults) {

            // Need to check if the books are checked out
            query = "SELECT Date_in, Due_date, Date_out "
                    + "FROM BOOK_LOANS "
                    + "WHERE Isbn= '" + entry.ISBN + "'"
                    + "ORDER BY Date_out ASC;";
            System.out.println(query);
            try {
                bookLoansResultSet = statement0.executeQuery(query);
                while (bookLoansResultSet.next()) {

                    String dateInString = bookLoansResultSet.getString(1);
                    String dueDateString = bookLoansResultSet.getString(2);

                    System.out.println("DateIn: " + dateInString + " DueDate: " + dueDateString);

                    if (dateInString == null) {
                        entry.dueDate = dueDateString;
                    } else {
                        entry.dueDate = "Available";
                    }
                }
            } catch (SQLException ex) {
                Logger.getLogger(LibraryApp.class.getName()).log(Level.SEVERE, null, ex);
            }

            textResults += entry.ISBN + "  |  " + entry.author + "  |  "
                    + entry.title + "  |  " + entry.dueDate + "\n";
        }
        SearchResultsText.setText(textResults);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        NewUserFrame = new javax.swing.JFrame();
        NameText = new javax.swing.JLabel();
        SSNText = new javax.swing.JLabel();
        AddressText = new javax.swing.JLabel();
        PhoneText = new javax.swing.JLabel();
        NameField = new javax.swing.JTextField();
        SSNField = new javax.swing.JTextField();
        AddressField = new javax.swing.JTextField();
        PhoneField = new javax.swing.JTextField();
        CreateUserButton = new javax.swing.JButton();
        NewUserDialog = new javax.swing.JDialog();
        NewUserStatus = new javax.swing.JLabel();
        FineFrame = new javax.swing.JFrame();
        UpdateFinesButton = new javax.swing.JButton();
        FineUserIDText = new javax.swing.JLabel();
        FineUserIDField = new javax.swing.JTextField();
        FineBalanceButton = new javax.swing.JButton();
        FinePayButton = new javax.swing.JButton();
        FineBalanceText = new javax.swing.JLabel();
        FineBalanceField = new javax.swing.JTextField();
        FinesScrollPane = new javax.swing.JScrollPane();
        FinesTextArea = new javax.swing.JTextArea();
        FinesLoanIDField = new javax.swing.JTextField();
        FinesLoadIDText = new javax.swing.JLabel();
        FinesDescription = new javax.swing.JLabel();
        CheckInFrame = new javax.swing.JFrame();
        CheckInUserIDText = new javax.swing.JLabel();
        CheckInUserIDField = new javax.swing.JTextField();
        CheckInISBNText = new javax.swing.JLabel();
        CheckInISBNField = new javax.swing.JTextField();
        CheckInButton = new javax.swing.JButton();
        CheckInDialog = new javax.swing.JDialog();
        CheckInDialogText = new javax.swing.JLabel();
        CheckOutFrame = new javax.swing.JFrame();
        CheckOutUserIDText = new javax.swing.JLabel();
        CheckOutUserIDField = new javax.swing.JTextField();
        CheckOutISBNText = new javax.swing.JLabel();
        CheckOutISBNField = new javax.swing.JTextField();
        CheckOutButton = new javax.swing.JButton();
        CheckOutDialog = new javax.swing.JDialog();
        CheckOutDialogText = new javax.swing.JLabel();
        FineDialog = new javax.swing.JDialog();
        FineDialogText = new javax.swing.JLabel();
        SearchButton = new javax.swing.JButton();
        SearchField = new javax.swing.JTextField();
        SearchResults = new javax.swing.JScrollPane();
        SearchResultsText = new javax.swing.JTextArea();
        SearchResultsLabel = new javax.swing.JLabel();
        MenuBar = new javax.swing.JMenuBar();
        FileMenu = new javax.swing.JMenu();
        PayFine = new javax.swing.JMenuItem();
        NewUser = new javax.swing.JMenuItem();
        CheckIn = new javax.swing.JMenuItem();
        CheckOut = new javax.swing.JMenuItem();
        Exit = new javax.swing.JMenuItem();

        NameText.setText("Name");

        SSNText.setText("SSN");

        AddressText.setText("Address");

        PhoneText.setText("Phone");

        CreateUserButton.setText("Create");
        CreateUserButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CreateUserButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout NewUserFrameLayout = new javax.swing.GroupLayout(NewUserFrame.getContentPane());
        NewUserFrame.getContentPane().setLayout(NewUserFrameLayout);
        NewUserFrameLayout.setHorizontalGroup(
            NewUserFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(NewUserFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(NewUserFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(NewUserFrameLayout.createSequentialGroup()
                        .addGroup(NewUserFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(NameText)
                            .addComponent(SSNText)
                            .addComponent(AddressText)
                            .addComponent(PhoneText))
                        .addGap(18, 18, 18)
                        .addGroup(NewUserFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(NameField)
                            .addComponent(SSNField)
                            .addComponent(AddressField, javax.swing.GroupLayout.DEFAULT_SIZE, 408, Short.MAX_VALUE)
                            .addComponent(PhoneField)))
                    .addGroup(NewUserFrameLayout.createSequentialGroup()
                        .addComponent(CreateUserButton)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        NewUserFrameLayout.setVerticalGroup(
            NewUserFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(NewUserFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(NewUserFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(NameText)
                    .addComponent(NameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(NewUserFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(SSNText)
                    .addComponent(SSNField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(NewUserFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(AddressText)
                    .addComponent(AddressField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(NewUserFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(PhoneText)
                    .addComponent(PhoneField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(CreateUserButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        NewUserStatus.setText("Error Creating User");

        javax.swing.GroupLayout NewUserDialogLayout = new javax.swing.GroupLayout(NewUserDialog.getContentPane());
        NewUserDialog.getContentPane().setLayout(NewUserDialogLayout);
        NewUserDialogLayout.setHorizontalGroup(
            NewUserDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(NewUserDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(NewUserStatus)
                .addContainerGap(71, Short.MAX_VALUE))
        );
        NewUserDialogLayout.setVerticalGroup(
            NewUserDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(NewUserDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(NewUserStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(47, Short.MAX_VALUE))
        );

        UpdateFinesButton.setText("Update Fines");
        UpdateFinesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateFinesButtonActionPerformed(evt);
            }
        });

        FineUserIDText.setText("User ID");

        FineBalanceButton.setText("Balance");
        FineBalanceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FineBalanceButtonActionPerformed(evt);
            }
        });

        FinePayButton.setText("Pay");
        FinePayButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FinePayButtonActionPerformed(evt);
            }
        });

        FineBalanceText.setText("Balance");

        FinesTextArea.setColumns(20);
        FinesTextArea.setRows(5);
        FinesScrollPane.setViewportView(FinesTextArea);

        FinesLoadIDText.setText("LoanID");

        FinesDescription.setText("LoanID | ISBN | Due Date | Amount | Status");

        javax.swing.GroupLayout FineFrameLayout = new javax.swing.GroupLayout(FineFrame.getContentPane());
        FineFrame.getContentPane().setLayout(FineFrameLayout);
        FineFrameLayout.setHorizontalGroup(
            FineFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(FineFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(FineFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(FinesScrollPane)
                    .addGroup(FineFrameLayout.createSequentialGroup()
                        .addGroup(FineFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(FineUserIDText)
                            .addComponent(FineBalanceText)
                            .addComponent(FinesLoadIDText))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(FineFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(FineUserIDField, javax.swing.GroupLayout.DEFAULT_SIZE, 302, Short.MAX_VALUE)
                            .addComponent(FineBalanceField)
                            .addComponent(FinesLoanIDField)))
                    .addGroup(FineFrameLayout.createSequentialGroup()
                        .addGroup(FineFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(FineFrameLayout.createSequentialGroup()
                                .addComponent(FineBalanceButton)
                                .addGap(18, 18, 18)
                                .addComponent(FinePayButton)
                                .addGap(18, 18, 18)
                                .addComponent(UpdateFinesButton))
                            .addComponent(FinesDescription))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        FineFrameLayout.setVerticalGroup(
            FineFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, FineFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(FineFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(FineUserIDText)
                    .addComponent(FineUserIDField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(FineFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(FinesLoanIDField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(FinesLoadIDText))
                .addGap(18, 18, 18)
                .addGroup(FineFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(FineBalanceButton)
                    .addComponent(FinePayButton)
                    .addComponent(UpdateFinesButton))
                .addGap(18, 18, 18)
                .addGroup(FineFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(FineBalanceField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(FineBalanceText))
                .addGap(18, 18, 18)
                .addComponent(FinesDescription)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(FinesScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        CheckInUserIDText.setText("UserID:");

        CheckInISBNText.setText("ISBN: ");

        CheckInButton.setText("Check In");
        CheckInButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CheckInButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout CheckInFrameLayout = new javax.swing.GroupLayout(CheckInFrame.getContentPane());
        CheckInFrame.getContentPane().setLayout(CheckInFrameLayout);
        CheckInFrameLayout.setHorizontalGroup(
            CheckInFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(CheckInFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(CheckInFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(CheckInFrameLayout.createSequentialGroup()
                        .addComponent(CheckInButton)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(CheckInFrameLayout.createSequentialGroup()
                        .addGroup(CheckInFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(CheckInUserIDText)
                            .addComponent(CheckInISBNText))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(CheckInFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(CheckInISBNField, javax.swing.GroupLayout.DEFAULT_SIZE, 321, Short.MAX_VALUE)
                            .addComponent(CheckInUserIDField))))
                .addContainerGap())
        );
        CheckInFrameLayout.setVerticalGroup(
            CheckInFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(CheckInFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(CheckInFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(CheckInUserIDText)
                    .addComponent(CheckInUserIDField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(CheckInFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(CheckInISBNText)
                    .addComponent(CheckInISBNField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(CheckInButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        CheckInDialogText.setText("Error Checking In");

        javax.swing.GroupLayout CheckInDialogLayout = new javax.swing.GroupLayout(CheckInDialog.getContentPane());
        CheckInDialog.getContentPane().setLayout(CheckInDialogLayout);
        CheckInDialogLayout.setHorizontalGroup(
            CheckInDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(CheckInDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(CheckInDialogText)
                .addContainerGap(84, Short.MAX_VALUE))
        );
        CheckInDialogLayout.setVerticalGroup(
            CheckInDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(CheckInDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(CheckInDialogText)
                .addContainerGap(46, Short.MAX_VALUE))
        );

        CheckOutUserIDText.setText("UserID:");

        CheckOutISBNText.setText("ISBN: ");

        CheckOutButton.setText("Check Out");
        CheckOutButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CheckOutButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout CheckOutFrameLayout = new javax.swing.GroupLayout(CheckOutFrame.getContentPane());
        CheckOutFrame.getContentPane().setLayout(CheckOutFrameLayout);
        CheckOutFrameLayout.setHorizontalGroup(
            CheckOutFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(CheckOutFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(CheckOutFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(CheckOutFrameLayout.createSequentialGroup()
                        .addComponent(CheckOutButton)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(CheckOutFrameLayout.createSequentialGroup()
                        .addGroup(CheckOutFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(CheckOutUserIDText)
                            .addComponent(CheckOutISBNText))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(CheckOutFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(CheckOutISBNField, javax.swing.GroupLayout.DEFAULT_SIZE, 321, Short.MAX_VALUE)
                            .addComponent(CheckOutUserIDField))))
                .addContainerGap())
        );
        CheckOutFrameLayout.setVerticalGroup(
            CheckOutFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(CheckOutFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(CheckOutFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(CheckOutUserIDText)
                    .addComponent(CheckOutUserIDField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(CheckOutFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(CheckOutISBNText)
                    .addComponent(CheckOutISBNField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(CheckOutButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        CheckOutDialogText.setText("Error Checking Out");

        javax.swing.GroupLayout CheckOutDialogLayout = new javax.swing.GroupLayout(CheckOutDialog.getContentPane());
        CheckOutDialog.getContentPane().setLayout(CheckOutDialogLayout);
        CheckOutDialogLayout.setHorizontalGroup(
            CheckOutDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(CheckOutDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(CheckOutDialogText)
                .addContainerGap(71, Short.MAX_VALUE))
        );
        CheckOutDialogLayout.setVerticalGroup(
            CheckOutDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(CheckOutDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(CheckOutDialogText)
                .addContainerGap(46, Short.MAX_VALUE))
        );

        FineDialogText.setText("Error Paying Fine");

        javax.swing.GroupLayout FineDialogLayout = new javax.swing.GroupLayout(FineDialog.getContentPane());
        FineDialog.getContentPane().setLayout(FineDialogLayout);
        FineDialogLayout.setHorizontalGroup(
            FineDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(FineDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(FineDialogText)
                .addContainerGap(89, Short.MAX_VALUE))
        );
        FineDialogLayout.setVerticalGroup(
            FineDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(FineDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(FineDialogText)
                .addContainerGap(46, Short.MAX_VALUE))
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                formKeyPressed(evt);
            }
        });

        SearchButton.setText("Search");
        SearchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SearchButtonActionPerformed(evt);
            }
        });

        SearchField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                SearchFieldKeyReleased(evt);
            }
        });

        SearchResultsText.setColumns(20);
        SearchResultsText.setRows(5);
        SearchResults.setViewportView(SearchResultsText);

        SearchResultsLabel.setText("ISBN | Author | Title | Due Date");

        FileMenu.setText("File");

        PayFine.setText("Pay Fine");
        PayFine.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PayFineActionPerformed(evt);
            }
        });
        FileMenu.add(PayFine);

        NewUser.setText("New User");
        NewUser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NewUserActionPerformed(evt);
            }
        });
        FileMenu.add(NewUser);

        CheckIn.setText("Check In");
        CheckIn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CheckInActionPerformed(evt);
            }
        });
        FileMenu.add(CheckIn);

        CheckOut.setText("Check Out");
        CheckOut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CheckOutActionPerformed(evt);
            }
        });
        FileMenu.add(CheckOut);

        Exit.setText("Exit");
        Exit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ExitActionPerformed(evt);
            }
        });
        FileMenu.add(Exit);

        MenuBar.add(FileMenu);

        setJMenuBar(MenuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(SearchResults)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(SearchButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(SearchField, javax.swing.GroupLayout.DEFAULT_SIZE, 642, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(SearchResultsLabel)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(SearchButton)
                    .addComponent(SearchField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(SearchResultsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(SearchResults, javax.swing.GroupLayout.DEFAULT_SIZE, 165, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void PayFineActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PayFineActionPerformed
        // TODO add your handling code here:
        FineFrame.pack();
        FineFrame.setTitle("Fines");
        FineFrame.setVisible(true);
    }//GEN-LAST:event_PayFineActionPerformed

    private void ExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ExitActionPerformed
        // Close the DB connection
        System.exit(0);
    }//GEN-LAST:event_ExitActionPerformed

    private void NewUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NewUserActionPerformed

        NewUserFrame.pack();
        NewUserFrame.setTitle("New User");
        NewUserFrame.setVisible(true);
    }//GEN-LAST:event_NewUserActionPerformed

    private void CheckInActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CheckInActionPerformed
        CheckInFrame.pack();
        CheckInFrame.setTitle("Check In");
        CheckInFrame.setVisible(true);
    }//GEN-LAST:event_CheckInActionPerformed

    private void CreateUserButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CreateUserButtonActionPerformed

        String phone, ssn;

        // Remove any non-numerics from the ssn and phone strings;
        phone = PhoneField.getText().replaceAll("[^\\d.]", "");
        ssn = SSNField.getText().replaceAll("[^\\d.]", "");

        // Attempt to add this user
        String insert = "INSERT INTO BORROWER VALUES("
                + "'" + userID + "', "
                + "'" + ssn + "', "
                + "'" + NameField.getText() + "', "
                + "'" + AddressField.getText() + "', "
                + "'" + phone + "');";

        System.out.println(insert);
        NewUserStatus.setText("New User Added! Card_id = " + userID);
        userID += 1;

        try {
            statement0.execute(insert);
        } catch (SQLException ex) {
            Logger.getLogger(LibraryApp.class.getName()).log(Level.SEVERE, null, ex);
            userID -= 1;
            NewUserStatus.setText("All fields required, no duplicate SSN");
        }

        NewUserDialog.pack();
        NewUserDialog.setTitle("New User Status");
        NewUserDialog.setVisible(true);
    }//GEN-LAST:event_CreateUserButtonActionPerformed

    private void CheckInButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CheckInButtonActionPerformed

        // Update the BOOK_LOANS table
        String update = "UPDATE BOOK_LOANS "
                + "SET Date_in =  NOW() "
                + "WHERE Isbn = " + CheckInISBNField.getText()+" "
                + "AND Card_id = " + CheckInUserIDField.getText() + ";";
        System.out.println(update);

        try {
            statement0.execute(update);
            CheckInDialogText.setText("Book checked in!");
        } catch (SQLException ex) {
            Logger.getLogger(LibraryApp.class.getName()).log(Level.SEVERE, null, ex);
            userID -= 1;
            CheckInDialogText.setText("Failed! Can't find ISBN and Card_ID match");
        }

        CheckInDialog.pack();
        CheckInDialog.setTitle("Check In Status");
        CheckInDialog.setVisible(true);
    }//GEN-LAST:event_CheckInButtonActionPerformed

    private void SearchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SearchButtonActionPerformed

        executeSearch();
    }//GEN-LAST:event_SearchButtonActionPerformed

    private void CheckOutButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CheckOutButtonActionPerformed

        ResultSet bookLoansResultSet;
        ResultSet bookLoansCountResultSet;
        String result = "";
        String insert = "";
        String fine = "";

        // Search for this book in the Book_loans table
        String query = "SELECT Date_in, Date_out "
                + "FROM BOOK_LOANS "
                + "WHERE Isbn= '" + CheckOutISBNField.getText() + "' "
                + "ORDER BY Date_out DESC;";
        System.out.println(query);
        try {
            bookLoansResultSet = statement0.executeQuery(query);

            String dateInString = "Available";
            if (bookLoansResultSet.next()) {
                dateInString = bookLoansResultSet.getString(1);
            }

            Calendar today = Calendar.getInstance();
            Calendar dueDate = Calendar.getInstance();
            dueDate.add(Calendar.DATE, 14);

            if (dateInString != null) {
                // Book is available, make sure this user doesn't have more than 2 books
                // Currently checked out

                query = "SELECT COUNT(*)"
                        + "FROM BOOK_LOANS "
                        + "WHERE Card_id = " + CheckOutUserIDField.getText() + " "
                        + "AND Date_in is NULL;";
                
                System.out.println(query);
                bookLoansCountResultSet = statement1.executeQuery(query);
                
                int books = 0;
                if (bookLoansCountResultSet.next()) {
                    String booksString = bookLoansCountResultSet.getString(1);
                    books = Integer.parseInt(booksString);
                }
                
                System.out.println("books = "+books);
                
                if(books > 2) {
                    CheckOutDialogText.setText("Too many books checked out");
                } else {
                    // insert into BOOK LOANS
                    insert = "INSERT INTO BOOK_LOANS VALUES("
                            + "'" + loanID + "', "
                            + "'" + CheckOutISBNField.getText() + "', "
                            + "'" + CheckOutUserIDField.getText() + "', "
                            + "'" + dateFormat.format(today.getTime()) + "', "
                            + "'" + dateFormat.format(dueDate.getTime()) + "', "
                            + "NULL);";
                    fine = "INSERT INTO FINES VALUES("
                            + "'" + loanID + "', "
                            + "NULL, "
                            + "False);";

                    System.out.println(insert);
                    System.out.println(fine);

                    loanID += 1;
                    CheckOutDialogText.setText("Due Date:" + dateFormat.format(dueDate.getTime()));
                
                    try {
                        statement0.execute(insert);
                        statement0.execute(fine);
                    } catch (SQLException ex) {
                        Logger.getLogger(LibraryApp.class.getName()).log(Level.SEVERE, null, ex);
                        loanID -= 1;
                        CheckOutDialogText.setText("Check Out Failed!");
                    }
                }
            } else {
                // Book is not available
                CheckOutDialogText.setText("Book is not available for checkout");
            }
        } catch (SQLException ex) {
            Logger.getLogger(LibraryApp.class.getName()).log(Level.SEVERE, null, ex);
        }

        CheckOutDialog.pack();
        CheckOutDialog.setTitle("Check Out Status");
        CheckOutDialog.setVisible(true);
    }//GEN-LAST:event_CheckOutButtonActionPerformed

    private void CheckOutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CheckOutActionPerformed
        CheckOutFrame.pack();
        CheckOutFrame.setTitle("Check Out");
        CheckOutFrame.setVisible(true);
    }//GEN-LAST:event_CheckOutActionPerformed

    private void UpdateFinesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_UpdateFinesButtonActionPerformed

        ResultSet bookLoansResultSet;
        String query;
        String update;
        Calendar today = Calendar.getInstance();
        int year, month, day;

        // Need to check if the books are checked out
        query = "SELECT Date_in, Due_date, Loan_id "
                + "FROM BOOK_LOANS "
                + "WHERE Date_in IS NULL "
                + "ORDER BY Date_out ASC;";
        System.out.println(query);

        try {
            bookLoansResultSet = statement0.executeQuery(query);
            while (bookLoansResultSet.next()) {

                String dateInString = bookLoansResultSet.getString(1);
                String dueDateString = bookLoansResultSet.getString(2);
                String loanIDString = bookLoansResultSet.getString(3);

                year = Integer.parseInt(dueDateString.substring(0, 4));
                month = Integer.parseInt(dueDateString.substring(5, 7));
                day = Integer.parseInt(dueDateString.substring(8, 10));

                Calendar dueDate = Calendar.getInstance();
                dueDate.set(year, month - 1, day); // Calendar indexes the months starting with 0

                if (dateInString == null) {
                    // See if the book is past due

                    if (dueDate.before(today)) {
                        // Recalculate the fine
                        int days = daysBetween(today, dueDate);

                        update = "UPDATE FINES "
                                + "SET Fine_amt = " + Float.toString((float) (days * 0.25)) + " "
                                + "WHERE Loan_id = " + loanIDString + ";";
                        System.out.println(update);

                        statement1.execute(update);
                    }
                } else {
                    // Calculate the fine ammount if the book has already been turned in

                    year = Integer.parseInt(dateInString.substring(0, 4));
                    month = Integer.parseInt(dateInString.substring(5, 7));
                    day = Integer.parseInt(dateInString.substring(8, 10));

                    Calendar dateIn = Calendar.getInstance();
                    dateIn.set(year, month - 1, day); // Calendar indexes the months starting with 0

                    if (dueDate.before(dateIn)) {
                        // Recalculate the fine
                        int days = daysBetween(dateIn, dueDate);

                        update = "UPDATE FINES "
                                + "SET Fine_amt = " + Float.toString((float) (days * 0.25)) + " "
                                + "WHERE Loan_id = " + loanIDString + ";";
                        System.out.println(update);

                        statement1.execute(update);
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(LibraryApp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_UpdateFinesButtonActionPerformed

    private void FineBalanceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FineBalanceButtonActionPerformed
        // TODO Query the fine balance for the given userID

        ResultSet finesResultSet;
        ResultSet bookLoansResultSet;
        String query;
        String finesText = "";
        double balance = 0.0;

        // Need to check if the books are checked out
        query = "SELECT Loan_id, ISBN, Due_date, Date_in "
                + "FROM BOOK_LOANS "
                + "WHERE Card_id = " + FineUserIDField.getText() + ";";
        System.out.println(query);

        try {
            bookLoansResultSet = statement0.executeQuery(query);
            while (bookLoansResultSet.next()) {

                String loanIDString = bookLoansResultSet.getString(1);
                String ISBNString = bookLoansResultSet.getString(2);
                String dueDateString = bookLoansResultSet.getString(3);
                String Date_in = bookLoansResultSet.getString(4);

                query = "SELECT Fine_amt, Paid "
                        + "FROM FINES "
                        + "WHERE Loan_id = " + loanIDString + ";";

                System.out.println(query);
                finesResultSet = statement1.executeQuery(query);

                while (finesResultSet.next()) {
                    String fineAmtString = finesResultSet.getString(1);
                    String paidString = finesResultSet.getString(2);

                    if (fineAmtString == null) {
                        fineAmtString = "0.00";
                    }

                    if(Date_in != null && fineAmtString.equals("0.00")) {
                        // Book already turned in
                        continue;
                    } else if (paidString.equals("0")) {
                        paidString = "Due";
                    } else {
                        paidString = "Paid";
                    }

                    finesText += loanIDString + "  |  " + ISBNString + "  |  " + dueDateString + "  |  " + fineAmtString + "  |  " + paidString + "\n";

                    if (paidString.equals("Due")) {
                        balance += Double.valueOf(fineAmtString);
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(LibraryApp.class.getName()).log(Level.SEVERE, null, ex);
        }

        FineBalanceField.setText(Double.toString(balance));
        FinesTextArea.setText(finesText);
    }//GEN-LAST:event_FineBalanceButtonActionPerformed

    private void FinePayButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FinePayButtonActionPerformed
        
        ResultSet bookLoansResultSet;
        
        // Have to make sure this book has been turned in
        String query = "SELECT Date_in "
                + "FROM BOOK_LOANS "
                + "WHERE Loan_id = "+FinesLoanIDField.getText()+";";
        
        System.out.println(query);
        try {
            bookLoansResultSet = statement1.executeQuery(query);
            
            if (bookLoansResultSet.next()) {
                String dateIn = bookLoansResultSet.getString(1);
                System.out.println(dateIn);
                if(dateIn != null){
                    // Pay the fine for the given loan ID
                    String update;

                    update = "UPDATE FINES "
                            + "SET Paid=True  "
                            + "WHERE Loan_id = " + FinesLoanIDField.getText() + ";";
                    System.out.println(update);

                    statement0.execute(update);
                    FineDialogText.setText("Fine Paid!");
                } else {
                    // Book is still checked out, can't pay the fine
                    FineDialogText.setText("Book is still checked out");
                }
            } else {
                FineDialogText.setText("Can't find loan");
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(LibraryApp.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        FineDialog.pack();
        FineDialog.setTitle("Fine Status");
        FineDialog.setVisible(true);
    }//GEN-LAST:event_FinePayButtonActionPerformed

    private void formKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyPressed

    }//GEN-LAST:event_formKeyPressed

    private void SearchFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_SearchFieldKeyReleased

        if (evt.getKeyCode() == 10) {
            executeSearch();
        }
    }//GEN-LAST:event_SearchFieldKeyReleased

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {

        System.out.println("Staring Library Database Application");

        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(LibraryApp.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new LibraryApp().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField AddressField;
    private javax.swing.JLabel AddressText;
    private javax.swing.JMenuItem CheckIn;
    private javax.swing.JButton CheckInButton;
    private javax.swing.JDialog CheckInDialog;
    private javax.swing.JLabel CheckInDialogText;
    private javax.swing.JFrame CheckInFrame;
    private javax.swing.JTextField CheckInISBNField;
    private javax.swing.JLabel CheckInISBNText;
    private javax.swing.JTextField CheckInUserIDField;
    private javax.swing.JLabel CheckInUserIDText;
    private javax.swing.JMenuItem CheckOut;
    private javax.swing.JButton CheckOutButton;
    private javax.swing.JDialog CheckOutDialog;
    private javax.swing.JLabel CheckOutDialogText;
    private javax.swing.JFrame CheckOutFrame;
    private javax.swing.JTextField CheckOutISBNField;
    private javax.swing.JLabel CheckOutISBNText;
    private javax.swing.JTextField CheckOutUserIDField;
    private javax.swing.JLabel CheckOutUserIDText;
    private javax.swing.JButton CreateUserButton;
    private javax.swing.JMenuItem Exit;
    private javax.swing.JMenu FileMenu;
    private javax.swing.JButton FineBalanceButton;
    private javax.swing.JTextField FineBalanceField;
    private javax.swing.JLabel FineBalanceText;
    private javax.swing.JDialog FineDialog;
    private javax.swing.JLabel FineDialogText;
    private javax.swing.JFrame FineFrame;
    private javax.swing.JButton FinePayButton;
    private javax.swing.JTextField FineUserIDField;
    private javax.swing.JLabel FineUserIDText;
    private javax.swing.JLabel FinesDescription;
    private javax.swing.JLabel FinesLoadIDText;
    private javax.swing.JTextField FinesLoanIDField;
    private javax.swing.JScrollPane FinesScrollPane;
    private javax.swing.JTextArea FinesTextArea;
    private javax.swing.JMenuBar MenuBar;
    private javax.swing.JTextField NameField;
    private javax.swing.JLabel NameText;
    private javax.swing.JMenuItem NewUser;
    private javax.swing.JDialog NewUserDialog;
    private javax.swing.JFrame NewUserFrame;
    private javax.swing.JLabel NewUserStatus;
    private javax.swing.JMenuItem PayFine;
    private javax.swing.JTextField PhoneField;
    private javax.swing.JLabel PhoneText;
    private javax.swing.JTextField SSNField;
    private javax.swing.JLabel SSNText;
    private javax.swing.JButton SearchButton;
    private javax.swing.JTextField SearchField;
    private javax.swing.JScrollPane SearchResults;
    private javax.swing.JLabel SearchResultsLabel;
    private javax.swing.JTextArea SearchResultsText;
    private javax.swing.JButton UpdateFinesButton;
    // End of variables declaration//GEN-END:variables
}

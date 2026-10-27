# LibraryFlow — Library Management System

A Java Swing desktop library management application with a modern dashboard, sidebar navigation, styled forms and tables, animated statistics, and toast notifications.

## Login and roles

The application now opens on a sign-in screen. Use one of these demo accounts:

| Role | Username | Password | Access |
|---|---|---|---|
| Administrator / Librarian | `admin` | `admin123` | Full existing management interface: books, members, borrowing, returns, and history |
| Member | `member` | `member123` | Dashboard, available books, and search of available books only |

Members do not see the administration navigation items or book-management form/remove action. Their book search results are filtered to available books only. The application also guards page navigation against member access to restricted pages. Use **Log out** in the header to return to the login screen.

**Demo authentication note:** This project stores library data in memory and does not have a user database. The credentials above are sample credentials for demonstrating role-based access, not production-grade authentication. For a deployed system, store users in a database and use salted password hashes.

## Requirements
- JDK 25 (as configured in `pom.xml`)
- Maven (recommended; downloads FlatLaf automatically)

## Run with Maven
From the project directory:

```bash
mvn compile exec:java
```

## Run directly with the JDK
If you have JDK 25 installed:

```bash
mkdir -p out
javac --release 25 -d out src/model/*.java src/service/*.java src/ui/*.java src/App.java
java -cp out App
```

FlatLaf is optional. When it is not on the classpath, the application falls back to Nimbus look and feel.

## Features
- Login screen and role-based Administrator/Member views
- Member dashboard with available-book count
- Member-only available-book listing and live search
- Administrator dashboard counts for total, available, and borrowed books, plus members
- Animated statistic counters and recent borrowing activity
- Sidebar navigation and administrative quick actions
- Add, search, list, and remove books
- Add, search, and list members
- Borrow available books and return borrowed books
- Transaction history with status highlighting
- Search-as-you-type for books and members
- Confirmation before removing a book
- Toast success notifications and readable validation errors
- Duplicate and empty-field validation
- `ArrayList` storage; no database required

## Data storage
Library data is held in memory and resets when the program closes. This preserves the project's original database-free behavior.

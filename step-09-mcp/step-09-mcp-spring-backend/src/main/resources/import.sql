DROP TABLE IF EXISTS books;
DROP TABLE IF EXISTS categories;

CREATE TABLE categories
(
    category_id SERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE books
(
    book_id        SERIAL PRIMARY KEY,
    isbn           VARCHAR(20)    NOT NULL UNIQUE,
    title          VARCHAR(255)   NOT NULL,
    author         VARCHAR(255)   NOT NULL,
    description    TEXT,
    price          DECIMAL(10, 2) NOT NULL,
    stock_quantity INTEGER        NOT NULL,
    category       VARCHAR(100)   NOT NULL,
    image_url      VARCHAR(255),
    CONSTRAINT fk_category FOREIGN KEY (category) REFERENCES categories (name)
);

CREATE INDEX idx_book_isbn ON books (isbn);
CREATE INDEX idx_book_title ON books (title);
CREATE INDEX idx_book_category ON books (category);

-- Insert Categories
INSERT INTO categories (name)
VALUES ('Programming');
INSERT INTO categories (name)
VALUES ('Jakarta EE');
INSERT INTO categories (name)
VALUES ('Software Design');
INSERT INTO categories (name)
VALUES ('Software Architecture');
INSERT INTO categories (name)
VALUES ('Software Development');
INSERT INTO categories (name)
VALUES ('Java');

-- Insert Books
INSERT INTO books (isbn, title, author, description, price, stock_quantity, category, image_url)
VALUES ('9781098165413', 'Modern Concurrency in Java', 'A N M Bazlur Rahman',
        'A Deep Dive into Virtual Threads, Structured Concurrency, and Scoped Values', 44.99, 30, 'Programming',
        '/images/modern-concurrency.png'),
       ('9781633435025', 'Build Smart Java Application with LLMs', 'A N M Bazlur Rahman and Syed M Shaaf',
        'A Practical Guide for Java Developers', 44.99, 30, 'Programming',
        '/images/build-smart-app-with-java-and-llm.jpeg'),
       ('1933988347', 'EJB 3 in Action', 'Reza Rahman, Michael Remijan, Debu Panda and Ryan Cuprak', 'Second Edition',
        50.99, 20, 'Jakarta EE', '/images/ejb3-in-action.png'),
       ('9798868802935', 'Helidon Revealed', 'Michael P. Redlich',
        'A Practical Guide to Oracle''s Microservices Framework', 59.99, 20, 'Jakarta EE',
        '/images/helidon-revealed.png'),                      -- Corrected escaping
       ('9780134685991', 'Effective Java', 'Joshua Bloch', 'The definitive guide to Java platform best practices',
        49.99, 50, 'Programming', '/images/effective-java.png'),
       ('9780596009205', 'Head First Design Patterns', 'Eric Freeman', 'A brain-friendly guide to design patterns',
        44.99, 30, 'Programming', '/images/head-first.png'),
       ('9781484277717', 'Beginning Jakarta EE Web Development', 'Anghel Leonard',
        'Build robust and scalable web applications using Jakarta EE specifications.', 39.99, 20, 'Jakarta EE',
        '/images/beginning-jakarta-ee.png'),
       ('9780132350884', 'Clean Code: A Handbook of Agile Software Craftsmanship', 'Robert C. Martin',
        'Even bad code can function. But if code isn''t clean, it can bring a development organization to its knees.',
        45.99, 55, 'Software Design', '/images/default.png'), -- Corrected escaping
       ('9780201633610', 'Design Patterns: Elements of Reusable Object-Oriented Software',
        'Erich Gamma, Richard Helm, Ralph Johnson, John Vlissides', 'Elements of Reusable Object-Oriented Software',
        52.50, 40, 'Software Design', '/images/default1.png'),
       ('9780137081073', 'The Clean Coder: A Code of Conduct for Professional Programmers', 'Robert C. Martin',
        'A Code of Conduct for Professional Programmers', 48.99, 60, 'Software Design', '/images/default2.png'),
       ('9781491950357', 'Building Microservices', 'Sam Newman',
        'Building Microservices: Designing Fine-Grained Systems', 55.00, 35, 'Software Architecture',
        '/images/default3.png'),
       ('9780134757599', 'Refactoring: Improving the Design of Existing Code', 'Martin Fowler',
        'Refactoring: Improving the Design of Existing Code', 42.99, 70, 'Software Design', '/images/default.png'),
       ('9780201616224', 'The Pragmatic Programmer: Your Journey To Mastery', 'Andy Hunt, David Thomas',
        'The Making of a Software Engineer', 38.99, 50, 'Software Development', '/images/default1.png'),
       ('9780321125217', 'Domain-Driven Design: Tackling Complexity in the Heart of Software', 'Eric Evans',
        'Tackling Complexity in the Heart of Software', 53.95, 45, 'Software Design', '/images/default2.png'),
       ('9780321127426', 'Patterns of Enterprise Application Architecture', 'Martin Fowler',
        'Patterns of Enterprise Application Architecture', 47.80, 30, 'Software Architecture', '/images/default3.png'),
       ('9780134052502', 'The Software Craftsman: Professionalism, Pragmatism, Pride', 'Sandro Mancuso',
        'The Software Craftsman', 49.99, 50, 'Software Development', '/images/default.png'),
       ('9780134494166', 'Clean Architecture: A Craftsman''s Guide to Software Structure and Design',
        'Robert C. Martin', 'Clean Architecture: A Craftsman''s Guide to Software Structure and Design', 46.50, 65,
        'Software Architecture', '/images/default1.png'),     -- Corrected escaping
       ('9781617299377', 'Spring in Action, 6th Edition', 'Craig Walls',
        'Covers Spring Boot 3, Core Framework 6, and More', 59.99, 75, 'Java', '/images/default2.png'),
       ('9781484272361', 'Spring Boot: Up and Running', 'Mark Heckler',
        'Simplifying Application Development with Spring Boot', 50.00, 40, 'Java', '/images/default3.png'),
       ('9780321349606', 'Java Concurrency in Practice',
        'Brian Goetz, Tim Peierls, Joshua Bloch, Joseph Bowbeer, David Holmes, Doug Lea', 'Concurrency in Practice',
        44.95, 30, 'Java', '/images/default.png');

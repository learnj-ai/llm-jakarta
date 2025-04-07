package learning.jakarta.ai.bookstore.repository;

import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import learning.jakarta.ai.bookstore.domain.Book;

import javax.sql.DataSource;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class BookRepository implements Serializable {

    @PersistenceContext
    private EntityManager entityManager;

    public List<Book> findAll() {
        return entityManager.createQuery("SELECT b FROM Book b", Book.class)
                .getResultList();
    }

    public List<String> findAllCategories() {
        return entityManager.createQuery("select DISTINCT b.category from Book b", String.class)
                .getResultList();
    }

    public Optional<Book> findByIsbn(String isbn) {
        return Optional.ofNullable(entityManager.find(Book.class, isbn));
    }

    @Transactional
    public void save(Book book) {
        if (entityManager.find(Book.class, book.getIsbn()) == null) {
            entityManager.persist(book);
        } else {
            entityManager.merge(book);
        }
    }

    @Transactional
    public void updateStockQuantity(String isbn, int newStock) {
        Book book = entityManager.find(Book.class, isbn);
        if (book != null) {
            book.setStockQuantity(newStock);
            entityManager.merge(book);
        }
    }

    @Transactional
    public void initializeDefaultBooks() {
        if (findAll().isEmpty()) {
            save(Book.builder()
                    .isbn("9781098165413")
                    .title("Modern Concurrency in Java")
                    .author("A N M Bazlur Rahman")
                    .description("A Deep Dive into Virtual Threads, Structured Concurrency, and Scoped Values")
                    .price(44.99)
                    .stockQuantity(30)
                    .category("Programming")
                    .imageUrl("/images/modern-concurrency.png")
                    .build());

            save(Book.builder()
                    .isbn("9781633435025")
                    .title("Build Smart Java Application with LLMs")
                    .author("A N M Bazlur Rahman and Syed M Shaaf")
                    .description("A Practical Guide for Java Developers")
                    .price(44.99)
                    .stockQuantity(30)
                    .category("Programming") // Assuming the trailing comma in SQL was a typo
                    .imageUrl("/images/build-smart-app-with-java-and-llm.jpeg")
                    .build());

            save(Book.builder()
                    .isbn("1933988347")
                    .title("EJB 3 in Action")
                    .author("Reza Rahman, Michael Remijan, Debu Panda and Ryan Cuprak")
                    .description("Second Edition")
                    .price(50.99)
                    .stockQuantity(20)
                    .category("Jakarta EE")
                    .imageUrl("/images/ejb3-in-action.png")
                    .build());

            save(Book.builder()
                    .isbn("9798868802935")
                    .title("Helidon Revealed")
                    .author("Michael P. Redlich")
                    .description("A Practical Guide to Oracle's Microservices Framework")
                    .price(59.99)
                    .stockQuantity(20)
                    .category("Jakarta EE")
                    .imageUrl("/images/helidon-revealed.png")
                    .build());




            save(Book.builder()
                    .isbn("978-0134685991")
                    .title("Effective Java")
                    .author("Joshua Bloch")
                    .description("The definitive guide to Java platform best practices")
                    .price(49.99)
                    .stockQuantity(50)
                    .category("Programming")
                    .imageUrl("/images/effective-java.png")
                    .build());

            save(Book.builder()
                    .isbn("978-0596009205")
                    .title("Head First Design Patterns")
                    .author("Eric Freeman")
                    .description("A brain-friendly guide to design patterns")
                    .price(44.99)
                    .stockQuantity(30)
                    .category("Programming")
                    .imageUrl("/images/head-first.png")
                    .build());

            save(Book.builder()
                    .isbn("9781484277717")
                    .title("Beginning Jakarta EE Web Development")
                    .author("Anghel Leonard")
                    .description("Build robust and scalable web applications using Jakarta EE specifications.")
                    .price(39.99)
                    .stockQuantity(20)
                    .category("Jakarta EE")
                    .imageUrl("/images/beginning-jakarta-ee.png")
                    .build());

            save(Book.builder()
                    .isbn("1933988347")
                    .title("EJB 3 in Action")
                    .author("Reza Rahman, Michael Remijan, Debu Panda and Ryan Cuprak")
                    .description("Second Edition")
                    .price(50.99)
                    .stockQuantity(20)
                    .category("Jakarta EE")
                    .imageUrl("/images/ejb3-in-action.png")
                    .build());

            save(Book.builder()
                    .isbn("9798868802935")
                    .title("Helidon Revealed")
                    .author("Michael P. Redlich")
                    .description("A Practical Guide to Oracle's Microservices Framework")
                    .price(59.99)
                    .stockQuantity(20)
                    .category("Jakarta EE")
                    .imageUrl("/images/helidon-revealed.png")
                    .build());

            save(Book.builder()
                    .isbn("978-0132350884")
                    .title("Clean Code: A Handbook of Agile Software Craftsmanship")
                    .author("Robert C. Martin")
                    .description("Even bad code can function. But if code isn't clean, it can bring a development organization to its knees.")
                    .price(45.99)
                    .stockQuantity(55)
                    .category("Software Design")
                    .imageUrl("") // Empty image URL from SQL
                    .build());

            save(Book.builder()
                    .isbn("978-0201633610")
                    .title("Design Patterns: Elements of Reusable Object-Oriented Software")
                    .author("Erich Gamma, Richard Helm, Ralph Johnson, John Vlissides")
                    .description("Elements of Reusable Object-Oriented Software")
                    .price(52.50)
                    .stockQuantity(40)
                    .category("Software Design")
                    .imageUrl("")
                    .build());

            save(Book.builder()
                    .isbn("978-0137081073")
                    .title("The Clean Coder: A Code of Conduct for Professional Programmers")
                    .author("Robert C. Martin")
                    .description("A Code of Conduct for Professional Programmers")
                    .price(48.99)
                    .stockQuantity(60)
                    .category("Software Design")
                    .imageUrl("")
                    .build());

            save(Book.builder()
                    .isbn("978-1491950357")
                    .title("Building Microservices")
                    .author("Sam Newman")
                    .description("Building Microservices: Designing Fine-Grained Systems")
                    .price(55.00)
                    .stockQuantity(35)
                    .category("Software Architecture")
                    .imageUrl("")
                    .build());

            save(Book.builder()
                    .isbn("978-0134757599")
                    .title("Refactoring: Improving the Design of Existing Code")
                    .author("Martin Fowler")
                    .description("Refactoring: Improving the Design of Existing Code")
                    .price(42.99)
                    .stockQuantity(70)
                    .category("Software Design")
                    .imageUrl("")
                    .build());

            save(Book.builder()
                    .isbn("978-0201616224")
                    .title("The Pragmatic Programmer: Your Journey To Mastery")
                    .author("Andy Hunt, David Thomas")
                    .description("The Making of a Software Engineer")
                    .price(38.99)
                    .stockQuantity(50)
                    .category("Software Development")
                    .imageUrl("")
                    .build());

            save(Book.builder()
                    .isbn("978-0321125217")
                    .title("Domain-Driven Design: Tackling Complexity in the Heart of Software")
                    .author("Eric Evans")
                    .description("Tackling Complexity in the Heart of Software")
                    .price(53.95)
                    .stockQuantity(45)
                    .category("Software Design")
                    .imageUrl("")
                    .build());

            save(Book.builder()
                    .isbn("978-0321127426")
                    .title("Patterns of Enterprise Application Architecture")
                    .author("Martin Fowler")
                    .description("Patterns of Enterprise Application Architecture")
                    .price(47.80)
                    .stockQuantity(30)
                    .category("Software Architecture")
                    .imageUrl("")
                    .build());

            save(Book.builder()
                    .isbn("978-0134052502")
                    .title("The Software Craftsman: Professionalism, Pragmatism, Pride")
                    .author("Sandro Mancuso")
                    .description("The Software Craftsman")
                    .price(49.99)
                    .stockQuantity(50)
                    .category("Software Development")
                    .imageUrl("")
                    .build());

            save(Book.builder()
                    .isbn("978-0134494166")
                    .title("Clean Architecture: A Craftsman's Guide to Software Structure and Design")
                    .author("Robert C. Martin")
                    .description("Clean Architecture: A Craftsman's Guide to Software Structure and Design")
                    .price(46.50)
                    .stockQuantity(65)
                    .category("Software Architecture")
                    .imageUrl("")
                    .build());

            save(Book.builder()
                    .isbn("978-1617299377")
                    .title("Spring in Action, 6th Edition")
                    .author("Craig Walls")
                    .description("Covers Spring Boot 3, Core Framework 6, and More")
                    .price(59.99)
                    .stockQuantity(75)
                    .category("Java")
                    .imageUrl("")
                    .build());

            save(Book.builder()
                    .isbn("978-1484272361")
                    .title("Spring Boot: Up and Running")
                    .author("Mark Heckler")
                    .description("Simplifying Application Development with Spring Boot")
                    .price(50.00)
                    .stockQuantity(40)
                    .category("Java")
                    .imageUrl("") // Assuming empty URL based on pattern
                    .build());


            save(Book.builder()
                    .isbn("978-0321349606")
                    .title("Java Concurrency in Practice")
                    .author("Brian Goetz, Tim Peierls, Joshua Bloch, Joseph Bowbeer, David Holmes, Doug Lea")
                    .description("Concurrency in Practice")
                    .price(44.95)
                    .stockQuantity(30)
                    .category("Java")
                    .imageUrl("")
                    .build());

            save(Book.builder()
                    .isbn("978-1449394711")
                    .title("Cloud Native Java: Designing Resilient Systems with Spring Boot, Spring Cloud, and Cloud Foundry")
                    .author("Josh Long, Kenny Bastani")
                    .description("Build and Deploy Cloud-Native Applications")
                    .price(54.99)
                    .stockQuantity(50)
                    .category("Java")
                    .imageUrl("")
                    .build());

            save(Book.builder()
                    .isbn("978-1617297014")
                    .title("JUnit in Action, Third Edition")
                    .author("Catalin Tudose")
                    .description("A Comprehensive Guide to JUnit Testing")
                    .price(41.99)
                    .stockQuantity(60)
                    .category("Java")
                    .imageUrl("")
                    .build());

            save(Book.builder()
                    .isbn("978-1492086978")
                    .title("Quarkus Cookbook")
                    .author("Alex Soto Bueno, Jason Porter")
                    .description("An illustrative guide to building cloud-native applications with Quarkus and Kubernetes")
                    .price(51.99)
                    .stockQuantity(25)
                    .category("Java")
                    .imageUrl("")
                    .build());

            save(Book.builder()
                    .isbn("978-0134853987")
                    .title("Effective Python: 90 Specific Ways to Write Better Python")
                    .author("Brett Slatkin")
                    .description("Get the most out of Python's libraries and tools")
                    .price(48.85)
                    .stockQuantity(35)
                    .category("Python")
                    .imageUrl("")
                    .build());

            save(Book.builder()
                    .isbn("978-1491957660")
                    .title("Python for Data Analysis")
                    .author("Wes McKinney")
                    .description("Python for Data Analysis, 2nd Edition")
                    .price(53.99)
                    .stockQuantity(45)
                    .category("Python")
                    .imageUrl("")
                    .build());

            save(Book.builder()
                    .isbn("978-1593279288")
                    .title("Python Crash Course, 2nd Edition")
                    .author("Eric Matthes")
                    .description("A Hands-On, Project-Based Introduction to Programming")
                    .price(39.99)
                    .stockQuantity(80)
                    .category("Python")
                    .imageUrl("")
                    .build());

            save(Book.builder()
                    .isbn("978-1491946008")
                    .title("Fluent Python: Clear, Concise, and Effective Programming")
                    .author("Luciano Ramalho")
                    .description("Concise techniques for Python developers")
                    .price(47.49)
                    .stockQuantity(55)
                    .category("Python")
                    .imageUrl("")
                    .build());

            save(Book.builder()
                    .isbn("978-1942788003")
                    .title("The DevOps Handbook")
                    .author("Gene Kim, Patrick Debois, John Willis, Jez Humble")
                    .description("Building and Scaling Dependable Systems")
                    .price(35.99)
                    .stockQuantity(70)
                    .category("DevOps")
                    .imageUrl("")
                    .build());

            save(Book.builder()
                    .isbn("978-1491929124")
                    .title("Site Reliability Engineering: How Google Runs Production Systems")
                    .author("Betsy Beyer, Chris Jones, Jennifer Petoff, Niall Richard Murphy")
                    .description("How Google Runs Production Systems")
                    .price(40.00)
                    .stockQuantity(60)
                    .category("DevOps")
                    .imageUrl("")
                    .build());

            save(Book.builder()
                    .isbn("978-1492081010")
                    .title("Infrastructure as Code")
                    .author("Kief Morris")
                    .description("Infrastructure as Code, 2nd Edition")
                    .price(49.99)
                    .stockQuantity(50)
                    .category("DevOps")
                    .imageUrl("")
                    .build());

            save(Book.builder()
                    .isbn("978-1492044404")
                    .title("Docker: Up & Running")
                    .author("Karl Matthias, Sean P. Kane")
                    .description("Up and Running: Infrastructure and Applications")
                    .price(54.95)
                    .stockQuantity(45)
                    .category("DevOps")
                    .imageUrl("")
                    .build());

            save(Book.builder()
                    .isbn("978-1491978199")
                    .title("Kubernetes: Up and Running")
                    .author("Brendan Burns, Joe Beda, Kelsey Hightower")
                    .description("Scheduling Containers for Distributed Systems")
                    .price(51.99)
                    .stockQuantity(55)
                    .category("Cloud")
                    .imageUrl("")
                    .build());

            save(Book.builder()
                    .isbn("978-1449373320")
                    .title("Designing Data-Intensive Applications")
                    .author("Martin Kleppmann")
                    .description("Designing Data-Intensive Applications")
                    .price(48.99)
                    .stockQuantity(60)
                    .category("Databases")
                    .imageUrl("")
                    .build());

            save(Book.builder()
                    .isbn("978-1942788294")
                    .title("The Phoenix Project")
                    .author("Gene Kim, Kevin Behr, George Spafford")
                    .description("A Novel About IT, DevOps, and Helping Your Business Win")
                    .price(39.95)
                    .stockQuantity(30)
                    .category("DevOps")
                    .imageUrl("")
                    .build());

            save(Book.builder()
                    .isbn("978-1801072101")
                    .title("Continuous Delivery with Docker and Jenkins")
                    .author("Alan Richardson")
                    .description("Building Continuous Delivery pipelines using Jenkins, Docker, and Kubernetes")
                    .price(45.50)
                    .stockQuantity(40)
                    .category("DevOps")
                    .imageUrl("")
                    .build());

            save(Book.builder()
                    .isbn("978-1449369415")
                    .title("Introduction to Machine Learning with Python")
                    .author("Andreas C. Müller, Sarah Guido")
                    .description("An In-Depth Guide for Practical Data Scientists")
                    .price(62.99)
                    .stockQuantity(35)
                    .category("AI/ML")
                    .imageUrl("")
                    .build());

            save(Book.builder()
                    .isbn("978-1492032649")
                    .title("Hands-On Machine Learning with Scikit-Learn, Keras, and TensorFlow")
                    .author("Aurélien Géron")
                    .description("Building Intelligent Systems with Python")
                    .price(57.99)
                    .stockQuantity(50)
                    .category("AI/ML")
                    .imageUrl("")
                    .build());

            save(Book.builder()
                    .isbn("978-0596517748")
                    .title("JavaScript: The Good Parts")
                    .author("Douglas Crockford")
                    .description("The Good Parts")
                    .price(44.99)
                    .stockQuantity(65)
                    .category("Web Development")
                    .imageUrl("")
                    .build());

            save(Book.builder()
                    .isbn("978-1593279509")
                    .title("Eloquent JavaScript, 3rd Edition")
                    .author("Marijn Haverbeke")
                    .description("A Modern Introduction to Programming")
                    .price(40.99)
                    .stockQuantity(70)
                    .category("Web Development")
                    .imageUrl("")
                    .build());

            save(Book.builder()
                    .isbn("978-1491952023")
                    .title("JavaScript: The Definitive Guide")
                    .author("David Flanagan")
                    .description("Mastering the World's Most-Used Programming Language")
                    .price(53.50)
                    .stockQuantity(50)
                    .category("Web Development")
                    .imageUrl("")
                    .build());

            save(Book.builder()
                    .isbn("978-1720271895")
                    .title("The Road to React")
                    .author("Robin Wieruch")
                    .description("Modern Web Development with React")
                    .price(49.99)
                    .stockQuantity(45)
                    .category("Web Development")
                    .imageUrl("")
                    .build());

            save(Book.builder()
                    .isbn("978-1492041496")
                    .title("You Don't Know JS Yet: Get Started")
                    .author("Kyle Simpson")
                    .description("You Don't Know JS Yet: Get Started")
                    .price(46.80)
                    .stockQuantity(60)
                    .category("Web Development")
                    .imageUrl("")
                    .build());

            save(Book.builder()
                    .isbn("978-1118008188")
                    .title("HTML and CSS: Design and Build Websites")
                    .author("Jon Duckett")
                    .description("Covers HTML5, CSS3, and JavaScript")
                    .price(42.99)
                    .stockQuantity(55)
                    .category("Web Development")
                    .imageUrl("")
                    .build());

            save(Book.builder()
                    .isbn("978-1492051907")
                    .title("Learning React, 2nd Edition")
                    .author("Alex Banks, Eve Porcello")
                    .description("Build interactive UIs with React, Redux, MobX, Node, and GraphQL")
                    .price(47.95)
                    .stockQuantity(40)
                    .category("Web Development")
                    .imageUrl("")
                    .build());

            save(Book.builder()
                    .isbn("978-1095322410")
                    .title("Node.js Design Patterns")
                    .author("Basarat Ali Syed")
                    .description("Learn Node.js by building real-world applications")
                    .price(38.50)
                    .stockQuantity(65)
                    .category("Web Development")
                    .imageUrl("")
                    .build());

            save(Book.builder()
                    .isbn("978-1491962411")
                    .title("CSS: The Missing Manual")
                    .author("David Sawyer McFarland")
                    .description("The Missing Manual")
                    .price(50.00)
                    .stockQuantity(30)
                    .category("Web Development")
                    .imageUrl("")
                    .build());

            save(Book.builder()
                    .isbn("978-0991344611")
                    .title("ng-book: The Complete Book on Angular")
                    .author("Nate Murray, Felipe Coury, Ari Lerner, Carlos Taborda")
                    .description("Build applications with Angular and TypeScript")
                    .price(44.99)
                    .stockQuantity(50)
                    .category("Web Development")
                    .imageUrl("")
                    .build());

            save(Book.builder()
                    .isbn("978-1934356555")
                    .title("SQL Antipatterns")
                    .author("Bill Karwin")
                    .description("SQL Antipatterns: Avoiding the Pitfalls of Database Programming")
                    .price(55.99)
                    .stockQuantity(40)
                    .category("Databases")
                    .imageUrl("")
                    .build());

            save(Book.builder()
                    .isbn("978-0201896831")
                    .title("The Art of Computer Programming, Volume 1")
                    .author("Donald E. Knuth")
                    .description("The Art of Computer Programming, Volume 1: Fundamental Algorithms")
                    .price(60.50)
                    .stockQuantity(35)
                    .category("Computer Science")
                    .imageUrl("")
                    .build());

            save(Book.builder()
                    .isbn("978-1617292231")
                    .title("Grokking Algorithms")
                    .author("Aditya Y. Bhargava")
                    .description("An illustrated guide for programmers and other curious people")
                    .price(49.95)
                    .stockQuantity(60)
                    .category("Computer Science")
                    .imageUrl("")
                    .build());

            save(Book.builder()
                    .isbn("978-0262033848")
                    .title("Introduction to Algorithms")
                    .author("Thomas H. Cormen, Charles E. Leiserson, Ronald L. Rivest, Clifford Stein")
                    .description("Introduction to Algorithms, 3rd Edition")
                    .price(68.00)
                    .stockQuantity(25)
                    .category("Computer Science")
                    .imageUrl("")
                    .build());

            save(Book.builder()
                    .isbn("978-1680507225")
                    .title("A Common-Sense Guide to Data Structures and Algorithms")
                    .author("Jay Wengrow")
                    .description("A Common-Sense Guide to Data Structures and Algorithms")
                    .price(41.95)
                    .stockQuantity(55)
                    .category("Computer Science")
                    .imageUrl("")
                    .build());

            save(Book.builder()
                    .isbn("978-0321704542")
                    .title("NoSQL Distilled")
                    .author("Pramod J. Sadalage, Martin Fowler")
                    .description("The Definitive Guide, 2nd Edition")
                    .price(52.99)
                    .stockQuantity(48)
                    .category("Databases")
                    .imageUrl("")
                    .build());

            save(Book.builder()
                    .isbn("978-1491936160")
                    .title("Kafka: The Definitive Guide")
                    .author("Neha Narkhede, Gwen Shapira, Todd Palino")
                    .description("Building Reliable Systems with Kafka")
                    .price(47.80)
                    .stockQuantity(42)
                    .category("Messaging")
                    .imageUrl("")
                    .build());

            save(Book.builder()
                    .isbn("978-1492057619")
                    .title("Learning SQL")
                    .author("Alan Beaulieu")
                    .description("Learning SQL, 3rd Edition")
                    .price(43.99)
                    .stockQuantity(66)
                    .category("Databases")
                    .imageUrl("")
                    .build());

            save(Book.builder()
                    .isbn("978-0262510875")
                    .title("Structure and Interpretation of Computer Programs (SICP)")
                    .author("Harold Abelson, Gerald Jay Sussman, Julie Sussman")
                    .description("Structure and Interpretation of Computer Programs")
                    .price(58.00)
                    .stockQuantity(33)
                    .category("Computer Science")
                    .imageUrl("")
                    .build());

            save(Book.builder()
                    .isbn("978-1449337538")
                    .title("Redis in Action")
                    .author("Josiah L. Carlson")
                    .description("The Definitive Guide to Understanding the Redis Data Structure Server")
                    .price(37.50)
                    .stockQuantity(77)
                    .category("Databases")
                    .imageUrl("")
                    .build());
        }
    }
}
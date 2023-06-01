/*
 * Copyright (C) 2022 IUT Laval - Le Mans Université.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package mmiLibraryServer.runners;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import mmiLibraryServer.mongoModel.Author;
import mmiLibraryServer.mongoModel.Book;
import mmiLibraryServer.mongoModel.BookCategory;
import mmiLibraryServer.mongoModel.BookCategoryRepository;
import mmiLibraryServer.mongoModel.BookCopy;
import mmiLibraryServer.mongoModel.BookCopyRepository;
import mmiLibraryServer.mongoModel.BookRepository;
import mmiLibraryServer.mongoModel.BookState;
import mmiLibraryServer.mongoModel.Loan;
import mmiLibraryServer.mongoModel.LoanRepository;
import mmiLibraryServer.mongoModel.Member;
import mmiLibraryServer.mongoModel.MemberRepository;
import mmiLibraryServer.services.BookService;
import mmiLibraryServer.services.CategoryService;
import mmiLibraryServer.services.LoanService;
import mmiLibraryServer.services.MemberService;
import mmiLibraryServer.services.exceptions.HasOngoingLoanException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 *
 * @author Rémi Venant
 */
@Profile("sample-data")
@Component
public class SampleDataLoader implements CommandLineRunner {

    private static final Log LOG = LogFactory.getLog(SampleDataLoader.class);

    private final MemberService memberSvc;

    private final CategoryService categorySvc;

    private final BookService bookSvc;

    private final LoanService loanSvc;

    private final MemberRepository memberRepo;

    private final BookCategoryRepository bookCatRepo;

    private final BookRepository bookRepo;

    private final BookCopyRepository bookCopyRepo;

    private final LoanRepository loanRepo;

    private final boolean alwayResetData;

    @Autowired
    public SampleDataLoader(MemberService memberSvc, CategoryService categorySvc,
            BookService bookSvc, LoanService loanSvc, MemberRepository memberRepo,
            BookCategoryRepository bookCatRepo, BookRepository bookRepo,
            BookCopyRepository bookCopyRepo, LoanRepository loanRepo,
            @Value("${mmiLibraryServer.sampleData.alwayResetData:false}") boolean alwayResetData) {
        this.memberSvc = memberSvc;
        this.categorySvc = categorySvc;
        this.bookSvc = bookSvc;
        this.loanSvc = loanSvc;
        this.memberRepo = memberRepo;
        this.bookCatRepo = bookCatRepo;
        this.bookRepo = bookRepo;
        this.bookCopyRepo = bookCopyRepo;
        this.loanRepo = loanRepo;
        this.alwayResetData = alwayResetData;
    }

    @Override
    public void run(String... args) throws Exception {
        LOG.info("START sampling data...");
        if (!alwayResetData && this.hasDataInDb()) {
            LOG.info("Database contains data. do not sample data.");
            return;
        }
        if (alwayResetData) {
            LOG.info("Reset data from db.");
            this.resetData();
        }
        LOG.info("Create and save sample members...");
        final Map<String, Member> membersByName = this.createMembers();
        LOG.info(String.format("%d members created", membersByName.size()));

        LOG.info("Create and save sample book categories...");
        final Map<String, BookCategory> categoriesByCode = this.createBookCategories();
        LOG.info(String.format("%d book categories created", categoriesByCode.size()));

        LOG.info("Create and save sample books...");
        final Map<String, Book> bookByIsbn = this.createBooks(categoriesByCode);
        LOG.info(String.format("%d books created", bookByIsbn.size()));

        LOG.info("Create and save sample book copies...");
        final Map<String, List<BookCopy>> bookCopiesByIsbn = this.createBookCopies(bookByIsbn);
        LOG.info(String.format("%d book copies created",
                bookCopiesByIsbn.values().stream().flatMap(List::stream).count()));

        LOG.info("Create and save sample loans...");
        final List<Loan> loans = this.createLoans(membersByName, bookCopiesByIsbn);
        LOG.info(String.format("%d loans created", loans.size()));

        LOG.info("END sampling data...");
    }

    private boolean hasDataInDb() {
        long dataCount = this.memberRepo.count() + this.bookCatRepo.count()
                + this.bookRepo.count() + this.bookCopyRepo.count() + this.loanRepo.count();
        return dataCount > 0;
    }

    private void resetData() {
        this.loanRepo.deleteAll();
        this.bookCopyRepo.deleteAll();
        this.bookRepo.deleteAll();
        this.bookCatRepo.deleteAll();
        this.memberRepo.deleteAll();
    }

    /**
     * Create sample members.
     *
     * @return members by name
     */
    private Map<String, Member> createMembers() {
        final HashMap<String, Member> members = new HashMap<>();
        members.put("planck", this.memberSvc.createMember(new Member("Planck", "Max", generateRandomBirthday(false))));
        members.put("solvay", this.memberSvc.createMember(new Member("Solvay", "Ernest", generateRandomBirthday(false))));
        members.put("lorentz", this.memberSvc.createMember(new Member("Lorentz", "Hendrick", generateRandomBirthday(false))));
        members.put("enstein", this.memberSvc.createMember(new Member("Enstein", "Albert", generateRandomBirthday(true)))); // child
        members.put("curie", this.memberSvc.createMember(new Member("Curie", "Marie", generateRandomBirthday(true)))); // child
        return members;
    }

    /**
     * Create sample book categories
     *
     * @return categories by code
     */
    private Map<String, BookCategory> createBookCategories() {
        final HashMap<String, BookCategory> categories = new HashMap<>();
        categories.put("polar", this.categorySvc.createCategory(new BookCategory("polar", "Policier", false)));
        categories.put("love", this.categorySvc.createCategory(new BookCategory("love", "Amour", false)));
        categories.put("sf", this.categorySvc.createCategory(new BookCategory("sf", "Science-Fiction", false)));
        categories.put("violence", this.categorySvc.createCategory(new BookCategory("violence", "Violent", true)));
        categories.put("sex", this.categorySvc.createCategory(new BookCategory("sex", "Sexe", true)));
        return categories;
    }

    /**
     * Create sample books
     *
     * @param categories categories by code
     * @return book by isbn
     */
    private Map<String, Book> createBooks(Map<String, BookCategory> categories) {
        final HashMap<String, Book> books = new HashMap<>();
        //Un polar
        books.put("0001", this.bookSvc.createBook(new Book("9782070495023", "Total Kheops", "Folio",
                284, 2001, List.of(new Author("Jean-Claude", "Izzo")),
                List.of(categories.get("polar")))));
        //Un polar violent
        books.put("0002", this.bookSvc.createBook(new Book("9782757800232", "La cité des jarres",
                "Points", 336, 2006, List.of(new Author("Indriðason", "Arnaldur")),
                List.of(categories.get("polar"), categories.get("violence")))));
        //Un sf
        books.put("0003", this.bookSvc.createBook(new Book("9782367934372", "Les voyageurs, tome 1 : L'Espace d'un an ", "L'Atalante",
                448, 2016, List.of(new Author("Chambers", "Becky")),
                List.of(categories.get("sf")))));
        //Un amour+ sexe
        books.put("0004", this.bookSvc.createBook(new Book("9782264031150", "Amour, Prozac et autres curiosités ",
                "10/18", 288, 2005, List.of(new Author("Etxebarria", "Lucia")),
                List.of(categories.get("love"), categories.get("sex")))));
        return books;
    }

    /**
     * Create sample book copies
     *
     * @param books books by isb
     * @return book copies by book isbn
     */
    private Map<String, List<BookCopy>> createBookCopies(Map<String, Book> books) throws HasOngoingLoanException {
        final HashMap<String, List<BookCopy>> bookCopies = new HashMap<>();
        //des copies dans diverses etats
        bookCopies.put("0001", List.of(
                this.bookSvc.createBookCopies(books.get("0001").getId(), 2, BookState.GOOD),
                this.bookSvc.createBookCopies(books.get("0001").getId(), 1, BookState.USED)
        ).stream().flatMap(List::stream).collect(Collectors.toList()));
        bookCopies.put("0002", List.of(
                this.bookSvc.createBookCopies(books.get("0002").getId(), 2, BookState.VERY_GOOD),
                this.bookSvc.createBookCopies(books.get("0002").getId(), 1, BookState.GOOD),
                this.bookSvc.createBookCopies(books.get("0002").getId(), 1, BookState.BAD)
        ).stream().flatMap(List::stream).collect(Collectors.toList()));
        bookCopies.put("0003", List.of(
                this.bookSvc.createBookCopies(books.get("0003").getId(), 2, BookState.VERY_GOOD),
                this.bookSvc.createBookCopies(books.get("0003").getId(), 1, BookState.NEW)
        ).stream().flatMap(List::stream).collect(Collectors.toList()));
        bookCopies.put("0004", List.of(
                this.bookSvc.createBookCopies(books.get("0004").getId(), 3, BookState.VERY_GOOD),
                this.bookSvc.createBookCopies(books.get("0004").getId(), 2, BookState.NEW),
                this.bookSvc.createBookCopies(books.get("0004").getId(), 1, BookState.USED)
        ).stream().flatMap(List::stream).collect(Collectors.toList()));
        //remove two copies
        BookCopy bcToRemove = bookCopies.get("0002").get(3);
        bcToRemove.setRemoved(true);
        this.bookSvc.updateBookCopy(bcToRemove.getBook().getId(), bcToRemove);
        bookCopies.get("0004").get(5);
        bcToRemove.setRemoved(true);
        this.bookSvc.updateBookCopy(bcToRemove.getBook().getId(), bcToRemove);

        return bookCopies;
    }

    /**
     * Create sample loans
     *
     * @param members members by name
     * @param bookCopies book copies by book isbn
     * @return loans
     */
    private List<Loan> createLoans(Map<String, Member> members, Map<String, List<BookCopy>> bookCopies) {
        // On créer 5 emprunts passés pour des bc diff et 2 emprunts en cours pour d'autre bc et 2 emprunt en cours pour des bc emprunté
        List<Loan> loansToCreate = List.of(
                //emprunts passés
                createReturnedLoan(members.get("planck"), bookCopies.get("0001").get(0)),
                createReturnedLoan(members.get("lorentz"), bookCopies.get("0004").get(0)),
                createReturnedLoan(members.get("planck"), bookCopies.get("0002").get(1)),
                createReturnedLoan(members.get("enstein"), bookCopies.get("0003").get(1)),
                createReturnedLoan(members.get("curie"), bookCopies.get("0001").get(1)),
                //emprunt en cours sur d'autre copies
                createUnreturnedLoan(members.get("lorentz"), bookCopies.get("0002").get(0)),
                createUnreturnedLoan(members.get("curie"), bookCopies.get("0003").get(0)),
                //emprunt en cours sur des copies déjà emprunté
                createUnreturnedLoan(members.get("enstein"), bookCopies.get("0001").get(0)),
                createUnreturnedLoan(members.get("solvay"), bookCopies.get("0004").get(0))
        );
        // Met à jour les copies qui ne sont plus dispo
        List<BookCopy> unavailableBcs = List.of(bookCopies.get("0002").get(0), bookCopies.get("0003").get(0),
                bookCopies.get("0001").get(0), bookCopies.get("0004").get(0));
        unavailableBcs.forEach(bc -> {
            bc.setAvailable(false);
        });
        this.bookCopyRepo.saveAll(unavailableBcs);

        // Sauvegarde et retour de la liste des instances créées
        return StreamSupport.stream(this.loanRepo.saveAll(loansToCreate).spliterator(), false)
                .collect(Collectors.toList());
    }

    private static Loan createUnreturnedLoan(Member member, BookCopy copy) {
        //Une date entre aujourd'hui et il y a 20 jour
        LocalDateTime loanDate = generateNow().withHour(generateRandomHour()).withMinute(generateMinutes()).minusDays(randomLong(0, 20));
        return new Loan(member, copy, loanDate, copy.getState());
    }

    private static Loan createReturnedLoan(Member member, BookCopy copy) {
        assert copy.getState() != BookState.NEW;
        //Une date de retour entre il y a 30 et il y a 365 jours
        LocalDateTime returnDate = generateNow().withHour(generateRandomHour()).withMinute(generateMinutes()).minusDays(randomLong(30, 365));
        //Une date d'emprunt entre la date de retour - 10 jour et date de retour - 30
        LocalDateTime loanDate = returnDate.minusDays(randomLong(10, 30));
        //Un état d'emprunt, forcement > à l'état de retour (de la copie)
        BookState loanState = generateRandomState(copy.getState(), false, BookState.NEW, true);
        final Loan loan = new Loan(member, copy, loanDate, loanState);
        loan.setReturnDateTime(returnDate);
        loan.setReturnState(copy.getState());
        return loan;
    }

    private static int generateRandomHour() {
        return (int) (Math.random() * (19 - 9)) + 9;
    }

    private static int generateMinutes() {
        return (int) (Math.random() * 60);
    }

    private static LocalDateTime generateNow() {
        return LocalDateTime.now().withNano(0).withSecond(0);
    }

    private static BookState generateRandomState(BookState minBookState, boolean minInclusive,
            BookState maxBookState, boolean maxInclusive) {
        int minState = minBookState.getValue() + (minInclusive ? 0 : 1);
        int maxState = maxBookState.getValue() + (maxInclusive ? 1 : 0);
        if (maxState < minState) {
            throw new IllegalArgumentException("Max state < min state");
        }
        int randBookValue = (int) (Math.random() * (maxState - minState)) + minState;
        return BookState.fromValue(randBookValue);
    }

    private static LocalDate generateRandomBirthday(boolean child) {
        LocalDate birthday = LocalDate.now().minusYears(Member.MAJOR_YEAR_LIMIT);
        if (child) {
            long maxPlusDays = 10 * 365;
            birthday = birthday.plusDays(randomLong(1, maxPlusDays));
        } else {
            long maxMinusDays = 50 * 365;
            birthday = birthday.minusDays(randomLong(0, maxMinusDays));
        }
        return birthday;
    }

    private static long randomLong(long min, long max) {
        return ((long) (Math.random() * ((double) (max - min)))) + min;
    }

}

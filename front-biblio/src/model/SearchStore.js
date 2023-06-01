import { makeAutoObservable } from "mobx";

class BookSearchStore {
    searchParams = {
        title: "",
        categories: [],
        author: "",
        publicationYear: "",
        numOfPages: "",
        adultOnly: false,
    };

    searchResult = [];
    isLoading = false;
    error = null;

    constructor(bookStore) {
        this.bookStore = bookStore;
        makeAutoObservable(this);
    }

    setSearchParams(searchParams) {
        this.searchParams = searchParams;
    }

    setSearchResult(searchResult) {
        this.searchResult = searchResult;
    }

    setIsLoading(isLoading) {
        this.isLoading = isLoading;
    }

    setError(error) {
        this.error = error;
    }

    async searchBooks() {
        this.isLoading = true;
        this.error = null;
        try {
            const queryParams = new URLSearchParams(this.searchParams);
            const response = await fetch(
                `http://127.0.0.1:8080/api/v1/rest/books?${queryParams}`
            );
            const data = await response.json();
            const filteredData = data.filter((book) =>
                book.copies.some((copy) => !copy.removed && copy.available)
            );
            this.searchResult = filteredData.map((book) => ({
                ...book,
                copies: book.copies.filter((copy) => !copy.removed && copy.available),
            }));
            this.isLoading = false;
        } catch (error) {
            console.log("Erreur de recherche des livres", error);
            this.isLoading = false;
            this.error = error;
        }
    }

    async searchCopies() {
        const { title, categories, author, publicationYear, numOfPages, adultOnly } =
            this.searchParams;

        const filteredBooks = this.searchResult.filter((book) => {
            let matchesTitle = true;
            let matchesCategories = true;
            let matchesAuthor = true;
            let matchesPublicationYear = true;
            let matchesNumOfPages = true;
            let matchesAdultOnly = true;

            if (title) {
                matchesTitle = book.title.toLowerCase().includes(title.toLowerCase());
            }

            if (categories.length > 0) {
                matchesCategories = categories.every((category) =>
                    book.categories.includes(category)
                );
            }

            if (author) {
                matchesAuthor = book.author.toLowerCase().includes(author.toLowerCase());
            }

            if (publicationYear) {
                matchesPublicationYear =
                    parseInt(book.publicationYear) === parseInt(publicationYear);
            }

            if (numOfPages) {
                matchesNumOfPages = parseInt(book.numOfPages) === parseInt(numOfPages);
            }

            if (adultOnly) {
                matchesAdultOnly = book.adultOnly;
            }

            return (
                matchesTitle &&
                matchesCategories &&
                matchesAuthor &&
                matchesPublicationYear &&
                matchesNumOfPages &&
                matchesAdultOnly
            );
        });

        const filteredCopies = [];

        for (const book of filteredBooks) {
            await this.bookStore.fetchBookDetails(book.id);

            const copies = this.bookStore.bookDetails.copies.filter(
                (copy) => !copy.removed && copy.available
            );

            filteredCopies.push(...copies);
        }

        return filteredCopies;
    }
}
export { BookSearchStore };
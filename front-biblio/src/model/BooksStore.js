import { makeAutoObservable, runInAction } from "mobx";
import { useParams } from 'react-router-dom';
import axios from 'axios';
class BooksStore {
  books = [];
  bookDetails = null;
  bookLoans = [];
  isLoading = false;
  confirmationModal = null;
  confirmationAction = null;
  confirmationMessage = "";
  error = null;
  searchCriteria = {
    isbn: "",
    title: "",
    author: "",
    publicationYear: "",
    pageCount: "",
    adultContent: false,
  };

  constructor() {
    makeAutoObservable(this)
  }

  // CHARGEMENT DES LIVRES
  async fetchBooks() {
    this.isLoading = true;
    this.error = null;
    try {
      const response = await fetch("http://127.0.0.1:8080/api/v1/rest/books");
      const data = await response.json();
      runInAction(() => {
        this.books = data;
        this.isLoading = false;
      })

    } catch (error) {
      console.log("Erreur de récupération des livres", error);
      this.isLoading = false;
      this.error = error;
    }
  }

  // CHARGEMENT D'UN LIVRE
  async fetchBookDetails(bookId) {
    this.isLoading = true;
    this.error = null;
    try {
      const response = await fetch(`http://127.0.0.1:8080/api/v1/rest/books/${bookId}`);
      const data = await response.json();
      runInAction(() => {
        this.bookDetails = data;
        this.isLoading = false;
      })
    } catch (error) {
      console.log(`Erreur de récupération des détails du livre ${bookId}`, error);
      this.isLoading = false;
      this.error = error;
    }
  }

  // Livres Disponible à l'emprunt
  async fetchBookToLoan() {
    this.isLoading = true;
    this.error = null;
    try {
      // Chargement des livres
      const response = await fetch("http://127.0.0.1:8080/api/v1/rest/books");
      const data = await response.json();
      const booksWithCopy = await Promise.all(
        data.map(async (book) => {
          const { id } = book;
          const response = await fetch(
            // Récupération du détail
            `http://127.0.0.1:8080/api/v1/rest/books/${id}`
          );
          const data = await response.json();
          if (data.copies && data.copies.length > 0) {
            return book;
          }
        })
      );
      runInAction(() => {
        // Filtre de si il y a au moins une copie
        this.books = booksWithCopy.filter((book) => book !== undefined);
        this.isLoading = false;
      });
    } catch (error) {
      console.log("Erreur de récupération des livres", error);
      this.isLoading = false;
      this.error = error;
    }
  }

  // AJOUT D'UN LIVRE
  async createBook(newBook) {
    this.isLoading = true;
    this.error = null;
    try {
      const response = await fetch("http://127.0.0.1:8080/api/v1/rest/books", {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify(newBook)
      });
      const data = await response.json();
      runInAction(() => {
        this.books.push(data);
        this.isLoading = false;
      })

    } catch (error) {
      console.log("Erreur de création du livre", error);
      this.isLoading = false;
      this.error = error;
    }
  }


  // AJOUT D'UNE COPIE
  async createCopie({ id, newCopie }) {
    this.isLoading = true;
    this.error = null;
    try {
      const response = await fetch(`http://127.0.0.1:8080/api/v1/rest/books/${id}`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify(newCopie)
      });
      const data = await response.json();
      runInAction(() => {
        this.copie.push(data);
        this.isLoading = false;
      })

    } catch (error) {
      console.log("Erreur de création du livre", error);
      this.isLoading = false;
      this.error = error;
    }
  }


  // EDIT D'UN LIVRE 
  async editBook(id, updateBook = { isbn: '', title: '', editor: '', numOfPages: '', publicationYear: '' }) {
    fetch(`http://127.0.0.1:8080/api/v1/rest/books/${id}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ ...updateBook, id }),
    })
      .then((res) => res.json())
      .then(
        (updateBook) => {
          const idx = this.books.findIndex((b) => b.id === id);
          if (idx === -1) {
            console.warn('ça devrait pas');
            return;
          }
          runInAction(() => {
            this.books.splice(idx, 1, updateBook);
          });
        }
      ).catch(
        (error) => {
          runInAction(() => {
            this.error = error;
          });
          throw error;
        });
  }

  // EDIT D'UNE COPIE 
  async editCopie(id, updateCopie = { state: '', removed: '', available: '' }) {
    fetch(`http://127.0.0.1:8080/api/v1/rest/books/${id}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ ...updateCopie, id }),
    })
      .then((res) => res.json())
      .then(
        (updateCopie) => {
          const idx = this.copies.findIndex((c) => c.id === id);
          runInAction(() => {
            this.copies.splice(idx, 1, updateCopie);
          });
        }
      ).catch(
        (error) => {
          runInAction(() => {
            this.error = error;
          });
          throw error;
        });
  }

    // SUPPRESSION
    async removeCopie(id) {
      try {
        const response = await fetch(`http://127.0.0.1:8080/api/v1/rest/books/${id}`, {
          method: "DELETE",
        });
  
        if (response.ok) {
          this.books = this.books.filter((book) => book.id !== id);
          this.confirmationMessage = "books removed successfully";
        } else {
          this.confirmationMessage = "Error removing books";
        }
      } catch (error) {
        console.log("Error removing books:", error);
        this.confirmationMessage = "Error removing books";
      }
    }

  // RECHERCHE
  async searchBooks(searchCriteria) {
    this.isLoading = true;
    this.error = null;
    try {
      const queryParams = new URLSearchParams(searchCriteria).toString();
      const response = await axios.get(`http://127.0.0.1:8080/api/v1/rest/books?${queryParams}`);
      const data = await response.json();
      runInAction(() => {
        this.books = data;
        this.isLoading = false;
      });
    } catch (error) {
      console.log(`Erreur de recherche des livres`, error);
      this.isLoading = false;
      this.error = error;
    }
  }
}
export { BooksStore };
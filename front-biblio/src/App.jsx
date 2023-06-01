import React from 'react';
import { Container } from 'react-bootstrap';
import { BrowserRouter, Route, Routes } from 'react-router-dom';
import RootStore from './RootStore';
import STORE from './store';
// IMPORT DES COMPOSANTS
import AppNavbar from './components/AppNavbar';
import Members from './components/membres/MembersPage';
import BooKsPage from './components/livres/BooksPage';
import BookSearch from './components/recherche/SearchPage';
import Emprunts from './components/Prets/LoansPage';
import Statistics from './components/Statistiques/StatisticsPage';
import Categories from './components/categories/CategoryPage';
import BookDetails from './components/livres/Book_Detail';
import LoanBook from './components/Prets/Loan_Copie';

function App() {
  return (
    <BrowserRouter basename={APP_ENV.APP_PUBLIC_PATH}>
      <RootStore.Provider value={STORE}>
        <AppNavbar />
        <main>
          <Container>
            <Routes>
              <Route path="/" element={<Members />} />
              <Route path="/livres" element={<BooKsPage />} />
              <Route path="/categories" element={<Categories />} />
              <Route path="/recherche" element={<BookSearch />} />
              <Route path="/emprunts" element={<Emprunts />} />
              <Route path="/statistiques" element={<Statistics />} />
              <Route path="/livres/books/:bookId" element={<BookDetails />} />
              <Route path="/emprunts/emprunt/:bookId" element={<LoanBook />} />
            </Routes>
          </Container>
        </main>
      </RootStore.Provider>
    </BrowserRouter>

  );
}

export default App;

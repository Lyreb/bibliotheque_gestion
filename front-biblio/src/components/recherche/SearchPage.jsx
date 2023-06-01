import React, { useState, useContext, useEffect } from 'react';
import RootStore from '../../RootStore';
import { Card, Container, Form, Row, Col } from 'react-bootstrap';
import { observer } from 'mobx-react-lite';

function BookSearch() {
  const { categoryStore } = useContext(RootStore)
  const [searchValue, setSearchValue] = useState("");
  const [categoryValue, setCategoryValue] = useState("");
  const [authorValue, setAuthorValue] = useState("");
  const [yearValue, setYearValue] = useState("");
  const [pagesValue, setPagesValue] = useState("");
  const [results, setResults] = useState([]);
  const [categories, setCategories] = useState([]);

  // Charger les catégories
  useEffect(() => {
    categoryStore.fetchCategories();
  }, []);

  const fetchData = () => {
    fetch("http://127.0.0.1:8080/api/v1/rest/books")
      .then((response) => response.json())
      .then(json => {
        let filteredResults = json;

        if (searchValue) {
          filteredResults = filteredResults.filter((book) => {
            return (
              book.title && book.title.toLowerCase().includes(searchValue) ||
              book.isbn && book.isbn.toLowerCase().includes(searchValue)
            );
          });
        }

        if (categoryValue) {
          filteredResults = filteredResults.filter((book) => {
            return (
              book.category && book.category.toLowerCase().includes(categoryValue)
            );
          });
        }

        if (authorValue) {
          filteredResults = filteredResults.filter((book) => {
            return (
              book.author && book.author.toLowerCase().includes(authorValue)
            );
          });
        }

        if (yearValue) {
          filteredResults = filteredResults.filter((book) => {
            return (
              book.publicationYear && book.publicationYear.toString().includes(yearValue)
            );
          });
        }

        if (pagesValue) {
          filteredResults = filteredResults.filter((book) => {
            return (
              book.numOfPages && book.numOfPages.toString().includes(pagesValue)
            );
          });
        }
        setResults(filteredResults)
      });
  }

  const handleChange = (value) => {
    setSearchValue(value)
    fetchData()
  }

  const handleCategoryChange = (value) => {
    setCategoryValue(value)
    fetchData()
  }

  const handleAuthorChange = (value) => {
    setAuthorValue(value)
    fetchData()
  }

  const handleYearChange = (value) => {
    setYearValue(value)
    fetchData()
  }

  const handlePagesChange = (value) => {
    setPagesValue(value)
    fetchData()
  }

  return (
    <Container style={{ margin: '80px', display: 'flex', flexDirection: 'row' }}>
      <div style={{ width: '300px', marginRight: '50px' }}>
        <h3 style={{ marginBottom: '20px' }}>Liste des livres</h3>
        <Form.Group>
          <Form.Label>Recherche par titre ou ISBN :</Form.Label>
          <Form.Control
            type="text"
            placeholder="Entrez le titre ou l'ISBN..."
            value={searchValue}
            onChange={(e) => handleChange(e.target.value)}
          />
        </Form.Group>
        <Form.Group>
          <Form.Label>Catégories :</Form.Label>
          <Form.Select
            multiple
            value={categories}
            onChange={handleCategoryChange}
          >
            {categoryStore.categories.map((categorie) => (
              <option key={categorie.id} value={categorie.name}>
                {categorie.name}
              </option>
            ))}
          </Form.Select>
        </Form.Group>
        <Form.Group>
          <Form.Label>Recherche par auteur(s) :</Form.Label>
          <Form.Control
            type="text"
            placeholder="Entrez les auteurs séparés par des virgules..."
            value={authorValue}
            onChange={(e) => handleAuthorChange(e.target.value)}
          />
        </Form.Group>
        <Form.Group>
          <Form.Label>Recherche par année de parution :</Form.Label>
          <Form.Control
            type="text"
            placeholder="Entrez l'année de parution..."
            value={yearValue}
            onChange={(e) => handleYearChange(e.target.value)}
          />
        </Form.Group>
        <Form.Group>
          <Form.Label>Recherche par nombre de pages :</Form.Label>
          <Form.Control
            type="text"
            placeholder="Entrez le nombre de pages..."
            value={pagesValue}
            onChange={(e) => handlePagesChange(e.target.value)}
          />
        </Form.Group>
      </div>
      <div style={{ display: 'flex', flexWrap: 'wrap', justifyContent: 'flex-start' }}>
        {results.map((book) => (
          <Card
            key={book.id}
            className="m-3"
            style={{ width: '18rem' }}
          >
            <Card.Header style={{ textAlign: 'center' }}>
              <Card.Title>{book.title}</Card.Title>
            </Card.Header>
            <Card.Body>
              <Card.Subtitle className="mb-2 text-muted">{book.isbn}</Card.Subtitle>
              <Card.Text>{book.author}</Card.Text>
              <Card.Text>{book.numOfPages} pages</Card.Text>
              <Card.Text>Publié en {book.publicationYear}</Card.Text>
            </Card.Body>
          </Card>
        ))}
      </div>
    </Container>
  );
}
export default observer(BookSearch);
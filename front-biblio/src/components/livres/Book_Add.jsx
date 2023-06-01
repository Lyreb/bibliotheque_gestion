import { observer } from 'mobx-react-lite';
import React, { useEffect, useState, useContext } from 'react';
import RootStore from "../../RootStore";
import { Modal, Form, Button } from 'react-bootstrap';

function AddBooksPopup(props) {
  const { bookStore, categoryStore } = useContext(RootStore)
  const [isbn, setIsbn] = useState("");
  const [title, setTitle] = useState("");
  const [editor, setEditor] = useState("");
  const [numOfPages, setNumOfPages] = useState("");
  const [publicationYear, setPublicationYear] = useState("");
  const [authorsLastName, setAuthorsLastName] = useState("");
  const [authorsFirstName, setAuthorsFirstName] = useState("");
  const [categories, setCategories] = useState([]);

  const handleCategoryChange = (event) => {
    const selectedCategories = Array.from(
      event.target.selectedOptions,
      (option) => option.value
    );
    setCategories(selectedCategories);
  };

  // Charger les catégories
  useEffect(() => {
    categoryStore.fetchCategories();
  }, []);

  const handleSubmit = (e) => {
    e.preventDefault();
    const newBook = {
      isbn: isbn,
      title: title,
      editor: editor,
      numOfPages: numOfPages,
      publicationYear: publicationYear,
      authors: [{
        firstName: authorsFirstName,
        lastName: authorsLastName
      }],
      categories: categories.map((catName) => {
        const cat = categoryStore.categories.find((cat) => cat.name === catName);
        return { id: cat.id, code: cat.code, name: cat.name, adultOnly: cat.adultOnly };
      })
    };
    bookStore.createBook(newBook);
    props.onClose();
  }

  return (
    <Modal show={true} onHide={props.onClose}>
      <Form onSubmit={handleSubmit}>
        <Modal.Header closeButton>
          <Modal.Title>Ajouter un livre</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <Form.Group>
            <Form.Label>ISBN :</Form.Label>
            <Form.Control type="text" name="isbn" value={isbn} onChange={(e) => setIsbn(e.target.value)} />
          </Form.Group>
          <Form.Group>
            <Form.Label>Titre :</Form.Label>
            <Form.Control type="text" name="title" value={title} onChange={(e) => setTitle(e.target.value)} />
          </Form.Group>
          <Form.Group>
            <Form.Label>Editeur :</Form.Label>
            <Form.Control type="text" name="editor" value={editor} onChange={(e) => setEditor(e.target.value)} />
          </Form.Group>
          <Form.Group>
            <Form.Label>Nombre de pages :</Form.Label>
            <Form.Control type="number" name="numOfPages" value={numOfPages} onChange={(e) => setNumOfPages(e.target.value)} />
          </Form.Group>
          <Form.Group>
            <Form.Label>Nom auteur :</Form.Label>
            <Form.Control type="text" name="lastName" value={authorsLastName} onChange={(e) => setAuthorsLastName(e.target.value)} />
          </Form.Group>
          <Form.Group>
            <Form.Label>Prénom auteur :</Form.Label>
            <Form.Control type="text" name="firstName" value={authorsFirstName} onChange={(e) => setAuthorsFirstName(e.target.value)} />
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
            <Form.Label>Année de publication :</Form.Label>
            <Form.Control type="number" name="annee" value={publicationYear} onChange={(e) => setPublicationYear(e.target.value)} />
          </Form.Group>
        </Modal.Body>
        <Modal.Footer>
          <Button variant="primary" type="submit">
            Ajouter
          </Button>
          <Button variant="secondary" onClick={props.onClose}>
            Annuler
          </Button>
        </Modal.Footer>
      </Form>
    </Modal>
  );
}
export default observer(AddBooksPopup);
//Import globaux
import { observer } from 'mobx-react-lite';
import React, { useState, useContext } from 'react';
import RootStore from "../../RootStore";
//import Bootstrap
import { Modal, Button, Form } from 'react-bootstrap';

function BooksEditPopup({ book, onClose }) {
  const { bookStore } = useContext(RootStore)

  const [isbn, setIsbn] = useState(book.isbn);
  const [title, setTitle] = useState(book.title);
  const [editor, setEditor] = useState(book.editor);
  const [numOfPages, setNumOfPages] = useState(book.numOfPages);
  const [publicationYear, setPublicationYear] = useState(book.publicationYear);

  function handleSubmit(event) {
    event.preventDefault();
    bookStore.editBook(book.id, { isbn, title, editor, numOfPages, publicationYear }).then(onClose);
  }

  return (
    <Modal show={true} onHide={onClose}>
      <Form onSubmit={handleSubmit}>
        <Modal.Header closeButton>
          <Modal.Title>Modifier {book.name}</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <h2>Editer le livre</h2>
          <Form.Group controlId="formBookTitle">
            <Form.Label>Titre :</Form.Label>
            <Form.Control type="text" name="title" value={title} onChange={(e) => setTitle(e.target.value)} />
          </Form.Group>
          <Form.Group controlId="formBookIsbn">
            <Form.Label>ISBN :</Form.Label>
            <Form.Control type="text" name="isbn" value={isbn} onChange={(e) => setIsbn(e.target.value)} />
          </Form.Group>
          <Form.Group controlId="formBookEditor">
            <Form.Label>Editeur :</Form.Label>
            <Form.Control type="text" name="editor" value={editor} onChange={(e) => setEditor(e.target.value)} />
          </Form.Group>
          <Form.Group controlId="formBookNumOfPages">
            <Form.Label>Nombre de pages :</Form.Label>
            <Form.Control type="number" name="numOfPages" value={numOfPages} onChange={(e) => setNumOfPages(e.target.value)} />
          </Form.Group>
          <Form.Group controlId="formBookPublicationYear">
            <Form.Label>Année de publication :</Form.Label>
            <Form.Control type="number" name="publicationYear" value={publicationYear} onChange={(e) => setPublicationYear(e.target.value)} />
          </Form.Group>
          <Modal.Footer>
            <Button variant="secondary" onClick={onClose}>Annuler</Button>
            <Button type="submit" variant="primary">Enregistrer</Button>
          </Modal.Footer>
        </Modal.Body>
      </Form>
    </Modal>
  )
}
export default observer(BooksEditPopup);
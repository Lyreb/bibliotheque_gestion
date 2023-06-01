//Import globaux
import { observer } from 'mobx-react-lite';
import React, { useEffect, useState, useContext } from 'react';
import RootStore from "../../RootStore";
// Import des modales
import BooksEditPopup from './Book_Edit';
import AddBooksPopup from './Book_Add';
import BookInfos from './Book_info';
//import Bootstrap
import { Row, Col, Button, Card } from "react-bootstrap";
import ListGroup from 'react-bootstrap/ListGroup';
import { NavLink } from 'react-router-dom';

function BooKsPage(props) {
  const { bookStore } = useContext(RootStore)
  const [selectedBook, setSelectedBook] = useState({ isbn: '', title: '', nbPages: '', cat: '', child: '', available: '' });

  // Gérer l'état des popups
  const [isEditOpen, setIsEditOpen] = useState(false);
  const [isAddOpen, setIsAddOpen] = useState(false);
  const [isPlusOpen, setIsPlusOpen] = useState(false);

  // Charger les livres
  useEffect(() => {
    bookStore.fetchBooks();
  }, []);

  const POPUP_EDIT = 'edit';
  const POPUP_ADD = 'add';
  const POPUP_PLUS = 'plus';

  function handlePopupClick(type, categorie) {
    setSelectedBook(categorie);
    switch (type) {
      case POPUP_EDIT:
        setIsEditOpen(true);
        break;
      case POPUP_ADD:
        setIsAddOpen(true);
        break;
      case POPUP_PLUS:
        setIsPlusOpen(true);
        break;
      default:
        break;
    }
  }

  function handlePopupClose() {
    setIsEditOpen(false);
    setIsAddOpen(false);
    setIsPlusOpen(false);
  }

  return (
    <div style={{ margin: '80px' }}>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', marginBottom: '20px' }}>
        <h3>Liste des livres</h3>
        <Button style={{ marginLeft: '30px' }} onClick={() => handlePopupClick("add", null)}>
          Ajouter un livre
        </Button>
      </div>
      <Row>
        {bookStore.books.map((book) => (
          <Col xs={12} md={6} lg={4} className="mb-3" key={book.id}>
            <Card key={book.id}>
              <Card.Header style={{ textAlign: 'center' }}>
                <Card.Title bookId={selectedBook?.id} key={book.id} as={NavLink} to={`books/${book.id}`}>{book.title}</Card.Title>
              </Card.Header>
              <Card.Body>
                <ListGroup className="list-group-flush">
                  <p className="font-weight-bold">Information générales</p>
                  <ListGroup.Item>ISBN : {book.isbn}</ListGroup.Item>
                  <ListGroup.Item>Titre : {book.title}</ListGroup.Item>
                  <ListGroup.Item>Editeur : {book.editor}</ListGroup.Item>
                  <ListGroup.Item>Nombre de pages : {book.numOfPages}</ListGroup.Item>
                  <ListGroup.Item>Année de publication : {book.publicationYear}</ListGroup.Item>
                </ListGroup>
              </Card.Body>
              <Card.Footer>
                <Button variant="link" onClick={() => handlePopupClick("edit", book)}>Editer</Button>
                <Button variant="link" onClick={() => handlePopupClick("plus", book)}>Infos</Button>
              </Card.Footer>
            </Card>
          </Col>
        ))}
      </Row>
      {isAddOpen &&
        <AddBooksPopup onClose={handlePopupClose} onCancel={handlePopupClose} />
      }
      {isEditOpen &&
        <BooksEditPopup book={selectedBook} onClose={handlePopupClose} />
      }
      {isPlusOpen &&
        <BookInfos bookId={selectedBook.id} onClose={handlePopupClose} />
      }
    </div>
  );
}
export default observer(BooKsPage);
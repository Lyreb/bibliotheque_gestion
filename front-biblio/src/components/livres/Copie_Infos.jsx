import React, { useState } from 'react';
import { Modal, Card, Button, Container, Row, Col } from 'react-bootstrap';

function BooksInfoPopup(props) {
  const { onClose, bookId } = props;
  const [isLoaded, setIsLoaded] = useState(false);
  const [book, setBook] = useState([]);
  const [copies, setCopies] = useState([]);

  if (!book) {
    return <div>Loading...</div>;
  }

  if (!isLoaded) {
    return <div>Loading...</div>;
  }

  return (
    <Modal show={props.show} onHide={handleClose}>
      <Modal.Header closeButton>
        <Modal.Title>Informations et Copie(s) du livre {book.title}</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <Container>
          <Row>
            <Col>
              <h4>Informations générales</h4>
              <p>ISBN: {book.isbn}</p>
              <p>Titre: {book.title}</p>
              <p>Editeur: {book.editor}</p>
              <p>Nombre de pages: {book.numOfPages}</p>
              <p>Année de publication: {book.publicationYear}</p>
              <p>Auteur :
                {book.authors.map(author => (
                  <div key={author.lastname + author.firstname}>
                    <p>Nom: {author.lastname}</p>
                    <p>Prénom: {author.firstname}</p>
                  </div>
                ))}
              </p>
            </Col>
          </Row>
          <Row>
            <Col>
              <p>Catégories :
                {book.categories.map((category, index) => (
                  <span key={category.id}>
                    {category.name}
                    {index !== book.categories.length - 1 && ","}
                  </span>
                ))}
                <br /><br />{book.categories.some(category => category.adultOnly) && " -18 : réservé aux adultes"}
              </p>
            </Col>
          </Row>
          <p>Copies :
            {book.copies.map((copy, index) => (
              <Card style={{ width: '18rem' }} key={copy.id}>
                <Card.Body>
                  <Card.Title>Copy {index + 1}</Card.Title>
                  <Card.Text>
                    Etat: {copy.state}
                    Disponible: {copy.available ? "Oui" : "Non"}
                  </Card.Text>
                  <Button variant="primary" onClick={() => handlePlusClick(book)}>Plus </Button>
                  <Button variant="primary" onClick={() => handleEditClick(book)}> Editer </Button>
                  <Button variant="primary" onClick={handleDeleteClick}> Supprimer </Button>
                </Card.Body>
              </Card>
            ))}
          </p>
        </Container>
      </Modal.Body>
      <Modal.Footer>
        <Button variant="secondary" onClick={handleClose}>
          Close
        </Button>
      </Modal.Footer>
    </Modal>
  );
}
export default BooksInfoPopup;
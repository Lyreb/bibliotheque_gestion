// Imports globaux
import { observer } from 'mobx-react';
import React, { useContext, useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import RootStore from '../../RootStore';
// Import des popups
import CopieAdd from './Copie_Add';
import CopieEdit from './Copie_Edit';
import ConfirmationPopup from '../Confirmation_popup';
// Imports Bootstrap
import { Card, ListGroup, Button } from 'react-bootstrap';
import { Row, Col } from "react-bootstrap";

function BookDetails() {
  const { bookStore } = useContext(RootStore)
  const { bookId } = useParams();
  const [selectedCopie, setSelectedCopie] = useState({ state: '', removed: '', available: '' });

  // Charger un livre et ses coipes
  useEffect(() => {
    bookStore.fetchBookDetails(bookId);
  }, [bookId]);

  // Appel au ADD
  const handleAddCopie = (copie) => {
    bookStore.addCopie(copie);
    setIsAddOpen(false);
  };

  // Gérer l'état des popups
  const [isEditOpen, setIsEditOpen] = useState(false);
  const [isAddOpen, setIsAddOpen] = useState(false);

  const POPUP_EDIT = 'edit';
  const POPUP_ADD = 'add';

  function handlePopupClick(type, copieBook) {
    setSelectedCopie(copieBook);
    switch (type) {
      case POPUP_EDIT:
        setIsEditOpen(true);
        break;
      case POPUP_ADD:
        setIsAddOpen(true);
        break;
    }
  }

  function handlePopupClose() {
    setIsEditOpen(false);
    setIsAddOpen(false);
  }

  if (!bookStore.bookDetails) {
    return <div>...</div>
  }
  let authors = bookStore.bookDetails.authors.map((author) => author.firstname + ' ' + author.lastname).join(', ');
  let adultOnly = bookStore.bookDetails.categories && bookStore.bookDetails.categories.length > 0 && bookStore.bookDetails.categories[0].adultOnly;

  return (
    <div style={{ margin: '80px' }}>
      <h3 style={{ display: 'flex', justifyContent: 'center', marginBottom: '20px' }}>{bookStore.bookDetails.title} informations et exemplaire(s)</h3>
      <div style={{ display: 'flex', justifyContent: 'center' }}>
        <div style={{ border: '1px solid black', padding: '10px', marginBottom: '20px', display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
          <h4>Informations globales</h4>
          <div style={{ display: 'flex', flexDirection: 'row' }}>
            <div style={{ marginRight: '20px' }}>
              <b>ID : </b><p>{bookStore.bookDetails.id}</p>
              <b>ISBN : </b><p>{bookStore.bookDetails.isbn} </p>
              <b>Titre : </b><p>{bookStore.bookDetails.title} </p>
            </div>
            <div>
              <b>Editeur :</b><p>{bookStore.bookDetails.editor} </p>
              <b>Nombre de pages :</b><p>{bookStore.bookDetails.numOfPages} </p>
              <b>Année de publication</b><p>{bookStore.bookDetails.publicationYear} </p>
            </div>
          </div>
          <b>Auteur </b><p>{authors}</p>
          {adultOnly && <><b>-18</b><p>Réservé aux adultes</p></>}
          <Button onClick={() => handlePopupClick("add", null)}>
            Ajouter un exemplaire
          </Button>
        </div>
      </div>
      <Row>
        {bookStore.bookDetails.copies.map((copie) => (
          <Col xs={12} sm={6} md={4} lg={3} key={copie.id}>
            <Card key={copie.id}>
              <Card.Header>
                <Card.Title> {copie.id} </Card.Title>
              </Card.Header>
              <Card.Body >
                <ListGroup className="list-group-flush">
                  <ListGroup.Item>ID : {copie.id}</ListGroup.Item>
                  <ListGroup.Item>Etat : {copie.state} </ListGroup.Item>
                  <ListGroup.Item>Retiré : {copie.removed ? "oui" : "non"} </ListGroup.Item>
                  <ListGroup.Item>Disponible : {copie.available ? "oui" : "non"} </ListGroup.Item>
                </ListGroup>
              </Card.Body>
              <Card.Footer>
                <Button variant="link" onClick={() => handlePopupClick("edit", copie)}> Editer </Button>
                <ConfirmationPopup title="Supprimer" message="Etes vous sur de vouloir supprimer cette copie ?" onAction={() => bookStore.removeCopie(copie.id)} />
              </Card.Footer>
            </Card>
          </Col>
        ))}
      </Row>
      {isAddOpen &&
        <CopieAdd onClose={handlePopupClose} onSubmit={handleAddCopie} onCancel={handlePopupClose} copie={selectedCopie} />
      }
      {isEditOpen &&
        <CopieEdit copie={selectedCopie} onClose={handlePopupClose} />
      }
    </ div>
  );
}
export default observer(BookDetails);

import { observer } from 'mobx-react-lite';
import React, { useContext, useEffect } from 'react';
import RootStore from '../../RootStore';
import { Row, Col, Button, Card } from "react-bootstrap";
import ListGroup from 'react-bootstrap/ListGroup';
import { NavLink } from 'react-router-dom';

function LoansPage() {
  const { bookStore } = useContext(RootStore);

  // Charger les livres
  useEffect(() => {
    bookStore.fetchBookToLoan();
  }, []);

  if (!bookStore.bookLoans) {
    return <div>...</div>
  }

  return (
    <div style={{ margin: '80px' }}>
      <h3 style={{ display: 'flex', justifyContent: 'center', marginBottom: '20px' }}>Liste des livres disponibles à l'emprunt :</h3>
      <Row>
        {bookStore.books.map((book) => (
          <Col xs={12} md={6} lg={4} className="mb-3" key={book.id}>
            <Card>
              <Card.Body>
                <Card.Header style={{ textAlign: 'center' }}>
                  <Card.Title>{book.title}</Card.Title>
                </Card.Header>
                <Card.Body>
                  <ListGroup variant="flush">
                    <ListGroup.Item>ID : {book.id}</ListGroup.Item>
                  </ListGroup>
                </Card.Body>
                <Card.Footer>
                  <Button variant="link" as={NavLink} to={`emprunt/${book.id}`}>Choisir une copie</Button>
                </Card.Footer>
              </Card.Body>
            </Card>
          </Col>
        ))}
      </Row>
    </div>
  );
}
export default observer(LoansPage);
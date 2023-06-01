import { observer } from 'mobx-react-lite';
import React, { useContext, useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import RootStore from '../../RootStore';
import { Row, Col, Button, Card } from "react-bootstrap";
import ListGroup from 'react-bootstrap/ListGroup';
import EmprunterLivre from './Loan_Emprunt';

function LoanBook() {
    const { bookStore } = useContext(RootStore);
    const { bookId } = useParams();
    const [isEmpruntOpen, setIsEmpruntOpen] = useState(false);

    // Charger les livres
    useEffect(() => {
        bookStore.fetchBooks();
    }, []);

    // Charger un livre et ses coipes
    useEffect(() => {
        bookStore.fetchBookDetails(bookId);
    }, [bookId]);

    if (!bookStore.bookDetails) {
        return <div>...</div>
    }

    if (!bookStore.fetchBooks) {
        return <div>...</div>
    }

    const filteredCopies = bookStore.bookDetails.copies.filter(copie => copie.available && !copie.removed);

    function handleEmpruntClick() {
        setIsEmpruntOpen(true);
    }

    function handleEmpruntClose() {
        setIsEmpruntOpen(false);
    }

    return (
        <div style={{ margin: '80px' }}>
            <h3 style={{ display: 'flex', justifyContent: 'center', marginBottom: '20px' }}>Emprunt d'une copie de {bookStore.bookDetails.title}</h3>
            <Row>
                {filteredCopies.map((copie) => (
                    <Col xs={12} md={6} lg={4} className="mb-3" key={copie.id}>
                        <Card key={copie.id}>
                            <Card.Header style={{ textAlign: 'center' }}>
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
                                <Button variant="link" onClick={handleEmpruntClick}> Emprunter </Button>
                            </Card.Footer>
                        </Card>
                    </Col>
                ))}
            </Row>
            {isEmpruntOpen &&
                <EmprunterLivre onClose={handleEmpruntClose} />
            }
        </div>
    );
}
export default observer(LoanBook);
import { computed } from "mobx";
import React, { useState, useEffect } from "react";
import { Modal, Button, ListGroup, Card, Col } from "react-bootstrap";

function BookInfo(props) {
    const { bookId } = props;
    const [book, setBook] = useState(null);

    useEffect(() => {
        fetch(`http://127.0.0.1:8080/api/v1/rest/books/${bookId}`)
            .then(res => res.json())
            .then(
                (result) => {
                    setBook(result);
                },
                (error) => {
                    console.log(error);
                }
            )
    }, [bookId]);

    if (book === null) {
        return (
            <Modal show={true} onHide={props.onClose}>
                <Modal.Header closeButton>
                    <Modal.Title>...</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    Loading...
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" onClick={props.onClose}>
                        Fermer
                    </Button>
                </Modal.Footer>
            </Modal>
        );
    }

    let authors = book.authors.map((author) => author.firstname + ' ' + author.lastname).join(', ');
    let categories = book.categories.map((category) => category.name).join(', ');

    let copies = book.copies.map((copy) => (
        <Col key={copy.id} style={{ marginBottom: "10px" }}>
            <Card key={copy.id}>
                <Card.Body>
                    <Card.Title>{copy.state}</Card.Title>
                    <Card.Text>
                        Etat : {copy.state} <br />
                        Disponibilité : {copy.available ? "disponible" : "non disponible"} <br />
                        Retiré : {copy.removed ? "oui" : "non"} <br />
                    </Card.Text>
                </Card.Body>
            </Card>
        </Col>
    ));

    return (
        <Modal show={true} onHide={props.onClose}>
            <Modal.Header closeButton>
                <Modal.Title>{book.title}</Modal.Title>
            </Modal.Header>
            <Modal.Body>
                <ListGroup className="list-group-flush">
                    <ListGroup.Item>ISBN : {book.isbn}</ListGroup.Item>
                    <ListGroup.Item>Titre : {book.title}</ListGroup.Item>
                    <ListGroup.Item>Editeur : {book.editor}</ListGroup.Item>
                    <ListGroup.Item>Nombre de pages : {book.numOfPages}</ListGroup.Item>
                    <ListGroup.Item>Année de publication : {book.publicationYear}</ListGroup.Item>
                    {authors !== null && <ListGroup.Item>Auteurs : {authors}</ListGroup.Item>}
                    {categories !== null && <ListGroup.Item>Catégories : {categories}</ListGroup.Item>}
                </ListGroup>
                <h4>Exemplaires :</h4>
                {copies !== null ? (
                    copies
                ) : (
                    <p>Aucun exemplaire disponible pour ce livre.</p>
                )}
            </Modal.Body>
            <Modal.Footer>
                <Button variant="secondary" onClick={props.onClose}>
                    Fermer
                </Button>
            </Modal.Footer>
        </Modal>
    );
}
export default BookInfo;

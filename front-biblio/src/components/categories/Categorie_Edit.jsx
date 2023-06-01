//Import globaux
import { observer } from 'mobx-react-lite';
import React, { useState, useContext } from 'react';
import RootStore from "../../RootStore";
//import Bootstrap
import { Modal, Button, Form } from 'react-bootstrap';

function CategorieEdit({ categorie, onClose }) {
    const { categoryStore } = useContext(RootStore)

    const [code, setCode] = useState(categorie.code);
    const [name, setName] = useState(categorie.name);
    const [adultOnly, setAdultOnly] = useState(categorie.adultOnly);

    function handleSubmit(event) {
        event.preventDefault();
        categoryStore.editCategory(categorie.id, { code, name, adultOnly }).then(onClose);
    }

    return (
        <Modal show={true} onHide={onClose}>
            <Form onSubmit={handleSubmit}>
                <Modal.Header closeButton>
                    <Modal.Title>Modifier la catégorie {categorie.name} </Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <Form.Group controlId="formId">
                        <Form.Label>ID :</Form.Label>
                        <Form.Control type="text" name="id" value={categorie.id} disabled />
                    </Form.Group>
                    <Form.Group controlId="formName">
                        <Form.Label>Code :</Form.Label>
                        <Form.Control type="text" name="code" value={code} onChange={(e) => setCode(e.target.value)} />
                    </Form.Group>
                    <Form.Group controlId="formName">
                        <Form.Label>Nom :</Form.Label>
                        <Form.Control type="text" name="name" value={name} onChange={(e) => setName(e.target.value)} />
                    </Form.Group>
                    <Form.Group controlId="formName">
                        <Form.Label>Age :</Form.Label>
                        <Form.Control type="checked" name="age" value={adultOnly} onChange={(e) => setAdultOnly(e.target.value)} />
                    </Form.Group>
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" onClick={onClose}>Close</Button>
                    <Button variant="primary" type="submit">Save Changes</Button>
                </Modal.Footer>
            </Form>
        </Modal>
    )
}
export default observer(CategorieEdit);
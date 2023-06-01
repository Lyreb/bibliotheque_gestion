//Import globaux
import React, { useState } from 'react';
//import Bootstrap
import { Modal, Form, Button } from 'react-bootstrap';

function AddMemberPopup(props) {
  const [name, setName] = useState("");
  const [firstname, setFirstname] = useState("");
  const [birthday, setBirthday] = useState("");

  // RECUPERATION DES VALEURS
  const handleSubmit = (e) => {
    e.preventDefault();
    props.onSubmit({ name, firstname, birthday });
    setName("");
    setFirstname("");
    setBirthday("");
  };

  return (
    <Modal show={true} onHide={props.onClose}>
      <Modal.Header closeButton>
        <Modal.Title>Ajout membre</Modal.Title>
      </Modal.Header>
      <Form onSubmit={handleSubmit}>
        <Modal.Body>
          <Form.Group>
            <Form.Label>Nom</Form.Label>
            <Form.Control type="text" value={name} onChange={(e) => setName(e.target.value)} />
          </Form.Group>
          <Form.Group>
            <Form.Label>Prénom</Form.Label>
            <Form.Control type="text" value={firstname} onChange={(e) => setFirstname(e.target.value)} />
          </Form.Group>
          <Form.Group>
            <Form.Label>Date de naissance</Form.Label>
            <Form.Control type="date" value={birthday} onChange={(e) => setBirthday(e.target.value)} />
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
export default AddMemberPopup;
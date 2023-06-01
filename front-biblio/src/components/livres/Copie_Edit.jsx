//Import globaux
import { observer } from 'mobx-react-lite';
import React, { useState, useContext } from 'react';
import RootStore from "../../RootStore";
//import Bootstrap
import { Modal, Button, Form } from 'react-bootstrap';

function CopieEdit({ copie, onClose }) {
  const { bookStore } = useContext(RootStore)

  const [state, setState] = useState(copie.state);
  const [removed, setRemoved] = useState(copie.removed);
  const [available, setAvailable] = useState(copie.available);

  function handleSubmit(event) {
    event.preventDefault();
    bookStore.editCopie(copie.id, { state, removed, available }).then(onClose);
  }

  return (
    <Modal show={true} onHide={onClose}>
      <p>JE SUIS DANS LE EDIT DE LA COPIE</p>
      <Modal.Header closeButton>
        <Modal.Title>Editer la copie</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <Form onSubmit={handleSubmit}>
          <Form.Group controlId="state">
            <Form.Label>État de la copie :</Form.Label>
            <Form.Select value={state} onChange={(e) => setState(e.target.value)}>
              <option value="Neuf">Neuf</option>
              <option value="Très bon">Très bon</option>
              <option value="Bon">Bon</option>
              <option value="Usé">Usé</option>
              <option value="Mauvais">Mauvais</option>
            </Form.Select>
          </Form.Group>
          <Form.Group controlId="available">
            <Form.Label>Disponible :</Form.Label>
            <Form.Select value={available.toString()} onChange={(e) => setAvailable(e.target.value)}>
              <option value="true">Oui</option>
              <option value="false">Non</option>
            </Form.Select>
          </Form.Group>
          <Form.Group controlId="removed">
            <Form.Label>Retirée :</Form.Label>
            <Form.Select value={removed.toString()} onChange={(e) => setRemoved(e.target.value)}>
              <option value="false">Non</option>
              <option value="true">Oui</option>
            </Form.Select>
          </Form.Group>
          <Modal.Footer>
            <Button variant="primary" type="submit">
              Enregistrer
            </Button>
            <Button variant="secondary" onClick={onClose}>
              Annuler
            </Button>
          </Modal.Footer>
        </Form>
      </Modal.Body>
    </Modal>
  )
}
export default observer(CopieEdit);
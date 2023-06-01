import { observer } from 'mobx-react-lite';
import React, { useState, useContext } from 'react';
import RootStore from "../../RootStore";
import { Modal, Button, Form } from 'react-bootstrap';

function CopieAdd(props) {
  const { bookStore } = useContext(RootStore);
  const { bookId } = props;
  const [state, setState] = useState("Neuf");
  const [removed, setRemoved] = useState(false);
  const [available, setAvailable] = useState(true);

  const handleSubmit = (e) => {
    e.preventDefault();
    const newCopie = {
      state: state,
      removed: removed,
      available: available,
    };
    bookStore.createCopie({ newCopie, bookId });
    props.onClose();
  }

  return (
    <Modal show={true} onHide={props.onClose}>
      <Modal.Header closeButton>
        <Modal.Title>Ajouter une copie</Modal.Title>
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
            <Button variant="secondary" onClick={props.onClose}>
              Annuler
            </Button>
          </Modal.Footer>
        </Form>
      </Modal.Body>
    </Modal>
  );
}
export default observer(CopieAdd);
